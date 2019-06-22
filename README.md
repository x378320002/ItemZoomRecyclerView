# ItemZoomRecyclerView
一个支持条目中的view放大缩小和跟手的RecyclerView, 仿Instagram图片流双手效果.
基本能完全放instagram的效果, 支持图片, 视频, 或其他view.

项目中实现效果如下:

此demo效果如下;


使用:
第一种,将demo中主工程的libs文件夹中itemzoomrecy.aar直接拷贝到自己项目中, 在gradle中配置好此aar即可.
第二种,也可以直接将项目中lib库复制到自己的项目中, 这是个module, 直接依赖此module即可, 可以随意自定义代码.

代码:
  布局:
  ```
  <android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
    
    <com.wzx.lib.ItemZoomRecycleView
        android:id="@+id/recycleview"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </com.wzx.lib.ItemZoomRecycleView>
    
</android.support.constraint.ConstraintLayout>
  ```
  java中:
  ```
        //获取到ItemZoomRecycleView, 其他用法和原生一样, 添加以下两句即可支持手势.
        recyclerView.setActivity(this);
        recyclerView.setOriId(R.id.imageview); //条目中想要被放大的view
  ```
