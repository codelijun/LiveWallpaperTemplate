package com.apusapps.livewallpaper.core;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.apusapps.livewallpaper.BuildConfig;
import com.apusapps.livewallpaper.object.BaseTarget;
import com.apusapps.livewallpaper.util.GLUtil;
import com.apusapps.livewallpaper.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.apusapps.livewallpaper.util.GLUtil.loadShader;

/**
 * create by: lijun
 * date: 6/5/2018
 * OpenGL的Renderer类,类名不能修改,因为依赖的库对它有反射调用
 */
public class CoreRenderer implements GLSurfaceView.Renderer {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "CoreRenderer";
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTexcoord;" +
                    "varying vec2 vTexcoord;" +
                    "void main() {" +
                    " gl_Position = uMVPMatrix * vPosition;" +
                    " vTexcoord = aTexcoord;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    " gl_FragColor = texture2D(sTexture, vTexcoord);" +
                    "}";

    private Context mContext;
    private int mPositionHandle;
    private int mTexcoordHandle;
    private boolean mSurfaceChanged;
    private boolean mSurfaceViewVisible;

    private List<BaseTarget> mTargetList = new ArrayList<>();

    public CoreRenderer(Context context) {
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // 加载纹理
        int[] mMorningTextureIds = new int[ImageUtil.TestBitmapRes.length];
        float[] morningBitmapRatios = new float[ImageUtil.TestBitmapRes.length];
        GLUtil.loadTexture(mContext, mMorningTextureIds, morningBitmapRatios, ImageUtil.TestBitmapRes);
        //TODO: 将加载好的纹理ID和图片的宽高比通过构造方法传递到Object中, 并将Object add到List中
        //  mTargetList.add(object);

        initGL();
    }

    private void initGL() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        int mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexcoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        int mTexUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTexcoordHandle);

        // 调用Object的一系列set***Handle()方法,将mPositionHandle, mMatrixHandle, mTexCoordHandle, mTexUniformHandle传递进去
        for (BaseTarget baseSpacetime : mTargetList) {
            baseSpacetime.setPositionHandle(mPositionHandle);
            baseSpacetime.setMatrixHandle(mMatrixHandle);
            baseSpacetime.setTexCoordHandle(mTexcoordHandle);
            baseSpacetime.setTexUniformHandle(mTexUniformHandle);
        }
    }

    /**
     * 虽然onVisibilityChanged()方法是自定义的,但是方法名不能修改,因为依赖的库里对它有反射调用
     */
    public void onVisibilityChanged(boolean visible) {
        if (DEBUG) {
            Log.d(TAG, " onVisibilityChanged() " + "visible = [" + visible + "]");
        }
        this.mSurfaceViewVisible = visible;
        if (!mSurfaceChanged) {
            return;
        }
        // 调用Object的onVisibilityChanged(boolean)方法(自定义)将可见状态传递进去
        for (BaseTarget baseSpacetime : mTargetList) {
            baseSpacetime.onVisibilityChanged(visible);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceChanged = true;
        if (DEBUG) {
            Log.d(TAG, " onSurfaceChanged() ");
        }
        if (height == 0) {
            height = 1;
        }
        GLES20.glViewport(0, 0, width, height);
        float screenAspectRatio = (float) width / (float) height;
        // 调用Object的onSurfaceChanged(float)方法(自定义)将屏幕的宽高比传递进去
        for (BaseTarget baseSpacetime : mTargetList) {
            baseSpacetime.onSurfaceChanged(screenAspectRatio);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (DEBUG) {
            Log.d(TAG, " onDrawFrame() mSurfaceViewVisible== " + mSurfaceViewVisible);
        }
        if (!mSurfaceViewVisible) {
            return;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 调用Object的onDrawable()方法(自定义)绘制纹理
        for (BaseTarget baseTarget : mTargetList) {
            baseTarget.drawSelf();
        }
    }

    /**
     * 虽然onDestroy()方法是自定义的,但是方法名不能修改,因为依赖的库里对它有反射调用
     */
    public void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, " onDestroy() ");
        }
        // 调用Object的onDestroy()方法(自定义)释放资源
        for (BaseTarget baseTarget : mTargetList) {
            baseTarget.destroySelf();
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexcoordHandle);
    }
}
