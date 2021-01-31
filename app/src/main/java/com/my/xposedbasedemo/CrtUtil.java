package com.my.xposedbasedemo;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.robv.android.xposed.XposedBridge;

public class CrtUtil {

    public static InputStream interceptCrt(InputStream in, String pwd, String crtType) {
        InputStream originSteam = null;
        try {
            if (crtType.equals("PKCS12")) {
                crtType = "p12";
            }
            if (crtType.equals("BKS")) {
                crtType = "bks";
            }
            String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures" + File.separator + System.currentTimeMillis() + "-" + pwd + "." + crtType;
            XposedBridge.log("path : " + path);
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }

            // 创建基于文件的输出流
            FileOutputStream fos = new FileOutputStream(f);
            // 先把原工程里的流复制一份(一会要返回一份流,否则会干扰原工程文件的读写)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
                // 把数据写入到输出流
                fos.write(buffer, 0, len);
            }
            baos.flush();
            // 关闭输出流
            fos.close();

            originSteam = new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            XposedBridge.log(e.getMessage());
//            throw new AssertionError();
        }
        //避免干扰原工程里的文件读入流
        return originSteam;
    }

}
