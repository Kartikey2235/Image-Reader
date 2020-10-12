package com.example.imagereader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ImageReader extends AppCompatActivity {
    public static final int GALLERY_CODE = 1;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ImageView showImage;
    private ImageView postImage;
    private TextView AddImage;
    private TextView ShowText;
    private Button getText;
    private Button history;
    private Button addImageButton;
    private Uri imageUrl;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Details");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_reader);

        TextView textView = findViewById(R.id.textView);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        showImage=findViewById(R.id.imageView2);
        postImage=findViewById(R.id.postImage);
        AddImage=findViewById(R.id.post_text);
        ShowText=findViewById(R.id.textView5);
        getText=findViewById(R.id.getTextButton);
        history=findViewById(R.id.history);
        addImageButton=findViewById(R.id.addImageButton);
        String currentUserName = ImageApi.getInstance().getText();
        textView.setText(currentUserName);

        ShowText.setText("");
        getText.setVisibility(View.INVISIBLE);
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                getText.setVisibility(View.VISIBLE);
            }
        });

        getText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getTextFromImage(view);
                getText.setVisibility(View.INVISIBLE);
                addImageButton.setVisibility(View.VISIBLE);
                ShowText.setVisibility(View.VISIBLE);
                Map<String,String> userObj=new HashMap<>();
                userObj.put("title",ShowText.getText().toString());
                userObj.put("userId",user.getUid());
                collectionReference.add(userObj);
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                ShowText.setVisibility(View.INVISIBLE);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ImageReader.this,"This button will show all the image text ever seen from the databse",Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUrl = data.getData();
                showImage.setImageURI(imageUrl);
                postImage.setVisibility(View.INVISIBLE);
                AddImage.setVisibility(View.INVISIBLE);
                getText.setVisibility(View.VISIBLE);
                addImageButton.setVisibility(View.INVISIBLE);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.signout:
                if(user!=null&&firebaseAuth!=null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(ImageReader.this,MainActivity.class));
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getTextFromImage(View v){
        BitmapDrawable drawable = (BitmapDrawable) showImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();

        StringBuilder sb=new StringBuilder();


        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);
            sb.append(textBlock.getValue());
            sb.append("\n");
    }
        ShowText.setText(sb.toString());
}
}