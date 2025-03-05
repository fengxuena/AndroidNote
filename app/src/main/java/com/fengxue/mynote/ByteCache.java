package com.fengxue.mynote;
import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteCache implements AutoCloseable{
    private final Context context;
    private final File cacheFile;
    private String cachename;
    //构造器，初始化缓存文件
    public ByteCache(Context context,String cachename) {
        this.context = context;
        this.cachename=cachename;
        String thename=cachename+".cache";
        this.cacheFile = new File(context.getCacheDir(), thename);
        ensureCacheFileCreated();}
    //检查缓存文件是否存在
    private void ensureCacheFileCreated() {
        if (cacheFile.exists()) {cacheFile.delete();}
        try {cacheFile.createNewFile();
        } catch (IOException e) {throw new RuntimeException(e);}
    }
    //追加一个int数
    public void addInt(int i) throws IOException {
        byte b = (byte) (i & 0xFF); // 取最低 8 位
        try (FileOutputStream fos = new FileOutputStream(cacheFile, true)) { // 'true' 表示追加模式
            fos.write(b);}}
    //追加byte数组
    public void addBytes(byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(cacheFile, true)) { // 'true' 表示追加模式
            fos.write(bytes);}}
    //缓存文件输入流
    public FileInputStream getByteInputStream() throws IOException {
        return new FileInputStream(cacheFile);}
    //缓存文件输出流
    public FileOutputStream getByteOutputStream() throws IOException {
        return new FileOutputStream(cacheFile, true); }// 'true' 表示追加模式
    //获取缓存文件大小
    public int getcachelength() throws IOException{return getByteInputStream().available();}
    //获取cacheFile路径
    public File getCacheFile(){return this.cacheFile;}
    //清除缓存文件
    public void deleteCache(){
        if (cacheFile.exists()) {
            boolean deleted = cacheFile.delete();}}
    //对象销毁时，自动清理缓存
    @Override
    public void close() {deleteCache();}
    public String getCachename() {return cachename;}
}
