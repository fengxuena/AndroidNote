package com.fengxue.mynote;
import static com.fengxue.mynote.MainActivity.byte_decrypt;
import static com.fengxue.mynote.MainActivity.byte_encryption;
import static com.fengxue.mynote.MainActivity.datetime_to_chinese;
import static com.fengxue.mynote.MainActivity.get_datatime_string;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteActivity extends AppCompatActivity {
    private ListView listview;
    private String nownote;
    private SQLhelper dbHelper;
    private SQLiteDatabase db;
    private int TAKE_PHOTO = 2;
    private int GET_PHOTO = 1;
    private String photodate;
    private String note_names;
    private EditText contenteditxt;
    private boolean photochoose=false;
    private static final long DELAY_MILLIS = 10000;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {delayedshutdown();}};
    private boolean searchlog=false;
    @Override//入口
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new SQLhelper(this);
        db = dbHelper.getWritableDatabase();
        List<List> cu = dbHelper.getdata("SELECT * FROM config WHERE NAME = '主题'", "config");
        if (cu.size()!=0){if (cu.get(0).get(6).equals("1")){setTheme(R.style.Them_green);}else {setTheme(R.style.Them_Yellow);}}else {setTheme(R.style.Them_green);}
        setContentView(R.layout.note_ui);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Permission permission = new Permission();
        permission.checkPermissions(this);
        nownote = getIntent().getStringExtra("notes").replace(" ", "");//得到是那个笔记本，即note几
        note_names = getIntent().getStringExtra("names").replace(" ", "");
        TextView name = findViewById(R.id.notename);
        name.setText(note_names);
        Button postbt = findViewById(R.id.post_button);
        Button takephotobt = findViewById(R.id.takephoto_button);
        Button postphotobt = findViewById(R.id.postphoto_button);
        contenteditxt = findViewById(R.id.editTextTextMultiLine);
        Button searchbt=findViewById(R.id.search_buttons);
        searchbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchlog==true){
                    initdata();
                    searchlog=false;
                    searchbt.setText("搜索记录");
                }else {
                final EditText editText = new EditText(NoteActivity.this);
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(NoteActivity.this);
                inputDialog.setTitle("查找").setView(editText);
                inputDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取输入法管理器
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                // 如果输入法在显示状态，那么就隐藏输入法，反之则显示输入法
                                if (imm.isActive()) {imm.hideSoftInputFromWindow(contenteditxt.getWindowToken(), 0);}
                                String sql = "SELECT * FROM note WHERE ID = '" + nownote + "'";
                                List<List> data = dbHelper.getdata(sql, "note");
                                List<Searchitem> itlist = new ArrayList<Searchitem>();
                                if (data.size() >= 1) {
                                    for (int a = 0; a < data.size(); a++) {
                                        Searchitem itm = new Searchitem();
                                        SpannableString title = new SpannableString((String) data.get(a).get(3));
                                        itm.setTitles(title);
                                        if (data.get(a).get(2).equals("文本")) {
                                            try {
                                                String contbyte=(String) data.get(a).get(4);
                                                byte[] byteArray = Base64.getDecoder().decode(contbyte);
                                                byte[] dec = byte_decrypt(byteArray);
                                                String conttext = new String(dec, "UTF-8");
                                                SpannableString conts = highlightSearchKeyword(conttext, editText.getText().toString());
                                                itm.setContents(conts);
                                                itm.setImages(null);
                                            }catch (Exception e){e.printStackTrace();itm.setContents(null);itm.setImages(null);}
                                        } else {
                                            itm.setImages((String) data.get(a).get(4));
                                            itm.setContents(null);
                                        }
                                        itlist.add(itm);
                                    }
                                    listview = findViewById(R.id.mylistView);
                                    SearchAdapter adap = new SearchAdapter(NoteActivity.this, itlist);
                                    listview.setAdapter(adap);
                                    listview.setSelection(adap.getCount() - 1);
                                    InputMethodManager imms = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imms.isActive()) {imms.hideSoftInputFromWindow(contenteditxt.getWindowToken(), 0);}
                                    registerForContextMenu(listview);//给listview注册上下文菜单
                                    searchlog=true;
                                    searchbt.setText("关闭搜索");
                                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            // 在这里处理点击事件，根据需要执行相应的操作
                                            Searchitem items = (Searchitem) parent.getItemAtPosition(position); // 获取点击的列表项数据对象
                                            if (items.getImages() != null) {
                                                Intent intent = new Intent(NoteActivity.this, NoteImageView.class);
                                                intent.putExtra("img", items.getImages());
                                                intent.putExtra("notes", nownote);
                                                intent.putExtra("names", note_names);
                                                startActivity(intent);
                                                photochoose = true;}}
                                    });
                                }else{
                                    //没有数据的时候，直接是空的列表
                                    listview = findViewById(R.id.mylistView);
                                    SearchAdapter adap = new SearchAdapter(NoteActivity.this, itlist);
                                    listview.setAdapter(adap);
                                    InputMethodManager imm3 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm3.isActive()) {imm3.hideSoftInputFromWindow(contenteditxt.getWindowToken(), 0);}}
                                }}).show();
                }}});
        initdata();
        postbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.post_button) {
                    postword();
                }
            }
        });
        takephotobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.takephoto_button) {
                    takephoto();
                }
            }
        });
        postphotobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.postphoto_button) {
                    postphoto();
                }
            }
        });

    }
    //初始化数据
    public void initdata() {
        // 获取输入法管理器
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 如果输入法在显示状态，那么就隐藏输入法，反之则显示输入法
        if (imm.isActive()) {imm.hideSoftInputFromWindow(contenteditxt.getWindowToken(), 0);}
        String sql = "SELECT * FROM note WHERE ID = '" + nownote + "'";
        List<List> data = dbHelper.getdata(sql, "note");
        List<DataItem> itlist = new ArrayList<DataItem>();
        if (data.size() >= 1) {
            if (data.get(0).size() >= 1) {
                for (int a = 0; a < data.size(); a++) {
                    DataItem itm = new DataItem();
                    itm.setTitles((String) data.get(a).get(3));
                    String contbyte=(String) data.get(a).get(4);
                    if (data.get(a).get(2).equals("文本")) {
                        try {
                            byte[] byteArray = Base64.getDecoder().decode(contbyte);
                            byte[] dec= byte_decrypt(byteArray);
                            String conttext = new String(dec, "UTF-8");
                            itm.setContents(conttext);
                            itm.setImages(null);}catch (Exception e){e.printStackTrace();itm.setContents(null);itm.setImages(null);}
                    } else {
                        itm.setImages(contbyte);
                        itm.setContents(null);
                    }
                    itlist.add(itm);
                }
                listview = findViewById(R.id.mylistView);
                NoteAdapter adap = new NoteAdapter(NoteActivity.this, itlist);
                listview.setAdapter(adap);
                listview.setSelection(adap.getCount() - 1);
                registerForContextMenu(listview);//给listview注册上下文菜单
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // 在这里处理点击事件，根据需要执行相应的操作
                        DataItem items = (DataItem) parent.getItemAtPosition(position); // 获取点击的列表项数据对象
                        if (items.getImages() != null) {
                            Intent intent = new Intent(NoteActivity.this, NoteImageView.class);
                            intent.putExtra("img", items.getImages());
                            intent.putExtra("notes", nownote);
                            intent.putExtra("names", note_names);
                            startActivityForResult(intent,10);
                            photochoose=true;
                        }
                    }
                });
            } else {//没有数据的时候，直接是空的列表
                listview = findViewById(R.id.mylistView);
                NoteAdapter adap = new NoteAdapter(NoteActivity.this, itlist);
                listview.setAdapter(adap);
            }
        }

    }
    //记录文字
    public void postword(){
        try {
        String cont = contenteditxt.getText().toString();
        byte[] edc= byte_encryption(cont.getBytes("UTF-8"));
        String base64String = Base64.getEncoder().encodeToString(edc);
        ContentValues cv = new ContentValues();
        cv.put("ID", nownote);
        cv.put("TYP", "文本");
        cv.put("DAT", get_datatime_string());
        cv.put("CONT", base64String);
        db.insert("note", null, cv);
        contenteditxt.setText("");
        initdata();}catch (Exception e){e.printStackTrace();contenteditxt.setText("");}
    }
    //拍照记录
    public void takephoto() {
        // 创建保存照片的文件
        String date = get_datatime_string();
        photodate = date;
        String imgname = datetime_to_chinese(date) + ".jpg";
        File photoFile = new File(this.getFilesDir(), imgname);
        try {
            if (photoFile.exists()) {
                photoFile.delete();
            }
            photoFile.createNewFile();
            // 获取文件的Uri
            Uri photoUri = FileProvider.getUriForFile(NoteActivity.this, "com.fengxue.mynote.provider", photoFile);
            // 创建Intent
            photochoose=true;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 90);
            // 启动相机应用
            //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {//不能用这个，一用就注定打开不了
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
            //take_photo_save_date=date;
            //System.out.println("启动相机");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //记录图片
    public void postphoto() {
        photochoose=true;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GET_PHOTO);
    }
    @Override//获取回调(拍照回调/选择图片回调）
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photochoose=false;
        //图片选择回调
        if (requestCode == GET_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // 获取图片路径
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                //复制到file文件夹去
                String date = get_datatime_string();
                File privateDirectory = this.getFilesDir();
                // newpath=privateDirectory+"/"+date+".jpg";
                try {
                    File yuanfile = new File(picturePath);
                    File tofile = new File(privateDirectory, datetime_to_chinese(date) + ".jpg");
                    tofile.createNewFile();
                    InputStream fosfrom = new FileInputStream(yuanfile);
                    OutputStream fosto = new FileOutputStream(tofile);
                    byte bt[] = new byte[1024];
                    int c;
                    while ((c = fosfrom.read(bt)) > 0) {
                        fosto.write(bt, 0, c);
                    }
                    fosfrom.close();
                    fosto.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //储存到数据库
                ContentValues cv = new ContentValues();
                cv.put("ID", nownote);
                cv.put("TYP", "图片");
                cv.put("DAT", date);
                cv.put("CONT", datetime_to_chinese(date) + ".jpg");
                db.insert("note", null, cv);
                initdata(); //更新数据
            }
            //拍照回调
        } else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            if (photodate != null) {
                ContentValues cv = new ContentValues();
                cv.put("ID", nownote);
                cv.put("TYP", "图片");
                cv.put("DAT", photodate);
                cv.put("CONT", datetime_to_chinese(photodate) + ".jpg");
                db.insert("note", null, cv);
                initdata(); //更新数据
                photodate = null;
                initdata(); //更新数据
            }
        }else if (requestCode == 10 && resultCode == RESULT_OK) {//图片窗口被销毁的回调
            if (data != null) {
                String callbackData = data.getStringExtra("callback");
                if (callbackData.equals("0")){
                    finish();}}}
        else {}
    }
    @Override//创建上下文菜单
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //根据View生成对应的菜单
        if (v == listview) {
            //设置菜单图标和标题
            menu.setHeaderIcon(R.mipmap.ic_launcher_round);
            //添加菜单项
            menu.add(0, 0, 0, "修改");
            menu.add(0, 1, 0, "删除");
            menu.add(0, 2, 0, "复制");
        }
    }
    @Override//上下文菜单回调函数
    public boolean onContextItemSelected(MenuItem item) {
        //关键代码
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DataItem itemStr = (DataItem) listview.getItemAtPosition(menuInfo.position);
        String date = itemStr.getTitles();
        String sql = "SELECT * FROM note WHERE DAT = '" + date + "'";
        List<List> datas = dbHelper.getdata(sql, "note");
        switch (item.getItemId()) {
            case 0://修改，统一改成文本格式
                if (searchlog==true){}else {
                String oldtext;
                if (datas.get(0).get(2).equals("文本")) {
                    try {
                        String cont = (String) datas.get(0).get(4);
                        byte[] byteArray = Base64.getDecoder().decode(cont);
                        byte[] dec = byte_decrypt(byteArray);
                        oldtext=new String(dec,"UTF-8");
                    } catch (Exception e) {e.printStackTrace();oldtext="";}
                } else {
                    oldtext = String.valueOf(datas.get(0).get(4));
                }
                final EditText editText = new EditText(NoteActivity.this);
                editText.setText(oldtext);
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(NoteActivity.this);
                inputDialog.setTitle("修改内容").setView(editText);
                inputDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String newtext = editText.getText().toString();
                                    byte[] edc= byte_encryption(newtext.getBytes("UTF-8"));
                                    String base64String = Base64.getEncoder().encodeToString(edc);
                                    ContentValues cv = new ContentValues();
                                    cv.put("TYP", "文本");
                                    cv.put("CONT", base64String);
                                    db.update("note", cv, "DAT = ?", new String[]{date});
                                    initdata();} catch (Exception e) {e.printStackTrace();}
                            }
                        }).show();}
                break;
            case 1://删除，需要对应序号
                if (searchlog==true){}else {
                if (datas.get(0).get(2).equals("图片")) {
                    File filepath = getFilesDir();
                    String fileName = (String) datas.get(0).get(4);
                    File file = new File(filepath, fileName);
                    file.delete();
                }
                db.execSQL("DELETE FROM note WHERE DAT='" + date + "'");
                Toast.makeText(this, "已删除!", Toast.LENGTH_SHORT).show();
                initdata();}
                break;
            case 2://复制数据，图片在安卓复制每啥用，没有这个对象
                if (searchlog==true){}else {
                String posttext;
                if (datas.get(0).get(2).equals("图片")) {
                    posttext = (String) datas.get(0).get(4);
                } else {
                    try {
                        String cont = (String) datas.get(0).get(4);
                        byte[] byteArray = Base64.getDecoder().decode(cont);
                        byte[] dec = byte_decrypt(byteArray);
                        posttext=new String(dec,"UTF-8");
                    } catch (Exception e) {e.printStackTrace();posttext="";}}
                ClipData clipData = ClipData.newPlainText("text", posttext);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(clipData);
                break;}
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
    @Override//停止触发
    protected void onStop() {
        super.onStop();
        handler.postDelayed(runnable, DELAY_MILLIS);}
    @Override//启动触发
    protected void onStart() {
        super.onStart();
        photochoose=false;
        handler.removeCallbacks(runnable);
    }
    //延时函数
    public void delayedshutdown(){
        if (photochoose==false){
            finish();
        }else {
            handler.postDelayed(runnable, DELAY_MILLIS);
        }
    }
    @Override//回收
    protected void onDestroy() {
            super.onDestroy();
            try {unregisterForContextMenu(listview);
                handler.removeCallbacks(runnable); // 确保在Activity销毁时移除延时任务
            }catch (Exception e){e.printStackTrace();}

    }
    //搜索关键字
    public SpannableString highlightSearchKeyword(String text, String keyword) {
        SpannableString spannableString = new SpannableString(text);
        if (!TextUtils.isEmpty(keyword)) {
            int start = text.toLowerCase().indexOf(keyword.toLowerCase());
            while (start >= 0) {
                int end = start + keyword.length();
                StyleSpan sp=new StyleSpan(Typeface.BOLD);
                UnderlineSpan sp2=new UnderlineSpan();
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//整体呈黄色背景
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词字体颜色绿色
                spannableString.setSpan(sp, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词字体加粗
                spannableString.setSpan(sp2, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词下划线
                start = text.toLowerCase().indexOf(keyword.toLowerCase(), end);
            }
        }
        return spannableString;}
    //优化后的关键字查询
    // 搜索关键字并高亮显示
    public Map<Integer, SpannableString> highlightSearchKeyword2(String text, String keyword) {
        Map<Integer, SpannableString> result = new HashMap<>();
        SpannableString spannableString = new SpannableString(text);
        if (!TextUtils.isEmpty(keyword)) {
            int start = text.toLowerCase().indexOf(keyword.toLowerCase());
            while (start >= 0) {
                int end = start + keyword.length();
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//整体呈黄色背景
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词字体颜色红色
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词字体加粗
                spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//搜索词下划线
                start = text.toLowerCase().indexOf(keyword.toLowerCase(), end);
            }
            result.put(1, spannableString); // 如果有关键字，键为1
        } else {
            result.put(0, spannableString); // 如果没有关键字，键为0
        }

        return result;
    }
}
//要显示的对象类
class DataItem{
    private String Images;
    private String Titles;
    private String Contents;
    public String getImages() {return Images;}
    public void setImages(String images) {Images = images;}
    public String getTitles() {return Titles;}
    public void setTitles(String titles) {Titles = titles;}
    public String getContents() {return Contents;}
    public void setContents(String contents) {Contents = contents;}
}
//搜索的对象类
class Searchitem{
    private String Images;
    private SpannableString Titles;
    private SpannableString Contents;
    public String getImages() {return Images;}
    public void setImages(String images) {Images = images;}
    public SpannableString getTitles() {return Titles;}
    public void setTitles(SpannableString titles) {Titles = titles;}
    public SpannableString getContents() {return Contents;}
    public void setContents(SpannableString contents) {Contents = contents;}
}