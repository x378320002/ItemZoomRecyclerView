package com.wzx.itemzoomrecy;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wzx.lib.ItemZoomRecycleView;

public class MainActivity extends AppCompatActivity {
    int[] mImages = {R.drawable.demotest, R.drawable.test1, R.drawable.test2, R.drawable.test3};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ItemZoomRecycleView recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter());

        recyclerView.setActivity(this);
        recyclerView.setOriId(R.id.imageview);
    }

    public class MyHolder extends ViewHolder {
        public MyHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class MyAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return 40;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, viewGroup, false);
            ViewHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            ImageView imageView = viewHolder.itemView.findViewById(R.id.imageview);
            imageView.setImageResource(mImages[i%mImages.length]);
        }
    }
}
