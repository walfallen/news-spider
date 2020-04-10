package com.yc.spider.util;

import java.io.*;

/**
 * Created by youmingwei on 16/5/15.
 */
public class FileUtil {
//    public static void writeRowStrToFileWithWriter(String filePath, String rowStr, boolean append) {
//        FileWriter fileWriter = null;
//        BufferedWriter bufferedWriter = null;
//        try {
//            fileWriter = new FileWriter(filePath, append);
//            bufferedWriter = new BufferedWriter(fileWriter);
//            bufferedWriter.write(rowStr);
//            bufferedWriter.newLine();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (bufferedWriter != null) {
//                    bufferedWriter.flush();
//                    bufferedWriter.close();
//                }
//                if (fileWriter != null) {
//                    fileWriter.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    public static void writeRowStrToFileWithStream(String filePath, String rowStr, boolean append) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(filePath, append), "UTF-8"));
            bufferedWriter.write(rowStr);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void deleteAllFilesFromDir(String dirPath) {
        File dirFile = new File(dirPath);
        for (File file: dirFile.listFiles()) {
            file.delete();
        }
    }
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
