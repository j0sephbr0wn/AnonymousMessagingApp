package com.brownjs.anonymousmessagingapp;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public abstract class MyAppActivity extends AppCompatActivity {

    private static final String DEFAULT_USERNAME = "Anonymous User";
    private static final String DEFAULT_PASSWORD = "default_password";
    private static final String PSEUDO_ID = "00" +
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
            Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
            Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
            Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
            Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
            Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
            Build.USER.length() % 10;

    private static final String ANONYMOUS_EMAIL_SUFFIX = "@anonymous.com";
    private static final String CHAMPION_EMAIL_SUFFIX = "@capgemini.com";


    public MyAppActivity() {
    }

    /**
     * @return a id ...
     */
    String getDefaultUsername() {

        return DEFAULT_USERNAME;
    }

    /**
     * @return a id ...
     */
    String getDefaultPassword() {

        return DEFAULT_PASSWORD;
    }

    /**
     * @return a id ...
     */
    String getPseudoId() {
        // return a (nearly) unique id from the users hardware
        return PSEUDO_ID + ANONYMOUS_EMAIL_SUFFIX;
    }

    String getChampionEmailSuffix() {
        return CHAMPION_EMAIL_SUFFIX;
    }

    /**
     * Display loading animation
     */
    void startLoadingAnimation() {

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
    void endLoadingAnimation() {

        ImageView animationObject = findViewById(R.id.loading_spade);
        LinearLayout layout = findViewById(R.id.loading_page);

        // hide and clear animation
        layout.setVisibility(View.GONE);
        animationObject.clearAnimation();

    }

    void status(final String status) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();
            assert userEmail != null;
            boolean isChampion = userEmail.endsWith(CHAMPION_EMAIL_SUFFIX);

            // update status if champion
            if (isChampion) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", status);

                if (status.equals("online")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
                    String currentDate = sdf.format(System.currentTimeMillis());
                    hashMap.put("status_online_time", currentDate);
                }

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(firebaseUser.getUid());
                reference.updateChildren(hashMap);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        status("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();

        status("online");
    }
}
