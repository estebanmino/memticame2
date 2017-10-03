package com.memeticame.memeticame.chats;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity {


    //CONSTANTS
    private static final int WRITE_EXTERNAL_REEQUEST = 1886;
    private static final int CAMERA_REQUEST_FOR_PICTURE = 1888;
    private static final int CAMERA_REQUEST_FOR_VIDEO = 1880;
    private static  final int CAMERA_REQUEST_PICTURE = 1887;
    private static final int SELECT_IMAGE = 1885;
    private static final int READ_EXTERNAL_REQUEST = 1884;
    private static final int RECORD_AUDIO_REQUEST = 1883;
    private static final int FILES_REQUEST = 1882;

    private static String ABSOLUTE_STORAGE_PATH;
    private static String mRecordFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    boolean mStartRecording = true;
    boolean mStartPlaying = true;
    boolean isRecording =  false;

    private final Contact chatContact = new Contact();
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";

    private final Database firebaseDatabase = new Database();
    private final ArrayList<Message> messagesList = new ArrayList<>();
    private  ChatRoomAdapter chatRoomAdapter;

    private FloatingActionButton fabSend;
    private EditText editMessage;

    private String currentUserPhone;

    private ConstraintLayout constraintAttachments;
    private ImageView imageAddAttachment;
    private ListView listView;

    private FloatingActionButton fabCamera;
    private FloatingActionButton fabVideo;
    private FloatingActionButton fabAudio;
    private FloatingActionButton fabImages;
    private FloatingActionButton fabFiles;

    //LOCAL VARIABLES
    private String mPath;
    private View mLayout;

    public static Intent getIntent(Context context, String name, String phone, String chatRoomUuid) {
        Intent intent = new Intent(context,ChatRoomActivity.class);
        intent.putExtra(KEY_USERNAME,name);
        intent.putExtra(KEY_PHONE,phone);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseDatabase.init();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData",Context.MODE_PRIVATE);
        currentUserPhone =sharedPreferences.getString("phone", null);

        setChatContact();
        ABSOLUTE_STORAGE_PATH = getExternalCacheDir().getAbsolutePath();
        mRecordFileName = ABSOLUTE_STORAGE_PATH + "/audiorecordtest.3gp";


        //back toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(chatContact.getName());
        }

        mLayout = findViewById(R.id.chat_room_layout);

        fabAudio = (FloatingActionButton) findViewById(R.id.fab_audio);
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabVideo = (FloatingActionButton) findViewById(R.id.fab_video);
        fabFiles = (FloatingActionButton) findViewById(R.id.fab_files);
        fabImages = (FloatingActionButton) findViewById(R.id.fab_images);

        constraintAttachments = (ConstraintLayout) findViewById(R.id.constraint_attachments);
        constraintAttachments.setVisibility(View.GONE);
        imageAddAttachment = (ImageView) findViewById(R.id.image_add_attachment);
        listView = (ListView) findViewById(R.id.reyclerview_message_list);

        setOnClickImageAddAttachment();

        listenSentMessage();
        chatRoomAdapter  = new ChatRoomAdapter(ChatRoomActivity.this, messagesList,firebaseDatabase.mAuth, currentUserPhone);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        listenForMessages();

        listView.setAdapter(chatRoomAdapter);

        setOnClickFabAudio();
        setOnClickFabCamera();
        setOnClickFabVideo();
        setOnClickFabFiles();
        setOnClickFabImages();

    }

    public void setOnClickFabAudio(){
        fabAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (!isRecording) {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        startRecording();
                    }
                } else {
                    getRecorAudioPermissions();
                }
                return true;
            }
        });

        fabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    stopRecording();
                } else {}
            }
        });
    };

    //RECORD AUDIO

    public void getRecorAudioPermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, "Para que podamos grabar audio, necesitamos permiso.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                RECORD_AUDIO_REQUEST);
                    }
                }).show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO_REQUEST);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void onPlay(boolean start, String recordFileName) {
        if (start) {
            startPlaying(recordFileName);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(String recordFileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(recordFileName);
            Log.i("AUDIO RECORD SOURCE", recordFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        isRecording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mRecordFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
        }

    }

    private void stopRecording() {
        isRecording = false;
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        //lesson.getMultimediaAudiosFiles().add(new MultimediaFile("AUDIO",mRecordFileName, transferUtility, S3_BUCKET_NAME));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void setOnClickFabCamera(){
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.i("PERMISSION", "Storage Permission");
                    getWriteStoragePermissions();
                }
                else if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.i("PERMISSION", "Camera Permission");
                    getCameraForPicturePermissions();
                }
                else {
                    Log.i("PERMISSION", "Granted");
                    dispatchTakePictureIntent();
                }
            }
        });
    };

    public void getCameraForPicturePermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, "To take a picture you must give access to camera",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_FOR_PICTURE);
                    }
                }).show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_FOR_PICTURE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    public void getWriteStoragePermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, "To take save your picture you must give access to storage",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_REEQUEST);
                    }
                }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_REEQUEST);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    private void dispatchTakePictureIntent() {

        String APP_DIRECTORY = "memeticaMe";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), APP_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if (!isDirectoryCreated) {
            isDirectoryCreated = file.mkdir();
        }
        Long timestamp = System.currentTimeMillis() / 1000;
        String imageName = timestamp.toString() + ".jpg";

        mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                File.separator +
                APP_DIRECTORY + File.separator + imageName;
        File newFile = new File(file, imageName);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_PICTURE);
    }

    public void setOnClickFabVideo(){
        fabVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.i("PERMISSION", "Storage Permission");
                    getWriteStoragePermissions();
                }
                else if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.i("PERMISSION", "Camera Permission");
                    getCameraForVideoPermissions();
                }
                else {
                    Log.i("PERMISSION", "Granted");
                    dispatchRecordVideoIntent();
                }
            }
        });
    };

    public void getCameraForVideoPermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, "To record a video you must give access to camera",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_FOR_VIDEO);
                    }
                }).show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_FOR_VIDEO);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void dispatchRecordVideoIntent() {

        String APP_DIRECTORY = "memeticaMe";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), APP_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if (!isDirectoryCreated) {
            isDirectoryCreated = file.mkdir();
        }
        Long timestamp = System.currentTimeMillis() / 1000;
        String imageName = timestamp.toString() + ".mp4";

        mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                File.separator +
                APP_DIRECTORY + File.separator + imageName;
        File newFile = new File(file, imageName);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_FOR_VIDEO);
    }

    public void setOnClickFabFiles(){
        fabFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(ABSOLUTE_STORAGE_PATH); // a directory
                intent.setDataAndType(uri, "*/*");
                startActivityForResult(Intent.createChooser(intent, "Open"), FILES_REQUEST);
        }
            });
    };
    public void setOnClickFabImages(){
        fabImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Acceso a galería", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    getReadStoragePermissions();
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
                }
            }
        });
    };

    public void getReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mLayout, "To take see a picture you must give access to the gallery",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                READ_EXTERNAL_REQUEST);
                    }
                }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_REQUEST);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void setOnClickImageAddAttachment() {
        imageAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraintAttachments.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void listenForMessages() {
        String chatRoomUuid = getIntent().getStringExtra(KEY_CHAT_ROOM_UUID);
        DatabaseReference chatRommReference = firebaseDatabase.getReference("chatRooms/"+chatRoomUuid+"/messages");

        chatRommReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();
                for(DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    assert message != null;
                    if (message.getAuthor()!=null) {
                        messagesList.add(message);
                    }
                }
                //Collections.sort(messagesList, (e1, e2)-> new Date(e1.getTimestamp()).compareTo(new Date(e2.getTimestamp())));
                Collections.sort(messagesList, comparator);
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setChatContact() {
        //set contact
        chatContact.setName(getIntent().getStringExtra(KEY_USERNAME));
        chatContact.setPhone(getIntent().getStringExtra(KEY_PHONE));
    }

    public void listenSentMessage() {
        fabSend = (FloatingActionButton)findViewById(R.id.fab_send);
        editMessage = (EditText) findViewById(R.id.edit_message);
            fabSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        if (!editMessage.getText().toString().matches("")){
                        firebaseDatabase.sendMessageTo(
                                editMessage.getText().toString(),
                                currentUserPhone,
                                chatContact.getPhone());
                        editMessage.setText("");
                    }
                }
            });
    }

    Comparator<Message> comparator = new Comparator<Message>() {
        @Override
        public int compare(Message left, Message right) {
            return new Date(left.getTimestamp()).compareTo(new Date(right.getTimestamp()));
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_FOR_PICTURE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
                        dispatchTakePictureIntent();
                    }
                }   else {
                    Toast.makeText(this, "You must give permission to take pictures", Toast.LENGTH_LONG).show();
                }
                break;
            case CAMERA_REQUEST_FOR_VIDEO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
                        dispatchRecordVideoIntent();
                    }
                }   else {
                    Toast.makeText(this, "You must give permission to record videos", Toast.LENGTH_LONG).show();
                }
                break;

            case WRITE_EXTERNAL_REEQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCameraForPicturePermissions();
                } else {
                    Toast.makeText(this, "You must give permission to storage",
                            Toast.LENGTH_LONG).show();
                }
                break;


            case READ_EXTERNAL_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);

                } else {
                    Toast.makeText(this, "You must give permission to pick a file",
                            Toast.LENGTH_LONG).show();
                }
                break;


            case RECORD_AUDIO_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You gave permission to record audio",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "You must give permission to record audio",
                            Toast.LENGTH_LONG).show();
                }
                break;

        }
    }
}
