package com.poli.usp.erichain.ui.main;

/**
 * Modified by aerina on 12/7/2021
 * Modified by aerina on 10/5/2022
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.poli.usp.erichain.Constants;
import com.poli.usp.erichain.data.local.ChatServer;
import com.poli.usp.erichain.data.local.Contact;
import com.poli.usp.erichain.data.local.ContactDatabaseHelper;
import com.poli.usp.erichain.data.local.ErrorManager;
import com.poli.usp.erichain.data.local.NodeDiscovery;
import com.poli.usp.erichain.data.local.PGPManager;
import com.poli.usp.erichain.data.local.PGPManagerSingleton;
import com.poli.usp.erichain.data.local.PreferencesHelper;
import com.poli.usp.erichain.data.local.ProgressDialogHelper;
import com.poli.usp.erichain.data.local.SignECDSABenchmark;
import com.poli.usp.erichain.data.local.Utils;
import com.poli.usp.erichain.data.remote.models.SignatureResponse;
import com.poli.usp.erichain.ui.base.BaseActivity;
import com.poli.usp.erichain.ui.chat.ChatActivity;
import com.poli.usp.erichain.ui.dht_visualization.DhtVisualizationActivity;
import com.poli.usp.erichain.utils.exceptions.ContactNotFoundException;
import com.poli.usp.erichain.utils.exceptions.KeyPairNullException;
import com.poli.tcc.dht.DHT;
import com.poli.tcc.dht.DHTException;
import com.poli.tcc.dht.DHTNode;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.List;

import poli.com.mobile2you.whatsp2p.R;

import com.poli.usp.erichain.data.remote.models.MessageResponse;
import com.poli.usp.erichain.ui.common.TrustDialogFragment;

import org.spongycastle.openpgp.PGPSignature;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity implements MainMvpView {

    private MainPresenter mMainPresenter;
    private MainAdapter mAdapter;
    private String mUserId;
    private Contact me;
    private Thread chatThread;
    private NodeDiscovery nodeDiscovery;
    private NewMessageBroadcast newMessageBroadcast;
    private SignatureUpdateBroadcast signatureUpdateBroadcast;
    private DHTConnectionBroadcast dhtConnectionBroadcast;
    private ProgressDialogHelper progressDialog;
    private int mChatPort;
    private int mDHTPort;
    private Boolean mInitialized = false;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    public String trackerAddress = "192.168.15.2";
    // public String trackerAddress = "[FE80:0:0:0:7CEB:534A:C5BF:870D]";
    public int trackerPort = 4001;

    private Pattern pattern;
    private Matcher matcher;

    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainPresenter = new MainPresenter();
        mMainPresenter.attachView(this);

        ErrorManager.setActivity(this);

        newMessageBroadcast = new NewMessageBroadcast();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(newMessageBroadcast, new IntentFilter(Constants.FILTER_CHAT_RECEIVER));

        signatureUpdateBroadcast = new SignatureUpdateBroadcast();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(signatureUpdateBroadcast, new IntentFilter(Constants.FILTER_SIGNATURE_UPDATE));

        dhtConnectionBroadcast = new DHTConnectionBroadcast();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dhtConnectionBroadcast, new IntentFilter(Constants.FILTER_DHT_CONNECTION));

        mChatPort = Utils.getAvailablePort();
        mDHTPort = Utils.getAvailablePort();

        progressDialog = new ProgressDialogHelper(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainAdapter(new MainAdapter.OnClicked() {
            @Override
            public void onContactClicked(final Contact contact) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startChat(contact, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public boolean onContactLongClicked(Contact contact) {
                showContactSettingsDialog(contact);
                return true;
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPresenter.loadContacts(v.getContext());
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        setActionBar("Eri-chain"); // app_name

        mUserId = PreferencesHelper.getInstance().getUserId();
        if (mUserId.equals("")) {
            showUserNameDialog();
        } else if (!mInitialized) {
            initialize();
        }

    }

    public void initialize() {
        me = new Contact(mUserId);
        nodeDiscovery = new NodeDiscovery(getApplicationContext(), mDHTPort);
        buildPGPManager();
        connectToDHT();
        startChatServer(mChatPort);
        mMainPresenter.loadContacts(this);
        mInitialized = true;
    }

    public void startChat(Contact contact, boolean directConnection) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_MYSELF, me);
        intent.putExtra(Constants.EXTRA_CONTACT, contact);
        intent.putExtra(Constants.EXTRA_DIRECT_CONNECTION, directConnection);
        startActivity(intent);
    }

    public void startDhtVisualization() {
        Intent intent = new Intent(MainActivity.this, DhtVisualizationActivity.class);
        startActivity(intent);
    }

    public void showMessage(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showNewContactDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_add_contact, null);
        // create alert dialog
        AlertDialog alertDialog = getNewContactDialogBuilder(dialogView).create();
        alertDialog.show();
    }

    public void showEditContactDialog(Contact contact) {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_add_contact, null);
        // create alert dialog
        AlertDialog alertDialog = getEditContactDialogBuilder(dialogView, contact).create();
        alertDialog.show();
    }


    public void showContactSettingsDialog(final Contact contact) {
        CharSequence options[] = new CharSequence[]{"Edit", "Clear messages", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contact.getName());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showEditContactDialog(contact);
                        break;
                    case 1:
                        deleteConversation(contact);
                        break;
                    case 2:
                        deleteContact(contact);
                        break;
                }
            }
        });
        builder.show();
    }

    public void showUserNameDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_set_username, null);
        // create alert dialog
        AlertDialog alertDialog = getUserNameDialogBuilder(dialogView).create();
        alertDialog.show();
    }

    public void showEditUserNameDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_set_username, null);
        // create alert dialog
        AlertDialog alertDialog = editUserNameDialogBuilder(dialogView).create();
        alertDialog.show();
    }

    public void showDirectConnectionDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.dialog_direct_connection, null);
        // create alert dialog
        AlertDialog alertDialog = directConnectionDialogBuilder(dialogView).create();
        alertDialog.show();
    }

    public AlertDialog.Builder getNewContactDialogBuilder(View dialogView) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);

        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.edit_text_contact_name);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String username = userInput.getText().toString();
                        addContact(username);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder;

    }

    public AlertDialog.Builder getEditContactDialogBuilder(View dialogView, final Contact contact) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);

        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.edit_text_contact_name);

        userInput.setText(contact.getId());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String username = userInput.getText().toString();
                        contact.setId(username);
                        editContact(contact);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder;

    }

    public AlertDialog.Builder getUserNameDialogBuilder(View dialogView) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);

        final EditText editTextUserName = (EditText) dialogView
                .findViewById(R.id.edit_text_user_name);
        final EditText editTextPassword = (EditText) dialogView
                .findViewById(R.id.edit_text_user_password);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String userName = editTextUserName.getText().toString();
                        PreferencesHelper.getInstance().putUserId(userName);
                        String userPassword = editTextPassword.getText().toString();
                        PreferencesHelper.getInstance().putUserPassword(userPassword);
                        mUserId = userName;
                        initialize();
                    }
                });
        return alertDialogBuilder;

    }

    public AlertDialog.Builder editUserNameDialogBuilder(View dialogView) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);

        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.edit_text_user_name);
        userInput.setText(mUserId);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String userName = userInput.getText().toString();
                        PreferencesHelper.getInstance().putUserId(userName);
                        mUserId = userName;
                    }
                });
        return alertDialogBuilder;

    }

    public AlertDialog.Builder directConnectionDialogBuilder(View dialogView) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);

//      final EditText editTextName = (EditText) dialogView
//                .findViewById(R.id.edit_text_user_name);
        final EditText editTextIP = (EditText) dialogView
                .findViewById(R.id.edit_text_contact_ip);
        final EditText editTextPort = (EditText) dialogView
                .findViewById(R.id.edit_text_contact_port);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = "Direct connection";//editTextName.getText().toString();
                        // TODO Insert a verification if editTextIP and editTextPort are not null.
                        String ip = editTextIP.getText().toString();
                        int port = Integer.parseInt(editTextPort.getText().toString());
                        createDirectConnection(name, ip, port);
                    }
                });
        return alertDialogBuilder;

    }

    public void addContact(final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog.show("Buscando usuário...");
                    final PublicKey signPublicKey = (PublicKey) DHT.get(username);  // There is ECDSA Public Key
                    if (signPublicKey == null) {
                        throw new ContactNotFoundException();
                    }
                    final InetSocketAddress address = (InetSocketAddress) DHT.getProtected("chatAddress", signPublicKey);
                    final byte[] chatPublicKeyRingEncoded = (byte[]) DHT.getProtected("chatPublicKey", signPublicKey);  // There is PGP public key
                    // TODO Get here the EGDH public key
                    if (address == null || chatPublicKeyRingEncoded == null) {
                        throw new ContactNotFoundException();
                    }
                    final String ip = address.getHostName();
                    final int port = address.getPort();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMainPresenter.addContact(getApplicationContext(), username, ip, port, signPublicKey.getEncoded(), chatPublicKeyRingEncoded);
                        }
                    });
                    progressDialog.hide();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    progressDialog.hide();
                } catch (ContactNotFoundException e) {
                    progressDialog.hide();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    public void editContact(Contact contact) {
        mMainPresenter.updateContact(this, contact);
    }

    public void deleteContact(Contact contact) {
        mMainPresenter.deleteConversation(this, contact);
        mMainPresenter.deleteContact(this, contact);
    }

    public void deleteConversation(Contact contact) {
        mMainPresenter.deleteConversation(this, contact);
    }

    public boolean validate(final String ip) {
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    public void createDirectConnection(String name, String ip, int port) {

        pattern = Pattern.compile(IPADDRESS_PATTERN);
        matcher = pattern.matcher(ip);  // verify with regex if this is a IP number
        if (matcher.matches() && port > 0 && port < 65536) {
            try {
                if (DHT.connectTo(ip, port)) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.FILTER_DHT_CONNECTION));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid IP address.");
        }
    }

    private void buildPGPManager() {
        try {
            progressDialog.show("Getting security keys...");
            String userPassword = PreferencesHelper.getInstance().getUserPassword();
            PGPManagerSingleton.initialize(new PGPManager(this.getApplicationContext(), PreferencesHelper.getInstance().getUserId(), userPassword.toCharArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog.hide();
    }

    private void connectToDHT() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog.show("Connecting to tracker...");
                    String mUserId = PreferencesHelper.getInstance().getUserId();
                    Number160 peerId = DHT.createPeerID(mUserId);
                    DHTNode thisNode = new DHTNode(peerId);
                    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
                    KeyPair signKeyPair = Utils.getKeyPairFromKeyStore(getApplicationContext(), "EC");
                    if (signKeyPair == null) {
                        throw new KeyPairNullException();
                    }
                    thisNode.setUsername(mUserId);
                    thisNode.setIp(Utils.getIPAddress(true));
                    thisNode.setPort(mChatPort);
                    thisNode.setSignKeyPair(signKeyPair);
                    me.setIp(Utils.getIPAddress(true));
                    me.setPort(mChatPort);
                    me.setSignPublicKeyEncoded(signKeyPair.getPublic().getEncoded());
                    me.setChatPublicKeyRingEncoded(PGPManagerSingleton.getInstance().getPublicKeyRing().getEncoded());
                    // TODO Add ECDH key ring here.
                    DHT.start(thisNode, mDHTPort);
                    if (DHT.connectTo(trackerAddress, trackerPort)) {
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.FILTER_DHT_CONNECTION));
                    }
                    //nodeDiscovery.startLookup();

                } catch (DHTException.UsernameAlreadyTakenException e) {
                    showToast("This username is not available!");
                } catch (KeyPairNullException e) {
                    showToast("Could not generate DSA keys!");
                    e.printStackTrace();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                progressDialog.hide();
            }
        }).start();
    }

    private void startChatServer(int port) {
        chatThread = new Thread(new ChatServer(port, getApplicationContext()));
        chatThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_person:
                showNewContactDialog();
                break;
            case R.id.action_edit_info:
                showEditUserNameDialog();
                break;
            case R.id.action_view_dht:
                startDhtVisualization();
                break;
            case R.id.action_reconnect:
                connectToDHT();
                break;
            case R.id.action_view_certificate:
                FragmentManager fm = getSupportFragmentManager();
                TrustDialogFragment trustDialogFragment = TrustDialogFragment.newInstance(me, true);
                trustDialogFragment.show(fm, "dialog_contact_trust");
                break;
            case R.id.action_direct_connection:
                showDirectConnectionDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainPresenter.loadContacts(this);
    }

    protected void onPause() {
        super.onPause();
        if (nodeDiscovery != null) {
            nodeDiscovery.stopLookup();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainPresenter.detachView();
        DHT.shutDown();
        nodeDiscovery.shutdown();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(newMessageBroadcast);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(signatureUpdateBroadcast);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(dhtConnectionBroadcast);
    }

    //@Override
    public void showEmptyContacts() {
        showToast("No contacts.");
    }

    //@Override
    public void showError(String error) {
        showToast(error);
    }

    //@Override
    public void showProgress(boolean show) {
        showProgressDialog(show);
    }

    @Override
    public void showContacts(List<Contact> contacts) {
        mAdapter.setContacts(contacts);
    }

    private class NewMessageBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MessageResponse mes = (MessageResponse) intent.getSerializableExtra("message");
            showToast("New message from " + mes.getSender().getId());
            ContactDatabaseHelper dbHelper = new ContactDatabaseHelper(context);
            if (dbHelper.search(mes.getSender().getId()).isEmpty()) {
                mMainPresenter.addContact(getApplicationContext(), mes.getSender());
            }
        }
    }

    private class SignatureUpdateBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                SignatureResponse mes = (SignatureResponse) intent.getSerializableExtra("message");
                me.setChatPublicKeyRingEncoded(PGPManagerSingleton.getInstance().getPublicKeyRing().getEncoded());
                if (mes.getTrust()) {
                    showToast(mes.getIdentifier() + " just signed your certificate!");
                } else {
                    showToast(mes.getIdentifier() + " just revoked the signature on your certificate!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DHTConnectionBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DHT.verify();
                        Log.i("DHT", "[DHT] Broadcasting my chat address: " + me.getIp() + ":" + mChatPort);
                        FuturePut fput = DHT.putProtected("chatAddress", new InetSocketAddress(me.getIp(), mChatPort));
                        if (fput.isFailed()) {
                            Log.i("DHT", "[DHT] Chat address update failed: " + fput.failedReason());
                            showMessage("Chat address update failed!");
                        }
                        Log.i("DHT", "[DHT] Broadcasting my public PGP chat key");
                        fput = DHT.putProtected("chatPublicKey", PGPManagerSingleton.getInstance().getPublicKeyRing().getEncoded());
                        if (fput.isFailed()) {
                            Log.i("DHT", "[DHT] Chat public key PGP update failed: " + fput.failedReason());
                            showMessage("Chat public PGP key update failed!");
                        }
                        showMessage("Connected to the DHT network!");
                    } catch (DHTException.UsernameAlreadyTakenException e) {
                        e.printStackTrace();
                        showMessage("Username not available!");
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressDialog.hide();
                }
            }).start();
        }
    }
}


//public class MainActivity extends BaseActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        Log.d("[Benchmark]", "On Start");
//
//        try {
//            SignECDSABenchmark ecdasBenchmark = new SignECDSABenchmark();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
