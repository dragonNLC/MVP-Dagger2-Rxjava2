package com.anylife.module_main.thirdpartyAPI.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.anylife.module_main.R;
import com.anylife.module_main.thirdpartyAPI.location.BoundLocationListener;
import com.anylife.module_main.thirdpartyAPI.model.Blog;
import com.anylife.module_main.thirdpartyAPI.viewmodel.MainViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zlb.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

@Route(path = "/jetpack/activity")
public class BlogListActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MainViewModel mainViewModel;

    @Override
    public int getLayoutId() {
        return R.layout.activity_blog_list;
    }

    @Override
    protected void initViews() {

        getLifecycle().addObserver(new BoundLocationListener(new BoundLocationListener.ThingChangeListener() {
            @Override
            public void onThingChanged(long thingId) {
                Log.e("TAG","LIFE receive:"+thingId);
            }
        }));


        setActivityTitle("LiveData&ViewModule");

        swipeRefresh = findViewById(R.id.swiperefresh);
        mRecyclerView = findViewById(R.id.blogRecyclerView);

        //ViewModel 就相当于MVP 的P了；好像没法和Dagger 结合起来使用的啊

        //当Activity重建的时候，虽然 onCreate() 方法会重新走一遍，但是这个mainViewModel实例，
        //仍然是第一次创建的那个实例，在ViewModelProviders.of(this).get(***.class)中的get方法中进行了缓存
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //ViewModel 最终消亡是在 Activity 被销毁的时候，会执行它的onCleared()进行数据的清理。


        //去获取博客数据
        getPopularBlog();

        // 数据刷新获取数据
        swipeRefresh.setOnRefreshListener(() -> getPopularBlog());

    }


    /**
     * 去获取数据
     */
    public void getPopularBlog() {
        swipeRefresh.setRefreshing(true);

        //不用回调了，多爽！数据更变自动通知.
        mainViewModel.getAllBlog().observe(this, new Observer<List<Blog>>() {
            @Override
            public void onChanged(@Nullable List<Blog> blogList) {
                swipeRefresh.setRefreshing(false);
                prepareRecyclerView(blogList);
            }
        });

    }

    /**
     * 准备设置数据的改变
     *
     * @param blogList
     */

    List<Blog> blogList = new ArrayList<>();

    private void prepareRecyclerView(List<Blog> blogList) {
        BlogAdapter mBlogAdapter = new BlogAdapter(blogList);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        }
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mBlogAdapter);
        mBlogAdapter.notifyDataSetChanged();

        this.blogList.addAll(blogList);

        mBlogAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String link = blogList.get(position).getLink();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            }
        });

    }

}
