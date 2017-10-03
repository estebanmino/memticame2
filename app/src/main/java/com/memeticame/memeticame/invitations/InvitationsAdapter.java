package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.Invitation;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ESTEBANFML on 01-10-2017.
 */

public class InvitationsAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Invitation> invitationsArrayList;

    private Button btnAccept;
    private Button btnReject;

    private Database mDatabase;
    private String currentUserPhone;

    private  String invitationMessage;
    private  String invitationMail;
    private  String invitationPhone;
    private  String invitationUid;

    public InvitationsAdapter(Context context, ArrayList<Invitation> contactsArrayList) {
        this.context = context;
        this.invitationsArrayList = contactsArrayList;
    }

    @Override
    public int getCount() {
        return invitationsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return invitationsArrayList.get(position);
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
            convertView = layoutInflater.inflate(R.layout.list_item_invitation,null);
        }

        final TextView contactName = convertView.findViewById(R.id.invitation_contact_name);
        final TextView contactMessage = convertView.findViewById(R.id.invitation_message);

        btnAccept = convertView.findViewById(R.id.btn_accept);
        btnReject = convertView.findViewById(R.id.btn_reject);

        mDatabase = new Database();
        mDatabase.init();

        btnAcceptSetOnClickListener();
        btnRejectSetOnClickListener();

        invitationMail = invitationsArrayList.get(position).getAuthorMail();
        invitationMessage = invitationsArrayList.get(position).getMessage();
        invitationPhone = invitationsArrayList.get(position).getAuthorPhone();
        invitationUid = invitationsArrayList.get(position).getUid();

        contactName.setText(invitationMail);
        contactMessage.setText(invitationMessage);

        SharedPreferences shared = context.getSharedPreferences("UserData", MODE_PRIVATE);
        currentUserPhone = (shared.getString("phone", ""));

        return convertView;
    }

    public void btnAcceptSetOnClickListener(){
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.acceptInvitation(invitationUid, currentUserPhone, invitationPhone);
            }
        });
    }

    public void btnRejectSetOnClickListener(){
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.rejectInvitation(invitationUid, currentUserPhone, invitationPhone);
            }
        });
    }
}
