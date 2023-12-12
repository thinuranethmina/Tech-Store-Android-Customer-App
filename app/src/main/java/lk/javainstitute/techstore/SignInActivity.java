package lk.javainstitute.techstore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
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
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import lk.javainstitute.techstore.model.User;
import lk.javainstitute.techstore.utill.Encryption;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainLayoutActivity.class));
        } else {

            EditText editEmail = findViewById(R.id.email);
            EditText editPassword = findViewById(R.id.password);

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

            findViewById(R.id.registerBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                }
            });

            findViewById(R.id.signinBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String email = editEmail.getText().toString();
                    String password = Encryption.encrypt(editPassword.getText().toString());

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(getApplicationContext(), "Verified", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignInActivity.this, MainLayoutActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Not Verified", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid Details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainLayoutActivity.class));
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
                                                                    Toast.makeText(SignInActivity.this, "Register Success", Toast.LENGTH_LONG);
                                                                    startActivity(new Intent(SignInActivity.this, MainLayoutActivity.class));
                                                                    finish();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(SignInActivity.this, MainLayoutActivity.class));
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