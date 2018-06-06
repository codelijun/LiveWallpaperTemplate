package com.apusapps.livewallpaper.core;

import android.util.Log;
import android.view.SurfaceHolder;

import com.apusapps.livewallpaper.BuildConfig;
import com.example.livewallpapergllibrary.glwallpaperservice.GLWallpaperService;

/**
 * create by: lijun
 * date: 6/5/2018
 * 类名不能修改,因为类名被依赖的库使用
 */
public class CoreWallpaperService extends GLWallpaperService {
    private static final String TAG = "CoreWallpaperService";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public Engine onCreateEngine() {
        return new LiveWallpaperEngine();
    }

    private class LiveWallpaperEngine extends GLWallpaperService.GLEngine {
        private CoreRenderer mRenderer;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.mRenderer = new CoreRenderer(getApplicationContext());
            setEGLContextClientVersion(2);
            setRenderer(mRenderer);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (DEBUG) {
                Log.d(TAG, " onVisibilityChanged() " + "visible = [" + visible + "]");
            }
            mRenderer.onVisibilityChanged(visible);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (DEBUG) {
                Log.d(TAG, " onDestroy() ");
            }
            mRenderer.onDestroy();
        }
    }
}
