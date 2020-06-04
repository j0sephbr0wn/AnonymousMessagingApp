package com.brownjs.anonymousmessagingapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

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

public class ProfileActivity extends MyAppActivity {

    private static final int REQUEST_READ_EXTERNAL_PERMISSION = 11;
    private static final int IMAGE_REQUEST = 22;

    private CircleImageView imgProfileImage;
    private CircleImageView imgOnline;
    private CircleImageView imgOffline;
    private TextView txtDisplayName;
    private TextView txtStatus;
    private TextView txtPhoneNumber;
    private TextView txtEmailAddress;
    private TextView txtRole;
    private TextView txtLocation;
    private TextView txtAbout;

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
        imgOnline = findViewById(R.id.online);
        imgOffline = findViewById(R.id.offline);
        txtDisplayName = findViewById(R.id.display_name);
        txtStatus = findViewById(R.id.status);
        txtPhoneNumber = findViewById(R.id.phone_number);
        txtEmailAddress = findViewById(R.id.email_address);
        txtRole = findViewById(R.id.role);
        txtLocation = findViewById(R.id.location);
        txtAbout = findViewById(R.id.about);
        Button btnNewMessage = findViewById(R.id.btn_message);

        // get userId from intent and current userId from Firebase
        final String championId = getIntent().getStringExtra("userId");
        String currentUserId = FirebaseAuth.getInstance().getUid();

        assert championId != null;
        final boolean isCurrentUser = championId.equals(currentUserId);

        if (isCurrentUser) {
            imgProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNewProfileImage();
                }
            });

            imgOnline.setImageResource(R.drawable.ic_edit_blue_24dp);
            imgOffline.setImageResource(R.drawable.ic_edit_blue_24dp);
            btnNewMessage.setVisibility(View.GONE);
        } else {
            btnNewMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, SetupChatActivity.class);
                    intent.putExtra("championId", championId);
                    startActivity(intent);
                }
            });
        }

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("id")
                .equalTo(championId)
                .limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    txtDisplayName.setText(user.getUsername());
                    txtPhoneNumber.setText(user.getPhone());
                    txtEmailAddress.setText(user.getEmail());
                    txtRole.setText(user.getRole());
                    txtLocation.setText(user.getLocation());
                    txtAbout.setText(user.getDescription());

                    // set profile image
                    if (user.getImageURL().equals("default")) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.spade_red)
                                .into(imgProfileImage);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(user.getImageURL())
                                .into(imgProfileImage);
                    }

                    // online or offline
                    if (user.getStatus().equals("online")) {
                        imgOnline.setVisibility(View.VISIBLE);
                        imgOffline.setVisibility(View.GONE);
                        String statusText = "Online now";
                        txtStatus.setText(statusText);
                    } else {
                        imgOnline.setVisibility(View.GONE);
                        imgOffline.setVisibility(View.VISIBLE);
                        String statusText = "Offline";
                        txtStatus.setText(statusText);
                    }

//                    if (isCurrentUser) {
//                        txtDisplayName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_blue_24dp, 0, 0, 0);
//                        txtEmailAddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_blue_24dp, 0, 0, 0);
//                    }
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

        // get permissions to read external
        int readPermissions = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readPermissions != PackageManager.PERMISSION_GRANTED) {
            try {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                    Toast.makeText(ProfileActivity.this, "Upload unsuccessful", Toast.LENGTH_SHORT).show();
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
