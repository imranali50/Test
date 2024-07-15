package com.findmykids.tracker.panda.waves;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.findmykids.tracker.panda.R;
import com.findmykids.tracker.panda.util.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class VoicePlayerView extends LinearLayout {

    private int seekBarProgressColor, seekBarThumbColor, progressTimeColor, visualizationPlayedColor, visualizationNotPlayedColor;
    private boolean showTiming, enableVirtualizer;
    private Context context;
    private String path;
    private LinearLayout main_layout, container_layout;
    private ImageView imgPlay, imgPause;
    private TextView txtProcess;
    private CustomMediaPlayer mediaPlayer;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer.getCurrentPosition() > -1) {
                    try {
//                        Log.e("TAG", "run: >>>>>>>>>  ");
                        update(mediaPlayer, txtProcess, context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private PlayerVisualizerSeekbar seekbarV;
    private Uri contentUri = null;

    public VoicePlayerView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.main_view, this);
        this.context = context;
    }

    public VoicePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
        this.context = context;
    }

    public VoicePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
        this.context = context;
    }

    private void initViews(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VoicePlayerView, 0, 0);

        try {
            showTiming = typedArray.getBoolean(R.styleable.VoicePlayerView_showTiming, true);
            seekBarProgressColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarProgressColor, getResources().getColor(R.color.gradiant1));
            seekBarThumbColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarThumbColor, getResources().getColor(R.color.white));
            progressTimeColor = typedArray.getColor(R.styleable.VoicePlayerView_progressTimeColor, Color.WHITE);
            enableVirtualizer = typedArray.getBoolean(R.styleable.VoicePlayerView_enableVisualizer, false);
            visualizationNotPlayedColor = typedArray.getColor(R.styleable.VoicePlayerView_visualizationNotPlayedColor, getResources().getColor(R.color.stroke_color));
            visualizationPlayedColor = typedArray.getColor(R.styleable.VoicePlayerView_visualizationPlayedColor, getResources().getColor(R.color.white));
        } finally {
            typedArray.recycle();
        }


        LayoutInflater.from(context).inflate(R.layout.main_view, this);
        main_layout = this.findViewById(R.id.collectorLinearLayout);
//        padded_layout = this.findViewById(R.id.paddedLinearLayout);
        container_layout = this.findViewById(R.id.containerLinearLayout);
        imgPlay = this.findViewById(R.id.imgPlay);
        imgPause = this.findViewById(R.id.imgPause);
        txtProcess = this.findViewById(R.id.txtTime);
        seekbarV = this.findViewById(R.id.seekBarV);


        txtProcess.setPadding(16, 0, 16, 0);
        txtProcess.setTextColor(progressTimeColor);

        if (!showTiming) txtProcess.setVisibility(INVISIBLE);

        if (enableVirtualizer) {
            seekbarV.setVisibility(VISIBLE);
            seekbarV.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
            seekbarV.getThumb().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
            seekbarV.setColors(visualizationPlayedColor, visualizationNotPlayedColor);
        }

    }


    //Set the audio source and prepare mediaplayer

    public MediaPlayer setAudio(String audioPath, int position) {
        CustomMediaPlayer mediaPlayer1 = MyApplication.Companion.getMedia();
        String mediaPath = MyApplication.Companion.getMediaPaths();
//        Log.e("TAG", "setAudio:  Set Audio 1692351604477 >>>>>>>>>>> " + (Objects.equals(audioPath, mediaPath)));
        if (mediaPlayer1 != null && mediaPlayer != null) {
            if ((Objects.equals(audioPath, mediaPath))) {
//                Log.e("TAG", "samemedia 1692351604477 : >>>>>>>>>>>>>>>> ");
                mediaPlayer1.seekTo(0);
                mediaPlayer1.pause();
                handler.removeCallbacks(runnable);
                handler = new Handler();
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);
                txtProcess.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                MyApplication.Companion.setMediaPlayer(null);
                MyApplication.Companion.setMediaPaths(null);
//                MainThreadApplicationClass.Companion.setVoicePlayer(null);
            }
        }

