package com.memeticame.memeticame.threading;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.memeticame.memeticame.models.ChatRoom;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ESTEBANFML on 14-10-2017.
 */

public class ChatGroupInformation  extends AsyncTask<String,Float,Integer> {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageRef;

    private static String chatRoomUuid;

    private TextView textGroupName;
    private TextView textCreatedAt;
    private TextView textCreator;

    public ChatGroupInformation(String chatRoomUuid, TextView textGroupName, TextView textCreatedAt, TextView textCreator) {
        this.chatRoomUuid = chatRoomUuid;
        this.textGroupName = textGroupName;
        this.textCreatedAt = textCreatedAt;
        this.textCreator = textCreator;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(String... strings) {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference chatRoomsReference = mDatabase.getReference("chatRooms");
        chatRoomsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(chatRoomUuid) != null){
                    Log.i("CHATROOM",dataSnapshot.child(chatRoomUuid).getValue().toString());
                    ChatRoom chatRoom = dataSnapshot.child(chatRoomUuid).getValue(ChatRoom.class);
                    textGroupName.setText(chatRoom.getGroupName());
                    textCreator.setText(chatRoom.getCreator());
                    Date date = new Date(chatRoom.getCreatedAt());
                    Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    textCreatedAt.setText(format.format(date));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return 1;

    }
}
