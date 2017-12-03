package com.memeticame.memeticame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.memeticame.memeticame.chats.ChatRoomsAdapter;
import com.memeticame.memeticame.managers.MediaPlayerManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Exchanger;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("memeticaMe Gallery");
        }
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/memeticaMe/");
        File[] files = directory.listFiles();
        ListView listView = (ListView) findViewById(R.id.paths_list);
        final ArrayList<File> fileList = new ArrayList<File>();
        GalleryAdapter galleryAdapter = new GalleryAdapter(GalleryActivity.this, fileList);
        listView.setAdapter(galleryAdapter);

        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++) {

            File[] folder = files[i].listFiles();
            if (!files[i].getName().equals("zips")) {
                for (int j = 0; j < folder.length; j++) {
                    fileList.add(folder[j]);
                }
            }
        }
        galleryAdapter.notifyDataSetChanged();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    openFile(GalleryActivity.this,fileList.get(i));
                } catch (Exception e) {}
            }
        });
            /*
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
                imageMeme.setVisibility(View.VISIBLE);
                imageMeme.setImageBitmap(bmRotated2);
                //imageMeme.setAnimation(AnimationUtils.loadAnimation(MemeAudioActivity.this
                //        .getApplicationContext(), R.anim.zoomin));
                //imageMeme.setAnimation(AnimationUtils.loadAnimation(MemeAudioActivity.this
                //        .getApplicationContext(), R.anim.zoomout));
                //photoViewAttacher = new PhotoViewAttacher(imageMeme);

            }
            Log.d("Files", "FileName:" + files[i].getName());
        }
            */
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context,GalleryActivity.class);
        return intent;
    }

    public static void openFile(Context context, File file) throws IOException {
        Uri uri = Uri.fromFile(file);
        String url = file.getPath();
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
            Intent intentMeme = MemeAudioActivity.getIntent(context, "", file.getPath());
            context.startActivity(intentMeme);
            return;
        }

        Intent chooser = Intent.createChooser(intent, title);
        if (chooser.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }
}
