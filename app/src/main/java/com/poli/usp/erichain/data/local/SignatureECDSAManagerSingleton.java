package com.poli.usp.erichain.data.local;

public class SignatureECDSAManagerSingleton {

    // TODO: Verificar
    // Warning:(5, 13) Do not place Android context classes in static fields
    // (static reference to `SignatureECDSAManager` which has field `mContext` pointing to `Context`);
    // this is a memory leak (and also breaks Instant Run)
    private static SignatureECDSAManager holder;

    public static void initialize(SignatureECDSAManager ecdsaManager) {
        holder = ecdsaManager;
    }
    public static SignatureECDSAManager getInstance() {return holder;}
}
