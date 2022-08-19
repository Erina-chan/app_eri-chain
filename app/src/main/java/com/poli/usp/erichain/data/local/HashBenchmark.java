package com.poli.usp.erichain.data.local;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

public class HashBenchmark {
    /** Security level */
    public static final int SEC_LEVEL = 128;

    public static int subLoop = 500;

    public HashBenchmark(String message) {
        long time1;
        long time2;
        byte[] generatedHash;
        boolean verifyECDSA;
        double average;
        double standardDeviation;
        double median;

        long[] times = new long[subLoop];
        for (int i = 0; i < subLoop; i++) {
            try {
                time1 = Calendar.getInstance().getTimeInMillis();
                generatedHash = HashManager.generateSha3(message);
                time2 = Calendar.getInstance().getTimeInMillis();
                times[i] = time2 - time1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Get DH key exchange time average
        average = calculateAVG(times);
        Log.d("[Benchmark]", String.format("Get SHA3-256 hash time average: %.9f", average));

        //Get DH key exchange time standard deviation
        standardDeviation = calculateSD(times);
        Log.d("[Benchmark]", String.format("Get SHA3-256 hash time standard deviation: %.9f", standardDeviation));

        //Get DH key exchange time median
        median = median(times);
        Log.d("[Benchmark]", String.format("Get SHA3-256 hash time median: %.9f", median));
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
