package com.poli.usp.erichain.data.local;

import android.content.Context;

import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
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
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.Iterator;

import javax.crypto.KeyAgreement;

public class ECDHManager {
    // Elliptic Curve Diffie-Hellman(ECDH) key exchange class

    private Context mContext;
    private String myId;
    private PGPPublicKeyRing ecdhPubKeyRing;
    private PGPSecretKeyRing ecdhSecKeyRing;

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public ECDHManager(Context context, String identifier, char[] passwd) throws Exception {
        this.mContext = context;
        this.myId = identifier;

        try {
            FileInputStream pubFile = context.openFileInput(myId + "ECDH.pkr");
            FileInputStream secFile = context.openFileInput(myId + "ECDH.skr");
            this.ecdhPubKeyRing = new PGPPublicKeyRing(pubFile, new JcaKeyFingerprintCalculator());
            this.ecdhSecKeyRing = new PGPSecretKeyRing(secFile, new JcaKeyFingerprintCalculator());
        } catch (FileNotFoundException e) {
            PGPKeyRingGenerator krgen = generateEcdhKeyRingGenerator(identifier, passwd);
            this.ecdhPubKeyRing = krgen.generatePublicKeyRing();
            this.ecdhSecKeyRing = krgen.generateSecretKeyRing();
            this.save();
        }
    }

    public void save() throws IOException {

        BufferedOutputStream pubout = new BufferedOutputStream
                (mContext.openFileOutput(myId + "ECDH.pkr", Context.MODE_PRIVATE));
        this.ecdhPubKeyRing.encode(pubout);
        pubout.close();

        BufferedOutputStream secout = new BufferedOutputStream
                (mContext.openFileOutput(myId + "ECDH.skr", Context.MODE_PRIVATE));
        this.ecdhSecKeyRing.encode(secout);
        secout.close();
    }

    public final static PGPKeyRingGenerator generateEcdhKeyRingGenerator (String id, char[] passwd)
            throws Exception {
        // Reference: https://nelenkov.blogspot.com/2011/12/using-ecdh-on-android.html
        // Using ECDH algorithm by Spongy Castle library.
        // secp224k1 EC parameter is a 224-bit prime field Weierstrass curve, a Koblitz curve.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
        kpg.initialize(new ECGenParameterSpec("secp224k1"));

        // Generate user key pair to ECDH
        KeyPair kpSign = kpg.generateKeyPair();
        PGPKeyPair ecdhKeyPair = new JcaPGPKeyPair(PGPPublicKey.ECDH, kpSign, new Date());

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
                ecdhKeyPair, id, sha256Calc, null, null,
                new JcaPGPContentSignerBuilder(ecdhKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                encryptor);

        return keyRingGen;
    }

    public PGPPublicKeyRing getEcdhPublicKeyRing() { return ecdhPubKeyRing; }

    public static byte[] ECDHKeyExchange (ECPrivateKey mySecKey, ECPublicKey friendPubKey) throws Exception {
        // Reference: https://nelenkov.blogspot.com/2011/12/using-ecdh-on-android.html
        byte[] sharedKey = null;

        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
        keyAgreement.init(mySecKey);
        keyAgreement.doPhase(friendPubKey, true);

        sharedKey = keyAgreement.generateSecret();

        return sharedKey;
    }

    public PGPPublicKey getSignKey() {
        Iterator<PGPPublicKey> it = ecdhPubKeyRing.getPublicKeys();
        while (it.hasNext()) {
            PGPPublicKey key = it.next();
            int algorithm = key.getAlgorithm();
            if (algorithm == 18) {  // PublicKeyPacket.class -> case 18: ECDHPublicBCPGKey
                return key;
            }
        }
        return null;
    }

    public PGPPrivateKey getPrivateKey(char[] passwd) throws PGPException {
        PGPSecretKey pgpSecKey = ecdhSecKeyRing.getSecretKey();
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


}
