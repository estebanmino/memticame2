package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.contacts.ContactsAdapter;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.Invitation;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.util.ArrayList;


public class InvitationsFragment extends Fragment {

    SharedPreferences sharedPreferences;
    Database firebaseDatabase;

    private ListView invitationsList;
    private FloatingActionButton fabAddGroupInvitation;
    private InvitationsAdapter invitationsAdapter;
    private ArrayList<Invitation> invitationsArray = new ArrayList<Invitation>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase = new Database();
        firebaseDatabase.init();
        return inflater.inflate(R.layout.fragment_invitations, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invitationsList = view.findViewById(R.id.list_invitations);
        invitationsAdapter = new InvitationsAdapter(getActivity(), invitationsArray);
        fabAddGroupInvitation = view.findViewById(R.id.fab_add_group_invitation);
        invitationsList.setAdapter(invitationsAdapter);
        //requestContactsPermission();
        setInvitationsListener();
        setFabAddGroupInvitationListener();
    }

    private void setFabAddGroupInvitationListener(){
        fabAddGroupInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(NewGroupInvitationActivity.getIntent(getActivity()));
            }
        });
    }

    public void setInvitationsListener() {
        sharedPreferences = getActivity().getSharedPreferences("UserData",Context.MODE_PRIVATE);
        String currentUserPhone =sharedPreferences.getString("phone", null);
        DatabaseReference currentUserInvitationsRef = firebaseDatabase.getReference("users/"+currentUserPhone+"/invitations");
        currentUserInvitationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                invitationsArray.clear();
                for(DataSnapshot inviteSnapshot : dataSnapshot.getChildren()) {
                    Invitation invitation = inviteSnapshot.getValue(Invitation.class);
                    invitation.setUid(inviteSnapshot.getKey());
                    invitationsArray.add(invitation);
                }
                invitationsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
