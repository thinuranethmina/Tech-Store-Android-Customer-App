package lk.javainstitute.techstore;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import lk.javainstitute.techstore.model.User;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(Color.RED)
                .borderWidthDp(2)
                .cornerRadiusDp(100)
                .oval(false)
                .build();


        if (firebaseAuth.getCurrentUser() != null) {
            if (firebaseAuth.getCurrentUser().isEmailVerified()) {

                firestore.collection("users")
                        .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                String userId = documentSnapshot.getId();
                                    User user = documentSnapshot.toObject(User.class);

                                    TextView emailview = view.findViewById(R.id.email);
                                    emailview.setText(user.getEmail());

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
                                                                        .transform(transformation)
                                                                        .into((ImageView) view.findViewById(R.id.imageButton1));
                                                            }
                                                        });
                                            }
                                        }).start();
                                    } else {
                                        Picasso.get()
                                                .load(R.drawable.user)
                                                .fit()
                                                .into((ImageView) view.findViewById(R.id.imageButton1));
                                    }

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(400);
                                            } catch (
                                                    InterruptedException e) {
                                            }
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getActivity().findViewById(R.id.loader).setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }).start();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity().getApplicationContext(), SettingsActivity.class));
                    }
                });

                view.findViewById(R.id.orders).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity().getApplicationContext(), OrderHistoryActivity.class));
                    }
                });

                view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseAuth.signOut();
                        startActivity(new Intent(getActivity().getApplicationContext(), SettingsActivity.class));
                        getActivity().finish();
                    }
                });


            } else {
                firebaseAuth.getCurrentUser().sendEmailVerification();
                Toast.makeText(getActivity().getApplicationContext(), "Check your email verify your account.", Toast.LENGTH_LONG).show();

            }
        } else {
            startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
            Toast.makeText(getActivity().getApplicationContext(), "Please signin first.", Toast.LENGTH_LONG).show();

        }


//                firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail())
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(getActivity().getApplicationContext(), "Check your email inbox for reset password", Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(getActivity().getApplicationContext(), "Please try again later", Toast.LENGTH_LONG).show();
//                            }
//                        });


        return view;
    }
}