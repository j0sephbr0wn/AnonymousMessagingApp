package com.brownjs.anonymousmessagingapp;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class MyAppActivity extends AppCompatActivity {

//    private int onStartCount = 0;

    public MyAppActivity() {
    }


//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        onStartCount = 1;
//        if (savedInstanceState == null)
//        {
//            this.overridePendingTransition(R.anim.anim_slide_in_left,
//                    R.anim.anim_slide_out_left);
//        } else // already created so reverse animation
//        {
//            onStartCount = 2;
//        }
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (onStartCount > 1) {
//            this.overridePendingTransition(R.anim.anim_slide_in_right,
//                    R.anim.anim_slide_out_right);
//
//        } else if (onStartCount == 1) {
//            onStartCount++;
//        }
//    }

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
                Build.USER.length() % 10 + "@anonymous.com";
    }

    /**
     * Display loading animation
     */
    public void startLoadingAnimation() {

        ImageView animationObject = findViewById(R.id.loading_spade);
        LinearLayout layout = findViewById(R.id.loading_page);

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
     */
    public void endLoadingAnimation() {

        ImageView animationObject = findViewById(R.id.loading_spade);
        LinearLayout layout = findViewById(R.id.loading_page);

        // hide and clear animation
        layout.setVisibility(View.GONE);
        animationObject.clearAnimation();

    }
}
