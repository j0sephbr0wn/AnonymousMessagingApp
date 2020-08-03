package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.brownjs.anonymousmessagingapp.fragments.ChampionsFragment;
import com.brownjs.anonymousmessagingapp.fragments.ChatsFragment;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity to hold fragments
 */
public class MainActivity extends MyAppActivity {

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup common toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // get layout elements for tabs
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // get layout elements for fab
        final FloatingActionButton fabNewMessage = findViewById(R.id.fab_new_message);

        // on click listeners for fab
        fabNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = FirebaseDatabase.getInstance().getReference("Users")
                        .orderByChild("champion")
                        .equalTo(true);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User latestOnline = null;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            assert user != null;

                            if (latestOnline == null) {
                                latestOnline = user;
                            } else {
                                if (latestOnline.getStatusOnlineTime().before(user.getStatusOnlineTime())) {
                                    latestOnline = user;
                                }
                            }
                        }


                        Intent intent = new Intent(MainActivity.this, SetupChatActivity.class);
                        assert latestOnline != null;
                        intent.putExtra("championId", latestOnline.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        // hide tabs until we know if it needs to be shown
        tabLayout.setVisibility(View.GONE);

        // get Firebase current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());

        // show animation while loading data
        startLoadingAnimation();

        // get user information from document store (only do this once)
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // get layout elements
                CircleImageView imgProfile = findViewById(R.id.profile_image);
                TextView txtUsername = findViewById(R.id.username);

                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);
                assert currentUser != null;
                String userEmail = firebaseUser.getEmail();
                assert userEmail != null;

                // user is a champion
                if (currentUser.isChampion()) {
                    // set username
                    txtUsername.setText(currentUser.getUsername());

                    // set profile
                    Glide.with(getApplicationContext())
                            .load(currentUser.getImageURL())
                            .into(imgProfile);

                    // set champion specific ui elements
                    fabNewMessage.hide();
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    viewPagerAdapter.addFragment(new ChampionsFragment(), "Champions");
                    tabLayout.setVisibility(View.VISIBLE);

                    txtUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("userId", firebaseUser.getUid());
                            startActivity(profileIntent);
                        }
                    });

                    imgProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("userId", firebaseUser.getUid());
                            startActivity(profileIntent);
                        }
                    });
                }
                // user is not a champion
                else {
                    // set username
                    txtUsername.setText(R.string.DEFAULT_USERNAME);

                    // set anon user ui specific elements
                    fabNewMessage.show();
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");

                    //subscribe to AnonymousUsers topic to receive notifications
                    subscribeToStatusChanges();
                }

                refreshToken();

                // set fragments in the activity
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

                // work done, end loading animation
                endLoadingAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Depending on user show available menu items
     * {@inheritDoc}
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        String userEmail = firebaseUser.getEmail();
        assert userEmail != null;

        // if user is not a champion hide unwanted options
        if (!userEmail.endsWith(getChampionEmailSuffix())) {
            MenuItem adminItem = menu.findItem(R.id.admin);
            MenuItem profileItem = menu.findItem(R.id.profile);
            adminItem.setVisible(false);
            profileItem.setVisible(false);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;

        // handle different menu selections
        switch (item.getItemId()) {

            case R.id.profile:

                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                profileIntent.putExtra("userId", firebaseUser.getUid());
                startActivity(profileIntent);

                return true;

            case R.id.admin:
                Toast.makeText(this, "Admin", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.about:
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.logout:

                status("offline");

                // log user out of Firebase account
                FirebaseAuth.getInstance().signOut();

                // navigate to StartActivity, clearing the back stack
                Intent logoutIntent = new Intent(MainActivity.this, StartActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
                finish();

                return true;
        }

        return false;
    }

    /**
     * Subscribe user to notifications of champions coming online
     */
    private void subscribeToStatusChanges() {

        FirebaseMessaging.getInstance().subscribeToTopic("anonymousUsers")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "There was an issue subscribing to notifications", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Refresh token for notifications
     */
    private void refreshToken() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
//        String uid = firebaseUser.getUid();
//        Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        if (task.getResult() != null) {
                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("token", token);

//                            Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                            Log.println(Log.INFO, "TOKEN", token);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid());
                            reference.updateChildren(hashMap);
                        }
                    }
                });
    }

    /**
     *Adapter for view pager
     */
    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        /**
         * @param fm FragmentManager
         */
        ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public int getCount() {
            return fragments.size();
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @return
         */
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        /**
         * @param fragment to be added to adapter
         * @param title    of the fragment to be added
         */
        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @return
         */
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
