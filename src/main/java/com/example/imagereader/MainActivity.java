package com.example.imagereader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener listener;


    private ProgressBar progressBar;
    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = findViewById(R.id.email_signIn_button);
        Button createAccount = findViewById(R.id.email_create_button);

        progressBar=findViewById(R.id.login_progress);
        emailAddress=findViewById(R.id.email);
        password=findViewById(R.id.password);
        firebaseAuth=FirebaseAuth.getInstance();

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        listener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    user=firebaseAuth.getCurrentUser();
                    final String currentUserId = user.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                            }
                            assert value != null;
                            if (!value.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : value) {
                                    ImageApi imageApi = ImageApi.getInstance();
                                    imageApi.setText(snapshot.getString("username"));
                                    imageApi.setUserId(currentUserId);

                                    startActivity(new Intent(MainActivity.this, ImageReader.class));
                                    finish();
                                }
                            }
                        }
                    });
                }
            }
        };

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Create_account_activity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),password.getText().toString().trim());
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(listener);
        }
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        if(!TextUtils.isEmpty(email)&&
                !TextUtils.isEmpty(pwd)){
            firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser user=firebaseAuth.getCurrentUser();
                    assert user!=null;
                    final String currentUserId=user.getUid();

                    collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            assert value != null;
                            if (!value.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : value) {
                                    ImageApi imageApi = ImageApi.getInstance();
                                    imageApi.setText(snapshot.getString("username"));
                                    imageApi.setUserId(currentUserId);

                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(MainActivity.this, ImageReader.class));
                                    finish();
                                }
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"Enter correct email address and password",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });

        }else{
            Toast.makeText(MainActivity.this,"Enter Email and Password",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}