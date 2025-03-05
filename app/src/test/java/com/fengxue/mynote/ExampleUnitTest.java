package com.fengxue.mynote;

import static com.fengxue.mynote.MainActivity.byte_decrypt;
import static com.fengxue.mynote.MainActivity.byte_encryption;
import static com.fengxue.mynote.MainActivity.bytesToHex;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testHexStringToByteArray(){
        String str="test";
        byte[] bytes=hexStringToByteArray(str);
        byte[] newbyte = byte_decrypt(bytes);
        try {
            String stt=new String(newbyte, "UTF-8");
            //System.out.println(stt);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //System.out.println(bytesToHex(newbyte));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            // 从16进制字符串中提取两个字符
            String hexChar = s.substring(i, i + 2);
            // 将两个字符转换为一个字节
            data[i / 2] = (byte) Integer.parseInt(hexChar, 16);
        }

        return data;
    }

}



        //创建文件夹
        //sardine.createDirectory(URL+"NOTE/");

        //判断文件（夹）是否存在，文件夹必须以 / 结尾
        //sardine.exists(URL+"NOTE/");

        //获取某文件夹/目录下全部文件名
        //sardine.list(URL+"NOTE/");

        //下载文件
        // 参数（String类型）：文件路径，如： "https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹/测试.txt"
        //sardine.get(参数);//注：不能下载目录，必须下载具体文件。

        //上传文件
        //参数1（String类型）：文件路径，如："https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹/测试.txt",参数2：文件内容
        //注1：参数1中必须注明上传文件的类型，如.txt.注2：参数2的类型有多种，如，byte[]字节数组类型，file类型
        //sardine.put(参数1, 参数2);

        //移动、重命名文件
        //参数1（String类型）：旧文件路径，如：
        //"https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹1/测试1.txt"
        //参数2（String类型）：新文件路径，如：
        //1.移动操作：
        //"https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹2/测试1.txt"
        //2.重命名操作：
        // "https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹1/测试2.txt"
        //注：移动和重命名是同一个方法
        //sardine.move(参数1, 参数2);

        //删除文件、文件夹
        //参数1（String类型）：文件路径，如：
        //（删除文件）"https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹/测试.txt"
        //（删除文件夹）"https://dav.jianguoyun.com/dav/我的坚果云/测试新建文件夹"
        //sardine.delete(参数);






















