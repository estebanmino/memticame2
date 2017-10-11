package com.memeticame.memeticame.invitations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.memeticame.memeticame.MainActivity;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Database;

public class NewGroupInvitationActivity extends AppCompatActivity {

    private Button btnCreateChatRoomGroup;
    private EditText editGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_invitation);

        editGroupName = (EditText) findViewById(R.id.edit_group_name);
        btnCreateChatRoomGroup = (Button)findViewById(R.id.btn_create_chat_room_group);
        btnCreateChatRoomGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database database = new Database();
                database.init();
                SharedPreferences sharedPreferences = getSharedPreferences("UserData",Context.MODE_PRIVATE);
                String currentUserPhone =sharedPreferences.getString("phone", null);
                database.createChatRoomGroup(currentUserPhone, editGroupName.getText().toString());
            }
        });
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context,NewGroupInvitationActivity.class);
        return intent;
    }

}
