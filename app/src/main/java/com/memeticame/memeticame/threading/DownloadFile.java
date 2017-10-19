package com.memeticame.memeticame.threading;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.chats.ChatRoomActivity;
import com.memeticame.memeticame.managers.MediaPlayerManager;
import com.memeticame.memeticame.models.Message;

import java.io.File;
import java.io.IOException;

/**
 * Created by ESTEBANFML on 17-10-2017.
 */

public class DownloadFile extends AsyncTask<String,Float,Integer> {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageRef;
    private Button btnDownload;
    private Message message;
    private String fileDownloadedPath;
    private ProgressBar progressBar;
    private Context context;
    private LinearLayout layoutAudioHandler;
    private ImageView imagePlay;
    private ImageView imagePause;
    private ImageView imageStop;

    public DownloadFile(Context context, Button btnDownload, ProgressBar progressBar,
                        Message message, LinearLayout layoutAudioHandler, ImageView imagePlay,
                        ImageView imagePause, ImageView imageStop) {
        this.context = context;
        this.btnDownload = btnDownload;
        this.progressBar = progressBar;
        this.message = message;
        this.layoutAudioHandler = layoutAudioHandler;
        this.imagePause = imagePause;
        this.imagePlay = imagePlay;
        this.imageStop = imageStop;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(10);
        btnDownload.setVisibility(View.GONE);

    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        message.setMultimediaPath(fileDownloadedPath);

    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        int p = Math.round(values[0]);
        Log.i("PROGRESS INT", Integer.toString(p));

        progressBar.setProgress(p);
    }

    @Override
    protected Integer doInBackground(String... strings) {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        final String currentUserPhone = strings[0];
        final String multimediaFile = strings[2];
        final String receiverPhone = strings[1];

        publishProgress(10f);

        if (multimediaFile != null) {
            StorageReference islandRef = mStorageRef.child(multimediaFile);
            fileDownloadedPath = "-";
            StorageReference riversRef = mStorageRef.child(multimediaFile);
            String ABSOLUTE_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

            try {
                final File localFile = new File(ABSOLUTE_STORAGE_PATH+"/memeticaMe/"+multimediaFile.substring(multimediaFile.lastIndexOf("/")+1));

                riversRef.getFile(localFile)
                        .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests")  float progress =(float) (taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                Log.i("Download is ", progress + "% done");
                                publishProgress(progress*90);
                            }
                        })

                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Successfully downloaded data to local file
                                // ...
                                ((ChatRoomActivity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!multimediaFile.contains("audios")) {
                                            btnDownload.setText("Open");
                                            btnDownload.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            btnDownload.setVisibility(View.GONE);
                                            progressBar.setVisibility(View.GONE);
                                            layoutAudioHandler.setVisibility(View.VISIBLE);

                                            final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(context,
                                                    Uri.fromFile(new File(localFile.getPath())), imagePlay, imagePause, imageStop);

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
                                        }
                                    }
                                });

                                if (multimediaFile.substring(0, multimediaFile.lastIndexOf("/")) != "audios") {

                                    btnDownload.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            try {
                                                openFile(context, Uri.fromFile(new File(localFile.getPath())), localFile.getPath());
                                            } catch (Exception e) {
                                            }
                                        }
                                    });

                                }    }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download
                        // ...
                    }
                });
            } catch (Exception e) {
            }


            publishProgress(30f);
        }
        Log.i("PROGRESS","50");
        publishProgress(50f);

        return null;
    }

    public static void openFile(Context context, Uri uri, String url) throws IOException {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String title = context.getResources().getString(R.string.hello_blank_fragment);

        // so Android knew what application to use to open the file
        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.contains(".wav") || url.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") || url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        Intent chooser = Intent.createChooser(intent, title);
        if (chooser.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }
}

