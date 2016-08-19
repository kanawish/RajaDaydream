package com.kanawish.raja.controlroom.utils;

import android.opengl.Matrix;

/**
 * Created by kanawish on 2016-08-03.
 */
public class TangoMath {

    /**
     * Calculates a transformation matrix based on a point, a normal and the up gravity vector.
     * The coordinate frame of the target transformation will be Z forward, X left, Y up.
     */
    public static float[] matrixFromPointNormalUp(double[] point, double[] normal, float[] up) {
        float[] zAxis = new float[]{(float) normal[0], (float) normal[1], (float) normal[2]};
        normalize(zAxis);
        float[] xAxis = crossProduct(zAxis, up);
        normalize(xAxis);
        float[] yAxis = crossProduct(zAxis, xAxis);
        normalize(yAxis);
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        m[0] = xAxis[0];
        m[1] = xAxis[1];
        m[2] = xAxis[2];
        m[4] = yAxis[0];
        m[5] = yAxis[1];
        m[6] = yAxis[2];
        m[8] = zAxis[0];
        m[9] = zAxis[1];
        m[10] = zAxis[2];
        m[12] = (float) point[0];
        m[13] = (float) point[1];
        m[14] = (float) point[2];
        return m;
    }

    /**
     * Normalize a vector.
     */
    private static void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    /**
     * Cross product between two vectors following the right hand rule.
     */
    private static float[] crossProduct(float[] v1, float[] v2) {
        float[] result = new float[3];
        result[0] = v1[1] * v2[2] - v2[1] * v1[2];
        result[1] = v1[2] * v2[0] - v2[2] * v1[0];
        result[2] = v1[0] * v2[1] - v2[0] * v1[1];
        return result;
    }

    /**
     * Calculate the pose of the plane based on the position and normal orientation of the plane
     * and align it with gravity.
     */
    public static float[] calculatePlaneTransform(double[] point, double normal[],
                                                  float[] openGlTdepth) {
        // Vector aligned to gravity.
        float[] openGlUp = new float[]{0, 1, 0, 0};
        float[] depthTOpenGl = new float[16];
        Matrix.invertM(depthTOpenGl, 0, openGlTdepth, 0);
        float[] depthUp = new float[4];
        Matrix.multiplyMV(depthUp, 0, depthTOpenGl, 0, openGlUp, 0);
        // Create the plane matrix transform in depth frame from a point, the plane normal and the
        // up vector.
        float[] depthTplane = matrixFromPointNormalUp(point, normal, depthUp);
        float[] openGlTplane = new float[16];
        Matrix.multiplyMM(openGlTplane, 0, openGlTdepth, 0, depthTplane, 0);
        return openGlTplane;
    }
}
