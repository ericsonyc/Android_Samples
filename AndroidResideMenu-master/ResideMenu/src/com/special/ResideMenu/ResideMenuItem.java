package com.special.ResideMenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午11:05
 * Mail: specialcyci@gmail.com
 */
public class ResideMenuItem extends LinearLayout{

    private static final String TAG = "MenuActivity";

    /** menu item  icon  */
    private ImageView iv_icon;
    /** menu item  title */
    private TextView tv_title;

    public ResideMenuItem(Context context) {
        super(context);
        Log.i(TAG, "ResideMenuItem:Constructor");
        initViews(context);
    }

    public ResideMenuItem(Context context, int icon, int title) {
        super(context);
        Log.i(TAG, "ResideMenuItem:Constructor2:");
        initViews(context);
//        iv_icon.setImageResource(icon);
//        tv_title.setText(title);
        this.setIcon(icon);
        this.setTitle(title);
    }

    public ResideMenuItem(Context context, int icon, String title) {
        super(context);
        Log.i(TAG, "ResideMenuItem:Constructor3:");
        initViews(context);
        this.setIcon(icon);
        this.setTitle(title);
//        tv_title.setText(title);
    }

    private void initViews(Context context){
        Log.i(TAG, "ResideMenuItem:initViews");
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_item, this);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    /**
     * set the icon color;
     *
     * @param icon
     */
    public void setIcon(int icon){
        iv_icon.setImageResource(icon);
    }

    /**
     * set the title with resource
     * ;
     * @param title
     */
    public void setTitle(int title){
        tv_title.setText(title);
    }

    /**
     * set the title with string;
     *
     * @param title
     */
    public void setTitle(String title){
        tv_title.setText(title);
    }
}
