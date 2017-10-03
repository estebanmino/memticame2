package com.memeticame.memeticame.models;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


/**
 * Created by efmino on 24-08-17.
 */

public class Database {

    private FirebaseDatabase mDatabase;
    public FirebaseAuth mAuth;
    public Boolean isMyContactResult = false;
    private StorageReference mStorageRef;


    public void init() {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public DatabaseReference getReference(String somewhere) {
        return mDatabase.getReference(somewhere);
    }

    private void uploadFile(final Message message, String filePath) {
        if (message.getMultimedia() != null) {
            Uri file = Uri.fromFile(new File(filePath));
            StorageReference riversRef = mStorageRef.child(message.getMultimedia());

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
    }

    public void sendMessageTo(final String content, final Message message, final String receiverPhone, final String filePath) {
        final String currentUserPhone = message.getAuthor();
        final String multimediaFile = message.getMultimedia();
        DatabaseReference currentUserContactsReference = mDatabase.getReference("users/"+
                currentUserPhone+"/contacts");

        uploadFile(message,filePath);

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
                    final DatabaseReference downloadUrlReference =
                            mDatabase.getReference(referencePath+"/multimediaUrl");

                    contentReference.setValue(content);
                    authorReference.setValue(currentUserPhone);
                    downloadUrlReference.setValue(message.getMultimediaUrl());
                    if (multimediaFile!= null) {
                        multimediaReference.setValue(message.getMultimedia());
                    } else {
                        multimediaReference.setValue(null);
                    }
                    Date date = new Date();
                    long timestamp =  date.getTime();
                    timestampReference.setValue(timestamp);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void acceptInvitation(final String uid, final String currentUserPhone, final String invitationPhone) {

        String uuidChatRoom = UUID.randomUUID().toString();
        DatabaseReference myUserPhoneReference =
                mDatabase.getReference("users/" + currentUserPhone + "/contacts/" + invitationPhone);
        myUserPhoneReference.setValue(uuidChatRoom);

        DatabaseReference contactUserPhoneReference =
                mDatabase.getReference("users/" + invitationPhone + "/contacts/" + currentUserPhone);
        contactUserPhoneReference.setValue(uuidChatRoom);

        DatabaseReference invitationReference =
                mDatabase.getReference("users/" + currentUserPhone + "/invitations/" + uid);
        invitationReference.removeValue();

        final DatabaseReference chatRoomReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom);

        final DatabaseReference firstUserReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/firstUser/");
        firstUserReference.setValue(currentUserPhone);
        final DatabaseReference secondUserReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/secondUser/");
        secondUserReference.setValue(invitationPhone);
    }

    public void rejectInvitation(final String uid, final String currentUserPhone, final String invitationPhone) {

        DatabaseReference invitationReference =
                mDatabase.getReference("users/" + currentUserPhone + "/invitations/" + uid);
        invitationReference.removeValue();
    }

    public void sendInvitation(final String contact_phone, final String message, final String currentUserPhone) {
        DatabaseReference usersReference = mDatabase.getReference("users");

        final String uuidInvitation = UUID.randomUUID().toString();
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(contact_phone)) {

                    final String receiverInvitesPath = "users/"+contact_phone+"/invitations/"+uuidInvitation+"/";

                    final DatabaseReference messageReference =
                            mDatabase.getReference(receiverInvitesPath+"message");
                    final DatabaseReference authorReference =
                            mDatabase.getReference(receiverInvitesPath+"authorMail");
                    final DatabaseReference authorPhoneReference =
                            mDatabase.getReference(receiverInvitesPath+"authorPhone");
                    final DatabaseReference timestampReference =
                            mDatabase.getReference(receiverInvitesPath+"timestamp");

                    messageReference.setValue(message);
                    authorReference.setValue(mAuth.getCurrentUser().getEmail());
                    authorPhoneReference.setValue(currentUserPhone);

                    Date date = new Date();
                    long timestamp =  date.getTime();
                    timestampReference.setValue(timestamp);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void sendNotification(String user, String message) {
    }

}
