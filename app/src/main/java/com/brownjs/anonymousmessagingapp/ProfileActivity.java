package com.brownjs.anonymousmessagingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 1;

    private CircleImageView imgProfileImage;
    private TextView txtDisplayName;
    private TextView txtPhoneNumber;
    private TextView txtEmailAddress;
    private TextView txtAbout;
    private Button btnNewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get layout elements
        imgProfileImage = findViewById(R.id.profile_image);
        txtDisplayName = findViewById(R.id.display_name);
        txtPhoneNumber = findViewById(R.id.phone_number);
        txtEmailAddress = findViewById(R.id.email_address);
        txtAbout = findViewById(R.id.about);
        btnNewMessage = findViewById(R.id.btn_message);

        imgProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewProfileImage();
            }
        });

        // get userId from intent and current userId from Firebase
        String userId = getIntent().getStringExtra("userId");
        String currentUserId = FirebaseAuth.getInstance().getUid();

        assert userId != null;
        final boolean isCurrentUser = userId.equals(currentUserId);

        if (isCurrentUser) btnNewMessage.setVisibility(View.GONE);

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("id")
                .equalTo(userId)
                .limitToFirst(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    txtDisplayName.setText(user.getUsername());
                    txtPhoneNumber.setText(user.getPhone());
                    txtEmailAddress.setText(user.getEmail());
                    txtAbout.setText(user.getDescription());
                    if (user.getImageURL().equals("default")) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.spade_red)
                                .into(imgProfileImage);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(user.getImageURL())
                                .into(imgProfileImage);
                    }

                    if (isCurrentUser) {
//                        txtDisplayName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_blue_24dp, 0, 0, 0);
//                        txtEmailAddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_blue_24dp, 0, 0, 0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Function is overwritten to give the same animation as using the device 'Back' command
     * {@inheritDoc}
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        onBackPressed();
        return true;
    }

    // create intent for image request
    private void setNewProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    // result for image request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri imageUri;

        // set data if request was successful
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (imageUri != null) {
                uploadNewProfileImage(imageUri);
            } else {
                //TODO something went wrong
            }
        }

//        if (uploadTask != null && uploadTask.isInProgress()) {
//            Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
//        } else {
//            uploadImage();
//        }
    }

    private void uploadNewProfileImage(Uri imageUri) {
        // show upload in progress
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads");
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));

        StorageTask<UploadTask.TaskSnapshot> uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", mUri);
                    reference.updateChildren(map);

                } else {
//                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    // get the file extension from a uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
