package com.memeticame.memeticame.chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.MyPhone;

import java.util.ArrayList;
import java.util.List;


public class ChatRoomsFragment extends Fragment {

    private ArrayList<String> myChatsList = new ArrayList<>();
    private ArrayList<String> chatsList = new ArrayList<>();
    private List<String> myPhoneContactsNumbers = new ArrayList<>();
    private List<String> myPhoneContactsNames = new ArrayList<>();

    private ChatRoomsAdapter arrayAdapter;
    private ArrayList<Contact> myPhoneContacts;
    private ArrayList<Contact> phoneChatRooms = new ArrayList<>();

    private Database firebaseDatabase = new Database();
    private MyPhone mPhone = new MyPhone();

    private SharedPreferences sharedPreferences;
    String currentUserPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //database connection init
        firebaseDatabase.init();
        sharedPreferences = getActivity().getSharedPreferences("UserData",Context.MODE_PRIVATE);
        currentUserPhone =sharedPreferences.getString("phone", null);

        myPhoneContacts = mPhone.getContacts(getActivity());
        for (Contact contact: myPhoneContacts) {
            myPhoneContactsNumbers.add(contact.getPhone());
            myPhoneContactsNames.add(contact.getEmail());
        }

        arrayAdapter = new ChatRoomsAdapter(getActivity(), phoneChatRooms, firebaseDatabase.mAuth);
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listview = view.findViewById(R.id.chats_list);

        listenChatContacts();

        listview.setAdapter(arrayAdapter);

        listenChatClick(listview);
    }

    private void listenChatClick(ListView listview) {

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserData",Context.MODE_PRIVATE);
                String currentUserPhone =sharedPreferences.getString("phone", null);
                final DatabaseReference chatRoomReference = firebaseDatabase.getReference("users/"+
                        currentUserPhone+"/chatRooms/");

                chatRoomReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(phoneChatRooms.get(position).getPhone()) != null){
                            String chatRoomUid = dataSnapshot.child(
                                    phoneChatRooms.get(position).getPhone()).getValue().toString();
                            Log.i("ISGROUPONTOUCH", phoneChatRooms.get(position).getIsGroup());
                            startActivity(
                                    ChatRoomActivity.getIntent(
                                            getActivity(),
                                    phoneChatRooms.get(position).getName(),
                                    phoneChatRooms.get(position).getPhone(),
                                    chatRoomUid,
                                            phoneChatRooms.get(position).getIsGroup()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void listenChatContacts() {
        final DatabaseReference userContactsDatabase = firebaseDatabase.getReference("users/"+currentUserPhone+"/chatRooms");

        userContactsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myChatsList.clear();
                chatsList.clear();
                phoneChatRooms.clear();
                for (DataSnapshot chatRoomInfo: dataSnapshot.getChildren()){
                    String chat_room_name = chatRoomInfo.getKey().toString();
                    if (myPhoneContactsNumbers.contains(chat_room_name)) {
                        int index = myPhoneContactsNumbers.indexOf(chat_room_name);
                        myPhoneContacts.get(index).setIsGroup("false");
                        phoneChatRooms.add(myPhoneContacts.get(index));
                    }
                    else {
                        Log.i("GETKEY",chatRoomInfo.getKey().toString());
                        Log.i("GETKEY",chatRoomInfo.getChildren().toString());
                        Contact unknownContact = new Contact();
                        unknownContact.setEmail(chat_room_name);
                        unknownContact.setName(chat_room_name);
                        unknownContact.setPhone(chat_room_name);
                        unknownContact.setIsGroup("true");
                        phoneChatRooms.add(unknownContact);
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
