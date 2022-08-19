package com.poli.usp.erichain.data.local;

import android.util.Log;

import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Calendar;

import static com.poli.usp.erichain.data.local.ECDHManager.ECDHKeyExchange;

public class ECDHBenchmark {

    /** Security level */
    public static final int SEC_LEVEL = 128;

    public static int subLoop = 500;

    /* Instances to cryptographic protocols:
     * Message Digest: SHA256
     * Key exchange method: ECDH (curve secp256)
     */
    public static String KEY_ALGORITHM = "ECDH";
    public static String PROVIDER = "SC";
    public static String CURVE_PARAMETER = "secp256k1";

    /* Key pair */
    private ECPrivateKey sk;
    public ECPublicKey pk;

    public ECDHBenchmark() {

        // Generate keys pair
        keyGen(null);

        long time1;
        long time2;
        byte[] exchangedKey;
        boolean verifyECDSA;
        double totalAvg;
        double totalSD;
        double totalMed;

        long[] times = new long[subLoop];
        for (int i = 0; i < subLoop; i++) {
            try {
                time1 = Calendar.getInstance().getTimeInMillis();
                exchangedKey = ECDHKeyExchange(sk, pk);
                time2 = Calendar.getInstance().getTimeInMillis();
                times[i] = time2 - time1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Get DH key exchange time average
        totalAvg = calculateAVG(times);
        Log.d("[Benchmark]", String.format("Get ECDH key exchange time average: %.9f", totalAvg));

        //Get DH key exchange time standard deviation
        totalSD = calculateSD(times);
        Log.d("[Benchmark]", String.format("Get ECDH key exchange time standard deviation: %.9f", totalSD));

        //Get DH key exchange time median
        totalMed = median(times);
        Log.d("[Benchmark]", String.format("Get ECDH key exchange time median: %.9f", totalMed));
    }

    /**
     * Generates a new random key pair for ECDH or sets an existing key pair.
     * @param keys an existing key pair, it can be <code>null</code> to generate new keys
     */
    public void keyGen(KeyPair keys) {
        if(keys == null) {

            KeyPairGenerator kpg;
            try {
                kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
                kpg.initialize(new ECGenParameterSpec(CURVE_PARAMETER));

                // Generate user key pair to ECDH
                keys = kpg.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
        this.sk = (ECPrivateKey) keys.getPrivate();
        this.pk = (ECPublicKey) keys.getPublic();
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
