package com.memeticame.memeticame.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.UUID;


/**
 * Created by efmino on 24-08-17.
 */

public class Database {

    private FirebaseDatabase mDatabase;
    public FirebaseAuth mAuth;
    public Boolean isMyContactResult = false;


    public void init() {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public DatabaseReference getReference(String somewhere) {
        return mDatabase.getReference(somewhere);
    }

    public void sendMessageTo(final String content, final String currentUserPhone, final String receiverPhone) {
        DatabaseReference currentUserContactsReference = mDatabase.getReference("users/"+
                currentUserPhone+"/contacts");

        Log.i("REFERENCE PATH", "users/"+
                currentUserPhone+"/contacts");

        final String uuidMessage = UUID.randomUUID().toString();
        currentUserContactsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverPhone)) {
                    final String referencePath = "chatRooms/"+dataSnapshot.
                            child(receiverPhone).getValue().toString()+"/"+uuidMessage+"/";

                    final DatabaseReference contentReference =
                            mDatabase.getReference(referencePath+"content");
                    final DatabaseReference authorReference =
                            mDatabase.getReference(referencePath+"author");
                    final DatabaseReference timestampReference =
                            mDatabase.getReference(referencePath+"timestamp");

                    contentReference.setValue(content);
                    authorReference.setValue(currentUserPhone);

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
}
