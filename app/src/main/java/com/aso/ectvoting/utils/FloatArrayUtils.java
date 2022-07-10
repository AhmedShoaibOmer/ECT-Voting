package com.aso.ectvoting.utils;

public class FloatArrayUtils {
    public static String toString(float[] arr) {
        StringBuilder res = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            res.append(arr[i]);
            if (i < arr.length - 1) res.append(", ");
        }
        res.append("]");
        return res.toString();
    }

    public static float[] toArray(String str) {
        String sub = str.substring(1, str.length() - 2);
        String[] strOfFloats = sub.split(",");
        float[] floats = new float[strOfFloats.length];
        for (int i = 0; i < strOfFloats.length; i++) {
            floats[i] = Float.parseFloat(strOfFloats[i]);
        }
        return floats;
    }
}
