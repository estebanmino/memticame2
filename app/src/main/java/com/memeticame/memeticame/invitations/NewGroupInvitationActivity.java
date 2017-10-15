package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.MainActivity;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.contacts.ContactsAdapter;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.MyPhone;

import java.util.ArrayList;
import java.util.List;

public class NewGroupInvitationActivity extends AppCompatActivity {

    private Button btnCreateChatRoomGroup;
    private EditText editGroupName;
    private ListView listContacts;

    private ArrayList<Contact> myPhoneContacts;
    private MyPhone mPhone = new MyPhone();
    private ContactsAdapter contactsAdapter;
    private ListView contactsList;

    private final Database firebaseDatabase = new Database();

    private final List<String> myPhoneContactsNumbers = new ArrayList<>();
    private final List<String> myPhoneContactsNames = new ArrayList<>();
    private ArrayList<String> numberList = new ArrayList<>();
    private final ArrayList<Contact> myPhoneContactsInDatabase = new ArrayList<>();

    private ArrayList<Contact> contactsSelected =  new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_invitation);

        firebaseDatabase.init();

        editGroupName = (EditText) findViewById(R.id.edit_group_name);

        listContacts = (ListView) findViewById(R.id.list_contacts);
        contactsAdapter = new ContactsAdapter(NewGroupInvitationActivity.this, myPhoneContactsInDatabase);
        listContacts.setAdapter(contactsAdapter);
        setContactsListListener();
        setDataUsersListener();

        btnCreateChatRoomGroup = (Button)findViewById(R.id.btn_create_chat_room_group);
        btnCreateChatRoomGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database database = new Database();
                database.init();
                SharedPreferences sharedPreferences = getSharedPreferences("UserData",Context.MODE_PRIVATE);
                String currentUserPhone =sharedPreferences.getString("phone", null);
                database.createChatRoomGroup(currentUserPhone, editGroupName.getText().toString(),contactsSelected);
            }
        });
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context,NewGroupInvitationActivity.class);
        return intent;
    }


    private void setContactsListListener(){
        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contact selected_contact = myPhoneContactsInDatabase.get(i);

                if (contactsSelected.contains(selected_contact)){
                    contactsSelected.remove(contactsSelected.indexOf(selected_contact));
                    listContacts.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_dark,null));
                } else {
                    contactsSelected.add(selected_contact);
                    listContacts.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_disabled,null));
                }
            }
        });
    }

    private void setDataUsersListener() {
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

                myPhoneContactsInDatabase.clear();
                ArrayList<Contact> myPhoneContacts = mPhone.getContacts(NewGroupInvitationActivity.this);
                for (Contact contact: myPhoneContacts) {
                    myPhoneContactsNumbers.add(contact.getPhone());
                    myPhoneContactsNames.add(contact.getEmail());
                }
                for (String contact_number: numberList){
                    int index = myPhoneContactsNumbers.indexOf(contact_number);
                    if (index != -1) {
                        myPhoneContactsInDatabase.add(myPhoneContacts.get(index));
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


}
