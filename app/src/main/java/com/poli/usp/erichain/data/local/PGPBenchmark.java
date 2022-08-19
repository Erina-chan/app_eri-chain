package com.poli.usp.erichain.data.local;

import android.util.Log;

import android.content.Context;

import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSignature;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class PGPBenchmark {

    public static int subLoop = 500;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  ;

    /* Key pair */
    private PGPPublicKeyRing pubKeyRing;
    private PGPSecretKeyRing secKeyRing;

    public PGPBenchmark(Context context, String msg) {
        String ident = "benchmark";
        char[] pwd = "senha".toCharArray();

        try {
            long time1;
            long time2;
            long time3;
            long time4;
            long time5;
            long time6;
            PGPSignature signPGP;
            byte[] encryptPGP;
            byte[] decryptPGP;

            double totalAvgSign;
            double totalSDSign;
            double totalMedSign;
            double totalAvgEncrypt;
            double totalSDEncrypt;
            double totalMedEncrypt;
            double totalAvgDecrypt;
            double totalSDDecrypt;
            double totalMedDecrypt;

            long[] timeSign = new long[subLoop];
            long[] timeEncrypt = new long[subLoop];
            long[] timeDecrypt = new long[subLoop];

            PGPManager pgpManager = new PGPManager(context, ident, pwd);

            for (int i = 0; i < subLoop; i++) {
                time1 = Calendar.getInstance().getTimeInMillis();
                // sign
                signPGP = pgpManager.generateSignatureForPublicKey(pgpManager.getEncryptionKey(), pwd);
                time2 = Calendar.getInstance().getTimeInMillis();
                timeSign[i] = time2 - time1;

                time3 = Calendar.getInstance().getTimeInMillis();
                // encryption  byte[] rawData, PGPPublicKey encKey)
                encryptPGP = pgpManager.encrypt(msg.getBytes(), pgpManager.getEncryptionKey());
                time4 = Calendar.getInstance().getTimeInMillis();
                timeEncrypt[i] = time4 - time3;

                time5 = Calendar.getInstance().getTimeInMillis();
                // decryption
                decryptPGP = pgpManager.decrypt(encryptPGP, pwd);
                time6 = Calendar.getInstance().getTimeInMillis();
                timeDecrypt[i] = time6 - time5;
            }

            // Get sign time average
            totalAvgSign = calculateAVG(timeSign);
            Log.d("[Benchmark]", String.format("Get PGP sign time average: %.9f", totalAvgSign));

            //Get sing time standard deviation
            totalSDSign = calculateSD(timeSign);
            Log.d("[Benchmark]", String.format("Get PGP sign time standard deviation: %.9f", totalSDSign));

            //Get sing time median
            totalMedSign = median(timeSign);
            Log.d("[Benchmark]", String.format("Get PGP sign time median: %.9f", totalMedSign));


            // Get encryption time average
            totalAvgEncrypt = calculateAVG(timeEncrypt);
            Log.d("[Benchmark]", String.format("Get PGP encryption time average: %.9f", totalAvgEncrypt));

            //Get encryption time standard deviation
            totalSDEncrypt = calculateSD(timeEncrypt);
            Log.d("[Benchmark]", String.format("Get PGP encryption time standard deviation: %.9f", totalSDEncrypt));

            //Get encryption time median
            totalMedEncrypt = median(timeEncrypt);
            Log.d("[Benchmark]", String.format("Get PGP encryption time median: %.9f", totalMedEncrypt));


            // Get decryption time average
            totalAvgDecrypt = calculateAVG(timeDecrypt);
            Log.d("[Benchmark]", String.format("Get PGP decryption time average: %.9f", totalAvgDecrypt));

            //Get decryption time standard deviation
            totalSDDecrypt = calculateSD(timeDecrypt);
            Log.d("[Benchmark]", String.format("Get PGP decryption time standard deviation: %.9f", totalSDDecrypt));

            //Get decryption time median
            totalMedDecrypt = median(timeDecrypt);
            Log.d("[Benchmark]", String.format("Get PGP decryption time median: %.9f", totalMedDecrypt));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
