package com.fengxue.mynote;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SQLhelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FengxueNoteDB.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "config";
    public static final String TABLE_NAME2="note";
    public SQLhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    //内容描述，内容，数值
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                "NUB INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME TEXT," +
                "VALU1 TEXT," +
                "VALU2 TEXT," +
                "VALU3 TEXT," +
                "VALU4 TEXT,"+
                "INTS INTEGER"+")";
        //序号,笔记名称，数据类型，时间，内容，path
        String CREATE_TABLE2 = "CREATE TABLE " + TABLE_NAME2 + "(" +
                "NUB INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ID TEXT," +
                "TYP TEXT," +
                "DAT TEXT," +
                "CONT TEXT" + ")";
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE2);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean isDatabaseExists(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }
    //内部查询，外部查询用另外一个
    public List<List> getdata(String sql,String tablename){
        List<List> outlist=new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery(sql, null);
        if (tablename.equals(TABLE_NAME)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    List<String> list=new ArrayList<>();
                    @SuppressLint("Range") String nub=cursor.getString(cursor.getColumnIndex("NUB"));
                    @SuppressLint("Range") String name=cursor.getString(cursor.getColumnIndex("NAME"));
                    @SuppressLint("Range") String valu1=cursor.getString(cursor.getColumnIndex("VALU1"));
                    @SuppressLint("Range") String valu2=cursor.getString(cursor.getColumnIndex("VALU2"));
                    @SuppressLint("Range") String valu3=cursor.getString(cursor.getColumnIndex("VALU3"));
                    @SuppressLint("Range") String valu4=cursor.getString(cursor.getColumnIndex("VALU4"));
                    @SuppressLint("Range") int intdata=cursor.getInt(cursor.getColumnIndex("INTS"));
                    //  0   1        2      3      4    5   6
                    //序号 名称      值1     值2   值3   密码  数字
                    //序号 笔记本   笔记序号 笔记名称 日期  密码  数字
                    list.add(nub);
                    list.add(name);
                    list.add(valu1);
                    list.add(valu2);
                    list.add(valu3);
                    list.add(valu4);
                    list.add(String.valueOf(intdata));
                    //System.out.println(list.toString());
                    outlist.add(list);
                    //System.out.println(outlist);
                }
            }
        } else if (tablename.equals(TABLE_NAME2)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    List<String> list=new ArrayList<>();
                    @SuppressLint("Range") int nub=cursor.getInt(cursor.getColumnIndex("NUB"));
                    @SuppressLint("Range") String id=cursor.getString(cursor.getColumnIndex("ID"));
                    @SuppressLint("Range") String typ=cursor.getString(cursor.getColumnIndex("TYP"));
                    @SuppressLint("Range") String dat=cursor.getString(cursor.getColumnIndex("DAT"));
                    @SuppressLint("Range") String cont=cursor.getString(cursor.getColumnIndex("CONT"));
                    //  0     1      2      3    4
                    //序号 笔记本名称  类型   日期  内容
                    list.add(String.valueOf(nub));
                    list.add(id);
                    list.add(typ);
                    list.add(dat);
                    list.add(cont);
                    //System.out.println(list.toString());
                    outlist.add(list);
                }
            }
        }
        return outlist;}
    // 删除数据库
    public static void deleteDatabase(Context context) {
        String databaseName = "FengxueNoteDB.db";
        context.deleteDatabase(databaseName);
        String journalFileName = databaseName + "-journal";
        File journalFile = context.getDatabasePath(journalFileName);
        if (journalFile.exists()) {
            journalFile.delete();
        }
    }
}



