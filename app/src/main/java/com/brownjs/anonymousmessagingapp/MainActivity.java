package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends MyAppActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

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
        final FloatingActionButton fab_new_message = findViewById(R.id.fab_new_message);

        // on click listeners for fab
        fab_new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showNewMessageDialog();
                startActivity(new Intent(MainActivity.this, MessageActivity.class));
            }
        });

        // hide tabs until we know if it needs to be shown
        tabLayout.setVisibility(View.GONE);

        // get Firebase current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());

        // show animation while loading data
//        startLoadingAnimation();

        // get user information from document store (only do this once)
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // get layout elements
                CircleImageView profile_image = findViewById(R.id.profile_image);
                TextView textView_username = findViewById(R.id.username);

                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);

                // set username
                assert currentUser != null;
                textView_username.setText(currentUser.getUsername());

                // set profile image
                // use default if image not set
                if (currentUser.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.spade_red);
                }
                // load image if set
                else {
                    Glide.with(getApplicationContext())
                            .load(currentUser.getImageURL())
                            .into(profile_image);
                }

                // if user is champion show two fragments
                if (currentUser.isChampion()) {
                    fab_new_message.hide();
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    viewPagerAdapter.addFragment(new ChampionsFragment(), "Champions");
                    tabLayout.setVisibility(View.VISIBLE);
                }
                // if user is not a champion show single fragment
                else {
                    fab_new_message.show();
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");

                    //subscribe to AnonymousUsers topic to receive notifications
                    subscribeToStatusChanges();
                }

                // set fragments in the activity
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

                // work done, end loading animation
//                endLoadingAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // get user information from document store
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);

                // if user is not a champion hide unwanted options
                assert currentUser != null;
                if (!currentUser.isChampion()) {
                    MenuItem adminItem = menu.findItem(R.id.admin);
                    MenuItem profileItem = menu.findItem(R.id.profile);
                    adminItem.setVisible(false);
                    profileItem.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

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

    private void status(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        status("offline");
    }

    private void subscribeToStatusChanges() {
        FirebaseMessaging.getInstance().subscribeToTopic("anonymousUsers")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(MainActivity.this, "Subscribe successful", Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
    }

    /**
     *
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