//        if (mediaPlayer1 != null) {
//            if (mediaPlayer1 == mediaPlayer) {
//                Log.e("TAG", "samemedia 1692351604477 : >>>>>>>>>>>>>>>> ");
//                mediaPlayer1.seekTo(0);
//                mediaPlayer1.pause();
//                handler.removeCallbacks(runnable);
//                handler = new Handler();
//                imgPause.setVisibility(View.GONE);
//                imgPlay.setVisibility(View.VISIBLE);
//                MainThreadApplicationClass.Companion.setMediaPlayer(null);
//                MainThreadApplicationClass.Companion.setMediaPaths(null);
//            }
//        }
        path = audioPath;
        mediaPlayer = new CustomMediaPlayer();
        mediaPlayer.position = position;
        if (path != null) {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setVolume(10, 10);
                //START and PAUSE are in other listeners
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (seekbarV.getVisibility() == VISIBLE) {
                            seekbarV.setMax(mp.getDuration());
                        }
                        txtProcess.setText(convertSecondsToHMmSs(mp.getDuration() / 1000));
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
//                        Log.e("TAG", "onCompletion: >>>>>>>>>>>>> ");
                        imgPause.setVisibility(View.GONE);
                        imgPlay.setVisibility(View.VISIBLE);
//                        seekbarV.setProgress(0);
//                        seekbarV.updateVisualizer(new File(path));

//                        txtProcess.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));

                        mediaPlayer.seekTo(0);
                        handler.removeCallbacks(runnable);
//                    if (seekbarV.getVisibility() == VISIBLE) {
//                        seekbarV.setProgress(0);
                        txtProcess.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                        seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
//                    }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imgPlay.setOnClickListener(imgPlayClickListener);
        imgPause.setOnClickListener(imgPauseClickListener);
        if (seekbarV.getVisibility() == VISIBLE) {
            seekbarV.updateVisualizer(new File(path));
        }
        seekbarV.setOnSeekBarChangeListener(seekBarListener);
        seekbarV.setEnabled(false);
        seekbarV.updateVisualizer(new File(path));
        return mediaPlayer;
    }


    //Components' listeners

    OnClickListener imgPlayClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgPause.setVisibility(View.VISIBLE);
                    imgPlay.setVisibility(View.GONE);
                }
            });
            CustomMediaPlayer mediaPlayer1 = MyApplication.Companion.getMedia();
