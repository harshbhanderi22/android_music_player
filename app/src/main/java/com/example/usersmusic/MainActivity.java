package com.example.usersmusic;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ListView listsong;
    Button next,prev,play;
    SeekBar seek;
    String[] songs;
    MediaPlayer mp;
     TextView end,start;
     private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listsong=findViewById(R.id.list);
        next=findViewById(R.id.next);
        prev=findViewById(R.id.prev);
        play=findViewById(R.id.play);
        seek=findViewById(R.id.seek);
        end=findViewById(R.id.end);
        start=  findViewById(R.id.start);
        runtimepermission();
        AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId("ca-app-pub-3792927035387740/9013159472");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }
    private void runtimepermission()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {


                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(MainActivity.this, "Click on Any song Onetime to start player", Toast.LENGTH_LONG).show();
                        display();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    public ArrayList<File> findSong (File file)
    {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();


        if (files != null) {
            for (File singlefile: files)
            {
                if (singlefile.isDirectory() && !singlefile.isHidden())
                {
                    arrayList.addAll(findSong(singlefile));
                }
                else
                {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav") )
                    {
                        arrayList.add(singlefile);
                    }
                }
            }
        }
        return arrayList;
    }
    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }
    final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
    private void display()
    {

        songs = new String[mySongs.size()];
        for (int i = 0; i<mySongs.size();i++) {
            findSong(mySongs.get(i));
            songs[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }
            ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, songs);
        listsong.setAdapter(myAdapter);
        click();
    }
    public void click()
    {
        listsong.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            if (mp!=null)
            {
                mp.reset();
                mp.release();
                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(i)));
                mp.start();
                seek.setMax(mp.getDuration());
                seek.setProgress(0);
                long time=mp.getDuration();
                String ftime=timerConversion(time);
                end.setText(ftime);
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            mp.seekTo(i);
                        }
                        start.setText(String.valueOf(timerConversion(mp.getCurrentPosition())));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                Thread updateseekbar = new Thread()
                {
                    @Override
                    public void run() {
                        int totalDuration = mp.getDuration();
                        int currentposition = 0;

                        while (currentposition<totalDuration)
                        {
                            try {
                                sleep(500);
                                currentposition = mp.getCurrentPosition();
                                seek.setProgress(currentposition);
                            }
                            catch (InterruptedException | IllegalStateException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                seek.setMax(mp.getDuration());
                updateseekbar.start();
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mp.isPlaying()) {
                            play.setText("Play");
                            mp.pause();

                        }
                        else
                        {
                            play.setText("Pause");
                            mp.start();

                        }
                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    int  currentSongIndex= 0;
                    @Override
                    public void onClick(View view) {
                        mp.stop();
                        if(currentSongIndex < (mySongs.size() - 1)){
                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex+1)));
                            mp.start();
                            currentSongIndex = currentSongIndex + 1;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }else if (currentSongIndex==mySongs.size()-1){

                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(0)));
                            mp.start();
                            currentSongIndex = 0;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }

                    }
                });
                prev.setOnClickListener(new View.OnClickListener() {
                    int  currentSongIndex= i;
                    @Override
                    public void onClick(View view) {
                        mp.stop();
                        if(currentSongIndex > 0){
                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex-1)));
                            mp.start();
                            currentSongIndex = currentSongIndex - 1;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }else if (currentSongIndex==0){

                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(mySongs.size()-1)));
                            mp.start();
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }

                    }
                });


            }
            else {

                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(i)));
                mp.start();
                seek.setMax(mp.getDuration());
                long time=mp.getDuration();
                String ftime=timerConversion(time);

                end.setText(ftime);
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            mp.seekTo(i);
                        }
                        start.setText(String.valueOf(timerConversion(mp.getCurrentPosition())));

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                Thread updateseekbar = new Thread()
                {
                    @Override
                    public void run() {
                        int totalDuration = mp.getDuration();
                        int currentposition = 0;

                        while (currentposition<totalDuration)
                        {
                            try {
                                sleep(500);
                                currentposition = mp.getCurrentPosition();
                                seek.setProgress(currentposition);
                            }
                            catch (InterruptedException | IllegalStateException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                seek.setMax(mp.getDuration());
                updateseekbar.start();
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mp.isPlaying()) {
                            play.setText("Play");
                            mp.pause();

                        }
                        else
                        {
                            play.setText("Pause");
                            mp.start();

                        }
                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    int  currentSongIndex= 0;
                    @Override
                    public void onClick(View view) {
                        mp.stop();
                        if(currentSongIndex < (mySongs.size() - 1)){
                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex+1)));
                            mp.start();
                            currentSongIndex = currentSongIndex + 1;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }else if (currentSongIndex==mySongs.size()-1){

                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(0)));
                            mp.start();
                            currentSongIndex = 0;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }

                    }
                });
                prev.setOnClickListener(new View.OnClickListener() {
                    int  currentSongIndex= i;
                    @Override
                    public void onClick(View view) {
                        mp.stop();
                        if(currentSongIndex > 0){
                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex-1)));
                            mp.start();
                            currentSongIndex = currentSongIndex - 1;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }else if (currentSongIndex==0){

                            mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(mySongs.size()-1)));
                            mp.start();
                            currentSongIndex =  mySongs.size()-1;
                            long time=mp.getDuration();
                            String ftime=timerConversion(time);
                            end.setText(ftime);
                        }

                    }
                });
            }
        });
    }
//    public void nextsong()
//    {
//        int dur=mp.getCurrentPosition();
//        int  currentSongIndex= 0;
//        if (dur==mp.getDuration())
//        {
//            if(currentSongIndex < (mySongs.size() - 1)){
//                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex+1)));
//                mp.start();
//                currentSongIndex++;
//                long time=mp.getDuration();
//                String ftime=timerConversion(time);
//                end.setText(ftime);
//            }else if (currentSongIndex==mySongs.size()-1){
//                currentSongIndex = 0;
//                mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(mySongs.get(currentSongIndex)));
//                mp.start();
//
//                long time=mp.getDuration();
//                String ftime=timerConversion(time);
//                end.setText(ftime);
//            }
//        }
//    }

}