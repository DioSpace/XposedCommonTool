package com.my.xposedbasedemo;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.robv.android.xposed.XC_MethodHook;

public class HookUtil {
    public static XC_MethodHook.MethodHookParam hook_param = null;// new XC_MethodHook.MethodHookParam();
    public static Class<?> TargetClass = null;
    public static int num = 0;
    public static boolean first_start = true;

    /*
     * 读取sd卡中的文件中内容
     * fname 相对路径下的文件名 如： "Pictures/test.txt"
     * */
    public static String loadFromSDFile(String fname) {
        fname = "/" + fname;
        String result = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + fname);
//            System.out.println(f.getAbsoluteFile());
            int length = (int) f.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(f);
            fin.read(buff);
            fin.close();
            result = new String(buff, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入内容到SD卡中的文本
     * content为内容
     */
    public static void writeToSDFile(String content, String fname) {
        fname = "/" + fname;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + fname);
//            System.out.println(f1.getAbsoluteFile());
            if (!f.exists()) {
                boolean isOr = f.createNewFile();
                if (!isOr) {
                    return;
                }
            }
            byte[] data = content.getBytes();
            // 创建基于文件的输出流
            FileOutputStream fos = new FileOutputStream(f);
            // 把数据写入到输出流
            fos.write(data);
            // 关闭输出流
            fos.close();
//            System.out.println("输入完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //往尾部追加数据
    public static void write_endingSDFile(String content, String fname) {
        fname = "/" + fname;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + fname);
            if (!f.exists()) {
                f.createNewFile();
            }
            System.out.println(f.getAbsoluteFile());
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
