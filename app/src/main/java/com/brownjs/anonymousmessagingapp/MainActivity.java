package com.brownjs.anonymousmessagingapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends MyAppActivity {

    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup common_toolbar
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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        fab_new_message.show();
                        break;

                    case 1:
                        fab_new_message.hide();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // get Firebase current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        // show loading animation while loading data
        startLoadingAnimation();

        // get user information from document store
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // get layout elements
                CircleImageView profile_image = findViewById(R.id.profile_image);
                TextView textView_username = findViewById(R.id.username);

                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);

                // set username
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

                // if user is not a champion show single fragment
                if (!currentUser.isChampion()) {

                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                }
                // if user is champion show two fragments
                else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    viewPagerAdapter.addFragment(new ChampionsFragment(), "Champions");
                    tabLayout.setVisibility(View.VISIBLE);
                }

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

//    public void showNewMessageDialog() {
//
//        final Dialog dialog = new Dialog(MainActivity.this);
//        dialog.setContentView(R.layout.dialog_new_message);
//
//        ImageView close_new_message = dialog.findViewById(R.id.close_new_message);
//        final EditText editText_new_subject = dialog.findViewById(R.id.editText_new_subject);
//        Button btn_new_message = dialog.findViewById(R.id.btn_new_message);
//
//        close_new_message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        btn_new_message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String subject = editText_new_subject.getText().toString();
//
//                if (subject.isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Please enter a subject.", Toast.LENGTH_SHORT).show();
//                } else {
//                    //todo pass subject to new activity
//                    startActivity(new Intent(MainActivity.this, MessageActivity.class));
//                    dialog.dismiss();
//                }
//            }
//        });
//
//        dialog.show();
//    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // get user information from document store
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);

                // if user is not a champion hide the "Administration" option
                if (!currentUser.isChampion()) {
                    MenuItem adminItem = menu.findItem(R.id.admin);
                    adminItem.setVisible(false);
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

                return true;

            case R.id.admin:

                return true;

            case R.id.logout:
                // log user out of Firebase account
                FirebaseAuth.getInstance().signOut();

                // navigate to StartActivity, clearing the back stack
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                return true;
        }

        return false;
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
