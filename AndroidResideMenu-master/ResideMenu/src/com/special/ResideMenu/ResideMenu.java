package com.special.ResideMenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午10:44
 * Mail: specialcyci@gmail.com
 */
public class ResideMenu extends FrameLayout {

    private String TAG = "MenuActivity";
    private String TAG1 = "ResideMenu";

    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    private static final int PRESSED_MOVE_HORIZONTAL = 2;
    private static final int PRESSED_DOWN = 3;
    private static final int PRESSED_DONE = 4;
    private static final int PRESSED_MOVE_VERTICAL = 5;

    private ImageView imageViewShadow;
    private ImageView imageViewBackground;
    private LinearLayout layoutLeftMenu;
    private LinearLayout layoutRightMenu;
    private View scrollViewLeftMenu;
    private View scrollViewRightMenu;
    private View scrollViewMenu;
    /**
     * Current attaching activity.
     */
    private Activity activity;
    /**
     * The DecorView of current activity.
     */
    private ViewGroup viewDecor;
    private TouchDisableView viewActivity;
    /**
     * The flag of menu opening status.
     */
    private boolean isOpened;
    private float shadowAdjustScaleX;
    private float shadowAdjustScaleY;
    /**
     * Views which need stop to intercept touch events.
     */
    private List<View> ignoredViews;
    private List<ResideMenuItem> leftMenuItems;
    private List<ResideMenuItem> rightMenuItems;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private OnMenuListener menuListener;
    private float lastRawX;
    private boolean isInIgnoredView = false;
    private int scaleDirection = DIRECTION_LEFT;
    private int pressedState = PRESSED_DOWN;
    private List<Integer> disabledSwipeDirection = new ArrayList<Integer>();
    // Valid scale factor is between 0.0f and 1.0f.
    private float mScaleValue = 0.5f;

    private boolean mUse3D;
    private static final int ROTATE_Y_ANGLE = 10;

    public ResideMenu(Context context) {
        super(context);
        Log.i(TAG, "ResideMenu:Constructor1:");
        initViews(context, -1, -1);//initialize views
    }

    /**
     * This constructor provides you to create menus with your own custom
     * layouts, but if you use custom menu then do not call addMenuItem because
     * it will not be able to find default views
     */
    public ResideMenu(Context context, int customLeftMenuId,
                      int customRightMenuId) {
        super(context);
        Log.i(TAG, "ResideMenu:Constructor2:");
        initViews(context, customLeftMenuId, customRightMenuId);
    }

