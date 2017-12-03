package com.memeticame.memeticame.models;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.memeticame.memeticame.MainActivity;

import java.util.ArrayList;

/**
 * Created by ESTEBANFML on 30-09-2017.
 */

public class MyPhone {

    public ArrayList<Contact> getContacts(Context context) {
        ArrayList<Contact> array_list_contacts = new ArrayList<>();
        Context applicationContext = context;
        Cursor cursor_contacts = null;

        ContentResolver contentResolver = applicationContext.getContentResolver();
        try {
            cursor_contacts = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
        } catch (Exception ex) {
            Log.e("Error in contacts", ex.getMessage());
        }
        if (cursor_contacts != null) {

            if (cursor_contacts.getCount() > 0) {

                while (cursor_contacts.moveToNext()) {
                    Contact contact = new Contact();
                    String contact_name = cursor_contacts.getString(cursor_contacts.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String contact_phone = cursor_contacts.getString(cursor_contacts.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contact.setName(contact_name);
                    contact.setPhone(contact_phone.replace(" ",",").replace("-",""));
                    array_list_contacts.add(contact);
                }
            }
            cursor_contacts.close();
        }
        return array_list_contacts;
    }
}
