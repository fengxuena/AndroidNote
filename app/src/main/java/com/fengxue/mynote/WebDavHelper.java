package com.fengxue.mynote;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class WebDavHelper {

    private String URL;
    private String USERNAME;
    private String PASSWORD;
    private String FILE_PATH;
    private Sardine sardine;
    private Context context;
    //初始化webdav
    public WebDavHelper(@NonNull Context context,@NonNull String URL, @NonNull String USERNAME, @NonNull String PASSWORD){
        this.URL=URL;
        this.USERNAME=USERNAME;
        this.PASSWORD=PASSWORD;
        this.context=context;
        this.FILE_PATH=URL + "fengxuenote/";
        sardine=new OkHttpSardine();
        sardine.setCredentials(this.USERNAME,this.PASSWORD);
        firstwebdav();}
    //上传
    public void put_bytes_type_data(String filename,byte[] bytes){
        boolean isex=isexists(filename);
        if (isex){try {sardine.delete(FILE_PATH+filename);} catch (IOException e) {throw new RuntimeException(e);}}
        try {

            sardine.put(FILE_PATH+filename,bytes);
        } catch (IOException e) {throw new RuntimeException(e);}
    }
    //下载
    public byte[] get_bytes_type_data(String filename){
        byte[] outbytes=new byte[]{0,0,0,0};
        try (InputStream inputstream = sardine.get(FILE_PATH + filename)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputstream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);}
            // 将 ByteArrayOutputStream 转换为 byte 数组
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {e.printStackTrace();return outbytes;}}
    //判断文件是否存在
    public boolean isexists(String filename){
        try {
            List<DavResource> file_list = sardine.list(FILE_PATH);
            if (!file_list.isEmpty()){
                for (DavResource davResource:file_list){
                    List<String> webfilename_list = findMatches("笔记本数据备份.*?\\.notedata", davResource.getPath());
                    if (!webfilename_list.isEmpty()){
                        if (webfilename_list.get(0).equals(filename)){return true;}}}}
        } catch (Exception e) {return false;}
        return false;}
    //是否创建了目录,如果没有则会创建
    public void firstwebdav(){
        try {
            List<DavResource> file_list = sardine.list(FILE_PATH);
        } catch (IOException e) {
            try {sardine.createDirectory(FILE_PATH);
            } catch (IOException ex) {throw new RuntimeException(ex);}}}
    //正则表达式匹配
    public static List<String> findMatches(String regex, String text) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);// 创建 Pattern 对象
        Matcher matcher = pattern.matcher(text);// 创建 Matcher 对象
        while (matcher.find()) {// 使用 Matcher 对象查找匹配项
            matches.add(matcher.group());// 添加匹配的子串到列表中
        }
        return matches;}
    //byte[]转string
    public static String convertByteArrayToString(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);}
    //获取web文件列表
    public List<String> get_web_list(){
        List<String> list=new ArrayList<>();
        try {
            List<DavResource> file_list = sardine.list(FILE_PATH);
            if (!file_list.isEmpty()){
                for (DavResource davResource:file_list){
                    List<String> webfilename_list = findMatches("笔记本数据备份.*?\\.notedata", davResource.getPath());
                    if (!webfilename_list.isEmpty()){list.add(webfilename_list.get(0));};}}
        } catch (Exception e) {e.printStackTrace();}
        return list;
    }
    //新增的流式传输方法-上传
    public void put_file_data(String filename, ByteCache byteCache) {
        try {
            sardine.put(FILE_PATH+filename,byteCache.getCacheFile(),"application/octet-stream");
            //注：如果添加expectContinue属性，则会在请求前先给服务器申请此次的大文件是否可以传输，如果可以则传输
        } catch (IOException e) {throw new RuntimeException(e);}
    }
    //新增的流式传输方法-下载
    public ByteCache get_file_data(String filename)  {
        try (InputStream inputstream = sardine.get(FILE_PATH + filename)) {
            ByteCache byteCache=new ByteCache(context,"webdown");
            FileOutputStream outputstream = byteCache.getByteOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputstream.read(buffer)) != -1) {
                outputstream.write(buffer, 0, bytesRead);}
            outputstream.close();
            return byteCache;
        } catch (Exception e) {e.printStackTrace();return new ByteCache(context,"erro");}
    }
}
