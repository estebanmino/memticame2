package com.memeticame.memeticame.chats;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.FilesHandler;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.threading.SendMessage;

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
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    boolean isRecording =  false;

    private final Contact chatContact = new Contact();
    private static final String KEY_CHAT_ROOM_NAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";
    private static final String KEY_IS_GROUP = "isGroup";

    private static final String APP_DIRECTORY = "memeticaMe";
    private static final String IMAGE_FORMAT = ".jpg";
    private static final String VIDEO_FORMAT = ".mp4";


    private final Database firebaseDatabase = new Database();
    private final FilesHandler filesHandler = new FilesHandler();

    private final ArrayList<Message> messagesList = new ArrayList<>();
    private MessagesAdapter chatRoomAdapter;

    private FloatingActionButton fabSend;
    private EditText editMessage;

    private String currentUserPhone;

    private ConstraintLayout constraintAttachments;
    private ImageView imageAddAttachment;
    private ImageView imageAttachment;
    private ListView listView;

    private FloatingActionButton fabCamera;
    private FloatingActionButton fabVideo;
    private FloatingActionButton fabAudio;
    private FloatingActionButton fabImages;
    private FloatingActionButton fabFiles;
    private ProgressBar progressBar;

    //LOCAL VARIABLES
    private String mPath;
    private View mLayout;

    private String author;
    private String multimedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseDatabase.init();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData",Context.MODE_PRIVATE);
        currentUserPhone =sharedPreferences.getString("phone", null);

        setChatContact();
        ABSOLUTE_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

        //back toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(chatContact.getName());
        }

        author = currentUserPhone;

        mLayout = findViewById(R.id.chat_room_layout);

        fabAudio = (FloatingActionButton) findViewById(R.id.fab_audio);
        fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabVideo = (FloatingActionButton) findViewById(R.id.fab_video);
        fabFiles = (FloatingActionButton) findViewById(R.id.fab_files);
        fabImages = (FloatingActionButton) findViewById(R.id.fab_images);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        constraintAttachments = (ConstraintLayout) findViewById(R.id.constraint_attachments);
        constraintAttachments.setVisibility(View.GONE);
        imageAddAttachment = (ImageView) findViewById(R.id.image_add_attachment);
        imageAttachment = (ImageView) findViewById(R.id.image_attachment);
        imageAttachment.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.reyclerview_message_list);

        setOnClickImageAddAttachment();

        listenSentMessage();
        chatRoomAdapter  = new MessagesAdapter(ChatRoomActivity.this, messagesList,firebaseDatabase.mAuth, currentUserPhone);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getStringExtra(KEY_IS_GROUP).equals("true"))
            getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
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
                        String audioMultimedia = ABSOLUTE_STORAGE_PATH+"/"+ts.toString()+".3gp";
                        startRecording(audioMultimedia);
                        multimedia = "audios/"+ts.toString()+".3gp";
                        mPath = audioMultimedia;
                        imageAttachment.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_play_audio));
                        imageAttachment.setVisibility(View.VISIBLE);

                    }
                } else {
                    getRecorAudioPermissions();
                }
                return false;
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
                Snackbar.make(mLayout, "Para que podamos grabar audio, necesitamos permiso.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                RECORD_AUDIO_REQUEST);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO_REQUEST);
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
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording(String audioMultimedia) {
        isRecording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(audioMultimedia);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) { }
    }

    private void stopRecording() {
        isRecording = false;
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
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
                    getWriteStoragePermissions();
                }
                else if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    getCameraForPicturePermissions();
                }
                else {
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
                Snackbar.make(mLayout, "To take a picture you must give access to camera",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_FOR_PICTURE);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_FOR_PICTURE);
            }
        }
    }
    public void getWriteStoragePermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(mLayout, "To take save your picture you must give access to storage",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_REEQUEST);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_REEQUEST);
            }
        }
    }
    private void dispatchTakePictureIntent() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), APP_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if (!isDirectoryCreated) {
            file.mkdir();
        }
        Long timestamp = System.currentTimeMillis() / 1000;
        String imageName = timestamp.toString() + IMAGE_FORMAT;

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
                    getWriteStoragePermissions();
                }
                else if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    getCameraForVideoPermissions();
                }
                else {
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
                Snackbar.make(mLayout, "To record a video you must give access to camera",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_FOR_VIDEO);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_FOR_VIDEO);
            }
        }
    }

    private void dispatchRecordVideoIntent() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), APP_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if (!isDirectoryCreated) {
            file.mkdir();
        }
        Long timestamp = System.currentTimeMillis() / 1000;
        String imageName = timestamp.toString() + VIDEO_FORMAT;

        mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                File.separator +
                APP_DIRECTORY + File.separator + imageName;
        File newFile = new File(file, imageName);

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, CAMERA_REQUEST_FOR_VIDEO);
        }
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

                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    getReadStoragePermissions();
                }
                else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    Uri uri = Uri.parse(ABSOLUTE_STORAGE_PATH);
                    intent.setDataAndType(uri, "image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
                }
            }
        });
    };

    public void getReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(ChatRoomActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatRoomActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(mLayout, "To take see a picture you must give access to the gallery",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                READ_EXTERNAL_REQUEST);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_REQUEST);
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
        else if (item.getItemId() == R.id.action_show_info) {
            startActivity(ChatRoomInformationActivity.getIntent(ChatRoomActivity.this,
                    getIntent().getStringExtra(KEY_CHAT_ROOM_NAME),
                    getIntent().getStringExtra(KEY_CHAT_ROOM_UUID)));
        }
        else  if (item.getItemId() == R.id.action_add_member) {
            startActivity(ChatRoomAddMemberActivity.getIntent(ChatRoomActivity.this,
                    getIntent().getStringExtra(KEY_CHAT_ROOM_NAME),
                    getIntent().getStringExtra(KEY_CHAT_ROOM_UUID)));
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
                Collections.sort(messagesList, comparator);
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void setChatContact() {
        //set contact
        chatContact.setName(getIntent().getStringExtra(KEY_CHAT_ROOM_NAME));
        chatContact.setPhone(getIntent().getStringExtra(KEY_PHONE));
    }

    public void listenSentMessage() {
        fabSend = (FloatingActionButton)findViewById(R.id.fab_send);
        editMessage = (EditText) findViewById(R.id.edit_message);
            fabSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!editMessage.getText().toString().matches("")){
                        SendMessage sendMessage=  new SendMessage(
                                progressBar,imageAttachment, editMessage,
                                fabSend, multimedia, ChatRoomActivity.this, ChatRoomActivity.this);

                        sendMessage.execute(author, chatContact.getPhone(),
                                multimedia, mPath, editMessage.getText().toString());
                        multimedia = null;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case CAMERA_REQUEST_PICTURE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "scanned"+path+":");
                                    Log.i("ExternalStorage", "-> Uri"+uri);
                                }
                            });
                    multimedia = "images/"+mPath.substring(mPath.lastIndexOf("/") + 1);
                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    imageAttachment.setVisibility(View.VISIBLE);
                    imageAttachment.setImageBitmap(ThumbnailUtils.extractThumbnail(bitmap, 80, 80));
                    imageAttachment.setRotation(90);

                    break;

                case SELECT_IMAGE:

                    if (data != null)
                    {
                        mPath = filesHandler.getRealPathFromURI_API19(getApplicationContext(),data.getData());
                        multimedia = "images/"+mPath.substring(mPath.lastIndexOf("/") + 1);
                        Bitmap bitmapSlected = BitmapFactory.decodeFile(mPath);
                        imageAttachment.setVisibility(View.VISIBLE);
                        imageAttachment.setImageBitmap(ThumbnailUtils.extractThumbnail(bitmapSlected, 80, 80));
                        imageAttachment.setRotation(90);

                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        //Toast.makeText(LessonFormActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case FILES_REQUEST:
                    Uri selectedUri = data.getData();
                    mPath = filesHandler.getPath(ChatRoomActivity.this, selectedUri);
                    multimedia = "files/"+ mPath.substring(mPath.lastIndexOf("/") + 1);
                    imageAttachment.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_file_download));
                    imageAttachment.setVisibility(View.VISIBLE);
                    break;

                case CAMERA_REQUEST_FOR_VIDEO:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "scanned"+path+":");
                                    Log.i("ExternalStorage", "-> Uri"+uri);
                                }
                            });
                    Uri videoUri = data.getData();
                    mPath = filesHandler.getPath(ChatRoomActivity.this, videoUri);
                    multimedia = "videos/"+mPath.substring(mPath.lastIndexOf("/") + 1);
                    imageAttachment.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_play_video));
                    imageAttachment.setVisibility(View.VISIBLE);

                    break;
            }
        }
    }

    public static Intent getIntent(Context context, String name, String phone, String chatRoomUuid, String isGroup) {
        Intent intent = new Intent(context,ChatRoomActivity.class);
        intent.putExtra(KEY_CHAT_ROOM_NAME,name);
        intent.putExtra(KEY_PHONE,phone);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        intent.putExtra(KEY_IS_GROUP,isGroup);
        return intent;
    }

}
