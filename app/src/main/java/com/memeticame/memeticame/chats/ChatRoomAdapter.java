package com.memeticame.memeticame.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.security.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ESTEBANFML on 02-10-2017.
 */

public class ChatRoomAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Message> messagesList;
    private final FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String currentUserPhone;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public ChatRoomAdapter(Context context, ArrayList<Message> messagesList, FirebaseAuth mAuth, String currentUserPhone) {
        this.context = context;
        this.messagesList = messagesList;
        this.mAuth = mAuth;
        this.currentUserPhone = currentUserPhone;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.getAuthor().equals(currentUserPhone)) {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == VIEW_TYPE_MESSAGE_RECEIVED) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chat_message_received, null);
        }
        else {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chat_message_sent, null);
        }

        final TextView message = convertView.findViewById(R.id.text_message_body);
        final TextView timestamp = convertView.findViewById(R.id.text_message_time);

        final String messageContent = messagesList.get(position).getContent();
        final long messageTimestamp = messagesList.get(position).getTimestamp();

        message.setText(messageContent);
        Date date = new Date(messageTimestamp);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        timestamp.setText(format.format(date));

        return convertView;
    }
}
