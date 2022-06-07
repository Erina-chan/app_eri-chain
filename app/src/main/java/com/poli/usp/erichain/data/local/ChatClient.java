package com.poli.usp.erichain.data.local;

/**
 * Created by mayerlevy on 9/30/17.
 */

import android.content.Context;
import android.util.Log;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSignature;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchProviderException;

import com.poli.usp.erichain.data.remote.models.BaseResponse;
import com.poli.usp.erichain.utils.exceptions.CouldNotEncryptException;

public class ChatClient {

    private String ip;
    private int port;
    private Socket client;

    public ChatClient() {
        this.client = new Socket();
    }

    public Boolean connect(String ip, int port) {
        try {
            Log.d("Chat", "Connecting to " + ip + ":" + port);
            client = new Socket();
            client.connect(new InetSocketAddress(ip, port), 2000);
            if (this.isConnected()) {
                this.ip = ip;
                this.port = port;
                Log.d("Chat", "Connected!");
                return true;
            } else {
                Log.d("Chat", "Couldn't connect to " + ip + ":" + port);
                return false;
            }
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean sendMessage(BaseResponse message, Context context) {
        try {
            if (!this.isConnected()) {
                connect(this.ip, this.port);
            }
            if (this.isConnected()) {
                byte[] serializedMessage = Utils.serialize(message);
                PGPPublicKeyRing chatPublicKeyRing = PGPUtils.readPublicKeyRingFromStream(new ByteArrayInputStream(message.getReceiver().getChatPublicKeyRingEncoded()));
                PGPPublicKey chatPublicKey = PGPUtils.getEncryptionKeyFromKeyRing(chatPublicKeyRing);

//                String userPassword = PreferencesHelper.getInstance().getUserPassword();
//                SignatureECDSAManager signECDSA = new SignatureECDSAManager(context, message.getSender().getId(), userPassword.toCharArray());
//                PGPSignature msgSign = signECDSA.generateSignatureForMessage(userPassword.toCharArray(), serializedMessage);
//                byte[] serializedSign = Utils.serialize(msgSign);

                byte[] encryptedMessage = PGPManagerSingleton.getInstance().encrypt(serializedMessage, chatPublicKey);
                if (encryptedMessage == null) {
                    throw new CouldNotEncryptException();
                }
                DataOutputStream dOut1 = new DataOutputStream(this.client.getOutputStream());
//                DataOutputStream dOut2 = new DataOutputStream(this.client.getOutputStream());
                // dOut.writeInt(serializedMessage.length); // write length of the message
                // dOut.write(serializedMessage);            // write the message
                dOut1.writeInt(encryptedMessage.length); // write length of the message
                dOut1.write(encryptedMessage);            // write the message
//                dOut2.writeInt(serializedSign.length);   // TODO: Verificar se é possível mandar e receber em duas partes.
//                dOut2.write(serializedSign);
                return true;
            }
        } catch (IOException | CouldNotEncryptException | NoSuchProviderException | PGPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void shutdown() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    public Boolean isConnected() {
        return client != null && client.isConnected();
    }
}