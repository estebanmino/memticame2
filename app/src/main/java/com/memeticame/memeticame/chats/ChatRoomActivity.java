package com.memeticame.memeticame.chats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity {


    private final Contact chatContact = new Contact();
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";

    private final Database firebaseDatabase = new Database();
    private final ArrayList<Message> messagesList = new ArrayList<>();
    private  ChatRoomAdapter chatRoomAdapter;

    private FloatingActionButton fabSend;
    private EditText editMessage;

    private String currentUserPhone;


    public static Intent getIntent(Context context, String name, String phone, String chatRoomUuid) {
        Intent intent = new Intent(context,ChatRoomActivity.class);
        intent.putExtra(KEY_USERNAME,name);
        intent.putExtra(KEY_PHONE,phone);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseDatabase.init();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData",Context.MODE_PRIVATE);
        currentUserPhone =sharedPreferences.getString("phone", null);

        setChatContact();

        //back toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(chatContact.getName());
        }

        listenSentMessage();
        chatRoomAdapter  = new ChatRoomAdapter(ChatRoomActivity.this, messagesList,
                firebaseDatabase.mAuth, currentUserPhone);
        ListView listView = (ListView) findViewById(R.id.reyclerview_message_list);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        listenForMessages();
        listView.setAdapter(chatRoomAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void listenForMessages() {
        String chatRoomUuid = getIntent().getStringExtra(KEY_CHAT_ROOM_UUID);
        DatabaseReference chatRommReference = firebaseDatabase.getReference("chatRooms/"+chatRoomUuid+"/");

        chatRommReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();
                for(DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    assert message != null;
                    if (message.getAuthor()!=null) {
                        messagesList.add(message);
                    }
                }
                //Collections.sort(messagesList, (e1, e2)-> new Date(e1.getTimestamp()).compareTo(new Date(e2.getTimestamp())));
                Collections.sort(messagesList, comparator);
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setChatContact() {
        //set contact
        chatContact.setName(getIntent().getStringExtra(KEY_USERNAME));
        chatContact.setPhone(getIntent().getStringExtra(KEY_PHONE));
    }

    public void listenSentMessage() {
        fabSend = (FloatingActionButton)findViewById(R.id.fab_send);
        editMessage = (EditText) findViewById(R.id.edit_message);
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseDatabase.sendMessageTo(
                        editMessage.getText().toString(),
                        currentUserPhone,
                        chatContact.getPhone());
            }
        });
    }

    Comparator<Message> comparator = new Comparator<Message>() {
        @Override
        public int compare(Message left, Message right) {
            return new Date(left.getTimestamp()).compareTo(new Date(right.getTimestamp()));
        }
    };
}
