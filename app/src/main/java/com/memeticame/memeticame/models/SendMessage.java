package com.memeticame.memeticame.models;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ESTEBANFML on 04-10-2017.
 */

public class SendMessage extends AsyncTask<String,Float,Integer> {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageRef;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(String... strings) {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        final String currentUserPhone = strings[0];
        final String multimediaFile = strings[2];
        final String filePath = strings[3];
        final String receiverPhone = strings[1];
        final String content = strings[4];
        DatabaseReference currentUserContactsReference = mDatabase.getReference("users/"+
                currentUserPhone+"/contacts");

        if (multimediaFile != null) {
            Uri file = Uri.fromFile(new File(filePath));
            StorageReference riversRef = mStorageRef.child(multimediaFile);

            riversRef.putFile(file)

                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });
        }
        Log.i("PROGRESS","50");

        final String uuidMessage = UUID.randomUUID().toString();
        currentUserContactsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverPhone)) {
                    final String referencePath = "chatRooms/"+dataSnapshot.
                            child(receiverPhone).getValue().toString()+"/messages/"+uuidMessage;

                    final DatabaseReference contentReference =
                            mDatabase.getReference(referencePath+"/content");
                    final DatabaseReference authorReference =
                            mDatabase.getReference(referencePath+"/author");
                    final DatabaseReference timestampReference =
                            mDatabase.getReference(referencePath+"/timestamp");
                    final DatabaseReference multimediaReference =
                            mDatabase.getReference(referencePath+"/multimedia");
                    Log.i("PROGRESS","60");

                    contentReference.setValue(content);
                    authorReference.setValue(currentUserPhone);
                    Log.i("PROGRESS","80");

                    if (multimediaFile!= null) {
                        multimediaReference.setValue(multimediaFile);
                    } else {
                        multimediaReference.setValue(null);
                    }
                    Date date = new Date();
                    long timestamp =  date.getTime();
                    timestampReference.setValue(timestamp);
                    Log.i("PROGRESS","100");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return null;
    }
}
