package com.fengxue.mynote;
import static com.fengxue.mynote.MainActivity.bytetobitmap;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//适配器
class SearchAdapter extends BaseAdapter {
    private LayoutInflater mInflater;//创建一个LayoutInflater
    private List<Searchitem> mList;//对象list
    private Context itemcontext;//窗口对象
    public SearchAdapter(Context context,List<Searchitem> list) {
        mList=list;
        mInflater=LayoutInflater.from(context);
        itemcontext=context;}
    @Override
    public int getCount() {return mList.size();}
    @Override
    public Object getItem(int position) {return mList.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Searchitem bean =mList.get(position);
        ViewHolder viewHolder;
        //view处理
        if(convertView==null){//判断是否是第一次，第一次则没有绑定viewholder，需要绑定
            viewHolder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.note_item_ui,parent,false);
            viewHolder.setImageView((ImageView)convertView.findViewById(R.id.imageviewss));//将控件保存到viewHolder中
            viewHolder.setTitle((TextView) convertView.findViewById(R.id.dataview2));
            viewHolder.setContent((TextView) convertView.findViewById(R.id.contView2));
            convertView.setTag(viewHolder);}
        else {viewHolder=(ViewHolder)convertView.getTag();}//若已经有了convertView则直接获取其viewHolder
        ViewHolder viewHolder2 = (ViewHolder) convertView.getTag();
        String gettag = (String) viewHolder2.getTitle().getTag();
        if(gettag==null){//titleview无tag
            viewHolder.getTitle().setText(bean.getTitles());//传入日期
            viewHolder.getTitle().setTag(String.valueOf(position));
            if (bean.getContents()==null){viewHolder.getContent().setText("");
            }else {viewHolder.getContent().setText(bean.getContents());}//传入文字
            if(bean.getImages()==null){
                //.out.println("tag不匹配，本条数据也无图片,返回空图片");
                viewHolder.getImageView().setImageBitmap(null);
                viewHolder.getImageView().setVisibility(View.GONE);//不显示
            }else {
                try {File filepath=itemcontext.getFilesDir();
                    String imgpath=filepath+"/"+bean.getImages();
                    File pathtofile=new File(imgpath);
                    InputStream in = new FileInputStream(pathtofile);
                    byte[] data = toByteArray(in);
                    in.close();
                    Bitmap img= bytetobitmap(data);
                    viewHolder.getImageView().setImageBitmap(img);//传入图片
                    viewHolder.getImageView().setLayoutParams(new RelativeLayout.LayoutParams(260,320));
                    viewHolder.getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewHolder.getImageView().setVisibility(View.VISIBLE);//显示
                }catch (IOException E){E.printStackTrace();}}
            return convertView;
        }else {//titleview有tag
            if (String.valueOf(position)==gettag){//tag等于本线程的tag
                viewHolder.getTitle().setTag(String.valueOf(position));
                viewHolder.getTitle().setText(bean.getTitles());//传入日期
                if (bean.getContents()==null){viewHolder.getContent().setText("");
                }else {viewHolder.getContent().setText(bean.getContents());}//传入文字
                return convertView;
            }else {//有tag，tag不等于本线程的tag
                viewHolder.getTitle().setText(bean.getTitles());
                viewHolder.getTitle().setTag(String.valueOf(position));
                if (bean.getContents()==null){viewHolder.getContent().setText("");
                }else {viewHolder.getContent().setText(bean.getContents());}//传入文字
                if(bean.getImages()==null){
                    //System.out.println("tag不匹配，本条数据也无图片,返回空图片");
                    viewHolder.getImageView().setImageBitmap(null);
                    viewHolder.getImageView().setVisibility(View.GONE);//不显示
                }else {
                    //System.out.println("tag不匹配，本条数据有图片,返回本条数据的图片");
                    try {File filepath=itemcontext.getFilesDir();
                        String imgpath=filepath+"/"+bean.getImages();
                        File pathtofile=new File(imgpath);
                        InputStream in = new FileInputStream(pathtofile);
                        byte[] data = toByteArray(in);
                        in.close();
                        Bitmap img= bytetobitmap(data);
                        viewHolder.getImageView().setImageBitmap(img);//传入图片
                        viewHolder.getImageView().setLayoutParams(new RelativeLayout.LayoutParams(260,320));
                        viewHolder.getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
                        viewHolder.getImageView().setVisibility(View.VISIBLE);
                    }catch (IOException E){E.printStackTrace();}}
                return convertView;
            }
        }
    }
    //创建内部类,就是为了避免重复的findViewById操作
    class ViewHolder{
        private ImageView imageView;
        private TextView title;
        private TextView content;
        public ImageView getImageView() {
            return imageView;
        }
        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }
        public TextView getTitle() {
            return title;
        }
        public void setTitle(TextView title) {
            this.title = title;
        }
        public TextView getContent() {
            return content;
        }
        public void setContent(TextView content) {
            this.content = content;
        }
    }
    //传入file得到byte[ ]对象
    private byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);}
        return out.toByteArray();
    }
}

