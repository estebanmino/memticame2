package com.memeticame.memeticame.chats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.contacts.ContactsAdapter;
import com.memeticame.memeticame.invitations.NewGroupInvitationActivity;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.MyPhone;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomAddMemberActivity extends AppCompatActivity {

    private static final String KEY_CHAT_ROOM_NAME = "chatRoomName";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";


    private ListView listContacts;
    private Button btnAddContact;
    private MyPhone mPhone = new MyPhone();
    private ContactsAdapter contactsAdapter;

    private final Database firebaseDatabase = new Database();

    private final List<String> myPhoneContactsNumbers = new ArrayList<>();
    private final List<String> myPhoneContactsNames = new ArrayList<>();
    private ArrayList<String> numberList = new ArrayList<>();
    private final ArrayList<Contact> myPhoneContactsInDatabase = new ArrayList<>();

    private ArrayList<Contact> contactsSelected =  new ArrayList<>();

    private String chatRoomName;
    private String chatRoomUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_add_member);

        //back toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select contacts");
        }

        firebaseDatabase.init();

        chatRoomName = getIntent().getStringExtra(KEY_CHAT_ROOM_NAME);
        chatRoomUuid = getIntent().getStringExtra(KEY_CHAT_ROOM_UUID);


        listContacts  = (ListView) findViewById(R.id.list_contacts);
        btnAddContact = (Button) findViewById(R.id.btn_add_members);
        contactsAdapter = new ContactsAdapter(ChatRoomAddMemberActivity.this, myPhoneContactsInDatabase);
        listContacts.setAdapter(contactsAdapter);
        setContactsListListener();
        setDataUsersListener();

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database database = new Database();
                database.init();
                for (Contact contact: contactsSelected) {
                    database.sendInvitation(contact.getPhone(), "Invitation to join " + chatRoomName + " group", chatRoomName , chatRoomUuid);
                }
            }
        });
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
                ArrayList<Contact> myPhoneContacts = mPhone.getContacts(ChatRoomAddMemberActivity.this);
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

    public static Intent getIntent(Context context, String chatRoomName, String chatRoomUuid) {
        Intent intent = new Intent(context,ChatRoomAddMemberActivity.class);
        intent.putExtra(KEY_CHAT_ROOM_NAME,chatRoomName);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        return intent;
    }
}
