package com.fengxue.mynote;
import static com.fengxue.mynote.MainActivity.byte_decrypt;
import static com.fengxue.mynote.MainActivity.byte_encryption;
import static com.fengxue.mynote.MainActivity.bytesToHex;
import static com.fengxue.mynote.MainActivity.countSeparatorInFile;
import static com.fengxue.mynote.MainActivity.generateRandomSeparator;
import static com.fengxue.mynote.MainActivity.get_chinese_datatime;
import static com.fengxue.mynote.MainActivity.confuse;
import static com.fengxue.mynote.MainActivity.isnoteFile;
import static com.fengxue.mynote.MainActivity.search;
import static com.fengxue.mynote.MainActivity.show_tip;
import static com.fengxue.mynote.MainActivity.strtolist;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.json.JSONObject;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChooseActivity extends AppCompatActivity  {
    private SQLiteDatabase db;
    private SQLhelper dbHelper;
    private ListView listv;
    private int FILE_PICK=3;
    private FileOutputStream outs;
    private InputStream inputss;
    private Spinner spinner;
    private AlertDialog alertDialog;
    private ProgressBar progressBar;
    @Override//入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new SQLhelper(this);
        db = dbHelper.getWritableDatabase();
        List<List> cu = dbHelper.getdata("SELECT * FROM config WHERE NAME = '主题'", "config");
        if (cu.size()!=0){if (cu.get(0).get(6).equals("1")){setTheme(R.style.Them_green);}else {setTheme(R.style.Them_Yellow);}}else {setTheme(R.style.Them_green);}
        setContentView(R.layout.choose_ui);
        spinner=findViewById(R.id.setbt);
        //右上角设置按钮监听器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options, R.layout.msg_setting);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉菜单样式
        spinner.setAdapter(adapter);// 将适配器设置到Spinner上
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override//选中触发
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (position){
                    case 0:
                        spinner.setSelection(0);
                        break;
                    case 1://重置主密码
                        AlertDialog.Builder inputDialog2 = new AlertDialog.Builder(ChooseActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View view1 = inflater.inflate(R.layout.msg_update_password, null);
                        inputDialog2.setTitle("更改主密码");
                        inputDialog2.setView(view1);
                        EditText input1 = view1.findViewById(R.id.input33);
                        EditText input2 = view1.findViewById(R.id.input44);
                        inputDialog2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text= confuse(input1.getText().toString());
                                String text1= confuse(input2.getText().toString());
                                String sql="SELECT * FROM config WHERE NAME = '主密码'";
                                List<List> data=dbHelper.getdata(sql,"config");
                                String mima=String.valueOf(data.get(0).get(5));
                                if (mima.equals(text)){
                                    ContentValues cv = new ContentValues();
                                    cv.put("VALU4",text1);
                                    db.update("config", cv, "NAME = ?", new String[] {"主密码"});
                                    Toast.makeText(ChooseActivity.this, "主密码更改成功！" , Toast.LENGTH_SHORT).show();
                                    init();
                                }else {show_tip("主密码错误，请重试！",ChooseActivity.this);}}});
                        inputDialog2.show();
                        spinner.setSelection(0);
                        break;
                    case 2://重置笔记本
                        final EditText editText = new EditText(ChooseActivity.this);
                        AlertDialog.Builder inputDialog = new AlertDialog.Builder(ChooseActivity.this);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        inputDialog.setTitle("请确认是否重置笔记本，并输入主密码").setView(editText);
                        inputDialog.setPositiveButton("重置笔记本",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String sql2="SELECT * FROM config WHERE NAME = '主密码'";
                                        List<List> respon = dbHelper.getdata(sql2, "config");
                                        String mima = String.valueOf(respon.get(0).get(5));
                                        String text= confuse(editText.getText().toString());
                                        if (mima.equals(text)){
                                            try {
                                                File filesDir=getFilesDir();//删除数据库及files文件夹
                                                if (filesDir.isDirectory()) {
                                                    File[] files = filesDir.listFiles();
                                                    for (File file : files) {
                                                        file.delete();}}
                                            }catch (Exception e){e.printStackTrace();spinner.setSelection(0);}
                                            dbHelper.deleteDatabase(ChooseActivity.this);
                                            Toast.makeText(ChooseActivity.this, "重置成功！" , Toast.LENGTH_SHORT).show();
                                            restartApplication();
                                        }else {show_tip("密码错误，请重试！",ChooseActivity.this);}
                                    }}).show();
                        spinner.setSelection(0);
                        break;
                    case 3://导入数据
                        if (allfilepermission()) {
                            DialogProperties properties = new DialogProperties();
                            properties.selection_mode = DialogConfigs.SINGLE_MODE;//单双选模式
                            properties.selection_type = DialogConfigs.FILE_SELECT;//选择文件还是文件夹，或者all
                            //properties.root = new File(DialogConfigs.DEFAULT_DIR);
                            properties.root = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString());
                            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                            properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                            //如果要查看所有扩展名的文件，请将null传递给properties.extensions
                            //properties.extensions = null;
                            //如果要查看具有特定扩展名类型的文件，请将字符串数组传递到properties.extensions
                            properties.extensions = new String[]{"notedata"};
                            properties.show_hidden_files = false;
                            FilePickerDialog dialog = new FilePickerDialog(ChooseActivity.this, properties);
                            dialog.setTitle("note文件选择");
                            //选择监听
                            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                                @Override
                                public void onSelectedFilePaths(String[] files) {
                                    String filepathss = Arrays.stream(files).findFirst().get();
                                     /*new Thread(() ->{
                                        init_data(filepathss);
                                    }).start();*/
                                   new Thread(()->{
                                        new Handler(Looper.getMainLooper()).post(()->{creat_Progress("解包中...");});
                                        File file=new File(filepathss);
                                        ByteCache byteCache=new ByteCache(ChooseActivity.this,"diskget");
                                        byte[] bytes=new byte[1024];
                                        int len=0;
                                        try (FileInputStream fileInputStream=new FileInputStream(file);) {
                                            try (FileOutputStream fileOutputStream = byteCache.getByteOutputStream()) { // 'true' 表示追加模式
                                                while ((len = fileInputStream.read(bytes)) != -1) {
                                                    fileOutputStream.write(bytes, 0, len);}// 将读取的数据写入文件
                                            } catch (IOException e) {throw new RuntimeException(e);}
                                        } catch (IOException e) {throw new RuntimeException(e);}
                                        update_data(byteCache);
                                        }).start();
                        }});//设置监听
                        dialog.show();//导入选择器
                        }else {
                            show_tip("若要使用导入导出功能，请授予文件管理权限！",ChooseActivity.this);
                            //Toast.makeText(ChooseActivity.this,"若要使用导入导出，请授予文件管理权限！",Toast.LENGTH_LONG).show();
                            allfilepermission();}
                        spinner.setSelection(0);
                        break;
                    case 4://导出数据
                        //获取导出文件的密码
                        if (allfilepermission()){
                            final EditText editText55 = new EditText(ChooseActivity.this);
                            AlertDialog.Builder inputDialog55 = new AlertDialog.Builder(ChooseActivity.this);
                            editText55.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                            inputDialog55.setTitle("若要导出数据，请输入主密码").setView(editText55);
                            inputDialog55.setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String sql2="SELECT * FROM config WHERE NAME = '主密码'";
                                            List<List> respon = dbHelper.getdata(sql2, "config");
                                            String mima = String.valueOf(respon.get(0).get(5));
                                            String text= confuse(editText55.getText().toString());
                                            if (mima.equals(text)){
                                                DialogProperties properties = new DialogProperties();
                                                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                                                properties.selection_type = DialogConfigs.DIR_SELECT;
                                                //properties.root = new File(DialogConfigs.DEFAULT_DIR);
                                                properties.root = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString());
                                                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                                                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                                                //如果要查看所有扩展名的文件，请将null传递给properties.extensions
                                                properties.extensions = null;
                                                //如果要查看具有特定扩展名类型的文件，请将字符串数组传递到properties.extensions
                                                //properties.extensions = new String[]{"zip","jpg","mp3","csv"};
                                                properties.show_hidden_files = false;
                                                FilePickerDialog dialogs = new FilePickerDialog(ChooseActivity.this, properties);
                                                dialogs.setTitle("导出文件夹选择");
                                                //选择监听
                                                dialogs.setDialogSelectionListener(new DialogSelectionListener() {
                                                    @Override
                                                    public void onSelectedFilePaths(String[] files) {
                                                        String file_path=Arrays.stream(files).findFirst().get();
                                                        String name="笔记本数据备份"+get_chinese_datatime()+".notedata";
                                                        File file=new File(file_path,name);
                                                        try {
                                                            if (file.exists()) {file.delete();}
                                                            file.createNewFile();
                                                            new Thread(()->{
                                                                new Handler(Looper.getMainLooper()).post(()->{creat_Progress("打包中...");});
                                                                ByteCache byteCache=out_data();
                                                                if (!byteCache.getCachename().equals("erro")){
                                                                    new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                                    new Handler(Looper.getMainLooper()).post(()->{creat_Progress("导出中...");});
                                                                    byte[] bytes=new byte[1024];
                                                                    int len=0;
                                                                    try (InputStream inputStream = byteCache.getByteInputStream()) {
                                                                        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) { // 'true' 表示追加模式
                                                                            while ((len = inputStream.read(bytes)) != -1) {
                                                                                fileOutputStream.write(bytes, 0, len);}// 将读取的数据写入文件
                                                                        } catch (IOException e) {throw new RuntimeException(e);}
                                                                    } catch (IOException e) {throw new RuntimeException(e);}
                                                                    new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                                    new Handler(Looper.getMainLooper()).post(()->{show_tip("导出完成!",ChooseActivity.this);});
                                                                }else {
                                                                    byteCache.close();
                                                                    new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                                    new Handler(Looper.getMainLooper()).post(()->{show_tip("打包数据错误!",ChooseActivity.this);});}
                                                                byteCache.close();
                                                            }).start();
                                                        } catch (IOException e) {show_tip("文件创建错误，请检查是否授予文件权限!",ChooseActivity.this);}
                                                    }});
                                                dialogs.show();//文件选择窗口
                                            }else {show_tip("主密码错误，请重试!",ChooseActivity.this);spinner.setSelection(0);}
                                        }}).show();//确认主密码窗口
                        }else {
                            show_tip("若要使用导入导出功能，请授予文件管理权限！",ChooseActivity.this);
                            //Toast.makeText(ChooseActivity.this,"若要使用导入导出功能，请授予文件管理权限！",Toast.LENGTH_LONG).show();
                            allfilepermission();}
                        spinner.setSelection(0);
                        break;
                    case 5://切换主题
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChooseActivity.this);
                        builder.setTitle("切换主题");
                        builder.setSingleChoiceItems(new String[]{"绿色主题", "黄色主题"}, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 这里的'which'是被选中的单选按钮的索引，从0开始
                                if (which==0){
                                    db.execSQL("UPDATE config SET INTS="+1+" WHERE NAME='主题'");
                                    Toast.makeText(ChooseActivity.this,"已切换到绿色主题",Toast.LENGTH_SHORT).show();
                                    restartApplication();
                                } else if (which==1) {
                                    db.execSQL("UPDATE config SET INTS="+2+" WHERE NAME='主题'");
                                    Toast.makeText(ChooseActivity.this,"已切换到黄色主题",Toast.LENGTH_SHORT).show();
                                    restartApplication();
                                }else {}
                                 // dialog.dismiss();关闭对话框
                            }});
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        spinner.setSelection(0);
                        break;
                    case 6://设置WebDav
                        AlertDialog.Builder web_inputDialog = new AlertDialog.Builder(ChooseActivity.this);
                        LayoutInflater web_inflater = getLayoutInflater();
                        View web_view = web_inflater.inflate(R.layout.msg_webdav_setting, null);
                        web_inputDialog.setTitle("设置WebDav信息");
                        web_inputDialog.setView(web_view);
                        EditText web_input1 = web_view.findViewById(R.id.webdav_url);
                        EditText web_input2 = web_view.findViewById(R.id.webdav_user);
                        EditText web_input3 = web_view.findViewById(R.id.webdav_pass);
                        //更新数据
                        List<List> data = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAV'", "config");
                        if (!data.isEmpty()){
                            int logs=Integer.parseInt((String) data.get(0).get(6));
                            if (logs==1){
                                List<List> data2 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVURL'", "config");
                                List<List> data3 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVUSER'", "config");
                                List<List> data4 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVPASS'", "config");
                                if (data2.size()!=0){web_input1.setText((String)data2.get(0).get(2));}
                                if (data3.size()!=0){web_input2.setText((String)data3.get(0).get(2));}
                                if (data4.size()!=0){web_input3.setText((String)data4.get(0).get(2));}
                            }
                        }
                        web_inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (web_input1.getText().length()!=0 & web_input2.getText().length()!=0 &web_input3.getText().length()!=0){
                                    if (!data.isEmpty()){
                                        if (Integer.parseInt((String)data.get(0).get(6))==0){
                                            db.execSQL("UPDATE config SET INTS="+1+" WHERE NAME='WEBDAV'");
                                            db.execSQL("INSERT INTO config('NAME', 'VALU1') VALUES ('WEBDAVURL','"+web_input1.getText()+"')");
                                            db.execSQL("INSERT INTO config('NAME', 'VALU1') VALUES ('WEBDAVUSER','"+web_input2.getText()+"')");
                                            db.execSQL("INSERT INTO config('NAME', 'VALU1') VALUES ('WEBDAVPASS','"+web_input3.getText()+"')");
                                        } else if (Integer.parseInt((String)data.get(0).get(6))==1) {
                                            db.execSQL("UPDATE config SET INTS="+1+" WHERE NAME='WEBDAV'");
                                            db.execSQL("UPDATE config SET VALU1= '"+web_input1.getText()+"' WHERE NAME='WEBDAVURL'");
                                            db.execSQL("UPDATE config SET VALU1= '"+web_input1.getText()+"' WHERE NAME='WEBDAVUSER'");
                                            db.execSQL("UPDATE config SET VALU1= '"+web_input1.getText()+"' WHERE NAME='WEBDAVPASS'");
                                        }else {show_tip("设置错误!",ChooseActivity.this);}
                                        show_tip("已设置WebDav服务!",ChooseActivity.this);
                                    }else {show_tip("数据错误!",ChooseActivity.this);}
                                }else {show_tip("输入不可为空!",ChooseActivity.this);}
                            }});
                        web_inputDialog.show();
                        spinner.setSelection(0);
                        break;
                    case 7://WebDav备份
                        final EditText editText66 = new EditText(ChooseActivity.this);
                        AlertDialog.Builder inputDialog66 = new AlertDialog.Builder(ChooseActivity.this);
                        editText66.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        inputDialog66.setTitle("若要导出数据，请输入主密码").setView(editText66);
                        inputDialog66.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String sql2="SELECT * FROM config WHERE NAME = '主密码'";
                                        List<List> respon = dbHelper.getdata(sql2, "config");
                                        String mima = String.valueOf(respon.get(0).get(5));
                                        String text= confuse(editText66.getText().toString());
                                        if (mima.equals(text)){
                                            List<List> bfdata = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAV'", "config");
                                            if (!bfdata.isEmpty()){
                                                if (Integer.parseInt((String) bfdata.get(0).get(6))==1){
                                                    List<List> data2 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVURL'", "config");
                                                    List<List> data3 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVUSER'", "config");
                                                    List<List> data4 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVPASS'", "config");
                                                    String url=data2.size()!=0 ? (String) data2.get(0).get(2):null;
                                                    String user=data3.size()!=0 ? (String) data3.get(0).get(2):null;
                                                    String password=data4.size()!=0 ? (String) data4.get(0).get(2):null;
                                                    new Thread(()->{
                                                        new Handler(Looper.getMainLooper()).post(()->{creat_Progress("打包中...");});
                                                        WebDavHelper webDavHelper=new WebDavHelper(ChooseActivity.this,url,user,password);
                                                        //进展：现在要获取打包好的bytecache对象和文件
                                                        ByteCache byteCachedata= out_data();
                                                        if (!byteCachedata.getCachename().equals("erro")){
                                                            new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                            new Handler(Looper.getMainLooper()).post(()->{creat_Progress("上传中...");});
                                                            webDavHelper.put_file_data("笔记本数据备份"+ get_chinese_datatime()+".notedata",byteCachedata);
                                                            new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                            new Handler(Looper.getMainLooper()).post(()->{show_tip("上传完成!",ChooseActivity.this);});}
                                                        else {
                                                            byteCachedata.close();
                                                            new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                                                            new Handler(Looper.getMainLooper()).post(()->{show_tip("打包数据错误!",ChooseActivity.this);});
                                                        }
                                                        byteCachedata.close();
                                                    }).start();
                                                }else {show_tip("请先设置WebDav!",ChooseActivity.this);}
                                            }else {show_tip("请先设置WebDav!",ChooseActivity.this);}
                                        }else {show_tip("主密码错误，请重试！",ChooseActivity.this);}
                                    }});
                        inputDialog66.show();
                        spinner.setSelection(0);
                        break;
                    case 8://WebDav恢复
                        List<List> hfdata = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAV'", "config");
                        if (!hfdata.isEmpty()){
                            if (Integer.parseInt((String) hfdata.get(0).get(6))==1){
                                List<List> data2 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVURL'", "config");
                                List<List> data3 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVUSER'", "config");
                                List<List> data4 = dbHelper.getdata("SELECT * FROM config WHERE NAME = 'WEBDAVPASS'", "config");
                                String url=data2.size()!=0 ? (String) data2.get(0).get(2):null;
                                String user=data3.size()!=0 ? (String) data3.get(0).get(2):null;
                                String password=data4.size()!=0 ? (String) data4.get(0).get(2):null;
                                new Thread(() -> {
                                    WebDavHelper webDavHelper = new WebDavHelper(ChooseActivity.this,url, user, password);
                                    List<String> list = webDavHelper.get_web_list();
                                    new Handler(Looper.getMainLooper()).post(()->{show_web_list(list,url,user,password);});
                                }).start();
                            }else {show_tip("请先设置WebDav!",ChooseActivity.this);}
                        }else {show_tip("请先设置WebDav!",ChooseActivity.this);}
                        spinner.setSelection(0);
                        break;
                    default: spinner.setSelection(0);break;
                }}
            @Override// 无选项被选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {spinner.setSelection(0);}
        });
        Button addbt=findViewById(R.id.addnote);
        init();
        //添加笔记
        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {addnote();}});
    }
    //初始化
    public void init() {
        spinner.setSelection(0);
        String sql2="SELECT * FROM config WHERE NAME = '笔记本'";
        List<List> data=dbHelper.getdata(sql2,"config");
        if (data.size()>0){
            List<Noteitem> adpa=new ArrayList<>();
            for (int a=0;a<data.size();a++){
                String mima=null;
                if (data.get(a).get(5).toString().equals(confuse("".toString()))){mima="无密码";}else {mima="密码保护";}
                String name= (String) data.get(a).get(3);
                String date=(String) data.get(a).get(4);
                Noteitem item=new Noteitem();
                item.setMima(mima);
                item.setName(name);
                item.setDate(date);
                adpa.add(item);}
            listv=findViewById(R.id.noteview);
            ChooseAdapda myadapda=new ChooseAdapda(this,adpa);
            listv.setAdapter(myadapda);
            registerForContextMenu(listv);//给listview注册上下文菜单
            listv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //密码判断
                    Noteitem items =(Noteitem) parent.getItemAtPosition(position); // 获取点击的列表项数据对象
                    Noteitem itemStr = (Noteitem) listv.getItemAtPosition(position);
                    String date=items.getDate();
                    String sql2="SELECT * FROM config WHERE VALU3 = '"+date+"'";
                    List<List> respon = dbHelper.getdata(sql2, "config");
                    String mima = String.valueOf(respon.get(0).get(5));
                    if (mima.equals(confuse("".toString()))){
                        Intent intent=new Intent(ChooseActivity.this, NoteActivity.class);
                        intent.putExtra("notes",respon.get(0).get(2).toString());
                        intent.putExtra("names",respon.get(0).get(3).toString());
                        startActivity(intent);
                    }else {
                        final EditText editText = new EditText(ChooseActivity.this);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        AlertDialog.Builder inputDialog = new AlertDialog.Builder(ChooseActivity.this);
                        inputDialog.setTitle("请输入密码").setView(editText);
                        inputDialog.setPositiveButton("进入",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String sql2="SELECT * FROM config WHERE VALU3 = '"+date+"'";
                                        List<List> respon2 = dbHelper.getdata(sql2, "config");
                                        String mima2 = String.valueOf(respon2.get(0).get(5));
                                        String text= confuse(editText.getText().toString());
                                        if (mima2.equals(text)){
                                            Intent intent=new Intent(ChooseActivity.this, NoteActivity.class);
                                            intent.putExtra("notes",respon2.get(0).get(2).toString());
                                            intent.putExtra("names",respon2.get(0).get(3).toString());
                                            startActivity(intent);
                                        }else {Toast.makeText(ChooseActivity.this, "密码错误，请重试！" , Toast.LENGTH_SHORT).show();}
                                    }}).show();}
                }});//listview点击进入笔记
        }else {
            listv=findViewById(R.id.noteview);
            List<Noteitem> adpa=new ArrayList<>();
            ChooseAdapda myadapda=new ChooseAdapda(this,adpa);
            listv.setAdapter(myadapda);}
    }
    //添加笔记
    public void addnote(){
        //设置笔记名称
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(ChooseActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.msg_addnote, null);
        inputDialog.setTitle("添加笔记本");
        inputDialog.setView(view);
        EditText input1 = view.findViewById(R.id.input11);
        EditText input2 = view.findViewById(R.id.input22);
        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text=input1.getText().toString();
                        String text1= confuse(input2.getText().toString());
                        assert text!="";
                        assert text1!="";
                        int notmax=0;
                        try {
                        String sql="SELECT * FROM config WHERE NAME = '笔记本'";
                        List<List> notenumber=dbHelper.getdata(sql,"config");
                        for (List kk:notenumber){
                            int ss= Integer.parseInt(kk.get(2).toString().replace("NOTE",""));
                            if (ss>notmax){notmax=ss;}
                        }}catch (Exception ee){ee.printStackTrace();}
                        String date= get_chinese_datatime();
                        String currentTimeMillis = "NOTE"+String.valueOf(Calendar.getInstance().getTimeInMillis())+"—"+String.valueOf(db.getPath().length());
                        ContentValues cv = new ContentValues();
                        cv.put("NAME", "笔记本");
                        cv.put("VALU1", currentTimeMillis);
                        cv.put("VALU2",text);
                        cv.put("VALU3",date);
                        cv.put("VALU4",text1);
                        db.insert("config", null, cv);
                        Toast.makeText(ChooseActivity.this, "添加笔记成功！" , Toast.LENGTH_SHORT).show();
                        init();
                    }});
        inputDialog.show();
    }
    @Override//创建上下文菜单
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //根据View生成对应的菜单
        if (v == listv) {
            //设置菜单图标和标题
            menu.setHeaderIcon(R.mipmap.ic_launcher_round);
            //添加菜单项
            menu.add(0, 0, 0, "修改名称");
            menu.add(0, 1, 0, "修改密码");
            menu.add(0, 2, 0, "删除笔记");
        }
    }
    @Override//上下文菜单回调函数
    public boolean onContextItemSelected(MenuItem item){
        //关键代码
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Noteitem itemStr = (Noteitem) listv.getItemAtPosition(menuInfo.position);
        String date=itemStr.getDate();
        switch (item.getItemId()) {
            case 0://修改名称
                final EditText editText = new EditText(ChooseActivity.this);
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(ChooseActivity.this);
                inputDialog.setTitle("修改笔记名称").setView(editText);
                inputDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text=editText.getText().toString();
                                assert text!="";
                                ContentValues cv = new ContentValues();
                                cv.put("VALU2", text);
                                db.update("config", cv, "VALU3 = ?", new String[]{date});
                                Toast.makeText(ChooseActivity.this, "笔记名称修改成功！" , Toast.LENGTH_SHORT).show();
                                init();
                                //db.execSQL("UPDATE config SET VALU="+text+" WHERE ID='NOTE"+i+"'");
                            }}).show();
                break;
            case 1://修改密码
                AlertDialog.Builder inputDialog2 = new AlertDialog.Builder(ChooseActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.msg_update_password, null);
                inputDialog2.setTitle("更改密码");
                inputDialog2.setView(view);
                EditText input1 = view.findViewById(R.id.input33);
                EditText input2 = view.findViewById(R.id.input44);
                inputDialog2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text= confuse(input1.getText().toString());
                        String text1= confuse(input2.getText().toString());
                        String sql="SELECT * FROM config WHERE VALU3 = '"+date+"'";
                        List<List> data=dbHelper.getdata(sql,"config");
                        String mima=String.valueOf(data.get(0).get(5));
                        if (mima.equals(text)){
                            db.execSQL("UPDATE config SET VALU4='"+text1+"' WHERE VALU3='"+date+"'");
                            Toast.makeText(ChooseActivity.this, "密码更改成功！" , Toast.LENGTH_SHORT).show();
                            init();
                        }else {Toast.makeText(ChooseActivity.this, "密码错误，请重试！" , Toast.LENGTH_SHORT).show();}}});
                inputDialog2.show();
                break;
            case 2://删除笔记
                final EditText editText4 = new EditText(ChooseActivity.this);
                editText4.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                AlertDialog.Builder inputDialog4 = new AlertDialog.Builder(ChooseActivity.this);
                inputDialog4.setTitle("请输入笔记密码以删除").setView(editText4);
                inputDialog4.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text= confuse(editText4.getText().toString());
                                String sql="SELECT * FROM config WHERE VALU3 = '"+date+"'";
                                List<List> data=dbHelper.getdata(sql,"config");
                                String mima=String.valueOf(data.get(0).get(5));
                                String names=String.valueOf(data.get(0).get(2));
                                if (mima.equals(text)){
                                    try {
                                        List<List> notedata=dbHelper.getdata("SELECT * FROM note WHERE ID = '"+names+"'","note");
                                        for (List a:notedata){
                                            if (a.get(2).toString().equals("图片")){
                                                String imgpath = a.get(4).toString();
                                                File filepath = getFilesDir();
                                                File file = new File(filepath, imgpath);
                                                file.delete();}}
                                    db.execSQL("DELETE FROM note WHERE ID='"+names+"'");
                                    }catch (Exception e){e.printStackTrace();}
                                    db.execSQL("DELETE FROM config WHERE VALU3='"+date+"'");
                                    Toast.makeText(ChooseActivity.this, "删除笔记成功！" , Toast.LENGTH_SHORT).show();
                                    init();
                                }else {Toast.makeText(ChooseActivity.this, "密码错误，请重试！" , Toast.LENGTH_SHORT).show();}
                            }}).show();
                break;
        }
        return super.onContextItemSelected(item);
    }
    @Override//注销菜单
    protected void onDestroy() {
        super.onDestroy();
        //注销listView上的ContextMenu
        unregisterForContextMenu(listv);}
    //打开所有文件权限授予界面
    public boolean allfilepermission(){
        if (Environment.isExternalStorageManager()) {
            return true;
        } else {
            // 权限未被授予，需要申请
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
            return false;}
    }
    //重启程序
    private void restartApplication() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);}
    //获取数据库全部数据，以上传webdav
    public byte[] get_db_to_byte(){
        int outkeynumber = 0;
        List<List> notedata=dbHelper.getdata("SELECT * FROM config WHERE NAME = '笔记本'","config");
        List<List> notedb=dbHelper.getdata("SELECT * FROM note WHERE NUB >=1","note");
        JSONObject data = new JSONObject();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            if (notedata.size()>0){
                List<String> lis=new ArrayList<>();
                for (List a:notedata){
                    lis.add(a.get(1).toString());lis.add(a.get(2).toString());lis.add(a.get(3).toString());lis.add(a.get(4).toString());lis.add(a.get(5).toString());
                    data.put(String.valueOf(outkeynumber),lis.toString());
                    outkeynumber=outkeynumber+1;
                    lis.clear();}
                if (notedb.size() > 0) {for (List b:notedb){data.put(String.valueOf(outkeynumber),b.toString());outkeynumber=outkeynumber+1;}}
                //二进制加密文本数据
                byte[] datacy = byte_encryption(data.toString().getBytes("UTF-8"));//加密后的字符串
                //写入文本数据
                appendByteArray(byteArrayOutputStream,datacy);
                appendByteArray(byteArrayOutputStream,251);
                appendByteArray(byteArrayOutputStream,252);
                appendByteArray(byteArrayOutputStream,253);
                appendByteArray(byteArrayOutputStream,254);
                appendByteArray(byteArrayOutputStream,251);
                appendByteArray(byteArrayOutputStream,252);
                appendByteArray(byteArrayOutputStream,253);
                appendByteArray(byteArrayOutputStream,254);
                appendByteArray(byteArrayOutputStream,251);
                appendByteArray(byteArrayOutputStream,252);
                appendByteArray(byteArrayOutputStream,253);
                appendByteArray(byteArrayOutputStream,254);
                appendByteArray(byteArrayOutputStream,251);
                appendByteArray(byteArrayOutputStream,252);
                appendByteArray(byteArrayOutputStream,253);
                appendByteArray(byteArrayOutputStream,254);
                //遍历图片
                List<List> photolist=dbHelper.getdata("SELECT * FROM note WHERE TYP = '图片'","note");
                if (photolist.size()>0){
                    for (List c:photolist){
                        String img = (String) c.get(4);
                        if (img!=null & img!=""){
                            File filepath=getFilesDir();
                            String imgpath=filepath+"/"+img;
                            File pathtofile=new File(imgpath);
                            //遍历img数据
                            byte[] buf = new byte[1024];
                            int len = 0;
                            InputStream in = new FileInputStream(pathtofile);
                            int ces=0;
                            while( (len = in.read(buf)) != -1 ){
                                byte[] jiamihou= byte_encryption(buf);//二进制加密图片，以流的形式
                                appendByteArray(byteArrayOutputStream,jiamihou);}
                            appendByteArray(byteArrayOutputStream,251);
                            appendByteArray(byteArrayOutputStream,252);
                            appendByteArray(byteArrayOutputStream,253);
                            appendByteArray(byteArrayOutputStream,254);
                            appendByteArray(byteArrayOutputStream,251);
                            appendByteArray(byteArrayOutputStream,252);
                            appendByteArray(byteArrayOutputStream,253);
                            appendByteArray(byteArrayOutputStream,254);
                            appendByteArray(byteArrayOutputStream,251);
                            appendByteArray(byteArrayOutputStream,252);
                            appendByteArray(byteArrayOutputStream,253);
                            appendByteArray(byteArrayOutputStream,254);
                            appendByteArray(byteArrayOutputStream,251);
                            appendByteArray(byteArrayOutputStream,252);
                            appendByteArray(byteArrayOutputStream,253);
                            appendByteArray(byteArrayOutputStream,254);
                            in.close();}
                    }}
                return byteArrayOutputStream.toByteArray();
            }else {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ChooseActivity.this, "无数据！", Toast.LENGTH_SHORT).show());
                return new byte[]{ (byte)1, (byte)1, (byte)1, (byte) 1};}
        }catch (Exception ee){
            ee.printStackTrace();
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ChooseActivity.this, "导出错误！", Toast.LENGTH_SHORT).show());
            return new byte[]{ (byte)1, (byte)1, (byte)1, (byte) 1};}
    }
    //将webdav获取的数据，储存到数据库
    public void set_data_to_db(byte[] bytes){
        try {
            //获取数据库数据
            List<Integer>  jglist=new ArrayList<>();
            byte[] buf = new byte[1024];
            int len = 0;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            byte[] findbyte={(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254};
            int log=0;
            List<Integer> jieguolist=new ArrayList<>();
            while( (len = inputStream.read(buf)) != -1 ){
                int jieguo = search(buf, findbyte);
                if (jieguo!=-1){
                    jieguolist.add(jieguo+log);}
                log=log+1024;}
            byte[] buf2 = new byte[1000];
            len=0;
            log=0;
            inputStream = new ByteArrayInputStream(bytes);
            List<Integer> jieguolist2=new ArrayList<>();
            while( (len = inputStream.read(buf2)) != -1 ){
                int jieguo2 = search(buf2, findbyte);
                if (jieguo2!=-1){
                    jieguolist2.add(jieguo2+log);}
                log=log+1000;}
            //取两次校验分隔符，防止刚好被分隔符分割
            if (jieguolist.size()==0&jieguolist2.size()==0) {}else {
                inputStream = new ByteArrayInputStream(bytes);
                if (jieguolist.size() == jieguolist2.size()) {jglist.addAll(jieguolist);} else {if (jieguolist2.size() > jieguolist.size()) {jglist.addAll(jieguolist2);} else {jglist.addAll(jieguolist);}}
                String textvalue = "";
                List<String> imglist = new ArrayList<>();
                for (int hh = 0; hh < jglist.size(); hh++) {
                    if (hh == 0) {//第一个则直接读取，然后转换成数据
                        try {
                            inputStream = new ByteArrayInputStream(bytes);
                            int end = jglist.get(0);
                            byte[] bufs = new byte[end];
                            int respon = inputStream.read(bufs);
                            byte[] fil1 = byte_decrypt(bufs);
                            textvalue = new String(fil1, "UTF-8");
                            try {String regex = "[0-9]{4}年[0-9]{0,2}月[0-9]{0,2}日[0-9]{0,2}时[0-9]{0,2}分[0-9]{0,2}秒\\.jpg";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(textvalue);
                                while (matcher.find()) {imglist.add(matcher.group());}
                            }catch (Exception e){e.printStackTrace();}
                            JSONObject js = new JSONObject(textvalue);
                            for (int j=0;j<js.length();j++){
                                try {
                                    List<String> datalist=strtolist((String) js.get(String.valueOf(j)));
                                    if (datalist.get(0).toString().equals("笔记本")){//笔记本描述
                                        ContentValues cv = new ContentValues();
                                        cv.put("NAME", "笔记本");
                                        cv.put("VALU1", datalist.get(1));
                                        cv.put("VALU2", datalist.get(2));
                                        cv.put("VALU3", datalist.get(3));
                                        cv.put("VALU4", datalist.get(4));
                                        db.insert("config", null, cv);
                                    } else if (Integer.parseInt(datalist.get(0))>=0) {//内容
                                        ContentValues cv = new ContentValues();
                                        cv.put("ID", datalist.get(1));
                                        cv.put("TYP", datalist.get(2));
                                        cv.put("DAT", datalist.get(3));
                                        cv.put("CONT", datalist.get(4));
                                        db.insert("note", null, cv);
                                    }
                                }catch (Exception eee){eee.printStackTrace();}}
                        }catch (Exception ee){ee.printStackTrace();}
                    } else {//中间的是图片
                        if (textvalue != null) {
                            try {
                                inputStream = new ByteArrayInputStream(bytes);
                                if (imglist.size()!= 0 &imglist.size()+1==jglist.size()){
                                    int imgstar = jglist.get(hh - 1) + 16;
                                    int imgend = jglist.get(hh);
                                    File privateDirectory = getFilesDir();
                                    String imgpt = imglist.get(hh - 1);
                                    File imgfile = new File(privateDirectory, imgpt);
                                    if (imgfile.exists()) {
                                    } else {try {
                                            imgfile.createNewFile();
                                            outs = new FileOutputStream(imgfile);
                                            inputStream.skip(imgstar);// 跳过指定开始位置前的字节
                                            byte[] readbyte = new byte[1024];
                                            for (int pis = imgstar; pis <= imgend; pis = pis + 1024) {
                                                int result = inputStream.read(readbyte); // 读取指定长度的字节数组
                                                byte[] one = byte_decrypt(readbyte);
                                                outs.write(one);}
                                            outs.close();
                                        } catch (Exception eee) {eee.printStackTrace();}}
                                }else {new Handler(Looper.getMainLooper()).post(()->{show_tip("图片数量和数据不对!",ChooseActivity.this);});}
                                inputStream.close();
                            }catch (Exception e ){e.printStackTrace();}
                        } else {new Handler(Looper.getMainLooper()).post(()->{Toast.makeText(ChooseActivity.this, "导入完成，但数据无图片！", Toast.LENGTH_SHORT).show();});}}}
                new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                new Handler(Looper.getMainLooper()).post(()->{ show_tip("导入完成!",ChooseActivity.this);});
                new Handler(Looper.getMainLooper()).post(()->{init();});}
        }catch (Exception e){
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                new Handler(Looper.getMainLooper()).post(()->{init();});
                new Handler(Looper.getMainLooper()).post(()->{Toast.makeText(ChooseActivity.this, "导入错误，请确认文件是否正确！", Toast.LENGTH_SHORT).show();});}
    }
    //显示webdav数据列表
    public void show_web_list(List<String>list,String url,String user,String password){
        if (!list.isEmpty()){
            String[] items=list.toArray(new String[0]);
            AlertDialog.Builder list_builder = new AlertDialog.Builder(ChooseActivity.this);
            list_builder.setTitle("请选择要恢复的文件");
            list_builder.setItems(items, (DialogInterface.OnClickListener) (dialog12, which) -> {
                Toast.makeText(ChooseActivity.this, "选择了: " + items[which], Toast.LENGTH_SHORT).show();
                String choosename=items[which];
                if (isnoteFile(choosename)){
                new Thread(()->{
                    WebDavHelper webDavHelper = new WebDavHelper(ChooseActivity.this,url, user, password);
                    new Handler(Looper.getMainLooper()).post(()->{creat_Progress("下载中...");});
                    ByteCache byteCachedown=webDavHelper.get_file_data(choosename);
                    new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                    new Handler(Looper.getMainLooper()).post(()->{creat_Progress("解包中...");});
                    update_data(byteCachedown);
                }).start();
                }else {new Handler(Looper.getMainLooper()).post(()->{show_tip("文件类型错误!",ChooseActivity.this);});}
            });
            list_builder.setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss());
            AlertDialog list_dialog = list_builder.create();
            list_dialog.show();
        }else {show_tip("WebDav备份文件为空!",ChooseActivity.this);}
    }
    //追加byte数组
    public static void appendByteArray(ByteArrayOutputStream stream, byte[] data) {
        try {stream.write(data, 0, data.length);} catch (Exception e) {e.printStackTrace();}}
    public static void appendByteArray(ByteArrayOutputStream stream, int value) {
        try {stream.write((byte) value);} catch (Exception e) {e.printStackTrace();}}
    //创建进度条
    public void creat_Progress(String text){
        // 创建一个自定义的布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setGravity(Gravity.CENTER);  // 设置布局的重力为居中
        // 设置进度条
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(progressBar);
        // 添加文本说明
        TextView message = new TextView(this);
        message.setText(text);
        message.setGravity(Gravity.CENTER);
        message.setTextSize(24); // 设置字体大小
        layout.addView(message);
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(text);
        ProgressBar progressBar = new ProgressBar(this);
        builder.setView(progressBar);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();*/
    }
    //关闭进度条
    public void close_Progress(){if (alertDialog.isShowing()){alertDialog.dismiss();}}
    /**
     * 导入数据到指定文件。
     *
     * @param filepathss 文件的路径
     */
    public void init_data(String filepathss){
        new Handler(Looper.getMainLooper()).post(()->{creat_Progress("导入中...");});
        try {
            //File sharedDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);  //DOWNLOAD目录
            //获取数据库数据
            List<Integer>  jglist=new ArrayList<>();
            byte[] buf = new byte[1024];
            int len = 0;
            InputStream in = new FileInputStream(filepathss);
            byte[] findbyte={(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254,(byte) 251,(byte)252,(byte)253,(byte)254};
            int log=0;
            List<Integer> jieguolist=new ArrayList<>();
            while( (len = in.read(buf)) != -1 ){
                int jieguo = search(buf, findbyte);
                if (jieguo!=-1){
                    jieguolist.add(jieguo+log);}
                log=log+1024;}
            in.close();
            byte[] buf2 = new byte[1000];
            len=0;
            log=0;
            InputStream in2 = new FileInputStream(filepathss);
            List<Integer> jieguolist2=new ArrayList<>();
            while( (len = in2.read(buf2)) != -1 ){
                int jieguo2 = search(buf2, findbyte);
                if (jieguo2!=-1){
                    jieguolist2.add(jieguo2+log);}
                log=log+1000;}
            in2.close();
            //取两次校验分隔符，防止刚好被分隔符分割
            if (jieguolist.size()==0&jieguolist2.size()==0) {}else {
                jglist.add(77953);
                jglist.add(2528401);
                jglist.add(2646177);
                jglist.add(5642417);
                jglist.add(7202017);
                String textvalue = "";
                String st= "1:"+String.valueOf(jieguolist.size())+" 2:"+String.valueOf(jieguolist2.size())+" 3:"+String.valueOf(jglist.size());
                List<String> imglist = new ArrayList<>();
                for (int hh = 0; hh < jglist.size(); hh++) {
                    if (hh == 0) {//第一个则直接读取，然后转换成数据
                        try {
                            InputStream inputs = new FileInputStream(filepathss);
                            int end = jglist.get(0);
                            byte[] bufs = new byte[end];
                            int respon = inputs.read(bufs);
                            byte[] fil1 = byte_decrypt(bufs);
                            textvalue = new String(fil1, "UTF-8");
                            try {
                                String regex = "[0-9]{4}年[0-9]{0,2}月[0-9]{0,2}日[0-9]{0,2}时[0-9]{0,2}分[0-9]{0,2}秒\\.jpg";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(textvalue);
                                while (matcher.find()) {imglist.add(matcher.group());}
                            }catch (Exception e){e.printStackTrace();}
                            JSONObject js = new JSONObject(textvalue);
                            inputs.close();
                            for (int j=0;j<js.length();j++){
                                try {
                                    List<String> datalist=strtolist((String) js.get(String.valueOf(j)));
                                    if (datalist.get(0).toString().equals("笔记本")){//笔记本描述
                                        ContentValues cv = new ContentValues();
                                        cv.put("NAME", "笔记本");
                                        cv.put("VALU1", datalist.get(1));
                                        cv.put("VALU2", datalist.get(2));
                                        cv.put("VALU3", datalist.get(3));
                                        cv.put("VALU4", datalist.get(4));
                                        db.insert("config", null, cv);
                                    } else if (Integer.parseInt(datalist.get(0))>=0) {//内容
                                        ContentValues cv = new ContentValues();
                                        cv.put("ID", datalist.get(1));
                                        cv.put("TYP", datalist.get(2));
                                        cv.put("DAT", datalist.get(3));
                                        cv.put("CONT", datalist.get(4));
                                        db.insert("note", null, cv);
                                    }
                                }catch (Exception eee){eee.printStackTrace();}}
                        }catch (Exception ee){ee.printStackTrace();}
                    } else {//中间的是图片
                        if (textvalue != null) {
                            try {
                                inputss = new FileInputStream(filepathss);
                                int imgstar = jglist.get(hh - 1) + 16;
                                int imgend = jglist.get(hh);
                                File privateDirectory = getFilesDir();
                                String imgpt = imglist.get(hh - 1);
                                File imgfile = new File(privateDirectory, imgpt);
                                if (imgfile.exists()) {} else {
                                    try {
                                        imgfile.createNewFile();
                                        outs = new FileOutputStream(imgfile);
                                        byte[] readbyte=new byte[1024];
                                        inputss.skip(imgstar);// 跳过指定开始位置前的字节
                                        for(int pis = imgstar; pis<=imgend; pis=pis+1024){
                                            int result = inputss.read(readbyte); // 读取指定长度的字节数组
                                            byte[] one = byte_decrypt(readbyte);
                                            outs.write(one);}
                                        outs.close();
                                    }catch (Exception eee){eee.printStackTrace();}}
                                inputss.close();
                            }catch (Exception e ){e.printStackTrace();}
                        } else {
                            new Handler(Looper.getMainLooper()).post(()->{show_tip("无图片!",ChooseActivity.this);});
                        }}
                }
                new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                new Handler(Looper.getMainLooper()).post(()->{show_tip("导入完成!",ChooseActivity.this);});
                new Handler(Looper.getMainLooper()).post(()->{init();});
                }
        }catch (Exception e){
            new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
            new Handler(Looper.getMainLooper()).post(()->{show_tip("导入错误，请确认文件是否正确!",ChooseActivity.this);});
            new Handler(Looper.getMainLooper()).post(()->{init();});
            }
    }
    //webdav导出到缓存文件
    public ByteCache out_data(){
        byte[] separator = generateRandomSeparator();
        int separator_cycle=0;
        ByteCache byteCache=new ByteCache(ChooseActivity.this,"dataout");
        try{
        int outkeynumber = 0;
        List<List> notedata=dbHelper.getdata("SELECT * FROM config WHERE NAME = '笔记本'","config");
        List<List> notedb=dbHelper.getdata("SELECT * FROM note WHERE NUB >=1","note");
        JSONObject data = new JSONObject();
        byteCache.addBytes(byte_encryption(separator));
        if (notedata.size()>0){
            List<String> lis=new ArrayList<>();
            for (List a:notedata){
                lis.add(a.get(1).toString());lis.add(a.get(2).toString());lis.add(a.get(3).toString());lis.add(a.get(4).toString());lis.add(a.get(5).toString());
                data.put(String.valueOf(outkeynumber),lis.toString());
                outkeynumber=outkeynumber+1;
                lis.clear();}
            if (notedb.size() > 0) {for (List b:notedb){data.put(String.valueOf(outkeynumber),b.toString());outkeynumber=outkeynumber+1;}}
            //二进制加密文本数据
            byte[] datacy = byte_encryption(data.toString().getBytes("UTF-8"));//加密后的字符串
            //写入文本数据
            byteCache.addBytes(datacy);
            byteCache.addBytes(separator);
            separator_cycle+=1;
            //遍历图片
            List<List> photolist=dbHelper.getdata("SELECT * FROM note WHERE TYP = '图片'","note");
            if (photolist.size()>0){
                for (List c:photolist){
                    String img = (String) c.get(4);
                    if (img!=null & img!=""){
                        File filepath=getFilesDir();
                        String imgpath=filepath+"/"+img;
                        File pathtofile=new File(imgpath);
                        //遍历img数据
                        byte[] buf = new byte[1024];
                        int len = 0;
                        InputStream in = new FileInputStream(pathtofile);
                        int ces=0;
                        while( (len = in.read(buf)) != -1 ){
                            byte[] jiamihou= byte_encryption(buf);//二进制加密图片，以流的形式
                            byteCache.addBytes(jiamihou);}
                        byteCache.addBytes(separator);
                        separator_cycle+=1;
                        in.close();}
                }}
            int searchseparator = countSeparatorInFile(byteCache, separator);
            if (searchseparator==separator_cycle){
                return byteCache;
            }else {
                byteCache.close();
                out_data();}
        }else {
            byteCache.close();
            ByteCache byteCache2=new ByteCache(ChooseActivity.this,"erro");
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ChooseActivity.this, "无数据！", Toast.LENGTH_SHORT).show());
            return byteCache2;}
        }catch (Exception ee){
            ee.printStackTrace();}
            byteCache.close();
            ByteCache byteCache2=new ByteCache(ChooseActivity.this,"erro");
            return byteCache2;
    }
    //将webdav获取的数据解包，储存到数据库
    public void update_data(ByteCache byteCache){
        try (FileInputStream inputStream = byteCache.getByteInputStream()){
            //获取数据库数据
            //获取分隔符
            byte[] separator=new byte[16];
            inputStream.read(separator);
            byte[] findbyte=byte_decrypt(separator);
            inputStream.getChannel().position(0);
            // 滑动窗口读取文件
            List<Integer> jieguolist = new ArrayList<>();
            int log = 0;
            byte[] buffer = new byte[1024 + findbyte.length]; // 缓冲区比正常大一些，以容纳分隔符
            byte[] lastBytes = new byte[findbyte.length]; // 保存上次读取的最后几个字节
            int readBytes;
            while ((readBytes = inputStream.read(buffer, 0, buffer.length - findbyte.length)) != -1) {
                System.arraycopy(buffer, readBytes, lastBytes, 0, findbyte.length); // 复制最后一部分到lastBytes
                for (int i = 0; i <= readBytes; i++) { // 遍历读取的数据
                    if (i + findbyte.length <= readBytes + findbyte.length &&
                            Arrays.equals(Arrays.copyOfRange(buffer, i, i + findbyte.length), findbyte)) {
                        jieguolist.add(i + log);
                    }
                }
                System.arraycopy(lastBytes, 0, buffer, 0, findbyte.length); // 将lastBytes复制到buffer开头
                log += 1024;
            }
            if (jieguolist.size()!=0) {
                String textvalue = "";
                List<String> imglist = new ArrayList<>();
                for (int hh = 0; hh < jieguolist.size(); hh++) {
                    if (hh == 0) {//第一个则直接读取，然后转换成数据
                        try {
                            inputStream.getChannel().position(0);
                            int end = jieguolist.get(0);
                            byte[] bufs = new byte[end-16];
                            inputStream.skip(16);
                            int respon = inputStream.read(bufs);
                            byte[] fil1 = byte_decrypt(bufs);
                            textvalue = new String(fil1, "UTF-8");
                            try {String regex = "[0-9]{4}年[0-9]{0,2}月[0-9]{0,2}日[0-9]{0,2}时[0-9]{0,2}分[0-9]{0,2}秒\\.jpg";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(textvalue);
                                while (matcher.find()) {imglist.add(matcher.group());}
                            }catch (Exception e){e.printStackTrace();}
                            JSONObject js = new JSONObject(textvalue);
                            for (int j=0;j<js.length();j++){
                                try {
                                    List<String> datalist=strtolist((String) js.get(String.valueOf(j)));
                                    if (datalist.get(0).toString().equals("笔记本")){//笔记本描述
                                        ContentValues cv = new ContentValues();
                                        cv.put("NAME", "笔记本");
                                        cv.put("VALU1", datalist.get(1));
                                        cv.put("VALU2", datalist.get(2));
                                        cv.put("VALU3", datalist.get(3));
                                        cv.put("VALU4", datalist.get(4));
                                        db.insert("config", null, cv);
                                    } else if (Integer.parseInt(datalist.get(0))>=0) {//内容
                                        ContentValues cv = new ContentValues();
                                        cv.put("ID", datalist.get(1));
                                        cv.put("TYP", datalist.get(2));
                                        cv.put("DAT", datalist.get(3));
                                        cv.put("CONT", datalist.get(4));
                                        db.insert("note", null, cv);
                                    }
                                }catch (Exception eee){eee.printStackTrace();}}
                        }catch (Exception ee){ee.printStackTrace();}
                    } else {//中间的是图片
                        if (textvalue != null) {
                            try {
                                inputStream.getChannel().position(0);
                                if (imglist.size()!= 0 &imglist.size()+1==jieguolist.size()){
                                    int imgstar = jieguolist.get(hh - 1) + 16;
                                    int imgend = jieguolist.get(hh);
                                    File privateDirectory = getFilesDir();
                                    String imgpt = imglist.get(hh - 1);
                                    File imgfile = new File(privateDirectory, imgpt);
                                    if (imgfile.exists()) {
                                    } else {try {
                                        imgfile.createNewFile();
                                        outs = new FileOutputStream(imgfile);
                                        inputStream.skip(imgstar);// 跳过指定开始位置前的字节
                                        byte[] readbyte = new byte[1024];
                                        for (int pis = imgstar; pis <= imgend; pis = pis + 1024) {
                                            int result = inputStream.read(readbyte); // 读取指定长度的字节数组
                                            byte[] one = byte_decrypt(readbyte);
                                            outs.write(one);}
                                        outs.close();
                                    } catch (Exception eee) {eee.printStackTrace();}}
                                }else {new Handler(Looper.getMainLooper()).post(()->{show_tip("图片数量和数据不对!",ChooseActivity.this);});}
                            }catch (Exception e ){e.printStackTrace();}
                        } else {new Handler(Looper.getMainLooper()).post(()->{Toast.makeText(ChooseActivity.this, "导入完成，但无图片！", Toast.LENGTH_SHORT).show();});}}}
                new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
                new Handler(Looper.getMainLooper()).post(()->{ show_tip("导入完成!",ChooseActivity.this);});
                new Handler(Looper.getMainLooper()).post(()->{init();});}
        }catch (Exception e){
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(()->{close_Progress();});
            new Handler(Looper.getMainLooper()).post(()->{init();});
            new Handler(Looper.getMainLooper()).post(()->{Toast.makeText(ChooseActivity.this, "导入错误，请确认文件是否正确！", Toast.LENGTH_SHORT).show();});}
        byteCache.close();
    }
}


class Noteitem{
    private String name;
    private String date;
    private String mima;
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}
    public String getMima() {return mima;}
    public void setMima(String mima) {this.mima = mima;}
}
