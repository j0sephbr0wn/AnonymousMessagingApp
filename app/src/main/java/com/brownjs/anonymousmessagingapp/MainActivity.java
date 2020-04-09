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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends MyAppActivity {

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

        // get layout elements
        final CircleImageView profile_image = findViewById(R.id.profile_image);
        final TextView textView_username = findViewById(R.id.username);
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // hide tabs until we know if it needs to be shown
        tabLayout.setVisibility(View.GONE);

        // get Firebase current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        startLoadingAnimation();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // load current user in model class
                User currentUser = dataSnapshot.getValue(User.class);

                textView_username.setText(currentUser.getUsername());

                if (currentUser.getImageURL().equals("default")) {
                    Random random = new Random();
                    int randomInt = random.nextInt(3);

                    switch (randomInt) {
                        case 0:
                            profile_image.setImageResource(R.drawable.spade_green);
                            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorZestGreen));
                            break;

                        case 1:
                            profile_image.setImageResource(R.drawable.spade_purple);
                            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorDeepPurple));
                            break;

                        case 2:
                            profile_image.setImageResource(R.drawable.spade_red);
                            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorTechRed));
                            break;
                    }
                } else {
                    // todo load image
                }


                if (!currentUser.isChampion()) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    viewPagerAdapter.addFragment(new ChampionsFragment(), "Champions");
                    tabLayout.setVisibility(View.VISIBLE);
                }


                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

                endLoadingAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // handle different menu selections
        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                return true;

            case R.id.profile:

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
