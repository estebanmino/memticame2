package com.memeticame.memeticame.threading;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.memeticame.memeticame.cache.LRUCache;
import com.memeticame.memeticame.chats.ChatRoomActivity;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ESTEBANFML on 14-10-2017.
 */

public class SendMessage extends AsyncTask<String,Float,Integer> {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageRef;

    private ProgressBar progressBar;
    private ImageView imageAttachment;
    private EditText editMessage;
    private FloatingActionButton fabSend;
    private String multimedia;

    private Context context;
    private Activity activity;
    private static final String USER_CHAT_ROOMS = "chatRooms";

    public SendMessage(ProgressBar progressBar, ImageView imageAttachment, EditText editMessage,
                       FloatingActionButton fabSend, String multimedia, Context context, Activity activity) {

        this.progressBar = progressBar;
        this.imageAttachment = imageAttachment;
        this.editMessage = editMessage;
        this.fabSend = fabSend;
        this.multimedia = multimedia;
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setProgress(0);
        if (multimedia != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        imageAttachment.setVisibility(View.GONE);
        editMessage.setText("");
        if (multimedia != null ) {
            fabSend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cloud_up));
            Toast.makeText(context, "Starting upload", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        int p = Math.round(values[0]);
        Log.i("PROGRESS INT", Integer.toString(p));

        progressBar.setProgress(p);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(String... strings) {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        final String currentUserPhone = strings[0];
        final String multimediaFile = strings[2];
        final String filePath = strings[3];
        final String receiverPhone = strings[1];
        final String content = strings[4];

        DatabaseReference currentUserContactsReference = mDatabase.getReference("users/"+
                currentUserPhone+"/"+USER_CHAT_ROOMS);
        publishProgress(10f);

        if (multimediaFile != null) {
            Log.i("MPATH",filePath);
            Uri file = Uri.fromFile(new File(filePath));
            StorageReference riversRef = mStorageRef.child(multimediaFile);

            if (multimediaFile.contains("images")) {
                String[] thumbnailPath = multimediaFile.split("\\.");
                String thumbName = thumbnailPath[0];
                String thumbExtension = thumbnailPath[1];

                String thumbNewPath = thumbName +"_thumbnail." + thumbExtension;
                StorageReference thumbRef = mStorageRef.child(thumbNewPath);

                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 80);
                String bitmapPath = MediaStore.Images.Media.insertImage(context.getContentResolver(), thumbBitmap,null, null);
                thumbRef.putFile(Uri.parse(bitmapPath));
                LRUCache.getInstance().getLru().put(thumbNewPath, thumbBitmap);
            }

            publishProgress(30f);
            riversRef.putFile(file)

                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")  float progress =(float) (taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        publishProgress(70f * progress);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                        final String uuidMessage = UUID.randomUUID().toString();
                        DatabaseReference userContactsReference = mDatabase.getReference("users/"+
                                currentUserPhone+"/"+USER_CHAT_ROOMS);
                        userContactsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(receiverPhone)) {
                                    final String referencePath = "chatRooms/"+dataSnapshot.
                                            child(receiverPhone).getValue().toString()+"/messages/"+uuidMessage;

                                    final DatabaseReference multimediaReference =
                                            mDatabase.getReference(referencePath+"/multimedia");
                                    final DatabaseReference multimediaSizeReference =
                                            mDatabase.getReference(referencePath+"/multimediaSize");

                                    sendTextMessage(uuidMessage,currentUserPhone,receiverPhone,content);

                                    multimediaReference.setValue(multimediaFile);
                                    @SuppressWarnings("VisibleForTests") String size =
                                            humanReadableByteCount(taskSnapshot.getTotalByteCount(),true);
                                    multimediaSizeReference.setValue(size);
                                    publishProgress(100f);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Upload finished", Toast.LENGTH_SHORT).show();

                                if (multimedia != null ) {
                                    fabSend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cloud_ready));
                                    Toast.makeText(context, "Upload finished", Toast.LENGTH_SHORT).show();
                                }
                                fabSend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_send_dark));
                                multimedia = null;
                            }
                        });
                        publishProgress(100f);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(context, "Upload file failed, retry again", Toast.LENGTH_LONG).show();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (multimedia != null ) {
                                    fabSend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cloud_ready));

                                    Toast.makeText(context, "Upload finished", Toast.LENGTH_SHORT).show();

                                }
                                fabSend.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_send_dark));
                                multimedia = null;
                            }
                        });

                    }
                });
        } else {
            final String uidMessage = UUID.randomUUID().toString();

            sendTextMessage(uidMessage, currentUserPhone, receiverPhone, content);
        }
        publishProgress(100f);

        return null;
    }

    public void sendTextMessage(final String uidMessage,final String currentUserPhone, final String receiverPhone,
                                final String content){
        DatabaseReference currentUserContactsReference = mDatabase.getReference("users/"+
                currentUserPhone+"/"+USER_CHAT_ROOMS);

        currentUserContactsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverPhone)) {
                    final String referencePath = "chatRooms/"+dataSnapshot.
                            child(receiverPhone).getValue().toString()+"/messages/"+uidMessage;

                    final DatabaseReference contentReference =
                            mDatabase.getReference(referencePath+"/content");
                    final DatabaseReference authorReference =
                            mDatabase.getReference(referencePath+"/author");
                    final DatabaseReference timestampReference =
                            mDatabase.getReference(referencePath+"/timestamp");

                    publishProgress(60f);

                    contentReference.setValue(content);
                    authorReference.setValue(currentUserPhone);
                    publishProgress(80f);


                    Date date = new Date();
                    long timestamp =  date.getTime();
                    timestampReference.setValue(timestamp);
                    editMessage.setText("");
                    publishProgress(100f);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.0f %sB", bytes / Math.pow(unit, exp), pre);
    }
}