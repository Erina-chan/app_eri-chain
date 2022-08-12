package com.poli.usp.erichain.data.local;

import android.util.Log;

import com.poli.usp.erichain.data.remote.models.MessageResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

public class SignECDSABenchmark {
    /** Security level */
    public static final int SEC_LEVEL = 128;

    public static int subLoop = 500;

    /* Instances to cryptographic protocols:
     * Message Digest: SHA256
     * Signature method: ECDSA (curve secp256)
     */
    public static String KEY_ALGORITHM = "EC";
    public static String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    /* Key pair */
    private PrivateKey sk;
    public PublicKey pk;


    public SignECDSABenchmark() {
        // creat contact
        Contact contato = new Contact("userTest", "usertest");

        byte[] prevHash0 = new byte[256];
        // creat message with 5 chars
        MessageResponse msg = new MessageResponse(contato, contato, "Mensagem arbitária",
                null, prevHash0);

        try {
            byte[] serializedMsg = Utils.serialize(msg);

            // Generate keys pair
            keyGen(null);

            long time1;
            long time2;
            long time3;
            long time4;
            byte[] signECDSA;
            boolean verifyECDSA;
            double totalAvgSign;
            double totalSDSign;
            double totalMedSign;
            double totalAvgVerify;
            double totalSDVerify;
            double totalMedVerify;

            long[] timeSign = new long[subLoop];
            long[] timeVerify = new long[subLoop];

            for (int i = 0; i < subLoop; i++) {
                time1 = Calendar.getInstance().getTimeInMillis();
                // sign
                signECDSA = sign(serializedMsg);
                time2 = Calendar.getInstance().getTimeInMillis();
                timeSign[i] = time2 - time1;

                time3 = Calendar.getInstance().getTimeInMillis();
                verifyECDSA = verify(pk, serializedMsg, signECDSA);
             /*if (verifyECDSA) {
                 Log.d("[Benchmark]", String.format(" Verify %d true", i));
             }*/
                time4 = Calendar.getInstance().getTimeInMillis();
                timeVerify[i] = time4 - time3;
            }

            // Get sign time average
            totalAvgSign = calculateAVG(timeSign);
            Log.d("[Benchmark]", String.format("Get ECDSA sign time average: %.9f", totalAvgSign));

            //Get sing time standard deviation
            totalSDSign = calculateSD(timeSign);
            Log.d("[Benchmark]", String.format("Get ECDSA sign time standard deviation: %.9f", totalSDSign));

            //Get sing time median
            totalMedSign = median(timeSign);
            Log.d("[Benchmark]", String.format("Get ECDSA sign time median: %.9f", totalMedSign));


            // Get verfy time average
            totalAvgVerify = calculateAVG(timeVerify);
            Log.d("[Benchmark]", String.format("Get ECDSA verfy time average: %.9f", totalAvgVerify));

            //Get verify time standard deviation
            totalSDVerify = calculateSD(timeVerify);
            Log.d("[Benchmark]", String.format("Get ECDSA verify time standard deviation: %.9f", totalSDVerify));

            //Get verify time median
            totalMedVerify = median(timeVerify);
            Log.d("[Benchmark]", String.format("Get ECDSA verify time median: %.9f", totalMedVerify));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a new random key pair for ECDSA or sets an existing key pair.
     *
     * @param keys an existing key pair, it can be <code>null</code> to generate new keys
     */
    public void keyGen(KeyPair keys) {
        if(keys == null) {
            SecureRandom randGen = new SecureRandom();

            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                // TODO error handling
                return;
            }
            keyGen.initialize(2*SEC_LEVEL, randGen);
            keys = keyGen.generateKeyPair();
        }

        this.sk = keys.getPrivate();
        this.pk = keys.getPublic();
    }

    /**
     * Signs the hash of a defined message (it does not recompute the hash).
     *
     * @param msg the hash to be signed
     * @return a byte array representing the signature.
     */
    public byte[] sign(byte[] msg) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        } catch (NoSuchAlgorithmException e1) {
            // TODO error handling
            return null;
        }

        byte[] signature;
        try {
            sig.initSign(this.sk);
            sig.update(msg);
            signature = sig.sign();
        } catch (InvalidKeyException e) {
            // TODO error handling
            return null;
        } catch (SignatureException e) {
            // TODO error handling
            return null;
        }
        return signature;
    }

    /**
     * Verifies a signature from another party.
     *
     * @param pk the signer's public key
     * @param msg the signed message
     * @param signature the signature
     * @return <code>true</code> iff. the signature is valid.
     */
    public static boolean verify(PublicKey pk, byte[] msg, byte[] signature) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        } catch (NoSuchAlgorithmException e1) {
            // TODO error handling
            return false;
        }

        boolean verif;
        try {
            sig.initVerify(pk);
            sig.update(msg);
            verif = sig.verify(signature);
        } catch (InvalidKeyException e) {
            // TODO error handling
            return false;
        } catch (SignatureException e) {
            // TODO error handling
            return false;
        }
        return verif;
    }

    public static double calculateSD(long numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;
        for(double num : numArray) {
            sum += num;
        }
        double mean = sum/(double) length;
        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }

    public static double calculateAVG(long numArray[])
    {
        double sum = 0.0, average = 0.0;
        int length = numArray.length;
        for(double num : numArray) {
            sum += num;
        }
        average = sum/(double) length;
        return average;
    }

    static double median(long[] values) {
        // sort array
        Arrays.sort(values);
        double median;
        // get count of scores
        int totalElements = values.length;
        // check if total number of scores is even
        if (totalElements % 2 == 0) {
            long sumOfMiddleElements = values[totalElements / 2] + values[totalElements / 2 - 1];
            // calculate average of middle elements
            median = sumOfMiddleElements / (double) 2;
        } else {
            // get the middle element
            median = (double) values[values.length / 2];
        }
        return median;
    }
}
