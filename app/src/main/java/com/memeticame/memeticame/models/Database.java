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
    private StorageReference mStorageRef;

    public void init() {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public DatabaseReference getReference(String somewhere) {
        return mDatabase.getReference(somewhere);
    }

    public void acceptInvitation(final String uid, final String currentUserPhone, final String chatWith, String chatRoomUuid) {

        if (chatRoomUuid == null) {
            chatRoomUuid = UUID.randomUUID().toString();

            DatabaseReference contactUserPhoneReference = mDatabase.getReference("users/" + chatWith + "/chatRooms/" + currentUserPhone);
            contactUserPhoneReference.setValue(chatRoomUuid);

            final DatabaseReference chatRoomReference =
                    mDatabase.getReference("chatRooms/"+chatRoomUuid);
        }

        DatabaseReference myUserPhoneReference =
                mDatabase.getReference("users/" + currentUserPhone + "/chatRooms/" + chatWith);
        myUserPhoneReference.setValue(chatRoomUuid);


        DatabaseReference invitationReference =
                mDatabase.getReference("users/" + currentUserPhone + "/invitations/" + uid);
        invitationReference.removeValue();
    }

    public void createChatRoomGroup(final String currentUserPhone, final String groupName, final ArrayList<Contact> invitedContacts) {

        String uuidChatRoom = UUID.randomUUID().toString();

        DatabaseReference myUserPhoneReference =
                mDatabase.getReference("users/" + currentUserPhone + "/chatRooms/" + groupName);
        myUserPhoneReference.setValue(uuidChatRoom);

        final DatabaseReference usersReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/members/");
        usersReference.setValue("{"+currentUserPhone+"}");

        final DatabaseReference groupNameReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/groupName/");
        groupNameReference.setValue(groupName);

        final DatabaseReference groupCreatorReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/creator/");
        groupCreatorReference.setValue(currentUserPhone);

        final DatabaseReference groupCreatedAtReference =
                mDatabase.getReference("chatRooms/"+uuidChatRoom+"/createdAt/");
        Date date = new Date();
        long timestamp =  date.getTime();
        groupCreatedAtReference.setValue(timestamp);

        for (Contact contact: invitedContacts) {
            sendInvitation(contact.getPhone(),"Invitation to join "+ groupName + "group", groupName, uuidChatRoom);
        }
    }

    public void rejectInvitation(final String uid, final String currentUserPhone, final String invitationPhone) {

        DatabaseReference invitationReference =
                mDatabase.getReference("users/" + currentUserPhone + "/invitations/" + uid);
        invitationReference.removeValue();
    }

    public void sendInvitation(final String contact_phone, final String message, final String chatWith, final String uuidChatGroup) {
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
                    final DatabaseReference chatWitheReference =
                            mDatabase.getReference(receiverInvitesPath+"chatWith");
                    final DatabaseReference timestampReference =
                            mDatabase.getReference(receiverInvitesPath+"timestamp");

                    final DatabaseReference groupReference =
                            mDatabase.getReference(receiverInvitesPath+"chatRoomUuid");

                    messageReference.setValue(message);
                    authorReference.setValue(mAuth.getCurrentUser().getEmail());
                    chatWitheReference.setValue(chatWith);
                    groupReference.setValue(uuidChatGroup);

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

}
