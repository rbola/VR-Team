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
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
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
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;


import java.lang.ref.WeakReference;

import java.util.*;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "officeSpaceApp";
    final StitchAppClient client = Stitch.getDefaultAppClient();
    private NfcAdapter nfc;
    private TextView text;
    private StitchAppClient _client;
    private RemoteMongoClient _mongoClient;
    private RemoteMongoCollection _remoteCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        nfc = SetupNFC();
        text = findViewById(R.id.cardInfo);

        this._client = Stitch.getDefaultAppClient();
        this._client.getAuth().loginWithCredential(new AnonymousCredential());
        _mongoClient = this._client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");


        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        _remoteCollection = _mongoClient.getDatabase("officespace").getCollection("events", Event.class).withCodecRegistry(pojoCodecRegistry);


        _remoteCollection.sync().configure(
                DefaultSyncConflictResolvers.remoteWins(),
                new MyUpdateListener(),
                new MyErrorListener());

        TextView disclaimer = findViewById(R.id.disclaimer);
        disclaimer.setText("Ready To CheckIn!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs


        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag == null) {
                text.setText("tag == null");
            } else {
                String tagInfo = "";
                byte[] tagId = tag.getId();
                for (int i = 0; i < tagId.length; i++) {
                    tagInfo += Integer.toHexString(tagId[i] & 0xFF);
                }
                if (!tagInfo.isEmpty()) {


                    String summary = "Emergency Team Sync";
                    String location = "Dublin Office";
                    String description = "Office Space Demo";


                    Date s = new Date(System.currentTimeMillis());
                    Date e = new Date(s.getTime() + (30 * ONE_MINUTE_IN_MILLIS));

                    EventTime start = new EventTime();
                    EventTime end = new EventTime();

                    start.setDateTime(s);
                    start.setTimeZone("Ireland/Dublin");
                    end.setDateTime(e);
                    end.setTimeZone("Ireland/Dublin");

                    ArrayList attendees = new ArrayList();

                    Map<String, String> attendee0 = new HashMap<>();
                    attendee0.put("email", ("nunzio@example.com"));
                    Map<String, String> attendee1 = new HashMap<>();
                    attendee1.put("email", ("rita@example.com"));


                    attendees.add(attendee0);
                    attendees.add(attendee1);

                    addEvent(summary, location, description, start, end, attendees);

                    text.setText("Room 2003 is booked and my Team Alerted");

                } else {
                    text.setText("Room not available!");
                }


            }
        } else {
            Toast.makeText(this,
                    "Don't look so stressed, its bad for your health...",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void addEvent(String summary, String location, String description, EventTime start, EventTime end, ArrayList attendees) {
        final Event doc = new Event();
        doc.setOwner_id(client.getAuth().getUser().getId());
        doc.setSummary(summary);
        doc.setLocation(location);
        doc.setDescription(description);
        doc.setStart(start);
        doc.setEnd(end);
        doc.setAttendees(attendees);

        final Task<RemoteInsertOneResult> res = _remoteCollection.insertOne(doc);
        res.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull final Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.e(TAG, "New Event added", task.getException());
                } else {
                    Log.e(TAG, "Error Event item", task.getException());
                }
            }
        });
    }

    private NfcAdapter SetupNFC() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        return nfcAdapter;
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
}
