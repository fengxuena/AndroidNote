package com.fengxue.mynote;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.database.sqlite.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gwt.thirdparty.guava.common.primitives.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SQLhelper dbHelper;
    private SQLiteDatabase db;//初始化加载config数据库
    private AlertDialog alertDialog;
    @Override//入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new SQLhelper(this);
        db = dbHelper.getWritableDatabase();
        List<List> cu = dbHelper.getdata("SELECT * FROM config WHERE NAME = '主题'", "config");
        if (cu.size()!=0){if (cu.get(0).get(6).equals("1")){setTheme(R.style.Them_green);}else {setTheme(R.style.Them_Yellow);}}else {setTheme(R.style.Them_green);}
        setContentView(R.layout.activity_main);
        Permission permission = new Permission();
        permission.checkPermissions(this);
        Button loginbt=findViewById(R.id.login_button);
        loginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.login_button){login();}}});}


    //登录按钮
    public void login() {
        EditText tex=findViewById(R.id.mima_text);
        List<List> data = dbHelper.getdata("SELECT * FROM config WHERE NAME = '首次启动'", "config");
            if (data.size()!=0) {//有数据，则正常进行
                    int INTDATA = Integer.parseInt((String) data.get(0).get(6));
                    if (INTDATA == 0) {
                        set_secret_dialog();
                        tex.setText("");
                    } else {
                        List<List> cursor2 = dbHelper.getdata("SELECT * FROM config WHERE NAME = '主密码'", "config");
                        if (cursor2.get(0).size() != 0) {
                            String INTDATA2 = (String) cursor2.get(0).get(5);
                            String input = confuse(String.valueOf(tex.getText()));
                            if (input.equals(INTDATA2)) {
                                startActivityTakeString(this, ChooseActivity.class);
                                Toast.makeText(MainActivity.this, "密码正确！" , Toast.LENGTH_SHORT).show();
                                tex.setText("");
                            } else {
                                if (input.equals("442aa34241e132b344ebb24113311e3e414211a2aa2142e44b4b242a4441331e")){
                                    //开发者密码重置所有密码为空
                                    try {String str = confuse("".toString());
                                    db.execSQL("UPDATE config SET VALU4='"+str+"' WHERE NAME='主密码'");
                                    db.execSQL("UPDATE config SET VALU4='"+str+"' WHERE NAME='笔记本'");
                                    Toast.makeText(MainActivity.this, "开发者重置密码为空！" , Toast.LENGTH_SHORT).show();}catch (Exception aa){aa.printStackTrace();}
                                }else {
                                    tex.setText("");
                                    show_tip("密码错误！",MainActivity.this);
                                }}}}
            }else {
                db.execSQL("INSERT INTO config('NAME', 'INTS') VALUES ('主密码',0)");
                db.execSQL("INSERT INTO config('NAME', 'INTS') VALUES ('首次启动',0)");
                db.execSQL("INSERT INTO config('NAME', 'INTS') VALUES ('主题',1)");
                db.execSQL("INSERT INTO config('NAME', 'INTS') VALUES ('WEBDAV',0)");
                login();}
    }
    //设置密码
    public void set_secret_dialog(){
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputDialog.setTitle("初次使用,请设置主密码!").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text= confuse(editText.getText().toString());
                        ContentValues cv = new ContentValues();
                        cv.put("VALU4",text);
                        db.update("config", cv, "NAME = ?", new String[] {"主密码"});
                        //db.execSQL("UPDATE config SET VALU="+text+" WHERE ID='主密码'");
                        db.execSQL("UPDATE config SET INTS="+1+" WHERE NAME='首次启动'");
                        Toast.makeText(MainActivity.this, "主密码设置成功！" , Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
    //启动Active
    public static void startActivityTakeString(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);}
    //获取时间戳
    public static  String get_datatime_string(){
        Calendar date = Calendar.getInstance();
        int gongliYear=date.get(Calendar.YEAR);
        int[] mounlist={1,2,3,4,5,6,7,8,9,10,11,12};
        int gonglimouth=mounlist[date.get(Calendar.MONTH)];
        int gongliday=date.get(Calendar.DAY_OF_MONTH);
        int gonglihour=date.get(Calendar.HOUR_OF_DAY);
        int gonglimin=date.get(Calendar.MINUTE);
        int sec=date.get(Calendar.SECOND);
        String name=gongliYear+"-"+gonglimouth+"-"+gongliday+" "+gonglihour+":"+gonglimin+":"+sec;
        return name;
    }
    //获取时间戳2
    public static  String get_chinese_datatime(){
        Calendar date = Calendar.getInstance();
        int gongliYear=date.get(Calendar.YEAR);
        int[] mounlist={1,2,3,4,5,6,7,8,9,10,11,12};
        int gonglimouth=mounlist[date.get(Calendar.MONTH)];
        int gongliday=date.get(Calendar.DAY_OF_MONTH);
        int gonglihour=date.get(Calendar.HOUR_OF_DAY);
        int gonglimin=date.get(Calendar.MINUTE);
        int sec=date.get(Calendar.SECOND);
        String name=gongliYear+"年"+gonglimouth+"月"+gongliday+"日"+gonglihour+"时"+gonglimin+"分"+sec+"秒";
        return name;
    }
    //baty转Bitmap
    public static byte[] bitmaptobyte(Bitmap bitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {out.flush();
            out.close();
        } catch (IOException e) {e.printStackTrace();}
        return out.toByteArray();}
    //bitmap转byte
    public static Bitmap bytetobitmap(byte[] temp){
        if(temp != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        }else{return null;}}
    //日期格式，格式化为中文
    public static String datetime_to_chinese(String date){
        String newstr = date.replace(" ", "-").replace(":", "-");
        String[] list = newstr.split("-");
        int a=0;
        String outstr=null;
        for (String str:list){
            switch (a){
                case 0:outstr=str+"年";break;
                case 1:outstr=outstr+str+"月";break;
                case 2:outstr=outstr+str+"日";break;
                case 3:outstr=outstr+str+"时";break;
                case 4:outstr=outstr+str+"分";break;
                case 5:outstr=outstr+str+"秒";break;
                default:break;}
            a++;
        }
        return outstr;
    }
    //密码加密混淆
    public static String confuse(String text){
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes("UTF-8"));//256位bit，32位byte的数组
            for (byte b : hashBytes) {//转换为16进制str
                sb.append(String.format("%02x", b));}
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();}
        String out=sb.toString().replace("1","5").replace("2","6").replace("3","7").replace("4","8")
                .replace("5","9").replace("6","0").replace("7","1").replace("8","2")
                .replace("9","3").replace("0","4").replace("a","e").replace("c","a")
                .replace("d","f").replace("f","1");
        return out;
    }
    //退出程序
    public static void exit(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                // 结束所有活动
                android.os.Process.killProcess(appProcess.pid);
            }
        }
        System.exit(0);
    }
    //获取字节在内存中某一位的值,采用字符取值方式
    public static Integer getBitfromByte(byte b, int index) {
        if(index >= 8) { return null; }
        Integer val = null;
        String binStr = bytetostring(b);
        val = Integer.parseInt(String.valueOf(binStr.charAt(index)));
        return val;
    }
    //把单个字节转换成二进制字符串
    public static String bytetostring(byte b) {
        String zero = "00000000";
        String binStr = Integer.toBinaryString(b & 0xFF);
        if(binStr.length() < 8) {binStr = zero.substring(0, 8 -binStr.length()) + binStr;}
        return binStr;}
    //二进制对称加密
    public static byte[] byte_encryption(byte[] bytes) {
        byte[] KEY=new byte[256];
        try {KEY= confuse("".toString()).substring(0,16).getBytes("ASCII");} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
        List<Integer> listone = new ArrayList<>();
        List<Integer> listtwo = new ArrayList<>();
        List<Integer> listout = new ArrayList<>();
        List<Integer> newlistout = new ArrayList<>();
        int kk = 0;
        for (byte binaryDatum : bytes) {
            int wei8 = binaryDatum >> 0 & 1;
            int wei7 = binaryDatum >> 1 & 1;
            int wei6 = binaryDatum >> 2 & 1;
            int wei5 = binaryDatum >> 3 & 1;
            int wei4 = binaryDatum >> 4 & 1;
            int wei3 = binaryDatum >> 5 & 1;
            int wei2 = binaryDatum >> 6 & 1;
            int wei1 = binaryDatum >> 7 & 1;
            int cc = wei1*2*2*2 + wei3*2*2 + wei5*2  + wei7*1;
            int cc2 = wei2*2*2*2 + wei4*2*2 + wei6*2  + wei8*1;
            listone.add(cc);
            listtwo.add(cc2);
            kk++;
        }
        List<Integer> listsav = new ArrayList<>();
        listsav.addAll(listone);
        listsav.addAll(listtwo);
        for (int qq = 0; qq < listsav.size(); qq++) {
            if (qq % 2 == 0) {//0100  1001
                int yu4 = listsav.get(qq) % 2;//0
                int yu3 = (listsav.get(qq) / 2) % 2;//0
                int yu2 = (listsav.get(qq) / 4) % 2;//1
                int yu1 = (listsav.get(qq) / 8) % 2;//0
                int yu8 = listsav.get(qq + 1) % 2;//1
                int yu7 = (listsav.get(qq + 1) / 2) % 2;//0
                int yu6 = (listsav.get(qq + 1) / 4) % 2;//0
                int yu5 = (listsav.get(qq + 1) / 8) % 2;//1
                int newnub = yu1 * 2*2*2*2*2*2*2 + yu2 * 2*2*2*2*2*2 + yu3 * 2*2*2*2*2 + yu4 * 2*2*2*2 + yu5 * 2*2*2 + yu6 * 2*2 + yu7 * 2  + yu8 * 1;
                listout.add(newnub);
            }
        }
        int kyynb = 0;
        for (int nb:listout){
            if (kyynb==KEY.length){kyynb=0;}
            int ccc =(int) KEY[kyynb];
            int nnb = nb ^ ccc;
            newlistout.add(nnb);
            kyynb++;}
        byte[] out = Bytes.toArray(newlistout);
        return out;
    }
    //二进制对称解密
    public static byte[] byte_decrypt(byte[] bytes) {
        List<Integer> newlistout = new ArrayList<>();
        byte[] KEY=new byte[256];
        try {KEY= confuse("".toString()).substring(0,16).getBytes("ASCII");} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
        int kyynb = 0;
        for (int nb:bytes){
            if (kyynb==KEY.length){kyynb=0;}
            int ccc =(int) KEY[kyynb];
            int nnb = nb ^ ccc;
            newlistout.add(nnb);
            kyynb++;}
        byte[] newin = Bytes.toArray(newlistout);
        List<Integer> listone = new ArrayList<>();
        List<Integer> listout = new ArrayList<>();
        int qian = newin.length / 2;
        int cc = 0;
        for (byte aa : newin) {
            int wei8 = aa >> 0 & 1;
            int wei7 = aa >> 1 & 1;
            int wei6 = aa >> 2 & 1;
            int wei5 = aa >> 3 & 1;
            int wei4 = aa >> 4 & 1;
            int wei3 = aa >> 5 & 1;
            int wei2 = aa >> 6 & 1;
            int wei1 = aa >> 7 & 1;
            cc++;
            int qq = wei1 * 2*2*2 + wei2 * 2*2 + wei3 * 2 + wei4 * 1;
            int qq2 = wei5 * 2*2*2 + wei6 * 2*2 + wei7 * 2+ wei8 * 1;
            listone.add(qq);
            listone.add(qq2);
            cc++;
        }
        int half = listone.size() / 2;
        for (int uu = 0; uu < listone.size() / 2; uu++) {
            int nb7 = listone.get(uu) % 2;
            int nb5 = (listone.get(uu) / 2) % 2;
            int nb3 = (listone.get(uu) / 4) % 2;
            int nb1 = (listone.get(uu) / 8) % 2;
            int nb8 = listone.get(half + uu) % 2;
            int nb6 = (listone.get(half + uu) / 2) % 2;
            int nb4 = (listone.get(half + uu) / 4) % 2;
            int nb2 = (listone.get(half + uu) / 8) % 2;
            int newnub = nb1 * 2*2*2*2*2*2*2 + nb2 * 2*2*2*2*2*2 + nb3 * 2*2*2*2*2 + nb4 * 2*2*2*2 + nb5 * 2*2*2 + nb6 * 2*2 + nb7 * 2 + nb8 * 1;
            listout.add(newnub);
        }
        byte[] out = Bytes.toArray(listout);
        return out;
    }
    //在byte数组中，查找byte数值
    public static int search(byte[] text, byte[] pattern) {
        int n = text.length;
        int m = pattern.length;
        int[] lps = prefixFunction(pattern);

        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && pattern[j] != text[i]) {
                j = lps[j - 1];
            }
            if (pattern[j] == text[i]) {
                j++;
            }
            if (j == m) {
                return i - m + 1; // pattern found
            }
        }
        return -1; // pattern not found
    }
    //byte查找的配套函数
    private static int[] prefixFunction(byte[] pattern) {
        int m = pattern.length;
        int[] lps = new int[m];
        int j = 0;
        for (int i = 1; i < m; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = lps[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            lps[i] = j;
        }
        return lps;
    }
    //str转list
    public static List<String> strtolist(String str){
        List<String> out=new ArrayList<>();
        String[] aaa = str.replace("[", "").replace("]", "").replace("\"","").replace("'","").replace(" ","").split(",");
        for (String a:aaa){out.add(a);}
        return out;}
    //byte数组转str
    public static String bytetostring(byte[] bytes){
        StringBuilder out= new StringBuilder("");
        for(byte aa:bytes){
        out.append(bytetostring(aa));
        out.append("，");}
        return out.toString();
    }
    //显示提示框
    public static void show_tip(String text,Context context) {
        AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
        msgbox.setTitle("提示");
        msgbox.setMessage(text);
        msgbox.setPositiveButton("知悉", null);
        msgbox.show();}
    /**
     * 生成128位随机二进制分隔符
     * @return 128位随机二进制分隔符的字节数组
     */
    public static byte[] generateRandomSeparator() {
        SecureRandom random = new SecureRandom();
        byte[] separator = new byte[16]; // 128位，16字节
        random.nextBytes(separator);
        return separator;}
    /**
     * 检查分隔符 byte[] 是否在文件 File 内有重复
     * @param byteCache 缓存文件对象
     * @param separator 分隔符字节数组
     * @return 如果分隔符在文件中存在，则返回 true；否则返回 false
     */
    public static boolean isSeparatorInFile(ByteCache byteCache, byte[] separator) throws IOException {
        try (FileInputStream fis = new FileInputStream(byteCache.getCacheFile())) {
            byte[] buffer = new byte[16];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (bytesRead == 16 && new BigInteger(1, buffer).equals(new BigInteger(1, separator))) {
                    return true;
                }}}
        return false;}
    /**
     * 将字节数组转换为十六进制字符串，便于查看
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    /**
     * 计算分隔符在文件内的数量
     * @param byteCache 缓存文件对象
     * @param separator 分隔符字节数组
     * @return 分隔符的数量
     * @throws IOException 文件读取错误
     */
    public static int countSeparatorInFile(ByteCache byteCache, byte[] separator) throws IOException {
        int separatorCount = 0;
        int separatorLength = separator.length;
        byte[] buffer = new byte[1024 + separatorLength]; // 缓冲区比正常大一些，以容纳分隔符
        byte[] lastBytes = new byte[separatorLength]; // 保存上次读取的最后几个字节
        int readBytes;

        try (FileInputStream fis = new FileInputStream(byteCache.getCacheFile())) {
            while ((readBytes = fis.read(buffer, 0, buffer.length - separatorLength)) != -1) {
                System.arraycopy(buffer, readBytes, lastBytes, 0, separatorLength); // 复制最后一部分到lastBytes
                for (int i = 0; i <= readBytes; i++) { // 遍历读取的数据
                    if (i + separatorLength <= readBytes + separatorLength &&
                            Arrays.equals(Arrays.copyOfRange(buffer, i, i + separatorLength), separator)) {
                        separatorCount++;
                    }
                }
                System.arraycopy(lastBytes, 0, buffer, 0, separatorLength); // 将lastBytes复制到buffer开头
            }
        }
        return separatorCount;
    }
    /**
     * 检查文件路径是否以 .notedata 结尾
     *
     * @param filePath 文件路径
     * @return 如果文件路径以 .notedata 结尾，返回 true；否则返回 false
     */
    public static boolean isnoteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return filePath.toLowerCase().endsWith(".notedata");
    }
}
