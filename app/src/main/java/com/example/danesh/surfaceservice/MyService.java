package com.example.danesh.surfaceservice;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.sprylab.android.widget.TextureVideoView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyService extends Service {
    private Handler mHandler;
    private int mVideoPosition;

    public MyService() {
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
//                        if (Math.abs(layoutParams.x - (x + 50)) <= 5) {
//                            return;
//                        }

                        if (layoutParams.x == x + 50 && layoutParams.y == y + 50 && layoutParams.width == width - 100 && layoutParams.height == 1500) {
                            return;
                        }
                        layoutParams.x = x + 50;
                        layoutParams.y = y + 50;
                        layoutParams.width = width - 100;
                        layoutParams.height = 1500;

                        System.out.println("FIRST " + x);

                        final int visible = visibility ? View.VISIBLE : View.GONE;
                        if (!mViews.containsKey(position)) {
                            final View view = getView(position);
                            mViews.put(position, view);
                            windowManager.addView(view, layoutParams);
                        } else {
                            View view = mViews.get(position);
                            if (view.getVisibility() == visible && visible == View.GONE) {
                                return;
                            }
                            view.setVisibility(visible);
                            if (view instanceof TextureVideoView) {
                                TextureVideoView videoView = ((TextureVideoView) view);
                                if (view.getVisibility() == View.GONE && videoView.isPlaying()) {
                                    ((TextureVideoView) view).pause();
                                    mVideoPosition = videoView.getCurrentPosition();
                                } else if (!videoView.isPlaying()){
                                    videoView.seekTo(mVideoPosition);
                                    ((TextureVideoView) view).start();
                                }
                            }
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
        final View view;
                view = new DoodleView(getBaseContext());
                ((DoodleView) view).setLineWidth(10);
                view.setBackgroundColor(Color.BLUE);
        return view;
    }
}
