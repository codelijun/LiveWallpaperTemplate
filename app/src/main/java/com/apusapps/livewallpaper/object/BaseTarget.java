package com.apusapps.livewallpaper.object;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.apusapps.livewallpaper.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * create by: lijun
 * date: 6/5/2018
 */
public abstract class BaseTarget {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "BaseTarget";
    protected static final short[] VERTEX_TEXTURE = {
            0, 0,
            0, 1,
            1, 0,
            0, 1,
            1, 1,
    };

    protected final float[] mMVPMatrix = new float[16];
    protected final float[] mViewMatrix = new float[16];
    protected final float[] mModelMatrix = new float[16];
    protected final float[] mProjectionMatrix = new float[16];

    protected float[] mVertexRectangle;
    protected FloatBuffer mVertexBuffer;
    protected ShortBuffer mTexCoorBuffer;

    protected int mMatrixHandle;
    protected int mPositionHandle;
    protected int mTexcoordHandle;
    protected int mTexUniformHandle;
    protected int mTextureId;

    protected float mBitmapAspectRatio;   //图片的高宽比

    public void setMatrixHandle(int matrixHandle) {
        this.mMatrixHandle = matrixHandle;
    }

    public void setPositionHandle(int positionHandle) {
        this.mPositionHandle = positionHandle;
    }

    public void setTexCoordHandle(int texcoordHandle) {
        this.mTexcoordHandle = texcoordHandle;
    }

    public void setTexUniformHandle(int texUniformHandle) {
        this.mTexUniformHandle = texUniformHandle;
    }

    protected void initVertexData() {
        //根据纹理的宽高比绘制矩形,使得纹理不变形
        //此时矩形x轴的范围:[-1,1], y轴的范围:[-mBitmapAspectRatio, mBitmapAspectRatio].
        mVertexRectangle = new float[]{
                -1f, mBitmapAspectRatio, 0f,
                -1f, -mBitmapAspectRatio, 0f,
                1f, mBitmapAspectRatio, 0,
                -1f, -mBitmapAspectRatio, 0f,
                1f, -mBitmapAspectRatio, 0f,
        };

        mVertexBuffer = ByteBuffer.allocateDirect(mVertexRectangle.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexRectangle);
        mVertexBuffer.position(0);

        mTexCoorBuffer = ByteBuffer.allocateDirect(VERTEX_TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_TEXTURE);
        mTexCoorBuffer.position(0);
    }

    public void onSurfaceChanged(float screenAspectRatio) {
        float screenRightPositionX = screenAspectRatio * mBitmapAspectRatio;
        //纹理刚好以高为基准充满屏幕,宽自适应
        if (1 / mBitmapAspectRatio > screenAspectRatio) {
            Matrix.frustumM(mProjectionMatrix, 0, -screenRightPositionX, screenRightPositionX,
                    -mBitmapAspectRatio, mBitmapAspectRatio, 1f, 100f);
        } else {
            //纹理刚好以宽为基准充满屏幕,高自适应
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1,
                    -1 / screenAspectRatio, 1 / screenAspectRatio, 1f, 100f);
        }
    }

    public void onVisibilityChanged(boolean visible) {
        if (DEBUG) {
            Log.d(TAG, " onVisibilityChanged() " + "visible = [" + visible + "]");
        }
        if (visible) {
            startObjectAnimation();
        } else {
            stopObjectAnimation();
        }
    }

    protected void startObjectAnimation() {
    }

    protected void stopObjectAnimation() {
    }

    public abstract void drawSelf();

    public void destroySelf() {
        stopObjectAnimation();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
