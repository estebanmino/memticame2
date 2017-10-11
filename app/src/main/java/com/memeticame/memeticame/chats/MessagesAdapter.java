package com.memeticame.memeticame.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.author;
import static android.R.attr.targetActivity;

/**
 * Created by ESTEBANFML on 02-10-2017.
 */

public class MessagesAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Message> messagesList;
    private final FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String currentUserPhone;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public MessagesAdapter(Context context, ArrayList<Message> messagesList, FirebaseAuth mAuth, String currentUserPhone) {
        this.context = context;
        this.messagesList = messagesList;
        this.mAuth = mAuth;
        this.currentUserPhone = currentUserPhone;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.getAuthor().equals(currentUserPhone)) {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == VIEW_TYPE_MESSAGE_RECEIVED) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chat_message_received, null);
        }
        else {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.chat_message_sent, null);
        }

        final TextView message = convertView.findViewById(R.id.text_message_body);
        final TextView timestamp = convertView.findViewById(R.id.text_message_time);
        final ImageView imageAttachmentPreview = convertView.findViewById(R.id.image_attachment_preview);
        final Button btnDownload = convertView.findViewById(R.id.btn_download);
        final ProgressBar progressBar = convertView.findViewById(R.id.progress_bar);

        final Message messageFetched = messagesList.get(position);
        final String messageContent = messageFetched.getContent();
        final long messageTimestamp = messageFetched.getTimestamp();



        if (messageFetched.getMultimedia() == null){
            imageAttachmentPreview.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);

        } else if (messageFetched.getMultimediaPath() != null && messageFetched.getMultimediaPath().length() > 3) {
                final String multimediaFile = messageFetched.getMultimediaPath();
                btnDownload.setText("OPEN");
                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (multimediaFile.substring(0, multimediaFile.lastIndexOf("/"))) {
                            case "images":
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String title = context.getResources().getString(R.string.hello_blank_fragment);
                                intent.setDataAndType(Uri.parse(
                                        Uri.fromFile(new File(multimediaFile)).toString()), "image/*");
                                Intent chooser = Intent.createChooser(intent, title);
                                if (chooser.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(chooser);
                                }
                                break;
                            case "videos":
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                String title1 = context.getResources().getString(R.string.hello_blank_fragment);
                                intent1.setDataAndType(Uri.parse(
                                        Uri.fromFile(new File(multimediaFile)).toString()), "video/*");
                                Intent chooser1 = Intent.createChooser(intent1, title1);
                                if (chooser1.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(chooser1);
                                }
                                break;

                            case "audios":

                                MediaPlayer mediaPlayer = new MediaPlayer();
                                mediaPlayer.setOnPreparedListener(
                                        new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mediaPlayer) {
                                                mediaPlayer.start();
                                            }
                                        });
                                try {
                                    mediaPlayer.setDataSource(context,
                                            Uri.parse(multimediaFile));
                                    mediaPlayer.prepareAsync();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "files":
                                String ext = multimediaFile.substring(multimediaFile.lastIndexOf(".") + 1);

                                Intent intent3 = new Intent(Intent.ACTION_VIEW);
                                String title3 = context.getResources().getString(R.string.hello_blank_fragment);
                                intent3.setDataAndType(Uri.parse(
                                        Uri.fromFile(new File(multimediaFile)).toString()), "application/pdf");
                                Intent chooser3 = Intent.createChooser(intent3, title3);
                                if (chooser3.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(chooser3);
                                }
                                break;

                        }
                    }
                });
                } else {
                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DownloadFile downloadFile = new DownloadFile(context, btnDownload, progressBar, messageFetched);
                            downloadFile.execute(messageFetched.getAuthor(), "",
                                    messageFetched.getMultimedia());

                        }});
            switch(messageFetched.getMultimedia().substring(0, messageFetched.getMultimedia().lastIndexOf("/"))) {
                case "images":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gallery_dark));
                    break;
                case "files":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file_download));
                    break;

                case "audios":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_audio));
                    break;
                case "videos":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_video));
                    break;
            }
                 }
        message.setText(messageContent);
        Date date = new Date(messageTimestamp);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        timestamp.setText(format.format(date));

        return convertView;
    }

    public class DownloadFile extends AsyncTask<String,Float,Integer> {

        private FirebaseAuth mAuth;
        private FirebaseDatabase mDatabase;
        private StorageReference mStorageRef;
        private Button btnDownload;
        private Message message;
        private String fileDownloadedPath;
        private ProgressBar progressBar;
        private Context context;

        public DownloadFile(Context context, Button btnDoownload, ProgressBar progressBar,Message message) {
            this.context = context;
            this.btnDownload = btnDoownload;
            this.progressBar = progressBar;
            this.message = message;
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
            //progressBar.setVisibility(View.GONE);
            //imageAttachment.setVisibility(View.GONE);
            //editMessage.setText("");
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
                Log.i("STORAGE PATH", multimediaFile);
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
                                                btnDownload.setText("Open");
                                                btnDownload.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });


                                        btnDownload.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                switch(multimediaFile.substring(0, multimediaFile.lastIndexOf("/"))){
                                                    case "images":
                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        String title = context.getResources().getString(R.string.hello_blank_fragment);
                                                        intent.setDataAndType(Uri.parse(
                                                                Uri.fromFile(new File(localFile.getPath())).toString()), "image/*");
                                                        Intent chooser = Intent.createChooser(intent, title);
                                                        if (chooser.resolveActivity(context.getPackageManager()) != null) {
                                                            context.startActivity(chooser);
                                                        }
                                                        break;
                                                    case "videos":
                                                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                                        String title1 = context.getResources().getString(R.string.hello_blank_fragment);
                                                        intent1.setDataAndType(Uri.parse(
                                                                Uri.fromFile(new File(localFile.getPath())).toString()), "video/*");
                                                        Intent chooser1 = Intent.createChooser(intent1, title1);
                                                        if (chooser1.resolveActivity(context.getPackageManager()) != null) {
                                                            context.startActivity(chooser1);
                                                        }
                                                        break;

                                                    case "audios":

                                                        MediaPlayer mediaPlayer = new MediaPlayer();
                                                        mediaPlayer.setOnPreparedListener(
                                                                new MediaPlayer.OnPreparedListener() {
                                                                    @Override
                                                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                                                        mediaPlayer.start();
                                                                    }
                                                                });
                                                        try {
                                                            mediaPlayer.setDataSource(context,
                                                                    Uri.parse(localFile.getPath()));
                                                            mediaPlayer.prepareAsync();
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                        break;

                                                    case "files":
                                                        String ext = localFile.getPath().substring(localFile.getPath().lastIndexOf(".")+1);

                                                        Intent intent3 = new Intent(Intent.ACTION_VIEW);
                                                        String title3 = context.getResources().getString(R.string.hello_blank_fragment);
                                                        intent3.setDataAndType(Uri.parse(
                                                                Uri.fromFile(new File(localFile.getPath())).toString()), "application/pdf");
                                                        Intent chooser3 = Intent.createChooser(intent3, title3);
                                                        if (chooser3.resolveActivity(context.getPackageManager()) != null) {
                                                            context.startActivity(chooser3);
                                                        }
                                                        break;

                                                }


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
    public void startPlaying(MediaPlayer mPlayer, String filePath){
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception ex) {
        }
    }
    private void stopPlaying(MediaPlayer mediaPlayer) {
        Log.d("PLAYING", "stopPlaying: ");
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlay(boolean start, MediaPlayer mediaPlayer, String mPath) {
        if (start) {
            Log.i("PLAYING", "TRUE");
            mediaPlayer = new MediaPlayer();
            startPlaying(mediaPlayer, mPath);
        } else {
            Log.i("PLAYING", "FALSE");
            stopPlaying(mediaPlayer);
            //mediaPlayer = new MediaPlayer();
        }
    }


}
