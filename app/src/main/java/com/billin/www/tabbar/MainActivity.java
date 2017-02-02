package com.billin.www.tabbar;

import android.animation.ObjectAnimator;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Billin on 2016/12/20.
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        // init tabController
        TabController tabController = new TabController(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 256);
        layoutParams.setMargins(0, 100, 0, 0);
        tabController.setLayoutParams(layoutParams);

        List<String> data = new ArrayList<>();
        data.add("1");
        data.add("tmp");
        data.add("3");
        data.add("4");
        data.add("5");
        tabController.setTabText(data);
        tabController.notifyDataChanged();

        setContentView(tabController);
        */
        final TabBar tabBar = new TabBar(this);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                250);
        tabBar.setLayoutParams(layoutParams);
        tabBar.setListener(new TabBar.TabBarListener() {
            @Override
            public void onScroll(TabBar.OffsetOfPosition offsetOfPosition) {
//                Log.d(TAG, "onScroll: ");
                int count = tabBar.getChildCount();
                for (int i = 0; i < count; i++) {
                    TabBarItem tabBarItem = (TabBarItem) tabBar.getChildAt(i);
                    if (offsetOfPosition.get(i) > 65) {
                        tabBarItem.setLevel(0);
                        tabBarItem.setAlpha((100f - offsetOfPosition.get(i)) / 35f);
                    } else {
                        tabBarItem.setLevel((100 - offsetOfPosition.get(i) - 35) * 100 / 65);
                        tabBarItem.setAlpha(1);
                    }
                }
            }

            @Override
            public void onSelected(int position) {
                Log.d(TAG, "onSelected: " + position);
            }

            @Override
            public void onStateChange(int state) {
                Log.d(TAG, "onStateChange: " + state);
                int count = tabBar.getChildCount();
                if (state == TabBar.END) {
                    for (int i = 0; i < count; i++) {
                        TabBarItem tabBarItem = (TabBarItem) tabBar.getChildAt(i);

                        if (i == tabBar.getCurrPosition()) {
                            ObjectAnimator animator = (ObjectAnimator) tabBarItem.getTag();
                            if (animator != null) {
                                animator.cancel();
                            }

                            tabBarItem.setAlpha(1f);
                            continue;
                        }

                        ObjectAnimator animator = ObjectAnimator.ofFloat(tabBar.getChildAt(i), "alpha", 1f, 0f);
                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.setDuration(1000).start();
                        tabBarItem.setTag(animator);
                    }
                } else if (state == TabBar.ON_DOWN) {
                    for (int i = 0; i < count; i++) {
                        TabBarItem tabBarItem = (TabBarItem) tabBar.getChildAt(i);
                        ObjectAnimator animator = (ObjectAnimator) tabBarItem.getTag();
                        if (animator != null) {
                            animator.cancel();
                        }

                        tabBarItem.setAlpha(1f);
                    }
                }
            }
        });

        tabBar.setAdapter(new TabBar.TabBarAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public View getView(int position) {
                final TabBarItem tabBarItem = new TabBarItem(MainActivity.this);
                tabBarItem.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
                tabBarItem.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                return tabBarItem;
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.parseColor("#7aa9ff"));
        linearLayout.addView(tabBar);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(400, 300));

        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // TabBarItem
        final TabBarItem tabBarItem = new TabBarItem(this);
        tabBarItem.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tabBarItem.setBackgroundColor(Color.parseColor("#7aa9ff"));
        tabBarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabBarItem.setLevel(0);
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(tabBarItem, "level", 0, 100);
                objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                objectAnimator.setDuration(1000);
                objectAnimator.start();


            }
        });
        tabBarItem.setLevel(-1);

        linearLayout.addView(tabBarItem);

        setContentView(linearLayout);
    }

}
