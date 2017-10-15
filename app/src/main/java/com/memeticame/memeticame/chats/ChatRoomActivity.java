package com.memeticame.memeticame.chats;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.Database;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.threading.SendMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

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
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CHAT_ROOM_UUID = "chatRoomUuid";
    private static final String USER_CHAT_ROOMS = "chatRooms";

    private final Database firebaseDatabase = new Database();
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

    private Message message;
    private String author;
    private String multimedia;
    private String multimediaUrl;



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
                //Snackbar.make(view, "Acceso a galería", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                if (ContextCompat.checkSelfPermission(ChatRoomActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    getReadStoragePermissions();
                }
                else {
                    Intent intent = new Intent();
                    //intent.setType("image/*");
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
                        mPath = getRealPathFromURI_API19(getApplicationContext(),data.getData());
                        multimedia = "images/"+mPath.substring(mPath.lastIndexOf("/") + 1);
                        Bitmap bitmapSlected = BitmapFactory.decodeFile(mPath);
                        imageAttachment.setVisibility(View.VISIBLE);
                        imageAttachment.setImageBitmap(ThumbnailUtils.extractThumbnail(bitmapSlected, 80, 80));
                        imageAttachment.setRotation(90);
                        //lesson.getMultimediaPicturesFiles().add(new MultimediaFile(EXTENSION_PICTURE,mPath, transferUtility,S3_BUCKET_NAME));
                        //multimediaImagePictureAdapter.notifyDataSetChanged();


                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        //Toast.makeText(LessonFormActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case FILES_REQUEST:
                    Uri selectedUri = data.getData();
                    mPath = getPath(ChatRoomActivity.this, selectedUri);
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
                    mPath = getPath(ChatRoomActivity.this, videoUri);
                    multimedia = "videos/"+mPath.substring(mPath.lastIndexOf("/") + 1);
                    Log.i("SENDVIDEO",mPath);
                    imageAttachment.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_play_video));
                    imageAttachment.setVisibility(View.VISIBLE);

                    break;
            }
        }
    }

    public static Intent getIntent(Context context, String name, String phone, String chatRoomUuid) {
        Intent intent = new Intent(context,ChatRoomActivity.class);
        intent.putExtra(KEY_USERNAME,name);
        intent.putExtra(KEY_PHONE,phone);
        intent.putExtra(KEY_CHAT_ROOM_UUID,chatRoomUuid);
        return intent;
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
