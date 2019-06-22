package com.wzx.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
/**
 * 支持在里面的条目图片放大缩小的RecyclerView,
 * 条目中图片的id叫R.id.basedetail_imageview, 现在是写死的, 后续可以改成方法设置
 * 给这个view设置activity即可激活放大缩小
 */
public class ItemZoomRecycleView extends RecyclerView {
    private ViewGroup mDecorView;//用来承载view的最上层界面
    private View mOriView; //从原布局拿出来的view
    private ViewGroup.LayoutParams mOriLp; //原view的参数lp
    private int mOriIndex;
    private ViewGroup mOriParent; //原view的父布局
    private int mOriId = 0;
    private View mPlaceHolderView;//用来放到原来的位置, 占位用的view

    private int[] mOriTopLeft = new int[2];

    private float mDonwX, mDownY; //手指按下的点
    private Activity mActivity;
    private int mState; //响应自定义手势的几个状态, 0 默认状态, 系统处理, 1 准备状态  2拦截掉自己处理图片的状态 3回复到原位置的动画状态
    private PointF mLastCenter = new PointF(); //双指中心点
    private double mLastDistence; //双指距离

    public ItemZoomRecycleView(@NonNull Context context) {
        super(context);
    }

    public ItemZoomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemZoomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
        mPlaceHolderView = new View(activity);
    }

    public void setOriId(int oriId) {
        mOriId = oriId;
    }

    private void backToOri() {
        if (mState == 3) {
            return;
        }
        mState = 3;
        final float translationX = mOriView.getTranslationX();
        final float translationY = mOriView.getTranslationY();
        final float scaleX = mOriView.getScaleX();
        final float scaleY = mOriView.getScaleY();

        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0f);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                //从初始位置到0的过程
                mOriView.setTranslationX(translationX * f);
                mOriView.setTranslationY(translationY * f);
                //从初始位置到1的过程
                mOriView.setScaleX(1.0f + (scaleX - 1.0f) * f);
                mOriView.setScaleY(1.0f + (scaleY - 1.0f) * f);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //mImageView.setVisibility(GONE);
                //mOriImageView.setVisibility(VISIBLE);
                //把占位view拿出来, 把原来的view放回去
                mDecorView.removeView(mOriView);
                mOriParent.removeView(mPlaceHolderView);
                mOriParent.addView(mOriView, mOriIndex, mOriLp);
                mState = 0;
            }
        });
        animator.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mActivity != null && mOriId != 0) {
            int action = event.getActionMasked();
            if (mState == 1) {//准备
                int pointerCount = event.getPointerCount();
                if (pointerCount <= 1 ||
                        (pointerCount == 2 && (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL))) {
                    mState = 0;
                } else {
                    if (action == MotionEvent.ACTION_MOVE && mOriView != null) {
                        float x1 = event.getX(0);
                        float y1 = event.getY(0);
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);

                        double distence = getDistence(x1, y1, x2, y2);
                        if (Math.abs(distence - mLastDistence) >= 10) {
                            mDecorView = (ViewGroup) mActivity.getWindow().getDecorView();
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mOriView.getWidth(), mOriView.getHeight());
                            lp.topMargin = mOriTopLeft[1];
                            lp.leftMargin = mOriTopLeft[0];
                            lp.gravity = Gravity.LEFT|Gravity.TOP;

                            //新建view的方案, 只支持imageview
                            //if (mImageView == null) {
                            //    mImageView = new ImageView(mActivity);
                            //    mImageView.setElevation(4);
                            //}
                            //mImageView.setImageDrawable(mOriImageView.getDrawable());
                            //ViewParent parent = mImageView.getParent();
                            //if (parent != null) {
                            //    mImageView.setLayoutParams(lp);
                            //} else {
                            //    group.addView(mImageView, lp);
                            //}
                            //mImageView.setTranslationX(0);
                            //mImageView.setTranslationY(0);
                            //mImageView.setScaleX(1.0f);
                            //mImageView.setScaleY(1.0f);
                            //mImageView.setVisibility(VISIBLE);
                            //mOriImageView.setVisibility(INVISIBLE);

                            //把原来的view拿出来, 把占位view放进去
                            mOriLp = mOriView.getLayoutParams();
                            mOriParent = (ViewGroup) mOriView.getParent();
                            mOriIndex = mOriParent.indexOfChild(mOriView);
                            mOriParent.removeView(mOriView);
                            mPlaceHolderView.setId(mOriView.getId());
                            mOriParent.addView(mPlaceHolderView, mOriIndex, mOriLp);
                            mDecorView.addView(mOriView, lp);

                            //修复因为阈值引起的第一帧跳变
                            mDonwX = x1;
                            mDownY = y1;
                            mLastCenter.x = (x1 + x2) * 0.5f;
                            mLastCenter.y = (y1 + y2) * 0.5f;
                            mLastDistence = getDistence(x1, y1, x2, y2);

                            mState = 2;//进入拖动状态

                            //禁用父布局事件拦截
                            getParent().requestDisallowInterceptTouchEvent(true);
                            //有些控价, 如谷歌的SwipeRefreshLayout, 实现的requestDisallowInterceptTouchEvent有问题, 不会向上传递
                            //所以需要循环向上调用
                            //ViewParent par = getParent();
                            //while (par != null) {
                            //    par.requestDisallowInterceptTouchEvent(true);
                            //    par = par.getParent();
                            //}
                        }
                    }
                    return true;
                }
            } else if (mState == 2) { //自己处理手势
                //LogHelper.d("wangzixu", "detailpage onTouch x  = " + x + ", y = " + y);
                int pointerCount = event.getPointerCount();
                if (pointerCount <= 1 ||
                        (pointerCount == 2 && (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL))) {
                    backToOri();
                } else {
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);

                    //处理图片的移动
                    float halfX = (x1 + x2) * 0.5f - mLastCenter.x;
                    float halfY = (y1 + y2) * 0.5f - mLastCenter.y;
                    mOriView.setTranslationX(halfX);
                    mOriView.setTranslationY(halfY);

                    //处理图片的放大缩小
                    double distence = getDistence(x1, y1, x2, y2);
                    double scaleFactor = distence / mLastDistence;
                    mOriView.setScaleX((float) scaleFactor);
                    mOriView.setScaleY((float) scaleFactor);
                }
                return true;
            } else if (mState == 3) {//返回原位置中
                //nothing
                return true;
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    mDonwX = event.getX();
                    mDownY = event.getY();
                } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    float x1 = event.getX();
                    float y1 = event.getY();
                    if (Math.abs(x1 - mDonwX) < 8 && Math.abs(y1 - mDownY) < 8) { //手指抖动的阈值
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);
                        View view1 = findChildViewUnder(x1, y1);
                        View view2 = findChildViewUnder(x2, y2);
                        //LogHelper.d("wangzixu", "detailpage onTouch view1 = " + view1 + ", view2 = " + view2);
                        if (view1 != null && view1 == view2) {
                            int raw_x1 = (int) event.getRawX();
                            int raw_y1 = (int) event.getRawY();
                            int raw_x2 = (int) (raw_x1 + x2 - x1);
                            int raw_y2 = (int) (raw_y1 + y2 - y1);

                            View view = findUnderImageView((ViewGroup) view1, raw_x1, raw_y1, raw_x2, raw_y2);
                            //ImageView imageView = findUnderImageView2((ViewGroup) view1);

                            if (view != null) {
                                mOriView = view;
                                //LogHelper.d("wangzixu", "detailpage onTouch view1.findViewById mOriLocation  = " + mOriLocation + ", " + mOriTopLeft[0] + "," + mOriTopLeft[1]);
                                mState = 1; //双指按下, 并且双指按下的条目中有imageview, 并且双指的点都在imageview的区域中
                            }
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean viewContainXY(View view, int[] location, int x, int y) {
        if (x >= location[0] && x <= location[0] + view.getWidth()
                && y >= location[1] && y <= location[1] + view.getHeight()) {
            return true;
        }
        return false;
    }

    //算法一, 依赖正确的x,y, xy是相对屏幕的坐标,
    private View findUnderImageView(ViewGroup parent, int x1, int y1, int x2, int y2) {
        for (int i = parent.getChildCount(); i >= 0; i--) {
            View child = parent.getChildAt(i);
            if (child != null && child.getId() == mOriId) {
                child.getLocationOnScreen(mOriTopLeft);

                boolean contain1 = viewContainXY(child, mOriTopLeft, x1, y1);
                boolean contain2 = viewContainXY(child, mOriTopLeft, x2, y2);
                if (contain1 && contain2) {
                    return child;
                }
                if (contain1 ^ contain2) { //如果view包含其中一个点, 说明两个手指落在不同的view上了, 直接返回null
                    return null;
                }
            } else if (child instanceof ViewGroup) {
                View view = findUnderImageView((ViewGroup) child, x1, y1, x2, y2);
                if (view != null) {
                    return view;
                }
            }
        }
        return null;
    }

    //算法2
    //private View findUnderImageView2(ViewGroup parent) {
    //    ArrayList<View> changeIdlist = new ArrayList<>();
    //    View view = parent.findViewById(mOriId);
    //    while (view != null) {
    //        //找到的imageview有可能出在viewpager中, 在屏幕外了, 需要纠正找到屏幕中的
    //        view.getLocationOnScreen(mOriTopLeft);
    //        if (mOriTopLeft[0] == 0) {
    //            break;
    //        } else {
    //            view.setId(R.id.zan_redpoint);//随便设置一个id, 不要和原来的重复就好, 最好设置一个这界面都找不到的id
    //            changeIdlist.add(view);
    //            view = parent.findViewById(mOriId);
    //        }
    //    }
    //    for (int i = 0; i < changeIdlist.size(); i++) {
    //        changeIdlist.get(i).setId(mOriId); //一定需要把id恢复
    //    }
    //    return view;
    //}

    private double getDistence(float x1, float y1, float x2, float y2) {
        float deltaX = Math.abs(x1 - x2);
        float deltaY = Math.abs(y1 - y2);
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
