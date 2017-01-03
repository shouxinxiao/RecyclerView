package com.xin.recyclerviewtest;

import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    RefreshAdapter refreshAdapter;
    LinearLayoutManager mLayoutManager;
    int lastVisibleItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlue,
                R.color.colorGreen,
                R.color.colorRed,
                R.color.colorYellow);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setData();      //
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // 这句话是为了，第一次进入页面的时候显示加载进度条
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == refreshAdapter.getItemCount()) {
                    // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
                    simulateLoadMoreData();

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshAdapter = new RefreshAdapter(mList);
        mRecyclerView.setAdapter(refreshAdapter);

    }
    private void simulateLoadMoreData() {
        Observable
                .timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        loadMoreData();
                        refreshAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getApplicationContext(), "Load Finished!", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }).subscribe();
    }

    private void loadMoreData() {
        List<String> moreList = new ArrayList<>();
        for (int i = 10; i < 13; i++) {
            moreList.add("加载更多的数据"+ i);
        }
        mList.addAll(moreList);
    }

    List<String> mList = new ArrayList<>();

    private void setData() {
        for (int i = 0; i < 20; i++) {
            mList.add("第" + i + "个");
        }
    }

    @Override
    public void onRefresh() {
        Observable
                .timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map(new Func1<Long, Object>() {
                    @Override
                    public Object call(Long aLong) {
                        fetchingNewData();
                        mSwipeRefreshLayout.setRefreshing(false);
                        refreshAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Refresh Finished!", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }).subscribe();
    }

    private void fetchingNewData() {
        mList.add(0, "下拉刷新出来的数据");
    }

    public class RefreshAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<String> list;

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;


        public RefreshAdapter(List<String> data) {
            list = data;
        }

        // RecyclerView的count设置为数据总条数+ 1（footerView）
        @Override
        public int getItemCount() {
            return list.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            // 最后一个item设置为footerView
            if (position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ItemViewHolder) {
                ((ItemViewHolder) holder).bindData(list.get(position));
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        android.R.layout.simple_list_item_1, null);
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
                return new ItemViewHolder(view);
            }
            // type == TYPE_FOOTER 返回footerView
            else if (viewType == TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.footerview, null);
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
                return new FooterViewHolder(view);
            }

            return null;
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View view) {
                super(view);
            }

        }

        class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            private String data;

            public ItemViewHolder(View view) {
                super(view);
                textView = (TextView) view;
                textView.setTextColor(Color.RED);
                view.setOnClickListener(this);
            }

            public void bindData(String s){
                this.data = s;
                textView.setText(data);
            }
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"你点击了:"+data,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
