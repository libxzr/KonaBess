package xzr.konabess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import xzr.konabess.R;

public class ParamAdapter extends BaseAdapter {
    public static class item{
        public String title;
        public String subtitle;
    }

    List<item> items;
    Context context;
    public ParamAdapter(List<item> items, Context context){
        this.items=items;
        this.context=context;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=LayoutInflater.from(context).inflate(R.layout.param_list_item,null);
        TextView title=view.findViewById(R.id.title);
        TextView subtitle=view.findViewById(R.id.subtitle);

        title.setText(items.get(position).title);
        subtitle.setText(items.get(position).subtitle);
        return view;
    }
}