//            Log.e("TAG", "players : >>>>>>>>>>>  " + mediaPlayer1 + "\n" + mediaPlayer);
            if (mediaPlayer1 != null) {
                if (mediaPlayer1 != mediaPlayer) {
//                    if (mediaPlayer1.isPlaying()) {
//                    Log.e("TAG", "onClick: >>>>>>>>>>>. not same pause  ");
                    mediaPlayer1.pauseImage();
//                    }

                }
            }

            try {
                if (mediaPlayer != null) {
//                    Log.e("TAG", "onClick: >>>>>>>> start ");
                    MyApplication.Companion.setMedia(mediaPlayer);
//                    MainThreadApplicationClass.Companion.setVoicePlayer(VoicePlayerView.this);
                    MyApplication.Companion.setMediaPaths(path);
                    handler = new Handler();
                    mediaPlayer.start();
                    update(mediaPlayer, txtProcess, context);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
            if (fromUser) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.seekTo(progress);
                        update(mediaPlayer, txtProcess, context);
                        if (seekbarV.getVisibility() == VISIBLE) {
                            seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                        }
                    }
                });
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            ((Activity) context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    imgPause.setVisibility(View.GONE);
//                    imgPlay.setVisibility(View.VISIBLE);
//                }
//            });
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            ((Activity) context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    imgPlay.setVisibility(View.GONE);
//                    imgPause.setVisibility(View.VISIBLE);
//                    try {
//                        mediaPlayer.start();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });

        }
    };

    OnClickListener imgPauseClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.e("TAG", "pauseclick: >>>>>>>>>>  ");
                    imgPause.setVisibility(View.GONE);
                    imgPlay.setVisibility(View.VISIBLE);
                    try {
                        mediaPlayer.pause();
                        handler.removeCallbacks(runnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    public class CustomMediaPlayer extends MediaPlayer {

        Integer position;

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public void pauseImage() {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    handler.removeCallbacks(runnable);
                    handler = new Handler();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.e("TAG", "previous media pause or seek to 0 : >>>>>>>>>>>>.  ");
            imgPause.setVisibility(View.GONE);
            imgPlay.setVisibility(View.VISIBLE);
            txtProcess.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
            seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());

        }

//        public void pauseIt(CustomMediaPlayer mediaPlayer1) {
//            try {
//                if (mediaPlayer1 != null) {
//                    mediaPlayer1.seekTo(0);
//                    mediaPlayer1.pause();
//                    handler.removeCallbacks(runnable);
//                    handler = new Handler();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Log.e("TAG", "previous media pause or seek to 0 : >>>>>>>>>>>>.  ");
//            imgPause.setVisibility(View.GONE);
//            imgPlay.setVisibility(View.VISIBLE);
//            txtProcess.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
//            seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
//
//        }
    }

    //Updating seekBar in realtime
    private void update(final MediaPlayer mediaPlayer, final TextView time, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (seekbarV.getVisibility() == VISIBLE) {
                    seekbarV.setProgress(mediaPlayer.getCurrentPosition());
                    seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                }
//                Log.e("TAG", "run: >>>>>>>>>>> first " + mediaPlayer.getCurrentPosition() + ">>>>>>>>>>> " + mediaPlayer.getDuration());
                if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() > 100) {
                    time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition() / 1000));
                } else {
//                    time.setText(convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
//
////                    if (seekbarV.getVisibility() == VISIBLE) {
//                        seekbarV.setProgress(0);
//                        seekbarV.updatePlayerPercent(0);
////                    }
                }

                try {
                    handler.postDelayed(runnable, 200);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //Convert long milli seconds to a formatted String to display it

    private static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    //These both functions to avoid mediaplayer errors

//    public void onStop() {
//        try {
//            mediaPlayer.stop();
//            mediaPlayer.reset();
//            mediaPlayer.release();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onPause() {
//        try {
//            if (mediaPlayer != null) {
//                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imgPause.setVisibility(View.GONE);
//                imgPlay.setVisibility(View.VISIBLE);
//            }
//        });
//    }


    // Programmatically functions

    public void setTimingVisibility(boolean visibility) {
        if (!visibility) txtProcess.setVisibility(INVISIBLE);
        else txtProcess.setVisibility(VISIBLE);
    }

    public void showPlayProgressbar() {
        imgPlay.setVisibility(GONE);
    }

    public void hidePlayProgresbar() {
        imgPlay.setVisibility(VISIBLE);
    }

//    public void refreshPlayer(String audioPath) {
//        path = audioPath;
//        if (mediaPlayer != null) {
//            try {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                }
//                mediaPlayer.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        mediaPlayer = null;
//        mediaPlayer = new CustomMediaPlayer();
//        if (path != null) {
//            try {
//                mediaPlayer.setDataSource(path);
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mediaPlayer.prepare();
//                mediaPlayer.setVolume(10, 10);
//                //START and PAUSE are in other listeners
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(final MediaPlayer mp) {
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (seekbarV.getVisibility() == VISIBLE) {
//                                    seekbarV.setMax(mp.getDuration());
//                                    seekbarV.setProgress(0);
//                                }
//
//                                if (imgPause.getVisibility() == View.VISIBLE) {
//                                    Log.e("TAG", "on media Prepare : >>>>>>>>>>  ");
//                                    imgPause.setVisibility(View.GONE);
//                                    imgPlay.setVisibility(View.VISIBLE);
//                                }
//                                txtProcess.setText(convertSecondsToHMmSs(mp.getDuration() / 1000));
//                            }
//                        });
//                    }
//                });
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        ((Activity) context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.e("TAG", "media player Complete : >>>>>>>>>>>>>>>  ");
//                                imgPause.setVisibility(View.GONE);
//                                imgPlay.setVisibility(View.VISIBLE);
//                            }
//                        });
//                    }
//                });
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imgPlay.setOnClickListener(imgPlayClickListener);
//                imgPause.setOnClickListener(imgPauseClickListener);
//                if (seekbarV.getVisibility() == VISIBLE) {
//                    seekbarV.updateVisualizer(new File(path));
//                    seekbarV.setOnSeekBarChangeListener(seekBarListener);
//                    seekbarV.setEnabled(false);
//                    seekbarV.updateVisualizer(new File(path));
//                }
//            }
//        });
//
//        seekbarV.invalidate();
//        this.invalidate();
//    }


    public void refreshVisualizer() {
        if (seekbarV.getVisibility() == VISIBLE) {
            seekbarV.updateVisualizer(new File(path));
        }
    }

    public int getSeekBarProgressColor() {
        return seekBarProgressColor;
    }

    public void setSeekBarProgressColor(int seekBarProgressColor) {
        this.seekBarProgressColor = seekBarProgressColor;
    }

    public int getSeekBarThumbColor() {
        return seekBarThumbColor;
    }

    public void setSeekBarThumbColor(int seekBarThumbColor) {
        this.seekBarThumbColor = seekBarThumbColor;
    }

    public int getProgressTimeColor() {
        return progressTimeColor;
    }

    public void setProgressTimeColor(int progressTimeColor) {
        this.progressTimeColor = progressTimeColor;
    }

    public int getVisualizationPlayedColor() {
        return visualizationPlayedColor;
    }

    public void setVisualizationPlayedColor(int visualizationPlayedColor) {
        this.visualizationPlayedColor = visualizationPlayedColor;
    }

    public int getVisualizationNotPlayedColor() {
        return visualizationNotPlayedColor;
    }

    public void setVisualizationNotPlayedColor(int visualizationNotPlayedColor) {
        this.visualizationNotPlayedColor = visualizationNotPlayedColor;
    }

    public boolean isShowTiming() {
        return showTiming;
    }

    public void setShowTiming(boolean showTiming) {
        this.showTiming = showTiming;
    }

    public boolean isEnableVirtualizer() {
        return enableVirtualizer;
    }

    public void setEnableVirtualizer(boolean enableVirtualizer) {
        this.enableVirtualizer = enableVirtualizer;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public LinearLayout getMain_layout() {
        return main_layout;
    }

    public void setMain_layout(LinearLayout main_layout) {
        this.main_layout = main_layout;
    }

    public LinearLayout getContainer_layout() {
        return container_layout;
    }

    public void setContainer_layout(LinearLayout container_layout) {
        this.container_layout = container_layout;
    }

    public ImageView getImgPlay() {
        return imgPlay;
    }

    public void setImgPlay(ImageView imgPlay) {
        this.imgPlay = imgPlay;
    }

    public ImageView getImgPause() {
        return imgPause;
    }

    public void setImgPause(ImageView imgPause) {
        this.imgPause = imgPause;
    }

    public TextView getTxtProcess() {
        return txtProcess;
    }

    public void setTxtProcess(TextView txtProcess) {
        this.txtProcess = txtProcess;
    }

    public CustomMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(CustomMediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public PlayerVisualizerSeekbar getSeekbarV() {
        return seekbarV;
    }

    public void setSeekbarV(PlayerVisualizerSeekbar seekbarV) {
        this.seekbarV = seekbarV;
    }

    public OnClickListener getImgPlayClickListener() {
        return imgPlayClickListener;
    }

    public void setImgPlayClickListener(OnClickListener imgPlayClickListener) {
        this.imgPlayClickListener = imgPlayClickListener;
    }

    public SeekBar.OnSeekBarChangeListener getSeekBarListener() {
        return seekBarListener;
    }

    public void setSeekBarListener(SeekBar.OnSeekBarChangeListener seekBarListener) {
        this.seekBarListener = seekBarListener;
    }

    public OnClickListener getImgPauseClickListener() {
        return imgPauseClickListener;
    }

    public void setImgPauseClickListener(OnClickListener imgPauseClickListener) {
        this.imgPauseClickListener = imgPauseClickListener;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }


}


