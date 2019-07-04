package com.poli.usp.erichain.data.local;

import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.crypto.digests.KeccakDigest;

public class HashManager {
    /**
     * This function calculate the hash SHA3-256 of String
     * @param data
     * @return hash of data
     */
    public static byte[] generateSha3(String data) {
        SHA3Digest digest = new SHA3Digest(256);
        byte[] byteData= data.getBytes();
        digest.update(byteData, 0, byteData.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * This function calculate the hash Keccak-256 of String
     * @param text
     * @return hash of text
     */
    public static byte[] generateKeccak(String text) {
        try {
            KeccakDigest digest = new KeccakDigest(256);
            byte[] byteText= text.getBytes();
            digest.update(byteText, 0, byteText.length);
            byte[] hash = new byte[digest.getDigestSize()];
            digest.doFinal(hash, 0);
            return hash;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
