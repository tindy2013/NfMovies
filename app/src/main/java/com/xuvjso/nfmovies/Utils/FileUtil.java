package com.xuvjso.nfmovies.Utils;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static boolean write(String path, String filename, String content) {
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
        try {
            file = new File(path + filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getFromRaw(Context context, int id) {
        String result = "";
        try {
            InputStream in = context.getResources().openRawResource(id);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            result = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean deleteDirectory(File path) {
        Log.i("Deleted ", path.getAbsolutePath());
        if(path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return false;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean wasSuccessful = file.delete();
                    if (wasSuccessful) {
                        Log.i("Deleted ", "successfully");
                    }
                }
            }
        }
        return(path.delete());
    }
}
