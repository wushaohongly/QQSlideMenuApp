package com.wushaohong.qqslidemenuapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * 水平的滚动视图，实现侧滑功能
 */
public class SlideMenu extends HorizontalScrollView {

    private LinearLayout mWapper;
    private ViewGroup mMenuViewGroup;
    private ViewGroup mContentViewGroup;

    private int mScreenWidth;
    // 菜单默认右边距
    private int mMenuRightPadding = 50;
    // 侧滑菜单栏的宽度
    private int mMenuWidth;

    private boolean mIsOnce = true;

    // 开关状态
    private boolean mIsOpen;

    private float mOffsetX, mOffsetY;
    private float mLastPosX, mLastPosY;

    // 菜单动作回调
    private OnSlideListener mOnSlideListener;

    /**
     * 构造方法
     */
    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 屏幕属性，可抽取到MyApplication当中
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;

        // 读取attrs属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlideMenu,
                defStyleAttr, 0);
        int n = typedArray.length();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.SlideMenu_rightPadding:
                    mMenuRightPadding = typedArray.getDimensionPixelSize(
                            attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                    50, context.getResources().getDisplayMetrics())
                    );
                    break;
                default:
                    break;
            }
        }
        typedArray.recycle();
    }

    /**
     * 测量布局
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 绘制一次
        if (mIsOnce) {
            mWapper = (LinearLayout) getChildAt(0);
            mMenuViewGroup = (ViewGroup) mWapper.getChildAt(0);
            mContentViewGroup = (ViewGroup) mWapper.getChildAt(1);
            mMenuWidth = mMenuViewGroup.getLayoutParams().width = mScreenWidth - mMenuRightPadding;
            mContentViewGroup.getLayoutParams().width = mScreenWidth;
            mIsOnce = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            this.scrollTo(mMenuWidth, 0);
        }
    }

    /**
     * 事件响应
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                if (scrollX >= mMenuWidth / 2) {
                    smoothScrollTo(mMenuWidth, 0);
                    mIsOpen = false;
                    if (mOnSlideListener != null) {
                        mOnSlideListener.onClose();
                    }
                } else {
                    smoothScrollTo(0, 0);
                    mIsOpen = true;
                    if (mOnSlideListener != null) {
                        mOnSlideListener.onOpen();
                    }
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 打开菜单栏
     */
    public void openMenu() {
        if (mOnSlideListener != null) {
            mOnSlideListener.onOpen();
        }
        if (mIsOpen)
            return;
        smoothScrollTo(0, 0);
        mIsOpen = true;
    }

    /**
     * 关闭菜单栏
     */
    public void closeMenu() {
        if (mOnSlideListener != null) {
            mOnSlideListener.onClose();
        }
        if (!mIsOpen)
            return;
        smoothScrollTo(mMenuWidth, 0);
        mIsOpen = false;
    }

    /**
     * 切换菜单
     */
    public void toggleMenu() {
        if (mIsOpen)
            closeMenu();
        else
            openMenu();
    }

    /**
     * 页面滑动的距离的改变
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = l * 1.0f / mMenuWidth;
        ViewHelper.setTranslationX(mMenuViewGroup, mMenuWidth * scale * 0.7f);

        float rightScale = 0.8f + 0.2f * scale;
        ViewHelper.setPivotX(mContentViewGroup, 0);
        ViewHelper.setPivotY(mContentViewGroup, mContentViewGroup.getHeight() / 2);
        ViewHelper.setScaleX(mContentViewGroup, rightScale);
        ViewHelper.setScaleY(mContentViewGroup, rightScale);

        float leftScale = 1.0f - 0.2f * scale;
        ViewHelper.setScaleX(mMenuViewGroup, leftScale);
        ViewHelper.setScaleY(mMenuViewGroup, leftScale);

        float leftAlpha = 0.1f + 0.9f * (1 - scale);
        ViewHelper.setAlpha(mMenuViewGroup, leftAlpha);

        if (mOnSlideListener != null) {
            mOnSlideListener.onSliding(scale);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean result;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOffsetX = 0.0F;
                mOffsetY = 0.0F;
                mLastPosX = ev.getX();
                mLastPosY = ev.getY();
                result = super.onInterceptTouchEvent(ev);//false手势传递给子控件
                break;
            default:
                float thisPosX = ev.getX();
                float thisPosY = ev.getY();
                mOffsetX += Math.abs(thisPosX - mLastPosX);//x偏移
                mOffsetY += Math.abs(thisPosY - mLastPosY);//y轴偏移

                mLastPosY = thisPosY;
                mLastPosX = thisPosX;
                if (mOffsetX < 3 && mOffsetY < 3)
                    // 传给子控件
                    result = false;
                else if (mOffsetY < mOffsetX)
                    // 不传给子控件，自己水平滑动
                    result = true;
                else
                    // 传给子控件
                    result = false;
                break;
        }
        return result;
    }

    public void setOnSlideListener(OnSlideListener mOnSlideListener) {
        this.mOnSlideListener = mOnSlideListener;
    }

    /**
     * 获取当前菜单状态
     * @return true开，false关
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    public interface OnSlideListener {
        void onOpen();

        void onClose();

        void onSliding(float fraction);
    }
}