package com.memeticame.memeticame.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.ChatRoom;
import com.memeticame.memeticame.models.Contact;

import java.util.ArrayList;

/**
 * Created by ESTEBANFML on 02-10-2017.
 */

public class ChatRoomsAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Contact> arrayList;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public ChatRoomsAdapter(Context context, ArrayList<Contact> arrayList, FirebaseAuth mAuth) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chat_preview,null);
        }

        final TextView contactName = convertView.findViewById(R.id.contact_name);
        final TextView lastMessage = convertView.findViewById(R.id.last_message);

        final String elementName = arrayList.get(position).getName();

        contactName.setText(elementName);
        lastMessage.setText("last message");

        return convertView;
    }
}