    private void initViews(Context context, int customLeftMenuId,
                           int customRightMenuId) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_custom, this);
        Log.i(TAG, "ResideMenu initViews");

        if (customLeftMenuId >= 0) {
            scrollViewLeftMenu = inflater.inflate(customLeftMenuId, this, false);
        } else {
            scrollViewLeftMenu = inflater.inflate(
                    R.layout.residemenu_custom_left_scrollview, this, false);
            layoutLeftMenu = (LinearLayout) scrollViewLeftMenu.findViewById(R.id.layout_left_menu);
        }

        if (customRightMenuId >= 0) {
            scrollViewRightMenu = inflater.inflate(customRightMenuId, this, false);
        } else {
            scrollViewRightMenu = inflater.inflate(
                    R.layout.residemenu_custom_right_scrollview, this, false);
            layoutRightMenu = (LinearLayout) scrollViewRightMenu.findViewById(R.id.layout_right_menu);
        }

        imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
        imageViewBackground = (ImageView) findViewById(R.id.iv_background);

        RelativeLayout menuHolder = (RelativeLayout) findViewById(R.id.sv_menu_holder);
        menuHolder.addView(scrollViewLeftMenu);
        menuHolder.addView(scrollViewRightMenu);
    }

    /**
     * Returns left menu view so you can findViews and do whatever you want with
     */
    public View getLeftMenuView() {
        Log.i(TAG, "ResideMenu:getLeftMenuView:");
        return scrollViewLeftMenu;
    }

    /**
     * Returns right menu view so you can findViews and do whatever you want with
     */
    public View getRightMenuView() {
        Log.i(TAG, "ResideMenu:getRightMenuView");
        return scrollViewRightMenu;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        // Applies the content insets to the view's padding, consuming that
        // content (modifying the insets to be 0),
        // and returning true. This behavior is off by default and can be
        // enabled through setFitsSystemWindows(boolean)
        // in api14+ devices.

        // This is added to fix soft navigationBar's overlapping to content above LOLLIPOP
        Log.i(TAG, "ResideMenu:fitSystemWindows");
        int bottomPadding = viewActivity.getPaddingBottom() + insets.bottom;
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        if (!hasBackKey || !hasHomeKey) {//there's a navigation bar
            bottomPadding += getNavigationBarHeight();
        }

        this.setPadding(viewActivity.getPaddingLeft() + insets.left,
                viewActivity.getPaddingTop() + insets.top,
                viewActivity.getPaddingRight() + insets.right,
                bottomPadding);
        insets.left = insets.top = insets.right = insets.bottom = 0;
        return true;
    }

    private int getNavigationBarHeight() {
        Log.i(TAG, "ResideMenu:getNavigationBarHeight:");
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Set up the activity;
     *
     * @param activity
     */
    public void attachToActivity(Activity activity) {
        Log.i(TAG, "ResideMenu:attachToActivity");
        initValue(activity);
        setShadowAdjustScaleXByOrientation();
        viewDecor.addView(this, 0);
    }

    private void initValue(Activity activity) {
        Log.i(TAG, "ResideMenu:initValue");
        this.activity = activity;
        leftMenuItems = new ArrayList<ResideMenuItem>();
        rightMenuItems = new ArrayList<ResideMenuItem>();
        ignoredViews = new ArrayList<View>();
        viewDecor = (ViewGroup) activity.getWindow().getDecorView();

        viewActivity = new TouchDisableView(this.activity);

        View mContent = viewDecor.getChildAt(0);
        viewDecor.removeViewAt(0);
        Log.i(TAG, "ResideMenu:viewCount:" + viewDecor.getChildCount());
        viewActivity.setContent(mContent);
        this.addView(viewActivity);

        ViewGroup parent = (ViewGroup) scrollViewLeftMenu.getParent();
        parent.removeView(scrollViewLeftMenu);
        parent.removeView(scrollViewRightMenu);
    }

    private void setShadowAdjustScaleXByOrientation() {
        Log.i(TAG, "ResideMenu:setShadowAdjustScalaXByOrientation");
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            shadowAdjustScaleX = 0.034f;
            shadowAdjustScaleY = 0.12f;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            shadowAdjustScaleX = 0.06f;
            shadowAdjustScaleY = 0.07f;
        }
    }

    /**
     * Set the background image of menu;
     *
     * @param imageResource
     */
    public void setBackground(int imageResource) {
        Log.i(TAG, "ResideMenu:setBackground:");
        imageViewBackground.setImageResource(imageResource);
    }

    /**
     * The visibility of the shadow under the activity;
     *
     * @param isVisible
     */
    public void setShadowVisible(boolean isVisible) {
        if (isVisible)
            imageViewShadow.setBackgroundResource(R.drawable.shadow);
        else
            imageViewShadow.setBackgroundResource(0);
    }

    /**
     * Add a single item to the left menu;
     * <p/>
     * WARNING: It will be removed from v2.0.
     *
     * @param menuItem
     */
    @Deprecated
    public void addMenuItem(ResideMenuItem menuItem) {
        Log.i(TAG, "ResideMenu:addMenuItem:");
        this.leftMenuItems.add(menuItem);
        layoutLeftMenu.addView(menuItem);
    }

    /**
     * Add a single items;
     *
     * @param menuItem
     * @param direction
     */
    public void addMenuItem(ResideMenuItem menuItem, int direction) {
        Log.i(TAG, "ResideMenu:addMenuItem2:");
        if (direction == DIRECTION_LEFT) {
            this.leftMenuItems.add(menuItem);
            layoutLeftMenu.addView(menuItem);
        } else {
            this.rightMenuItems.add(menuItem);
            layoutRightMenu.addView(menuItem);
        }
    }

    /**
     * WARNING: It will be removed from v2.0.
     *
     * @param menuItems
     */
    @Deprecated
    public void setMenuItems(List<ResideMenuItem> menuItems) {
        Log.i(TAG, "ResideMenu:setMenuItems:");
        this.leftMenuItems = menuItems;
        rebuildMenu();
    }

    /**
     * Set menu items by a array;
     *
     * @param menuItems
     * @param direction
     */
    public void setMenuItems(List<ResideMenuItem> menuItems, int direction) {
        Log.i(TAG, "ResideMenu:setMenuItems:");
        if (direction == DIRECTION_LEFT)
            this.leftMenuItems = menuItems;
        else
            this.rightMenuItems = menuItems;
        rebuildMenu();
    }

    private void rebuildMenu() {
        Log.i(TAG, "ResideMenu:rebuildMenu:");
        if (layoutLeftMenu != null) {
            layoutLeftMenu.removeAllViews();
            for (ResideMenuItem leftMenuItem : leftMenuItems)
                layoutLeftMenu.addView(leftMenuItem);
        }

        if (layoutRightMenu != null) {
            layoutRightMenu.removeAllViews();
            for (ResideMenuItem rightMenuItem : rightMenuItems)
                layoutRightMenu.addView(rightMenuItem);
        }
    }

    /**
     * WARNING: It will be removed from v2.0.
     *
     * @return
     */
    @Deprecated
    public List<ResideMenuItem> getMenuItems() {
        Log.i(TAG, "ResideMenu:getMenuItems");
        return leftMenuItems;
    }

    /**
     * Return instances of menu items;
     *
     * @return
     */
    public List<ResideMenuItem> getMenuItems(int direction) {
        Log.i(TAG, "ResideMenu:getMenuItems2");
        if (direction == DIRECTION_LEFT)
            return leftMenuItems;
        else
            return rightMenuItems;
    }

    /**
     * If you need to do something on closing or opening menu,
     * set a listener here.
     *
     * @return
     */
    public void setMenuListener(OnMenuListener menuListener) {
        Log.i(TAG, "ResideMenu:setMenuListener");
        this.menuListener = menuListener;
    }


    public OnMenuListener getMenuListener() {
        Log.i(TAG, "ResideMenu:getMenuListener");
        return menuListener;
    }

    /**
     * Show the menu;
     */
    public void openMenu(int direction) {
        Log.i(TAG, "ResideMenu:openMenu");
        setScaleDirection(direction);

        isOpened = true;
        AnimatorSet scaleDown_activity = buildScaleDownAnimation(viewActivity, mScaleValue, mScaleValue);
        AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow,
                mScaleValue + shadowAdjustScaleX, mScaleValue + shadowAdjustScaleY);
        AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 1.0f);
        scaleDown_shadow.addListener(animationListener);
        scaleDown_activity.playTogether(scaleDown_shadow);
        scaleDown_activity.playTogether(alpha_menu);
        scaleDown_activity.start();
    }

    /**
     * Close the menu;
     */
    public void closeMenu() {
        Log.i(TAG, "ResideMenu:closeMenu");
        isOpened = false;
        AnimatorSet scaleUp_activity = buildScaleUpAnimation(viewActivity, 1.0f, 1.0f);
        AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow, 1.0f, 1.0f);
        AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 0.0f);
        scaleUp_activity.addListener(animationListener);
        scaleUp_activity.playTogether(scaleUp_shadow);
        scaleUp_activity.playTogether(alpha_menu);
        scaleUp_activity.start();
    }

    @Deprecated
    public void setDirectionDisable(int direction) {
        Log.i(TAG, "ResideMenu:setDirectionDisable");
        disabledSwipeDirection.add(direction);
    }

    public void setSwipeDirectionDisable(int direction) {
        Log.i(TAG, "ResideMenu:setSwipeDirectionDisable");
        disabledSwipeDirection.add(direction);
    }

    private boolean isInDisableDirection(int direction) {
        Log.i(TAG, "ResideMenu:isInDisableDirection");
        return disabledSwipeDirection.contains(direction);
    }

    private void setScaleDirection(int direction) {
        Log.i(TAG, "ResideMenu:setScalaDirection");
        int screenWidth = getScreenWidth();
        float pivotX;
        float pivotY = getScreenHeight() * 0.5f;

        if (direction == DIRECTION_LEFT) {
            scrollViewMenu = scrollViewLeftMenu;
            pivotX = screenWidth * 1.5f;
        } else {
            scrollViewMenu = scrollViewRightMenu;
            pivotX = screenWidth * -0.5f;
        }

        ViewHelper.setPivotX(viewActivity, pivotX);
        ViewHelper.setPivotY(viewActivity, pivotY);
        ViewHelper.setPivotX(imageViewShadow, pivotX);
        ViewHelper.setPivotY(imageViewShadow, pivotY);
        scaleDirection = direction;
    }

    /**
     * return the flag of menu status;
     *
     * @return
     */
    public boolean isOpened() {
        Log.i(TAG, "ResideMenu:isOpened");
        return isOpened;
    }

    private OnClickListener viewActivityOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isOpened()) closeMenu();
        }
    };

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (isOpened()) {
                showScrollViewMenu(scrollViewMenu);
                if (menuListener != null)
                    menuListener.openMenu();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // reset the view;
            if (isOpened()) {
                viewActivity.setTouchDisable(true);
                viewActivity.setOnClickListener(viewActivityOnClickListener);
            } else {
                viewActivity.setTouchDisable(false);
                viewActivity.setOnClickListener(null);
                hideScrollViewMenu(scrollViewLeftMenu);
                hideScrollViewMenu(scrollViewRightMenu);
                if (menuListener != null)
                    menuListener.closeMenu();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    /**
     * A helper method to build scale down animation;
     *
     * @param target
     * @param targetScaleX
     * @param targetScaleY
     * @return
     */
    private AnimatorSet buildScaleDownAnimation(View target, float targetScaleX, float targetScaleY) {
        Log.i(TAG, "ResideMenu:buildScaleDownAnimation:");
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", 0, targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", 0, targetScaleY)
        );

        if (mUse3D) {
            int angle = scaleDirection == DIRECTION_LEFT ? -ROTATE_Y_ANGLE : ROTATE_Y_ANGLE;
            scaleDown.playTogether(ObjectAnimator.ofFloat(target, "rotationY", angle));
        }

        scaleDown.setInterpolator(AnimationUtils.loadInterpolator(this.activity, android.R.anim.decelerate_interpolator));
        scaleDown.setDuration(250);
        return scaleDown;
    }

    /**
     * A helper method to build scale up animation;
     *
     * @param target
     * @param targetScaleX
     * @param targetScaleY
     * @return
     */
    private AnimatorSet buildScaleUpAnimation(View target, float targetScaleX, float targetScaleY) {
        Log.i(TAG, "ResideMenu:buildScaleUpAnimation");
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );

        if (mUse3D) {
            scaleUp.playTogether(ObjectAnimator.ofFloat(target, "rotationY", 0));
        }

        scaleUp.setDuration(250);
        return scaleUp;
    }

    private AnimatorSet buildMenuAnimation(View target, float alpha) {
        Log.i(TAG, "ResideMenu:buildMenuAnimation");
        AnimatorSet alphaAnimation = new AnimatorSet();
        alphaAnimation.playTogether(
                ObjectAnimator.ofFloat(target, "alpha", alpha)
        );

        alphaAnimation.setDuration(250);
        return alphaAnimation;
    }

    /**
     * If there were some view you don't want reside menu
     * to intercept their touch event, you could add it to
     * ignored views.
     *
     * @param v
     */
    public void addIgnoredView(View v) {
        Log.i(TAG, "ResideMenu:addIgnoredView");
        ignoredViews.add(v);
    }

    /**
     * Remove a view from ignored views;
     *
     * @param v
     */
    public void removeIgnoredView(View v) {
        Log.i(TAG, "ResideMenu:removeIgnoredView");
        ignoredViews.remove(v);
    }

    /**
     * Clear the ignored view list;
     */
    public void clearIgnoredViewList() {
        Log.i(TAG, "ResideMenu:clearIgnoredViewList");
        ignoredViews.clear();
    }

    /**
     * If the motion event was relative to the view
     * which in ignored view list,return true;
     *
     * @param ev
     * @return
     */
    private boolean isInIgnoredView(MotionEvent ev) {
        Log.i(TAG, "ResideMenu:isIngnoredView");
        Rect rect = new Rect();
        for (View v : ignoredViews) {
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return true;
        }
        return false;
    }

    private void setScaleDirectionByRawX(float currentRawX) {
        Log.i(TAG, "ResideMenu:setScaleDirectionByRawX");
        if (currentRawX < lastRawX)
            setScaleDirection(DIRECTION_RIGHT);
        else
            setScaleDirection(DIRECTION_LEFT);
    }

    private float getTargetScale(float currentRawX) {
        Log.i(TAG, "ResideMenu:getTargetScale");
        float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * 0.75f;
        scaleFloatX = scaleDirection == DIRECTION_RIGHT ? -scaleFloatX : scaleFloatX;

        float targetScale = ViewHelper.getScaleX(viewActivity) - scaleFloatX;
        targetScale = targetScale > 1.0f ? 1.0f : targetScale;
        targetScale = targetScale < 0.5f ? 0.5f : targetScale;
        return targetScale;
    }

    private float lastActionDownX, lastActionDownY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i(TAG, "ResideMenu:dispatchTouchEvent");
        Log.i(TAG1, "start");
        float currentActivityScaleX = ViewHelper.getScaleX(viewActivity);
        Log.i(TAG1, "currentActivityScaleX:" + currentActivityScaleX);
        if (currentActivityScaleX == 1.0f)
            setScaleDirectionByRawX(ev.getRawX());

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG1, "action_down");
                lastActionDownX = ev.getX();
                lastActionDownY = ev.getY();
                Log.i(TAG1, "lastx,y:" + lastActionDownX + "," + lastActionDownY);
                isInIgnoredView = isInIgnoredView(ev) && !isOpened();
                Log.i(TAG1, "isIngnoredView:" + isInIgnoredView);
                pressedState = PRESSED_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                Log.i(TAG1, "action_move");
                if (isInIgnoredView || isInDisableDirection(scaleDirection))
                    break;
                Log.i(TAG1, "isIngnoreView1");
                if (pressedState != PRESSED_DOWN &&
                        pressedState != PRESSED_MOVE_HORIZONTAL)
                    break;

                Log.i(TAG1, "pressedState");

                int xOffset = (int) (ev.getX() - lastActionDownX);
                int yOffset = (int) (ev.getY() - lastActionDownY);

                Log.i(TAG1, "xOffset:" + xOffset + ",yOffset:" + yOffset);

                if (pressedState == PRESSED_DOWN) {
                    Log.i(TAG1, "pressed_down");
                    if (yOffset > 25 || yOffset < -25) {
                        pressedState = PRESSED_MOVE_VERTICAL;
                        break;
                    }
                    Log.i(TAG1, "pressedState1:" + pressedState);
                    if (xOffset < -50 || xOffset > 50) {
                        pressedState = PRESSED_MOVE_HORIZONTAL;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    Log.i(TAG1, "pressedState2:" + pressedState);
                } else if (pressedState == PRESSED_MOVE_HORIZONTAL) {
                    Log.i(TAG1, "pressed_move_horizontal");
                    if (currentActivityScaleX < 0.95) {
                        Log.i(TAG1, "currentActivityScaleX:" + currentActivityScaleX);
                        showScrollViewMenu(scrollViewMenu);
                    }

                    float targetScale = getTargetScale(ev.getRawX());
                    if (mUse3D) {
                        Log.i(TAG1, "muse3d");
                        int angle = scaleDirection == DIRECTION_LEFT ? -ROTATE_Y_ANGLE : ROTATE_Y_ANGLE;
                        angle *= (1 - targetScale) * 2;
                        ViewHelper.setRotationY(viewActivity, angle);

                        ViewHelper.setScaleX(imageViewShadow, targetScale - shadowAdjustScaleX);
                        ViewHelper.setScaleY(imageViewShadow, targetScale - shadowAdjustScaleY);
                    } else {
                        ViewHelper.setScaleX(imageViewShadow, targetScale + shadowAdjustScaleX);
                        ViewHelper.setScaleY(imageViewShadow, targetScale + shadowAdjustScaleY);
                    }
                    ViewHelper.setScaleX(viewActivity, targetScale);
                    ViewHelper.setScaleY(viewActivity, targetScale);
                    ViewHelper.setAlpha(scrollViewMenu, (1 - targetScale) * 2.0f);

                    lastRawX = ev.getRawX();
                    Log.i(TAG1, "lastRawX:" + lastRawX);
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:
                Log.i(TAG1, "action_up");
                if (isInIgnoredView) break;
                if (pressedState != PRESSED_MOVE_HORIZONTAL) break;

                Log.i(TAG1, "pressedState:" + pressedState);

                pressedState = PRESSED_DONE;
                if (isOpened()) {
                    Log.i(TAG1, "isopened:" + currentActivityScaleX);
                    if (currentActivityScaleX > 0.56f)
                        closeMenu();
                    else
                        openMenu(scaleDirection);
                } else {
                    Log.i(TAG1, "!isopened:" + currentActivityScaleX);
                    if (currentActivityScaleX < 0.94f) {
                        openMenu(scaleDirection);
                    } else {
                        closeMenu();
                    }
                }

                break;

        }
        lastRawX = ev.getRawX();
        return super.dispatchTouchEvent(ev);
    }

    public int getScreenHeight() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        Log.i(TAG, "ResideMenu:getScreenWidth");
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public void setScaleValue(float scaleValue) {
        Log.i(TAG, "ResideMenu:setScaleValue");
        this.mScaleValue = scaleValue;
    }

    public void setUse3D(boolean use3D) {
        Log.i(TAG, "ResideMenu:setUse3D");
        mUse3D = use3D;
    }

    public interface OnMenuListener {

        /**
         * This method will be called at the finished time of opening menu animations.
         */
        public void openMenu();

        /**
         * This method will be called at the finished time of closing menu animations.
         */
        public void closeMenu();
    }

    private void showScrollViewMenu(View scrollViewMenu) {
        Log.i(TAG, "ResideMenu:showScrollViewMenu");
        if (scrollViewMenu != null && scrollViewMenu.getParent() == null) {
            addView(scrollViewMenu);
        }
    }

    private void hideScrollViewMenu(View scrollViewMenu) {
        Log.i(TAG, "ResideMenu:showScrollViewMenu");
        if (scrollViewMenu != null && scrollViewMenu.getParent() != null) {
            removeView(scrollViewMenu);
        }
    }
}
