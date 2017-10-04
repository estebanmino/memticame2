package com.memeticame.memeticame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.MainActivity;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.util.ArrayList;

public class AuthenticationActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private EditText editPhoneNumber;
    private Button btnSignIn;
    private Button btnSignUp;
    private Button btnToSignUp;
    private Button btnToSignIn;
    private TextView titleText;

    private Database firebaseDatabase = new Database();
    public FirebaseAuth mAuth;

    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        editEmail = (EditText) findViewById(R.id.edit_email);
        editPassword = (EditText) findViewById(R.id.edit_password);
        editPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);

        btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        btnToSignUp = (Button) findViewById(R.id.btn_to_sign_up);
        btnToSignIn = (Button) findViewById(R.id.btn_to_sign_in);

        titleText = (TextView) findViewById(R.id.text_title);
        titleText.setText("Log In MemeticaMe");


        btnSignUp.setVisibility(EditText.GONE);
        btnToSignIn.setVisibility(EditText.GONE);
        editPhoneNumber.setVisibility(EditText.INVISIBLE);

        onClickBtnToSignUp();
        onClickBtnToSignIn();
        onClickBtnSignIn();
        onClickBtnSignUp();

        firebaseDatabase.init();
        mAuth = firebaseDatabase.mAuth;
        currentUser = firebaseDatabase.mAuth.getCurrentUser();
        //updateUI(currentUser);

    }

    private void onClickBtnToSignUp() {
        btnToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn.setVisibility(Button.GONE);
                btnToSignUp.setVisibility(Button.GONE);
                btnSignUp.setVisibility(Button.VISIBLE);
                btnToSignIn.setVisibility(Button.VISIBLE);
                editPhoneNumber.setVisibility(EditText.VISIBLE);
                titleText.setText("Register in MemeticaMe");
                onClickBtnSignUp();
            }
        });
    }

    private void onClickBtnToSignIn() {
        btnToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn.setVisibility(Button.VISIBLE);
                btnToSignUp.setVisibility(Button.VISIBLE);
                btnSignUp.setVisibility(Button.GONE);
                btnToSignIn.setVisibility(Button.GONE);
                editPhoneNumber.setVisibility(EditText.INVISIBLE);

                titleText.setText("Log In MemeticaMe");
            }
        });
    }

    private void onClickBtnSignIn() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int validations = 0;
                String email = editEmail.getText().toString().replace(" ","");
                if (email.matches("") || !isValidEmail(email)) {
                    Toast.makeText(AuthenticationActivity.this, "You did not enter a valid email", Toast.LENGTH_SHORT).show();
                    validations++;
                }

                String password = editPassword.getText().toString();
                if (password.matches("")) {
                    Toast.makeText(AuthenticationActivity.this, "You did not enter a password", Toast.LENGTH_SHORT).show();
                    validations++;
                }
                if (validations == 0) {
                    signInWithEmailAndPassword(email, password);
                }
            }
        });
    }

    private void onClickBtnSignUp() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int validations = 0;
                String email = editEmail.getText().toString().replace(" ","");
                if (email.matches("") || !isValidEmail(email)) {
                    Toast.makeText(AuthenticationActivity.this, "You did not enter a valid email", Toast.LENGTH_SHORT).show();
                    validations++;
                }

                String password = editPassword.getText().toString();
                if (password.matches("")) {
                    Toast.makeText(AuthenticationActivity.this, "You did not enter a password", Toast.LENGTH_SHORT).show();
                    validations++;
                }

                String phone = editPhoneNumber.getText().toString();
                if (phone.matches("")) {
                    Toast.makeText(AuthenticationActivity.this, "You did not enter your phone number", Toast.LENGTH_SHORT).show();
                    validations++;
                }

                if (validations == 0) {
                    createUserWithEmailAndPassword(email, password, phone);
                }
            }
        });
    }

    private void createUserWithEmailAndPassword(final String email, final String password, final String  phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(MainActivity.getIntent(AuthenticationActivity.this));
                            //updateUI(user);
                            createUser(email, phone);
                            SharedPreferencesClass sharedPreferencesClass = new SharedPreferencesClass();
                            sharedPreferencesClass.setUsersPreferences(
                                    getSharedPreferences("UserData",Context.MODE_PRIVATE),
                                    email,
                                    phone
                            );


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signInWithEmailAndPassword(final String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(MainActivity.getIntent(AuthenticationActivity.this));
                            //updateUI(user);
                            setDatabaseListenerForPhone(email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNIN", "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void setDatabaseListenerForPhone(final String email){
        DatabaseReference usersDatabase = firebaseDatabase.getReference("users");
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = userSnapshot.getValue(Contact.class);
                    if (contact.getEmail().equals(email)){
                        SharedPreferencesClass sharedPreferencesClass = new SharedPreferencesClass();
                        sharedPreferencesClass.setUsersPreferences(
                                getSharedPreferences("UserData",Context.MODE_PRIVATE),
                                email,
                                contact.getPhone()
                        );
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context,AuthenticationActivity.class);
        return intent;
    }

    private void createUser(String email, String phone) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myUserRef = database.getReference("users");
        DatabaseReference phoneChild = myUserRef.child(phone);

        phoneChild.child("email").setValue(email);
        phoneChild.child("phone").setValue(phone);
    }
}
