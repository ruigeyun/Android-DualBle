/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.util;

import java.util.Arrays;

public class BytesUtil {

    public static String BytesToHexStringPrintf(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b) + " ");
        }
        return builder.toString();
    }

    public static String BytesToHexStringPrintfNoSpace(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static String BytesToHexStringPrintf(byte[] bytes, int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(String.format("%02x", bytes[i]) + " ");
        }
        return builder.toString();
    }

    public static int getParamLen(byte[] data, int index) {
        int len = data[index++] & 0xFF;
        len = len << 8;
        len = len | (data[index++] & 0xFF);
        return len;
    }

    //int转成4个字节byte[]
//    public static final byte[] intToByteArray(int value) {
//        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16),
//                (byte) (value >>> 8), (byte) value};
//    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{(byte) value, (byte) (value >>> 8),
                (byte) (value >>> 16), (byte) (value >>> 24)};
    }

    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    public static String bytesToDecString(byte[] bytes, int offset, int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(String.format("%d", bytes[offset + i]));
        }

        return builder.toString();
    }

    public static byte[] hexStringToBytes(String s) {
        int length = s.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0, j = 0; i < length; i = i + 2, j++) {
            bytes[j] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }

        return bytes;
    }

    /**
     * // TODO 16进制 byte[]和int之间的转换 字符串转byte[] 这个方法转换后的结果是会多一些
     * 48字符进来的就是代表的是0不知道为什么，但是可以只是取出指定的字符串就行了
     **/
    public static byte[] stringTo16Byte(String temp) {

        int len = temp.length();
        for (int i = 0; i < 16 - len; i++) {
            if (temp.length() == 16) {
                break;
            }
            temp = temp + "0";

        }
        return temp.getBytes();
    }

    /* int转16进制 */
    public static int stringTo16Int(String temp) {
        int output;
        output = Integer.parseInt(temp, 16);
        return output;
    }

    /* byte转short */
    public final static short getShort(byte[] buf, boolean asc, int len) {
        short r = 0;
        if (asc)
            for (int i = len - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }
        else
            for (int i = 0; i < len; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00ff);
            }

        return r;
    }

    /* B2 -> 0xB2 */
    public static int stringToByte(String in, byte[] b) throws Exception {
        if (b.length < in.length() / 2) {
            throw new Exception("byte array too small");
        }

        int j = 0;
        StringBuffer buf = new StringBuffer(2);
        for (int i = 0; i < in.length(); i++, j++) {
            buf.insert(0, in.charAt(i));
            buf.insert(1, in.charAt(i + 1));
            int t = Integer.parseInt(buf.toString(), 16);
            System.out.println("byte hex value:" + t);
            b[j] = (byte) t;
            i++;
            buf.delete(0, 2);
        }

        return j;
    }

    /* byte to int */
    public final static int getInt(byte[] buf, boolean asc, int len, int offset) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (len > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (asc)
            for (int i = len - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[(i + offset)] & 0x000000ff);
            }
        else
            for (int i = 0; i < len; i++) {
                r <<= 8;
                r |= (buf[(i + offset)] & 0x000000ff);
            }
        return r;
    }

    public final static int getInt2(byte[] buf, boolean asc, int len, int offset) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (len > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (asc) {
            for (int i = len - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[(i + offset)] & 0x000000ff);
            }
        } else {
            for (int i = 0; i < len; i++) {
                r <<= 8;
                r |= (buf[(i + offset)] & 0x000000ff);
            }
        }
        r = (r & 0x00007fff); // bit 7 为符号位，排除
        return r;
    }

    /* int -> byte[] */
    public static byte[] intToBytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }

        return b;
    }

    /* short to byte[] */
    public static byte[] shortToBytes(short num) {
        byte[] b = new byte[2];

        for (int i = 0; i < 2; i++) {
            b[i] = (byte) (num >>> (i * 8));
        }

        return b;
    }

    /* byte to String */
    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;

        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }

        return (char) (t - 10 + 'A');
    }

    public static String byteToString(byte b) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;

        high = (byte) ((b & maskHigh) >> 4);
        low = (byte) (b & maskLow);

        StringBuffer buf = new StringBuffer();
        buf.append(findHex(high));
        buf.append(findHex(low));

        return buf.toString();
    }

    /* short -> byte */
    public final static byte[] getBytes(short s, boolean asc) {
        byte[] buf = new byte[2];
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        return buf;
    }

    /* int -> byte[] */
    public final static byte[] getBytes(int s, boolean asc) {
        byte[] buf = new byte[4];
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        return buf;
    }

    /* long -> byte[] */
    public final static byte[] getBytes(long s, boolean asc) {
        byte[] buf = new byte[8];
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x00000000000000ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00000000000000ff);
                s >>= 8;
            }
        return buf;
    }

    /* byte[]->int */
    public final static int getInt(byte[] buf, boolean asc) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        else
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x000000ff);
            }
        return r;
    }

    public final static int getInt(boolean asc, byte... args) {
        if (args == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (args.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (asc)
            for (int i = args.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (args[i] & 0x000000ff);
            }
        else
            for (int i = 0; i < args.length; i++) {
                r <<= 8;
                r |= (args[i] & 0x000000ff);
            }
        return r;
    }

    /* byte[] -> long */
    public final static long getLong(byte[] buf, boolean asc) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 8) {
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        long r = 0;
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        else
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0x00000000000000ff);
            }
        return r;
    }

    // long类型转成byte数组
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    // byte数组转成long
    public static long byteToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;

        // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static int byteToInt(byte[] b, int offset) {
        int s = 0;
        int s0 = b[0 + offset] & 0xff;// 最高位
        //		LogUtil.v("", String.format("%02x", s0));
        int s1 = b[1 + offset] & 0xff;
        //		LogUtil.v("", String.format("%02x", s1));
        int s2 = b[2 + offset] & 0xff;
        //		LogUtil.v("", String.format("%02x", s2));
        int s3 = b[3 + offset] & 0xff;
        //		LogUtil.v("", String.format("%02x", s3));
        s0 <<= 24;
        s1 <<= 16;
        s2 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
