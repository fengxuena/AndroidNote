package com.fengxue.mynote;
import static com.fengxue.mynote.MainActivity.bytetobitmap;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class NoteImageView extends AppCompatActivity implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener{
    private ImageView imgview;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private SQLhelper dbHelper;
    private SQLiteDatabase db;
    private String nownote;
    private String note_names;
    // 保存上次两个手指触摸的位置
    private float prevSpacing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new SQLhelper(this);
        db = dbHelper.getWritableDatabase();
        List<List> cu = dbHelper.getdata("SELECT * FROM config WHERE NAME = '主题'", "config");
        if (cu.size()!=0){if (cu.get(0).get(6).equals("1")){setTheme(R.style.Them_green);}else {setTheme(R.style.Them_Yellow);}}else {setTheme(R.style.Them_green);}
        setContentView(R.layout.msg_imgview);
        nownote=getIntent().getStringExtra("notes").replace(" ","");
        note_names=getIntent().getStringExtra("names").replace(" ","");
        imgview = findViewById(R.id.imgview);
        try {
            String imgpath=getIntent().getStringExtra("img");
            File filepath=getFilesDir();
            String imgpath2=filepath+"/"+imgpath.replace(" ","");
            File pathtofile=new File(imgpath2);
            InputStream in = new FileInputStream(pathtofile);
            byte[] data = toByteArray(in);
            in.close();
            Bitmap img= bytetobitmap(data);
            imgview.setImageBitmap(img);//传入图片
            Button down_button=findViewById(R.id.down_image);
            down_button.setOnClickListener(vie ->{
                if (img!=null){
                    File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDirectory.exists()) {downloadsDirectory.mkdirs();}
                    File imageFile = new File(downloadsDirectory, imgpath.replace(" ",""));
                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                        boolean saved = img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        if (saved) {Toast.makeText(NoteImageView.this, "已保存到Download目录!", Toast.LENGTH_SHORT).show();}
                    } catch (IOException e) {e.printStackTrace();}}else {Toast.makeText(NoteImageView.this, "图片为空!", Toast.LENGTH_SHORT).show();}});
            scaleGestureDetector = new ScaleGestureDetector(this, this);
            gestureDetector = new GestureDetector(this,  this);
        }catch (IOException E){E.printStackTrace();}}
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float spacing = detector.getCurrentSpan();
        // 计算缩放比例
        scaleFactor *= detector.getScaleFactor();

        // 限制缩放级别
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));

        // 两个手指第一次触摸时记录距离
        if (spacing > 0 && prevSpacing > 0) {
            float scaleFactor = spacing / prevSpacing;
            imgview.setScaleX(scaleFactor * imgview.getScaleX());
            imgview.setScaleY(scaleFactor * imgview.getScaleY());
        }

        prevSpacing = spacing;
        return true;
    }
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        prevSpacing = detector.getCurrentSpan();
        return true;
    }
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        prevSpacing = 0;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 移动ImageView
        imgview.setTranslationX(imgview.getTranslationX() - distanceX);
        imgview.setTranslationY(imgview.getTranslationY() - distanceY);
        return true;
    }
    @Override
    public void onLongPress(MotionEvent e) {}
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
    @Override
    public void onStop() {
        Intent intent = new Intent();
        intent.putExtra("callback", "0");
        setResult(RESULT_OK, intent);
        finish();
        super.onStop();}
    //传入file得到byte[ ]对象
    private byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);}
        return out.toByteArray();}





}

