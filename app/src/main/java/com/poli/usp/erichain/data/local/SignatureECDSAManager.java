package com.poli.usp.erichain.data.local;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.Iterator;

import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPKeyRingGenerator;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

public class SignatureECDSAManager {
    // this class signs the messages

    // TODO: criar funções específicas para:
    //       - atualizar o par de chaves (?)
    //       - assinar uma mensagem
    //       - verificar a assinatura recebida junto com uma mensagem
    // TODO: Ver como enviar msg + assinatura e como receber esses dois conjuntos de bytes


    private Context mContext;
    private String myId;
    private PGPPublicKeyRing ecdsaPubKeyRing;
    private PGPSecretKeyRing ecdsaSecKeyRing;

    public SignatureECDSAManager(Context context, String identifier, char[] passwd) throws Exception {
        this.mContext = context;
        this.myId = identifier;

        try {
            FileInputStream pubFile = context.openFileInput(myId + "ECDSA.pkr");
            FileInputStream secFile = context.openFileInput(myId + "EDCSA.skr");
            this.ecdsaPubKeyRing = new PGPPublicKeyRing(pubFile, new JcaKeyFingerprintCalculator());
            this.ecdsaSecKeyRing = new PGPSecretKeyRing(secFile, new JcaKeyFingerprintCalculator());
        } catch (FileNotFoundException e) {
            PGPKeyRingGenerator krgen = generateEcdsaKeyRingGenerator(identifier, passwd);
            this.ecdsaPubKeyRing = krgen.generatePublicKeyRing();
            this.ecdsaSecKeyRing = krgen.generateSecretKeyRing();
            this.save();
        }
    }


    public void save() throws IOException {

        BufferedOutputStream pubout = new BufferedOutputStream
                (mContext.openFileOutput(myId + "ECDSA.pkr", Context.MODE_PRIVATE));
        this.ecdsaPubKeyRing.encode(pubout);
        pubout.close();

        BufferedOutputStream secout = new BufferedOutputStream
                (mContext.openFileOutput(myId + "ECDSA.skr", Context.MODE_PRIVATE));
        this.ecdsaSecKeyRing.encode(secout);
        secout.close();
    }

    public final static PGPKeyRingGenerator generateEcdsaKeyRingGenerator (String id, char[] passwd)
            throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "SC");
        keyGen.initialize(new ECGenParameterSpec("curve25519"));
        KeyPair kpSign = keyGen.generateKeyPair();
        PGPKeyPair ecdsaKeyPair = new JcaPGPKeyPair(PGPPublicKey.ECDSA, kpSign, new Date());

        // Add a self-signature on the id
        PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();
        signhashgen.setSignerUserID(false, id);

        // Objects used to encrypt the secret key.
        PGPDigestCalculator sha256Calc =
                new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);

        // generate a key ring
        JcePBESecretKeyEncryptorBuilder jcebuilder = new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calc);
        jcebuilder.setProvider("SC");
        PBESecretKeyEncryptor encryptor = jcebuilder.build(passwd);
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION,
                ecdsaKeyPair, id, sha256Calc, null, null,
                new JcaPGPContentSignerBuilder(ecdsaKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                encryptor);

        return keyRingGen;
    }

    public PGPPublicKeyRing getEcdsaPublicKeyRing() { return ecdsaPubKeyRing; }

    public PGPPublicKey getSignKey() {
        Iterator <PGPPublicKey> it = ecdsaPubKeyRing.getPublicKeys();
        while (it.hasNext()) {
            PGPPublicKey key = it.next();
            int algorithm = key.getAlgorithm();
            if (algorithm == 19) {  // PublicKeyPacket.class -> case 19: ECDSAPublicBCPGKey
                return key;
            }
        }
        return null;
    }

    public PGPPrivateKey getPrivateKey(char[] passwd) throws PGPException {
        PGPSecretKey pgpSecKey = ecdsaSecKeyRing.getSecretKey();
        if (pgpSecKey == null) {
            return null;
        }
        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(passwd);
        return pgpSecKey.extractPrivateKey(decryptor);
    }

    public String getUserId() {
        PGPPublicKey signKey = this.getSignKey();
        Iterator<String> it = signKey.getUserIDs();
        while (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public PGPSignature generateSignatureForMessage(char[] passwd, byte[] message) throws PGPException {
        PGPSignatureGenerator signGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(PGPPublicKey.ECDSA, HashAlgorithmTags.SHA256).setProvider("SC"));
        String userId = this.getUserId();
        signGen.init(PGPSignature.BINARY_DOCUMENT, this.getPrivateKey(passwd));
        signGen.update(message);
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
        spGen.setSignerUserID(false, userId);
        signGen.setHashedSubpackets(spGen.generate());
        PGPSignature sig = signGen.generate();
        return sig;
    }

    public boolean verifySignatureForMessage(PGPSignature sig, byte[] message) throws PGPException {
        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("SC"), this.getSignKey());
        sig.update(message);
        return sig.verify();
    }
}
