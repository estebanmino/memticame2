package com.memeticame.memeticame;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.MyPhone;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private ArrayList<Contact> myPhoneContacts;
    private MyPhone mPhone = new MyPhone();
    private ContactsAdapter contactsAdapter;
    private ListView contactsList;

    private final List<String> myPhoneContactsNumbers = new ArrayList<>();
    private final List<String> myPhoneContactsNames = new ArrayList<>();
    private ArrayList<String> numberList = new ArrayList<>();
    private final ArrayList<Contact> myPhoneContactsInDatabase = new ArrayList<>();

    private final Database firebaseDatabase = new Database();

    final private int CONTACTS_REQUEST = 111;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myPhoneContacts = mPhone.getContacts(getActivity());
        contactsAdapter = new ContactsAdapter(getActivity(), myPhoneContacts);


        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestContactsPermission();
    }

    private void getContacts(View view) {

        myPhoneContactsInDatabase.clear();
        ArrayList<Contact> myPhoneContacts = mPhone.getContacts(getActivity());
        for (Contact contact: myPhoneContacts) {
            Log.i("CONTACTPHONEFROMCONTACT", contact.getPhone());

            myPhoneContactsNumbers.add(contact.getPhone());
            myPhoneContactsNames.add(contact.getEmail());
        }

        for (String contact: myPhoneContactsNumbers) {
            Log.i("CONTACTPHONE", contact);
        }

        ContactsRequestFirebase contactsRequestFirebase = new ContactsRequestFirebase(firebaseDatabase,numberList);


        Log.i("LENGHT", Integer.toString(numberList.size()));
        for (String contact_number : numberList) {
            Log.i("INDEX", contact_number);
            int index = myPhoneContactsNumbers.indexOf(contact_number);
            if (index != -1) {
                myPhoneContactsInDatabase.add(myPhoneContacts.get(index));
            }
        }

        contactsList = view.findViewById(R.id.contacts_list);
        ContactsAdapter contactsAdapter = new ContactsAdapter(getActivity(), myPhoneContactsInDatabase);
        contactsList.setAdapter(contactsAdapter);


    }

    public void requestContactsPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        } else {
            getContacts(getView());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CONTACTS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    getContacts(getView());
                } else {
                    // permission denied
                    Toast.makeText(getActivity(), "Until you grant the permission, we canot display the contacts", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
