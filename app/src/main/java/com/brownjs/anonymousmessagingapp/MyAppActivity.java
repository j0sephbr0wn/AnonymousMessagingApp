package com.brownjs.anonymousmessagingapp;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public abstract class MyAppActivity extends AppCompatActivity {

    public MyAppActivity() {
    }

    /**
     * @return a id ...
     */
    public String buildPseudoId() {
        // build a (nearly) unique id from the users hardware
        return "00" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10 + "@capgemini.com";
    }

    /**
     * Display loading animation
     *
     * @param animationObject the object (image) that will be animated
     * @param layout the parent layout of the animationObject
     */
    public void startLoading(ImageView animationObject, LinearLayout layout) {

        // build animation
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        // set animation to image and display
        animationObject.startAnimation(rotate);
        layout.setVisibility(View.VISIBLE);

    }

    /**
     * Remove loading animation
     *
     * @param animationObject the object (image) that will be animated
     * @param layout the parent layout of the animationObject
     */
    public void finishLoading(ImageView animationObject, LinearLayout layout) {

        // hide and clear animation
        layout.setVisibility(View.GONE);
        animationObject.clearAnimation();

    }
}
