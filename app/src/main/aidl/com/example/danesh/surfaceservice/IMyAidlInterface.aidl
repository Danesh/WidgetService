// IMyAidlInterface.aidl
package com.example.danesh.surfaceservice;

import android.graphics.Rect;
// Declare any non-default types here with import statements

oneway interface IMyAidlInterface {
    void propertiesChanged(int position, int x, int y, int width, int height, boolean visibility);
    void onPause();
    void onResume();
}
