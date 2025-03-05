package com.fengxue.mynote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChooseAdapda extends BaseAdapter {
    private List<Noteitem> mylist;
    private Context itemcontext;
    public ChooseAdapda(Context context, List<Noteitem> list){
        itemcontext=context;
        mylist=list;
    }
    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public Object getItem(int position) {
        return mylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Noteitem bean =mylist.get(position);
        ViewHoldernote viewHolder;
        if(convertView==null){//判断是否是第一次，第一次则没有绑定viewholder，需要绑定
            viewHolder=new ViewHoldernote();
            convertView= LayoutInflater.from(itemcontext).inflate(R.layout.choose_item_ui,parent,false);
            viewHolder.setNotename((TextView) convertView.findViewById(R.id.itemnotename));
            viewHolder.setNotedate((TextView) convertView.findViewById(R.id.itemnotedate));
            viewHolder.setMima((TextView) convertView.findViewById(R.id.mimatof));
            convertView.setTag(viewHolder);
        }else {viewHolder=(ViewHoldernote) convertView.getTag();}
        viewHolder.getNotename().setText(bean.getName());
        viewHolder.getNotedate().setText(bean.getDate());
        viewHolder.getMima().setText(bean.getMima());
        return convertView;
    }
}
class ViewHoldernote{
    private TextView notename;
    private TextView notedate;
    private TextView mima;
    public TextView getNotename() {return notename;}
    public void setNotename(TextView notename) {this.notename = notename;}
    public TextView getNotedate() {return notedate;}
    public void setNotedate(TextView notedate) {this.notedate = notedate;}
    public TextView getMima() {return mima;}
    public void setMima(TextView mima) {this.mima = mima;}
}
