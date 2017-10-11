package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.memeticame.memeticame.MainActivity;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;

public class InvitationActivity extends AppCompatActivity {

    private TextView textTitle;
    private TextView textInvitation;
    private EditText editMessage;
    private Button btnSend;

    private static final String KEY_CONTACT_NAME = "contact_name";
    private static final String KEY_CONTACT_EMAIL = "contact_email";
    private static final String KEY_CONTACT_PHONE = "contact_phone";

    private final Contact inviteContact = new Contact();
    Database database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        database = new Database();
        database.init();

        textTitle = (TextView)  findViewById(R.id.text_title);
        textInvitation = (TextView)  findViewById(R.id.text_invitation);
        editMessage = (EditText)  findViewById(R.id.edit_message);
        btnSend = (Button) findViewById(R.id.btn_send);
        setChatContact();

        String placeholder = "Ivitation for ";
        if (!inviteContact.getName().isEmpty()){
            textInvitation.setText(placeholder + inviteContact.getName() + " (" + inviteContact.getPhone()+")");
        }
        else {
            textInvitation.setText(placeholder + inviteContact.getEmail() + " (" + inviteContact.getPhone()+")");
        }

        setBtnSendListener();
    }

    public void setBtnSendListener() {
        SharedPreferences shared = getSharedPreferences("UserData", MODE_PRIVATE);
        final String currentUserPhone = (shared.getString("phone", ""));
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.sendInvitation(inviteContact.getPhone(), editMessage.getText().toString(), currentUserPhone);
                MainActivity.getIntent(InvitationActivity.this);
            }
        });
    }

    public static Intent getIntent(Context context, String phone, String email, String name) {
        Intent intent = new Intent(context,InvitationActivity.class);
        intent.putExtra(KEY_CONTACT_PHONE, phone);
        intent.putExtra(KEY_CONTACT_EMAIL, email);
        intent.putExtra(KEY_CONTACT_NAME, name);
        return intent;
    }

    public void setChatContact() {
        //set contact
        inviteContact.setEmail(getIntent().getStringExtra(KEY_CONTACT_EMAIL));
        inviteContact.setPhone(getIntent().getStringExtra(KEY_CONTACT_PHONE));
        inviteContact.setName(getIntent().getStringExtra(KEY_CONTACT_NAME));
    }
}
