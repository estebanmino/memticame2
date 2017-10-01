package com.memeticame.memeticame;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;

import java.util.ArrayList;

/**
 * Created by ESTEBANFML on 30-09-2017.
 */

public class ContactsRequestFirebase extends AsyncTask {

    private Database firebaseDatabase;
    private final ArrayList<String> numberList;

    public ContactsRequestFirebase(Database firebaseDatabase, ArrayList<String> numberList) {
        this.firebaseDatabase = firebaseDatabase;
        this.numberList = numberList;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //database init
        firebaseDatabase.init();

        DatabaseReference usersDatabase = firebaseDatabase.getReference("users");
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numberList.clear();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = userSnapshot.getValue(Contact.class);
                    assert contact != null;
                    numberList.add(contact.getPhone());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        return numberList;
    }
}
