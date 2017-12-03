package com.memeticame.memeticame.chats;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.memeticame.memeticame.R;
import com.memeticame.memeticame.threading.ChatGroupInformation;

public class ChatRoomInformationActivity extends AppCompatActivity {

    private static final String KEY_CHAT_ROOM_NAME = "chatRoomName";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";

    private String chatRoomName;
    private String chatRoomUuid;

    private TextView textGroupName;
    private TextView textCreatedAt;
    private TextView textCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_information);

        chatRoomName = getIntent().getStringExtra(KEY_CHAT_ROOM_NAME);
        chatRoomUuid = getIntent().getStringExtra(KEY_CHAT_ROOM_UUID);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(chatRoomName);
        }

        textGroupName = (TextView) findViewById(R.id.text_group_name);
        textCreatedAt = (TextView) findViewById(R.id.text_created_at);
        textCreator = (TextView) findViewById(R.id.text_creator);

        ChatGroupInformation chatGroupInformation = new ChatGroupInformation(
                chatRoomUuid, textGroupName, textCreatedAt, textCreator);
        chatGroupInformation.execute();
    }

    public static Intent getIntent(Context context, String chatRoomName, String chatRoomUuid) {
        Intent intent = new Intent(context,ChatRoomInformationActivity.class);
        intent.putExtra(KEY_CHAT_ROOM_NAME,chatRoomName);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        return intent;
    }
}
