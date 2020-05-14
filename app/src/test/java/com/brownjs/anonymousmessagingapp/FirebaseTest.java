package com.brownjs.anonymousmessagingapp;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class FirebaseTest {

    private static FirebaseAuth firebaseAuth;

    @BeforeClass
    public static void setup() {

        // get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

    }



    @AfterClass
    public static void tearDown() {

        firebaseAuth.signOut();

    }


}
