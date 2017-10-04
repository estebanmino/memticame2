package com.memeticame.memeticame.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.memeticame.memeticame.R;
import com.memeticame.memeticame.models.Message;
import com.memeticame.memeticame.models.SharedPreferencesClass;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.author;

/**
 * Created by ESTEBANFML on 02-10-2017.
 */

public class ChatRoomAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Message> messagesList;
    private final FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String currentUserPhone;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public ChatRoomAdapter(Context context, ArrayList<Message> messagesList, FirebaseAuth mAuth, String currentUserPhone) {
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

        final Message messageFetched = messagesList.get(position);
        final String messageContent = messageFetched.getContent();
        final long messageTimestamp = messageFetched.getTimestamp();



        if (messageFetched.getMultimedia() == null){
            imageAttachmentPreview.setVisibility(View.GONE);
            btnDownload.setVisibility(View.GONE);
        } else  {
            if (messageFetched.getMultimediaPath() != null) {
                btnDownload.setText("DOWNLOADED");
            }
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadFile downloadFile =  new DownloadFile(btnDownload, messageFetched);
                    downloadFile.execute(messageFetched.getAuthor(), "",
                            messageFetched.getMultimedia());
                }
            });
            switch(messageFetched.getMultimedia().substring(0, messageFetched.getMultimedia().lastIndexOf("/"))) {
                case "images":
                    /*StorageReference riversRef = mStorageRef.child(messageFetched.getMultimedia());
                    try {
                        final File localFile = File.createTempFile("images", "jpg", context.getCacheDir());
                        /*riversRef.getFile(localFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Successfully downloaded data to local file
                                        // ...
                                        Bitmap bitmapSlected = BitmapFactory.decodeFile(localFile.getPath());
                                        imageAttachmentPreview.setVisibility(View.VISIBLE);
                                        imageAttachmentPreview.setImageBitmap(ThumbnailUtils.extractThumbnail(bitmapSlected, 80, 80));
                                        imageAttachmentPreview.setRotation(90);
                                        imageAttachmentPreview.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.fromFile(localFile), "image/*");
                                                context.startActivity(intent);
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
                    }*/
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

        public DownloadFile(Button btnDoownload, Message message) {
            this.btnDownload = btnDoownload;
            this.message = message;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setProgress(0);
            //if (multimedia != null) {
            //    progressBar.setVisibility(View.VISIBLE);
            //}
            //if (multimedia != null ) {
            //    fabSend.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_cloud_up));
            //    Toast.makeText(ChatRoomActivity.this, "Starting download", Toast.LENGTH_SHORT).show();

            //}
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
            int p = Math.round(values[0]);
            Log.i("PROGRESS INT", Integer.toString(p));

            //progressBar.setProgress(p);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            //progressBar.setVisibility(View.GONE);
            //imageAttachment.setVisibility(View.GONE);
            //editMessage.setText("");
            btnDownload.setText("Downloaded");
            message.setMultimediaPath(fileDownloadedPath);


            Log.i("FILE DOWNLOADES", fileDownloadedPath);
            //fabSend.setImageDrawable(ContextCompat.getDrawable(ChatRoomActivity.this, R.drawable.ic_send_dark));

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
                final long TEN_MEGABYTE = 1048576 * 1048576;
                fileDownloadedPath = "-";
                StorageReference riversRef = mStorageRef.child(multimediaFile);
                String ABSOLUTE_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

                try {
                    final File localFile = new File(ABSOLUTE_STORAGE_PATH+"/memeticaMe/"+multimediaFile.substring(multimediaFile.lastIndexOf("/")+1));

                        riversRef.getFile(localFile)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Successfully downloaded data to local file
                                        // ...
                                        //Bitmap bitmapSlected = BitmapFactory.decodeFile(localFile.getPath());
                                        //btnDownload.setText(localFile.getPath());
                                        Log.i("DOWNLOADDD",localFile.getPath());
                                        btnDownload.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                Log.i("CLICKECCCC","iehuiewhk");
                                                Log.i("CLICKECCCC",multimediaFile.substring(0, multimediaFile.lastIndexOf("/")));
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
                                                        Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                                        String title2 = context.getResources().getString(R.string.hello_blank_fragment);
                                                        intent2.setDataAndType(Uri.parse(
                                                                Uri.fromFile(new File(localFile.getPath())).toString()), "audio/*");
                                                        Intent chooser2 = Intent.createChooser(intent2, title2);
                                                        if (chooser2.resolveActivity(context.getPackageManager()) != null) {
                                                            context.startActivity(chooser2);
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

}
