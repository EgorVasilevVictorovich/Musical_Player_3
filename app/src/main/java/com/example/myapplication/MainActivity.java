package com.example.myapplication;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements Runnable{

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private SeekBar volumeBar;
    private boolean wasPlaying = false;
    private FloatingActionButton fabPlayPause;
    private FloatingActionButton fabBack;
    private FloatingActionButton fabRepeat;
    private FloatingActionButton fabForward;
    private TextView seekBarHint;
    private TextView metaDataAudio;
    private String metaData;
    private boolean isRepeat = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabBack = findViewById(R.id.fabBack);
        fabRepeat = findViewById(R.id.fabRepeat);
        fabForward = findViewById(R.id.fabForward);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        metaDataAudio = findViewById(R.id.metaDataAudio);
        volumeBar=findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum= progress/100f;
                        mediaPlayer.setVolume(volumeNum, volumeNum );
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                int timeTrack = (int) Math.ceil(progress/1000f);
                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60){
                    seekBarHint.setText("00:" + timeTrack);
                } else if (timeTrack >= 60 && timeTrack < 70) {
                    seekBarHint.setText("01:0" + (timeTrack - 60));
                } else if (timeTrack >= 70) {
                    seekBarHint.setText("01:" + (timeTrack - 60));
                } else if (timeTrack >= 120 && timeTrack < 130){
                    seekBarHint.setText("02:0" + (timeTrack - 120));
                } else if (timeTrack >= 130) {
                    seekBarHint.setText("02:" + (timeTrack - 120));
                }


                double percentTrack = progress / (double) seekBar.getMax();
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    MainActivity.this.seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabRepeat.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);


    }
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabPlayPause:
                    playSong();
                    break;
                case R.id.fabBack:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                    break;
                case R.id.fabForward:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                    break;
                case R.id.fabRepeat:
                    if (!isRepeat && mediaPlayer != null) {
                        mediaPlayer.setLooping(true);
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat));
                        isRepeat = true;
                    } else if (isRepeat && mediaPlayer != null){
                        mediaPlayer.setLooping(false);
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat_off));
                        isRepeat = false;
                    }
                    break;

            }
        }
    };

    public void playSong() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause ();
                mediaPlayer.seekTo (seekBar.getProgress ());
                seekBar.getProgress ();
                wasPlaying = true;
                fabPlayPause.setImageDrawable (ContextCompat.getDrawable (MainActivity.this, android.R.drawable.ic_media_play));
            } else{
                fabPlayPause.setImageDrawable (ContextCompat.getDrawable (MainActivity.this, android.R.drawable.ic_media_pause));
                mediaPlayer.start();
                new Thread(this).start();
                wasPlaying = false;
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                AssetFileDescriptor descriptor = getAssets().openFd("Н.А.Римский-Корсаков - Полёт шмеля.mp3");
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever();
                mediaMetadata.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                metaData = mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                metaData += "\n" + mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                mediaMetadata.release();
                metaDataAudio.setText(metaData);
                descriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                new Thread(this).start();
            }

            wasPlaying = false;

        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }
    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);

        }
    }


}
