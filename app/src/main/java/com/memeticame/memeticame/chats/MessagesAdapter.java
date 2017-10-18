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
import com.memeticame.memeticame.cache.LRUCache;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.models.SharedPreferencesClass;
import com.memeticame.memeticame.threading.DownloadFile;
import com.memeticame.memeticame.threading.DownloadImageThumbnail;

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

    Boolean mStartPlaying = true;
    MediaPlayer mPlayer = null;


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

        final TextView textFileName = convertView.findViewById(R.id.text_file_name);
        final TextView textFileSize = convertView.findViewById(R.id.text_file_size);
        final TextView message = convertView.findViewById(R.id.text_message_body);
        final TextView timestamp = convertView.findViewById(R.id.text_message_time);
        final TextView senderName = convertView.findViewById(R.id.text_sender_name);
        final ImageView imageAttachmentPreview = convertView.findViewById(R.id.image_attachment_preview);
        final Button btnDownload = convertView.findViewById(R.id.btn_download);
        final ProgressBar progressBar = convertView.findViewById(R.id.progress_bar);

        final Message messageFetched = messagesList.get(position);
        final String messageContent = messageFetched.getContent();
        final long messageTimestamp = messageFetched.getTimestamp();
        final String messageAuthor = messageFetched.getAuthor();



        if (messageFetched.getMultimedia() == null){
            imageAttachmentPreview.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);

        } else if (messageFetched.getMultimediaPath() != null && messageFetched.getMultimediaPath().length() > 3) {
            final String multimediaFile = messageFetched.getMultimediaPath();
            btnDownload.setText("OPEN");
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String title = context.getResources().getString(R.string.hello_blank_fragment);

                    switch (multimediaFile.substring(0, multimediaFile.lastIndexOf("/"))) {
                        case "images":
                            intent.setDataAndType(Uri.parse(
                                    Uri.fromFile(new File(multimediaFile)).toString()), "image/*");
                            Intent chooser = Intent.createChooser(intent, title);
                            if (chooser.resolveActivity(context.getPackageManager()) != null) {
                                context.startActivity(chooser);
                            }
                            break;
                        case "videos":
                            intent.setDataAndType(Uri.parse(
                                    Uri.fromFile(new File(multimediaFile)).toString()), "video/*");
                            Intent chooser1 = Intent.createChooser(intent, title);
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
                            btnDownload.setText("PLAY");

                            break;

                        case "files":
                            String ext = multimediaFile.substring(multimediaFile.lastIndexOf(".") + 1);
                            intent.setDataAndType(Uri.parse(
                                    Uri.fromFile(new File(multimediaFile)).toString()), "application/pdf");
                            Intent chooser3 = Intent.createChooser(intent, title);
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
            switch(messageFetched.getMultimediaType()) {
                case "images":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gallery_dark));

                    String[] thumbnailPath = messageFetched.getMultimedia().split("\\.");

                    String thumbName = thumbnailPath[0];
                    String thumbExtension = thumbnailPath[1];
                    textFileSize.setVisibility(View.VISIBLE);
                    textFileSize.setText(messageFetched.getMultimediaSize());

                    String thumbKey = thumbName +"_thumbnail." + thumbExtension;

                    if (LRUCache.getInstance().getLru().get(thumbKey) == null){
                        DownloadImageThumbnail downloadImageThumbnail = new DownloadImageThumbnail(context, progressBar, messageFetched,imageAttachmentPreview);
                        downloadImageThumbnail.execute(messageFetched.getAuthor(), "",
                                      thumbKey);

                    } else {
                        imageAttachmentPreview.setImageBitmap((Bitmap) LRUCache.getInstance().getLru().get(thumbKey));
                    };


                    break;
                case "files":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file_download));
                    textFileName.setVisibility(View.VISIBLE);
                    textFileSize.setVisibility(View.VISIBLE);
                    textFileName.setText(messageFetched.getMultimediaName());
                    textFileSize.setText(messageFetched.getMultimediaSize());
                    Log.i("MULTIMEDIASIZE",messageFetched.getMultimediaSize());
                    break;

                case "audios":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_audio));
                    textFileSize.setVisibility(View.VISIBLE);
                    textFileSize.setText(messageFetched.getMultimediaSize());
                    break;
                case "videos":
                    imageAttachmentPreview.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_video));
                    textFileSize.setVisibility(View.VISIBLE);
                    textFileSize.setText(messageFetched.getMultimediaSize());
                    break;
            }
                 }
        if (getItemViewType(position) == VIEW_TYPE_MESSAGE_RECEIVED) {
            senderName.setText(messageAuthor);
        }

        message.setText(messageContent);
        Date date = new Date(messageTimestamp);
        Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        timestamp.setText(format.format(date));

        return convertView;
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
        mPlayer.release();
        mPlayer = null;
    }

    public void onPlay(boolean start, MediaPlayer mediaPlayer, String mPath) {
        if (start) {
            mediaPlayer = new MediaPlayer();
            startPlaying(mediaPlayer, mPath);
        } else {
            stopPlaying(mediaPlayer);
        }
    }




}
