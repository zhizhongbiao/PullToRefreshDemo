package cn.com.tianyudg.pulltorefreshdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Author : WaterFlower.
 * Created on 2017/9/22.
 * Desc :
 */

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

    private Context context;

    public Adapter(Context context) {
        this.context = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.textView.setText("+" + position);
    }

    @Override
    public int getItemCount() {
        return 100;
    }


    class Holder extends RecyclerView.ViewHolder {

        public TextView textView;

        public Holder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
