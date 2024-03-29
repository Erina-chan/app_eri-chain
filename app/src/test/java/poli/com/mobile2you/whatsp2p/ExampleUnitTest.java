package poli.com.mobile2you.whatsp2p;

import android.content.ContextWrapper;

import org.junit.Test;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.util.encoders.Hex;

import static com.poli.usp.erichain.data.local.ECDHManager.ECDHKeyExchange;
import static com.poli.usp.erichain.data.local.Utils.hexStringToByteArray;
import static org.junit.Assert.assertEquals;

import com.poli.usp.erichain.data.local.HashManager;
import com.poli.usp.erichain.data.local.Utils;
import com.poli.usp.erichain.data.remote.models.MessageResponse;
import com.poli.usp.erichain.data.local.Contact;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test //ok
    public void sha3_isCorrect() {
        byte[] hash2 = HashManager.generateSha3("teste");
        assertEquals("c8a2d029074842a6ef31d8d8b6e10714ee16d679f029986fce0bf2ac6a5ceac2", Hex.toHexString(HashManager.generateSha3("teste")));
    }

    @Test //ok
    public void keccak_isCorrect() {
        byte[] hash2 = HashManager.generateKeccak("teste");
        assertEquals("e0d4f6e915eb01068ecd79ce922236bf16c38b2d88cccffcbc57ed53ef3b74aa", Hex.toHexString(HashManager.generateKeccak("teste")));
    }

    @Test //ok
    public void verifyPrevHash_isTrue() {
        Contact sender = new Contact("sender");
        Contact receiver = new Contact("receiver");

        MessageResponse messageTest1 = new MessageResponse(sender, receiver, "teste", null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        byte[] hash = MessageResponse.messageGenerateHash(messageTest1);
        MessageResponse messageTest2 = new MessageResponse(sender, receiver, "teste", null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        messageTest2.setPrevHash(hash);

        assertEquals(true, MessageResponse.verifyPrevHash(messageTest1, messageTest2));
    }

    @Test //ok
    public void verifyPrevHash_isFalse() {
        Contact sender = new Contact("sender");
        Contact receiver = new Contact("receiver");
        MessageResponse messageTest1 = new MessageResponse(sender, receiver, "teste", null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        MessageResponse messageTest2 = new MessageResponse(sender, receiver, "teste", null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        byte[] hash = hexStringToByteArray("e0d4f6e915eb01068ecd79ce922236bf16c38b2d88cccffcbc57ed53ef3b74aa");
        messageTest2.setPrevHash(hash);
        assertEquals(false, MessageResponse.verifyPrevHash(messageTest1, messageTest1));  // test with param prevHash == null
        assertEquals(false, MessageResponse.verifyPrevHash(messageTest1, messageTest2));  // test with param prevHash != hash(prevMessage)
    }

    @Test //not ok... Need to change the code to provide selective disclosure
    public void messageGenerateHash_isCorrect() {
        Contact sender = new Contact("sender");
        Contact receiver = new Contact("receiver");

        MessageResponse messageTest = new MessageResponse(sender, receiver, "teste", null, hexStringToByteArray("538972575dfafcb026f4f116f70093073e3c1062c20a02cc32e0e002a10964d2"));
        try {
            byte[] serializedMessage = Utils.serialize(messageTest);
            String serializedMessageString = new String(serializedMessage);
            System.out.println(serializedMessageString);
            byte[] hash = hexStringToByteArray("ea7c28d9bc4cf0acecdb320dd585ca29b9c795482aeaac54cd443e3cb23ac130");
            System.out.println(Arrays.toString(hash));

            byte[] hash2 = MessageResponse.messageGenerateHash(messageTest);
            System.out.println(Arrays.toString(hash2));

            // compare the prints
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test //ok
    public void ecdhKeyExchangeTest() throws Exception {
        // Using ECDH algorithm by Spongy Castle library.
        // secp224k1 EC parameter is a 224-bit prime field Weierstrass curve, a Koblitz curve.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
        kpg.initialize(new ECGenParameterSpec("secp224k1"));

        // Generate user key pair to ECDH
        KeyPair kp = kpg.generateKeyPair();
        ECPrivateKey secKey = (ECPrivateKey) kp.getPrivate();
        ECPublicKey pubKey = (ECPublicKey) kp.getPublic();

        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
        keyAgreement.init(secKey);
        keyAgreement.doPhase(pubKey, true);
        byte[] sharedKey = keyAgreement.generateSecret();

        byte[] sharedSecret = ECDHKeyExchange(secKey, pubKey);
        assertEquals(Hex.toHexString(sharedKey), Hex.toHexString(sharedSecret));
    }
}