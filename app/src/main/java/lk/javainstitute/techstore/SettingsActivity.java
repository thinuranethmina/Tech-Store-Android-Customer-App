package lk.javainstitute.techstore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.javainstitute.techstore.model.User;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private Uri imagePath;
    private User user;
    private Transformation transformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText nameView = findViewById(R.id.name);
        TextView emailView = findViewById(R.id.email);
        EditText mobileView = findViewById(R.id.mobile);
        EditText address1View = findViewById(R.id.address1);
        EditText address2View = findViewById(R.id.address2);
        EditText cityView = findViewById(R.id.city);
        EditText postalcodeView = findViewById(R.id.postalcode);

        String name = nameView.getText().toString();
        String address1 = address1View.getText().toString();
        String address2 = address2View.getText().toString();
        String city = cityView.getText().toString();
        String postalcode = postalcodeView.getText().toString();
        String mobile = mobileView.getText().toString();

        transformation = new RoundedTransformationBuilder()
                .borderColor(Color.RED)
                .borderWidthDp(2)
                .cornerRadiusDp(100)
                .oval(false)
                .build();

        findViewById(R.id.imageButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }
        });


//============================ LOAD DATA ==================================================
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            firestore.collection("users")
                    .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                String userId = documentSnapshot.getId();
                                user = documentSnapshot.toObject(User.class);

                                nameView.setText(user.getName());
                                emailView.setText(user.getEmail());

                                if (user.getImage() != null || user.getImage().isEmpty()) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            storage.getReference("user-images/" + user.getImage())
                                                    .getDownloadUrl()
                                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Picasso.get()
                                                                    .load(uri)
                                                                    .fit()
                                                                    .centerCrop()
                                                                    .transform(transformation)
                                                                    .into((ImageView) findViewById(R.id.imageButton1));
                                                        }
                                                    });
                                        }
                                    }).start();
                                } else {
                                    Picasso.get()
                                            .load(R.drawable.user)
                                            .fit()
                                            .centerCrop()
                                            .into((ImageView) findViewById(R.id.imageButton1));
                                }

                                if (user.getMobile() != null) {
                                    mobileView.setText(user.getMobile());
                                }
                                if (user.getAddress() != null) {
                                    String[] address = user.getAddress().split(",");

                                    if (address.length >= 1) {
                                        address1View.setText(address[0]);
                                    }
                                    if (address.length >= 2) {
                                        address2View.setText(address[1]);
                                    }
                                    if (address.length >= 3) {
                                        cityView.setText(address[2]);
                                    }
                                    if (address.length >= 4) {
                                        postalcodeView.setText(address[3]);
                                    }

                                }


                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(400);
                                        } catch (InterruptedException e) {
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                findViewById(R.id.loader).setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }).start();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please Login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
        }


        findViewById(R.id.updateProfileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText nameView = findViewById(R.id.name);
                EditText mobileView = findViewById(R.id.mobile);
                EditText address1View = findViewById(R.id.address1);
                EditText address2View = findViewById(R.id.address2);
                EditText cityView = findViewById(R.id.city);
                EditText postalcodeView = findViewById(R.id.postalcode);

                String name = nameView.getText().toString();
                String address1 = address1View.getText().toString().replace(',', ' ');
                String address2 = address2View.getText().toString().replace(',', ' ');
                String city = cityView.getText().toString().replace(',', ' ');
                String postalcode = postalcodeView.getText().toString().replace(',', ' ');
                String mobile = mobileView.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter Name", Toast.LENGTH_SHORT).show();
                } else if (address1.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter Address line1", Toast.LENGTH_SHORT).show();
                } else if (address2.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter Address line2", Toast.LENGTH_SHORT).show();
                } else if (city.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter City", Toast.LENGTH_SHORT).show();
                } else if (postalcode.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter Postal code", Toast.LENGTH_SHORT).show();
                } else if (mobile.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please enter Mobile", Toast.LENGTH_SHORT).show();
                } else {

                    firestore.collection("users")
                            .whereNotEqualTo("id", user.getId()).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                boolean existsMobile = false;

                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        User exitsItem = snapshot.toObject(User.class);
                                        if (mobile.equals(exitsItem.getMobile())) {
                                            existsMobile = true;
                                        }
                                    }
                                    if (existsMobile) {
                                        Toast.makeText(SettingsActivity.this, "Mobile number already exists", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String ref = String.valueOf(System.currentTimeMillis());

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("nsme", name);
                                        updates.put("address", address1 + "," + address2 + "," + city + "," + postalcode);
                                        updates.put("mobile", mobile);

                                        String image1Id = UUID.randomUUID().toString();

                                        if (imagePath == null) {
                                            updates.put("image", user.getImage());
                                        } else {

                                            updates.put("image", image1Id);
                                        }

                                        firestore.collection("users")
                                                .whereEqualTo("id", user.getId()).get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            List<String> documentIds = new ArrayList<>();

                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                String documentId = document.getId();

                                                                ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
                                                                dialog.setMessage("Adding new item...");
                                                                dialog.setCancelable(false);
                                                                dialog.show();

                                                                firestore.collection("users").document(documentId).update(updates)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                dialog.dismiss();

                                                                                StorageReference storageRef = storage.getReference();


                                                                                if (imagePath != null) {

                                                                                    ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
                                                                                    dialog.setMessage("Uploading");
                                                                                    dialog.setCancelable(false);
                                                                                    dialog.show();

                                                                                    if (user.getImage() != null) {
                                                                                        StorageReference desertRef = storageRef.child("user-images/" + user.getImage());

                                                                                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception exception) {
                                                                                            }
                                                                                        });
                                                                                    }


                                                                                    StorageReference reference = storage.getReference("user-images")
                                                                                            .child(image1Id);
                                                                                    reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                        @Override
                                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                                            dialog.dismiss();
                                                                                            Toast.makeText(getApplicationContext(), "Your profile has been updated successfully", Toast.LENGTH_LONG).show();
                                                                                            finish();
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            dialog.dismiss();
                                                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                                        @Override
                                                                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                                                                            dialog.setMessage("Image Uploading " + (int) progress + "%");
                                                                                        }
                                                                                    });


                                                                                } else {
                                                                                    Toast.makeText(getApplicationContext(), "Your profile has been updated successfully", Toast.LENGTH_LONG).show();
                                                                                    finish();
                                                                                }


                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });


                                                            }

                                                        } else {
                                                        }
                                                    }
                                                });


                                    }

                                }
                            });
                }
            }
        });

        }else{
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == SettingsActivity.this.RESULT_OK) {
                        imagePath = result.getData().getData();

                        Picasso.get()
                                .load(imagePath)
                                .fit()
                                .transform(transformation)
                                .into((ImageView) findViewById(R.id.imageButton1));


                    } else {
                        imagePath = null;
                    }
                }
            }
    );
}