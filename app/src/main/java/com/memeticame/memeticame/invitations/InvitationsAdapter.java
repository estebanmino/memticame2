package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Invitation;

import java.util.ArrayList;

/**
 * Created by ESTEBANFML on 01-10-2017.
 */

public class InvitationsAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Invitation> contactsArrayList;

    public InvitationsAdapter(Context context, ArrayList<Invitation> contactsArrayList) {
        this.context = context;
        this.contactsArrayList = contactsArrayList;
    }

    @Override
    public int getCount() {
        return contactsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactsArrayList.get(position);
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
            convertView = layoutInflater.inflate(R.layout.contact_view,null);
        }

        final TextView contactName = convertView.findViewById(R.id.contact_name);
        final TextView contactPhone = convertView.findViewById(R.id.contact_phone);

        final String elementAuthor = contactsArrayList.get(position).getAuthor();
        final String elementMessage = contactsArrayList.get(position).getMessage();

        contactName.setText(elementAuthor);
        contactPhone.setText(elementMessage);

        return convertView;
    }

}
