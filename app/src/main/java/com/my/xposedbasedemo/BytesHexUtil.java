package com.my.xposedbasedemo;

public class BytesHexUtil {

    // 将byte转化为16进制
    public static String byteToHex(byte[] bs) {
        if (bs.length == 0) {
            return "";
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < bs.length; i++) {
                String s = Integer.toHexString(bs[i] & 0xFF);
                if (1 == s.length()) {
                    stringBuffer.append("0");
                }
                stringBuffer = stringBuffer.append(s);
            }
            return stringBuffer.toString();
        }
    }

    // 将16进制转化为byte
    public static byte[] hexToByte(String ciphertext) {
        byte[] cipherBytes = ciphertext.getBytes();
        if ((cipherBytes.length % 2) != 0) {
            throw new IllegalArgumentException("长度不为偶数");
        } else {
            byte[] result = new byte[cipherBytes.length / 2];
            for (int i = 0; i < cipherBytes.length; i += 2) {
                String item = new String(cipherBytes, i, 2);
                result[i / 2] = (byte) Integer.parseInt(item, 16);
            }
            return result;
        }
    }

}
