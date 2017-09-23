package cn.com.tianyudg.pulltorefreshdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.com.tianyudg.pulltorefreshdemo.widget.RefreshLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RefreshLayout refresh;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        refresh = (RefreshLayout) findViewById(R.id.llRefresh);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter(this));

    }

    public void finishRefresh(View view) {
        refresh.finishRefreshing();
    }
}
