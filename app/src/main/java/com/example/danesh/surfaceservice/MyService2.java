package com.example.danesh.surfaceservice;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sprylab.android.widget.TextureVideoView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyService2 extends Service {
    private Handler mHandler;
    private int mVideoPosition;

    public MyService2() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private Map<Integer, View> mViews = Collections.synchronizedMap(new HashMap<Integer, View>());

    private WindowManager.LayoutParams generateWindowParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.format = PixelFormat.OPAQUE;
        layoutParams.preferredRefreshRate = 63;
        return layoutParams;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = new Handler();
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        return new IMyAidlInterface.Stub() {
            @Override
            public void propertiesChanged(final int position, final int x, final int y, final int width, final int height, final boolean visibility) throws RemoteException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final WindowManager.LayoutParams layoutParams;
                        if (!mViews.containsKey(position)) {
                            layoutParams = generateWindowParams();
                        } else {
                            layoutParams = (WindowManager.LayoutParams) mViews.get(position).getLayoutParams();
                        }
                        if (Math.abs(layoutParams.x - x) <= 5) {
                            return;
                        }

                        if (layoutParams.x == x + 50 && layoutParams.y == y + 50 && layoutParams.width == width - 100) {
                            return;
                        }
                        layoutParams.x = x + 50;
                        layoutParams.y = y + 50;
                        layoutParams.width = width - 100;
                        layoutParams.height = 550;


                        final int visible = visibility ? View.VISIBLE : View.GONE;
                        if (!mViews.containsKey(position)) {
                            final View view = getView(position);
                            mViews.put(position, view);
                            System.out.println("Add view " + position);
                            windowManager.addView(view, layoutParams);
                        } else {
                            View view = mViews.get(position);
                            if (view.getVisibility() == visible && visible == View.GONE) {
                                return;
                            }
                            view.setVisibility(visible);
                            if (view instanceof FrameLayout) {
                                TextureVideoView videoView = ((TextureVideoView) ((FrameLayout) view).getChildAt(0));
                                ImageView thumbnail = ((ImageView) ((FrameLayout) view).getChildAt(1));
                                thumbnail.setImageResource(R.drawable.ic_play);
                                videoView.pause();
                                thumbnail.getDrawable().setTint(Color.WHITE);
                                mVideoPosition = videoView.getCurrentPosition();
                            }
                            System.out.println(position + " -> " + view.getVisibility());
                            if (view.getVisibility() != View.GONE) windowManager.updateViewLayout(view, layoutParams);
                        }
                    }
                });
            }

            @Override
            public synchronized void onPause() throws RemoteException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i : mViews.keySet()) {
                            if (mViews.get(i) != null) {
                                try {
                                    windowManager.removeView(mViews.get(i));
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public synchronized void onResume() throws RemoteException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i : mViews.keySet()) {
                            if (mViews.get(i) != null) {
                                windowManager.addView(mViews.get(i), mViews.get(i).getLayoutParams());
                            }
                        }
                    }
                });
            }
        };
    }

    private View getView(int position) {
        final ImageView thumbnail = new ImageView(getBaseContext());
        thumbnail.setImageResource(R.drawable.ic_play);
        thumbnail.getDrawable().setTint(Color.WHITE);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(200, 200);
        layoutParams.gravity = Gravity.CENTER;
        thumbnail.setLayoutParams(layoutParams);
        final FrameLayout frameLayout = new FrameLayout(getBaseContext());
        final TextureVideoView videoView = new TextureVideoView(getBaseContext());
        videoView.setVideoURI(Uri.parse("content://media/external/video/media/1824"));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    videoView.seekTo(mVideoPosition);
                    videoView.start();
                    thumbnail.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                } else {
                    thumbnail.setImageResource(R.drawable.ic_play);
                    mVideoPosition = videoView.getCurrentPosition();
                    videoView.pause();
                }
                thumbnail.getDrawable().setTint(Color.WHITE);
            }
        });
        frameLayout.addView(videoView);
        frameLayout.addView(thumbnail);
        return frameLayout;
    }
}
