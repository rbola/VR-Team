package com.mongodb.office.bookroom;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.auth.StitchUser;

import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchAuthListener;
import com.mongodb.stitch.android.core.StitchAppClient;

import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.ChangeEvent;

import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ChangeEventListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.DefaultSyncConflictResolvers;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ErrorListener;
import org.bson.BsonValue;
import org.bson.Document;



import java.lang.ref.WeakReference;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfc;
    private TextView text;
    private static final String TAG = "blogApp";
    private StitchAppClient _client;
    private RemoteMongoClient _mongoClient;
    private RemoteMongoCollection _remoteCollection;
    final StitchAppClient client = Stitch.getDefaultAppClient();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        nfc = SetupNFC();
        text = findViewById(R.id.cardInfo);

        this._client = Stitch.getDefaultAppClient();
        this._client.getAuth().loginWithCredential(new AnonymousCredential());
        _mongoClient = this._client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        _remoteCollection = _mongoClient.getDatabase("blog").getCollection("comments");


        _remoteCollection.sync().configure(
                DefaultSyncConflictResolvers.remoteWins(),
                new MyUpdateListener(),
                new MyErrorListener());

        TextView disclaimer = findViewById(R.id.disclaimer);
        disclaimer.setText("Ready To CheckIn!");
    }

    @Override
    protected void onResume(){
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null){
                text.setText("tag == null");
            }else{
                String tagInfo = "";
                byte[] tagId = tag.getId();
                for(int i=0; i<tagId.length; i++){
                    tagInfo += Integer.toHexString(tagId[i] & 0xFF);
                }
                if(!tagInfo.isEmpty()){
                    addItem("Room 2003 Booked");

                    text.setText("Room 2003 is booked");

                }
                else{
                    text.setText("Room not available!");
                }


            }
        }else{
            Toast.makeText(this,
                    "Don't look so stressed, its bad for your health...",
                    Toast.LENGTH_SHORT).show();
        }

    }


    private static class MyAuthListener implements StitchAuthListener {

        private WeakReference<MainActivity> _main;
        private StitchUser _user;

        public MyAuthListener(final MainActivity activity) {
            _main = new WeakReference<>(activity);
        }



    }
    private class MyUpdateListener implements ChangeEventListener<Document> {
        @Override
        public void onEvent(final BsonValue documentId, final ChangeEvent<Document> event) {

            // Is this change coming from local or remote?

            if (event.hasUncommittedWrites()) { //change initiated on the device
                Log.d("STITCH", "Local change to document " + documentId);


            } else { //remote change
                Log.d("STITCH", "Remote change to document " + documentId);

            }
        }
    }

    private class MyErrorListener implements ErrorListener {
        @Override
        public void onError(BsonValue documentId, Exception error) {
            Log.e("Stitch", error.getLocalizedMessage());

            Set<BsonValue> docsThatNeedToBeFixed = (Set<BsonValue>) _remoteCollection.sync().getPausedDocumentIds();
            for (BsonValue doc_id : docsThatNeedToBeFixed) {
                // Add your logic to inform the user.
                // When errors have been resolved, call
                _remoteCollection.sync().resumeSyncForDocument(doc_id);
            }
        }
    }

    private void addItem(final String text) {
        final Document doc = new Document();
        doc.put("owner_id", _client.getAuth().getUser().getId());
        doc.put("comment", text);

            final Task<RemoteInsertOneResult> res = _remoteCollection.insertOne(doc);
        res.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull final Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.e(TAG, "New item added", task.getException());
                } else {
                    Log.e(TAG, "Error adding item", task.getException());
                }
            }
        });
    }


    private NfcAdapter SetupNFC(){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        }else if(!nfcAdapter.isEnabled()){
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        return nfcAdapter;
    }
}