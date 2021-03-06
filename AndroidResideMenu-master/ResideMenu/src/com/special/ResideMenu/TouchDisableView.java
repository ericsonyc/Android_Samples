package com.special.ResideMenu;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by thonguyen on 15/4/14.
 */
class TouchDisableView extends ViewGroup {

    private static final String TAG = "MenuActivity";

    private View mContent;

    //	private int mMode;
    private boolean mTouchDisabled = false;

    public TouchDisableView(Context context) {
        this(context, null);
    }

    public TouchDisableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContent(View v) {
        if (mContent != null) {
            this.removeView(mContent);
        }
        mContent = v;
        this.addView(mContent);
    }

    public View getContent() {
        return mContent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "TouchDisableView:onMeasure");
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "TouchDisableView:onLayout");
        final int width = (r - l);
        final int height = (b - t);
        mContent.layout(0, 0, width, height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i(TAG, "TouchDisableView:onInterceptTouchEvent");
        return mTouchDisabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    void setTouchDisable(boolean disableTouch) {
        mTouchDisabled = disableTouch;
    }

    boolean isTouchDisabled() {
        return mTouchDisabled;
    }
}
