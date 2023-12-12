package lk.javainstitute.techstore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import lk.javainstitute.techstore.model.User;
import lk.javainstitute.techstore.utill.Encryption;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainLayoutActivity.class));
        } else {

            signInClient = Identity.getSignInClient(getApplicationContext());

            findViewById(R.id.googleSignIn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                            .setServerClientId(getString(R.string.web_client_id)).build();

                    Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                    signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                        @Override
                        public void onSuccess(PendingIntent pendingIntent) {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest
                                    .Builder(pendingIntent).build();
                            signInLauncher.launch(intentSenderRequest);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            });

            findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                }
            });

            EditText editEmail = findViewById(R.id.email);
            EditText editPassword = findViewById(R.id.password);
            EditText editCpassword = findViewById(R.id.cpassword);
            EditText editName = findViewById(R.id.name);
            EditText editMobile = findViewById(R.id.mobile);

            findViewById(R.id.registerBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = editEmail.getText().toString();
                    String password = editPassword.getText().toString();
                    String cpassword = editCpassword.getText().toString();
                    String name = editName.getText().toString();
                    String mobile = editMobile.getText().toString();

                    if (name.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter name", Toast.LENGTH_SHORT).show();
                    } else if (mobile.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter mobile", Toast.LENGTH_SHORT).show();
                    } else if (email.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter email", Toast.LENGTH_SHORT).show();
                    } else if (password.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                    } else if (!password.equals(cpassword)) {
                        Toast.makeText(getApplicationContext(), "Not maching your confirm password.", Toast.LENGTH_SHORT).show();
                    } else {

                        firestore.collection("users").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                    boolean isexist = false;

                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                            User exitsUser = snapshot.toObject(User.class);
                                            if (email.equals(exitsUser.getEmail())) {
                                                isexist = true;
                                            }
                                        }


                                        if (isexist) {
                                            Toast.makeText(SignUpActivity.this, "This email already exists", Toast.LENGTH_SHORT).show();
                                        } else {
                                            firebaseAuth.createUserWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (task.isSuccessful()) {

                                                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                                                user.sendEmailVerification();

                                                                String ref = String.valueOf(System.currentTimeMillis());

                                                                User newUser = new User(ref, null, name, null, email, mobile, "");

                                                                firestore.collection("users").add(newUser)
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                Toast.makeText(SignUpActivity.this, "Register Success", Toast.LENGTH_LONG);
                                                                                startActivity(new Intent(SignUpActivity.this, MainLayoutActivity.class));
                                                                                finish();
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });


                                                            } else {
                                                                Toast.makeText(SignUpActivity.this, "Try Again Later.", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                });


                    }


                }
            });


            findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                }
            });

        }

    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(authCredential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    String ref = String.valueOf(System.currentTimeMillis());

                    User newUser = new User(ref, null, user.getDisplayName(), null, user.getEmail(), ((user.getPhoneNumber() == "null") ? null : user.getPhoneNumber()), null);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            firestore.collection("users").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                        boolean isexist = false;

                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                User exitsUser = snapshot.toObject(User.class);
                                                if (user.getEmail().equals(exitsUser.getEmail())) {
                                                    isexist = true;
                                                }

                                                if (!isexist) {
                                                    firestore.collection("users").add(newUser)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Toast.makeText(SignUpActivity.this, "Register Success", Toast.LENGTH_LONG);
                                                                    startActivity(new Intent(SignUpActivity.this, MainLayoutActivity.class));
                                                                    finish();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }

                                                updateUI(user);

                                                break;
                                            }
                                        }
                                    });
                        }
                    }).start();


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(SignUpActivity.this, MainLayoutActivity.class));
        }

    }

    private void handleSignInResult(Intent intent) {
        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
        }
    }


    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    handleSignInResult(o.getData());
                }
            }
    );

}