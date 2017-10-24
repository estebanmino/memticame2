package com.memeticame.memeticame;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.memeticame.memeticame.chats.ChatRoomActivity;
import com.memeticame.memeticame.managers.MediaPlayerManager;
import com.memeticame.memeticame.managers.ZipManager;
import com.memeticame.memeticame.models.ChatRoom;
import com.memeticame.memeticame.models.Contact;
import com.memeticame.memeticame.models.FilesHandler;

import java.io.File;
import java.io.IOException;

public class MemeAudioActivity extends AppCompatActivity {

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
    private static final String KEY_MEME_AUDIO = "memeaudio";
    private static final String KEY_IS_GROUP = "isGroup";

    private static final String APP_DIRECTORY = "memeticaMe";
    private static final String IMAGE_FORMAT = ".jpg";
    private static final String VIDEO_FORMAT = ".mp4";

    private final FilesHandler filesHandler = new FilesHandler();

    private View mLayout;
    private String imagePath;
    private String audioPath;


    private FloatingActionButton fabAdd;
    private FloatingActionButton fabGallery;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabRecordAudio;
    private ImageView imageMeme;

    private ImageView imagePlay;
    private ImageView imagePause;
    private ImageView imageStop;

    private ConstraintLayout constraintMemeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_audio);
        ABSOLUTE_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        //back toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MA for "+ getIntent().getStringExtra(KEY_CHAT_ROOM_NAME));
        }

        constraintMemeLayout = (ConstraintLayout) findViewById(R.id.constraint_meme_attachments);
        mLayout = findViewById(R.id.meme_audio_layout);
        imagePlay = (ImageView) findViewById(R.id.image_meme_play);
        imagePause = (ImageView) findViewById(R.id.image_meme_pause);
        imageStop = (ImageView) findViewById(R.id.image_meme_stop);
        imageMeme = (ImageView) findViewById(R.id.image_meme);


        if (getIntent().getStringExtra(KEY_MEME_AUDIO) == null) {


            fabAdd = (FloatingActionButton) findViewById(R.id.fab_meme_add);
            fabGallery = (FloatingActionButton) findViewById(R.id.fab_meme_images);
            fabCamera = (FloatingActionButton) findViewById(R.id.fab_meme_camera);
            fabRecordAudio = (FloatingActionButton) findViewById(R.id.fab_meme_audio);

            setFabAddOnClickListener();
            setOnClickFabImages();
            setFabCameraOnClickListener();
            setFabRecordAudioOnClickListener();
        } else {
            constraintMemeLayout.setVisibility(View.GONE);
            Log.i("MEMEAUDIO",getIntent().getStringExtra(KEY_MEME_AUDIO));
            File directory = new File(getIntent().getStringExtra(KEY_MEME_AUDIO));
            File[] files = directory.listFiles();
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].getName().contains("3gp")){
                    audioPath = files[i].getPath();
                    final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(MemeAudioActivity.this,
                            Uri.fromFile(new File(audioPath)), imagePlay, imagePause, imageStop);

                    imagePlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("IMAGEPLAY","CLICKED");
                            mediaPlayerManager.onPlay();
                        }
                    });

                    imagePause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayerManager.onPause();
                        }
                    });

                    imageStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayerManager.onStop();
                        }
                    });
                } else {
                    imagePath = files[i].getPath();
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    ExifInterface exif2 = null;
                    try {
                        exif2 = new ExifInterface(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation2 = exif2.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap bmRotated2 = rotateBitmap(bitmap, orientation2);
                    imageMeme.setImageBitmap(bmRotated2);
                    imageMeme.setVisibility(View.VISIBLE);
                    imageMeme.setImageBitmap(bmRotated2);
                    imageMeme.setAnimation(AnimationUtils.loadAnimation(MemeAudioActivity.this
                            .getApplicationContext(), R.anim.zoomin));
                    imageMeme.setAnimation(AnimationUtils.loadAnimation(MemeAudioActivity.this
                            .getApplicationContext(), R.anim.zoomout));
                }
                Log.d("Files", "FileName:" + files[i].getName());
            }
        }


    }

    public void setFabRecordAudioOnClickListener(){
        fabRecordAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (!isRecording) {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        audioPath = ABSOLUTE_STORAGE_PATH+"/"+ts.toString()+".3gp";
                        startRecording(audioPath);
                    }
                } else {
                    getRecorAudioPermissions();
                }
                return false;
            }
        });

        fabRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    stopRecording();
                    final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(MemeAudioActivity.this,
                            Uri.fromFile(new File(audioPath)), imagePlay, imagePause, imageStop);

                    imagePlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("IMAGEPLAY","CLICKED");
                            mediaPlayerManager.onPlay();
                        }
                    });

                    imagePause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayerManager.onPause();
                        }
                    });

                    imageStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayerManager.onStop();
                        }
                    });
                } else {}
            }
        });
    };

    //RECORD AUDIO

    public void getRecorAudioPermissions() {
        if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MemeAudioActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                Snackbar.make(mLayout, "Para que podamos grabar audio, necesitamos permiso.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MemeAudioActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                RECORD_AUDIO_REQUEST);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(MemeAudioActivity.this,
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

    public void setFabAddOnClickListener() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPath != null && imagePath != null) {
                    String[] files = {audioPath, imagePath};
                    Long tsLong = System.currentTimeMillis() / 1000;
                    String ts = tsLong.toString();
                    ZipManager zipManager = new ZipManager(files, ABSOLUTE_STORAGE_PATH + "/" + ts + ".zip");
                    zipManager.zip();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("path", ABSOLUTE_STORAGE_PATH + "/" + ts + ".zip");
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(MemeAudioActivity.this, "Can not send without audio nor picture", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void setOnClickFabImages(){
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MemeAudioActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
        if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MemeAudioActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(mLayout, "To take see a picture you must give access to the gallery",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MemeAudioActivity.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                READ_EXTERNAL_REQUEST);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(MemeAudioActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_REQUEST);
            }
        }
    }

    public void setFabCameraOnClickListener() {
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MemeAudioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    getWriteStoragePermissions();
                }
                else if (ContextCompat.checkSelfPermission(MemeAudioActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED){
                    getCameraForPicturePermissions();
                }
                else {
                    dispatchTakePictureIntent();
                }
            }
        });
    }

    public void getWriteStoragePermissions() {
        if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MemeAudioActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(mLayout, "To take save your picture you must give access to storage",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MemeAudioActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_REEQUEST);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(MemeAudioActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_REEQUEST);
            }
        }
    }


    public static Intent getIntent(Context context, String chatRoomName, String memePath) {
        Intent intent = new Intent(context,MemeAudioActivity.class);
        intent.putExtra(KEY_CHAT_ROOM_NAME,chatRoomName);
        intent.putExtra(KEY_MEME_AUDIO,memePath);
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_FOR_PICTURE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
                        dispatchTakePictureIntent();
                    }
                }   else {
                    Toast.makeText(this, "You must give permission to take pictures", Toast.LENGTH_LONG).show();
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

    public void getCameraForPicturePermissions() {
        if (ContextCompat.checkSelfPermission(MemeAudioActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MemeAudioActivity.this,
                    Manifest.permission.CAMERA)) {
                Snackbar.make(mLayout, "To take a picture you must give access to camera",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MemeAudioActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST_FOR_PICTURE);
                    }
                }).show();

            } else {
                ActivityCompat.requestPermissions(MemeAudioActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_FOR_PICTURE);
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

        imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                File.separator +
                APP_DIRECTORY + File.separator + imageName;
        File newFile = new File(file, imageName);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_PICTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case CAMERA_REQUEST_PICTURE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{imagePath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "scanned"+path+":");
                                    Log.i("ExternalStorage", "-> Uri"+uri);
                                }
                            });
                    //multimedia = "images/"+imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    imageMeme.setVisibility(View.VISIBLE);
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap bmRotated = rotateBitmap(bitmap, orientation);
                    imageMeme.setImageBitmap(bmRotated);

                    //imageAttachment.setRotation(90);

                    break;

                case SELECT_IMAGE:

                    if (data != null)
                    {
                        imagePath = filesHandler.getRealPathFromURI_API19(getApplicationContext(),data.getData());
                        //multimedia = "images/"+imagePath.substring(imagePath.lastIndexOf("/") + 1);
                        Bitmap bitmapSlected = BitmapFactory.decodeFile(imagePath);
                        imageMeme.setVisibility(View.VISIBLE);
                        ExifInterface exif2 = null;
                        try {
                            exif2 = new ExifInterface(imagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation2 = exif2.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        Bitmap bmRotated2 = rotateBitmap(bitmapSlected, orientation2);
                        imageMeme.setImageBitmap(bmRotated2);
                        //imageAttachment.setRotation(90);

                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        //Toast.makeText(LessonFormActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            // bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
