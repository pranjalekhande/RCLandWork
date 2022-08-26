package com.wit.rclandwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IVLCVout.Callback {

    RelativeLayout layout_joystick;
    public final static String TAG = "MainActivity";

    public String mFilePath,url;
    SurfaceView mSurface;
    SurfaceHolder holder;
    LibVLC libvlc;
    MediaPlayer mMediaPlayer = null;
    int mVideoWidth = 320 ;
    int mVideoHeight =240 ;
    EditText editText;
    TextView textView1,textView2,textView4,textView5,textViewProgress;
    JoyStickClass js;
    Button buttonStart, buttonStop, buttonMap, buttonConnect;
    RCClient rcClient;
    int pulseWidth = 1400;


    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                rcClient = new RCClient();
                int oldPW = 0;
                while (true) {
                    if (pulseWidth == 1100) {
                        break;
                    }
                    if (oldPW != pulseWidth) {
                        rcClient.turn(pulseWidth);
                        oldPW = pulseWidth;
                    }
                }
                rcClient.closeUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // connecting to the pie
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pulseWidth = 1400;
                thread.start();
                buttonStart.setEnabled(false);
                Toast.makeText(getApplicationContext(), "started", Toast.LENGTH_SHORT).show();
            }
        });

        // disconnecting to the pie

        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pulseWidth = 1100;
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
            }
        });
        //Navigate to Map Activity
        buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));

            }
        });
        editText = (EditText) findViewById(R.id.editText);
        //Navigate to Camera Activity
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = editText.getText().toString();
                Toast.makeText(getApplicationContext(), url,Toast.LENGTH_SHORT).show();

       //         startActivity(new Intent(getApplicationContext(), Camera.class));
            }
        });
        mFilePath = "http://192.168.43.76:8081";
        Log.d(TAG, "Playing: " + mFilePath);
        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();



        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);


        textViewProgress = (TextView) findViewById(R.id.textView3);


        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);


        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " +String.valueOf(js.getY()));
                    pulseWidth = (js.getX()*100) + js.getY();
                    textViewProgress.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        textView5.setText("Direction : Up");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if(direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textViewProgress.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                }


                return true;
            }
        });

        /*
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pulseWidth = 1130+i;
                textViewProgress.setText(""+pulseWidth);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        }); */

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }
    @Override
    protected void onResume() {
        super.onResume();
        createPlayer(mFilePath);
    }
    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;
        if (holder == null || mSurface == null)
            return;
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }
        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;
        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }
    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(this, options);
            holder.setKeepScreenOn(true);
            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();
            Media m = new Media(libvlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }
    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }
    @Override
    protected void onDestroy () {
        super.onDestroy();
        pulseWidth = 1100;

    }
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);
    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }
    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }
    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<MainActivity> mOwner;
        public MyPlayerListener(MainActivity owner) {
            mOwner = new WeakReference<MainActivity>(owner);
        }
        @Override
        public void onEvent(MediaPlayer.Event event) {
            MainActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

}
