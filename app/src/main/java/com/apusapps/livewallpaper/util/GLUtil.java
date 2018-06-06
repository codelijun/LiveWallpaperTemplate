package com.apusapps.livewallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * create by: lijun
 * date: 6/5/2018
 */
public class GLUtil {
    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderHandle = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shaderHandle, shaderCode);
        GLES20.glCompileShader(shaderHandle);
        checkGlError("glCompileShader");
        return shaderHandle;
    }

    /**
     * 绑定纹理
     *
     * @param context
     * @param textureId         纹理的id
     * @param bitmapAspectRatio 纹理的高宽比例
     */
    public static void loadTexture(Context context, int[] textureId, float[] bitmapAspectRatio, String[] bitmapRes) {
        InputStream is = null;
        int[] texNames = new int[bitmapRes.length];
        GLES20.glGenTextures(bitmapRes.length, texNames, 0);
        GLUtil.checkGlError("glGenTextures");
        for (int i = 0; i < bitmapRes.length; i++) {
            try {
                is = context.getAssets().open(bitmapRes[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (is == null) {
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            bitmapAspectRatio[i] = (float) height / (float) width;
            textureId[i] = GLUtil.loadTextures(texNames[i], bitmap);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.gc();
        }
    }

    private static int loadTextures(int texNames, Bitmap bitmap) {
        //http://www.arvrschool.com/read.php?tid=130
        if (texNames != 0) {
            //激活纹理单元，GL_TEXTURE0代表纹理单元0，GL_TEXTURE1代表纹理单元1，以此类推。OpenGL使用纹理单元来表示被绘制的纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texNames);
            //设置纹理过滤参数，GL_TEXTURE_MIN_FILTER代表纹理缩写的情况，GL_LINEAR_MIPMAP_LINEAR代表缩小时使用三线性过滤的方式
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            //GL_TEXTURE_MAG_FILTER代表纹理放大，GL_LINEAR代表双线性过滤
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);
            //加载实际纹理图像数据到OpenGL ES的纹理对象中，这个函数是Android封装好的，可以直接加载bitmap格式
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLUtil.checkGlError("texImage2D");
            bitmap.recycle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
        if (texNames == 0) {
            throw new RuntimeException(
                    "Error loading texture (empty texture handle).");
        }
        return texNames;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
