package com.memeticame.memeticame.threading;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.memeticame.memeticame.cache.LRUCache;
import com.memeticame.memeticame.chats.ChatRoomActivity;
import com.memeticame.memeticame.models.Message;

import java.io.File;
import java.io.IOException;

/**
 * Created by ESTEBANFML on 17-10-2017.
 */

public class DownloadImageThumbnail extends AsyncTask<String,Float,Integer> {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageRef;
    private Message message;
    private String fileDownloadedPath;
    private ProgressBar progressBar;
    private Context context;
    private ImageView imageView;

    public DownloadImageThumbnail(Context context, ProgressBar progressBar,Message message, ImageView imageView) {
        this.context = context;
        this.progressBar = progressBar;
        this.message = message;
        this.imageView = imageView;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(10);
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
            fileDownloadedPath = "-";
            StorageReference riversRef = mStorageRef.child(multimediaFile);
            String ABSOLUTE_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            String CACHE_DIR = Environment.getDownloadCacheDirectory().toString();
            Log.i("CACHEDIR",ABSOLUTE_STORAGE_PATH+"/"+multimediaFile.substring(multimediaFile.lastIndexOf("/")+1) );
            try {
                final File localFile = new File(context.getCacheDir(), multimediaFile.substring(multimediaFile.lastIndexOf("/")+1));

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
                            public void onSuccess(final FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Successfully downloaded data to local file
                                // ...
                                ((ChatRoomActivity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath());
                                        imageView.setImageBitmap(bitmap);
                                        progressBar.setVisibility(View.GONE);
                                        LRUCache.getInstance().getLru().put(multimediaFile,bitmap);

                                    }
                                });

                            }
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


}
