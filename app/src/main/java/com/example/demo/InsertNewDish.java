package com.example.demo;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.StackView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InsertNewDish extends AppCompatActivity implements View.OnClickListener {
    private EditText dishName;
    private EditText dishPrice;
    private EditText dishDescription;
    private EditText dishPreparationDuration;
    private DatabaseReference mDatabase;
    private ImageView imageCapture1;
    private ImageView imageCapture2;
    private ImageView imageCapture3;
    private Button upload;

    //TODO: ACKNOWLEDGEMENT: https://github.com/ArthurHub/Android-Image-Cropper/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_new_dish);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();


        dishName = (EditText) findViewById(R.id.enter_dish_name);
        dishPrice = (EditText) findViewById((R.id.enter_price));
        dishDescription = (EditText) findViewById(R.id.enter_description) ;
        dishPreparationDuration = (EditText) findViewById(R.id.enter_preparation_duration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageCapture1 = (ImageView) findViewById(R.id.photo1);
        imageCapture2 = (ImageView) findViewById(R.id.photo2);
        imageCapture3 = (ImageView) findViewById(R.id.photo3);

        FloatingActionButton takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setFixAspectRatio(true)
                        .setAspectRatio(1,1)
                        .setMinCropResultSize(128,128)
                        .setInitialCropWindowPaddingRatio(0)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropMenuCropButtonTitle("Submit")
                        .start(InsertNewDish.this);
            }
        });

        imageCapture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageCapture1.getDrawable() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InsertNewDish.this);
                    builder.setTitle("Alert")
                            .setMessage("Do you want to delete the photo?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    imageCapture1.setImageDrawable(null);
                                    Toast.makeText(InsertNewDish.this, "Photo Deleted", Toast.LENGTH_SHORT).show();
                                    Reorder(imageCapture1,imageCapture2,imageCapture3);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //meant to be empty
                                }
                            });
                    //Creating dialog box
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });






        findViewById(R.id.Post).setOnClickListener(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                try {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = result.getUri();
                        // get the cropped bitmap
                        Bitmap thePic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imageCapture1.setImageBitmap(thePic);
                    }
                }catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
                }

        }

    }



    private void Reorder(ImageView a, ImageView b, ImageView c){
        if(a.getDrawable() == null){
            if(b.getDrawable()!=null){
                a.setImageDrawable(b.getDrawable());
                if(c.getDrawable() != null){
                    b.setImageDrawable(c.getDrawable());
                    c.setImageDrawable(null);
                }else{
                    b.setImageDrawable(null);
                }
            }
        }else if(b.getDrawable() == null){
            if(c.getDrawable() != null){
                b.setImageDrawable(c.getDrawable());
                c.setImageDrawable(null);
            }
        }
    }

    private boolean validateInputs(String name, String price, String desc,String preparationDuration) {
        if (name.isEmpty()) {
            dishName.setError("Name required");
            dishName.requestFocus();
            return true;
        }

        if (price.isEmpty()) {
            dishPrice.setError("Price required");
            dishPrice.requestFocus();
            return true;
        }

        if (desc.isEmpty()) {
            dishDescription.setError("Description required");
            dishDescription.requestFocus();
            return true;
        }


        if (preparationDuration.isEmpty()) {
            dishPreparationDuration.setError("Preparation duration required");
            dishPreparationDuration.requestFocus();
            return true;
        }
        return false;
    }


    public void onClick(View v) {

        String name = dishName.getText().toString().trim();
        String price = dishPrice.getText().toString().trim();
        String desc = dishDescription.getText().toString().trim();
        String preparationDuration = dishPreparationDuration.getText().toString().trim();

        if (!validateInputs(name, price, desc,preparationDuration)) {
           FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
           String userID = user.getUid();
           DatabaseReference DishRef = mDatabase.child("sellers").child(userID).child("Dishes").push();
            Dish dish = new Dish(name,desc,price, DishRef.getKey());
           DishRef.setValue(dish);
           finish();
//            FSdish.add(dish)
//                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            Toast.makeText(InsertNewDish.this, "Dish Added", Toast.LENGTH_LONG).show();
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(InsertNewDish.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });

        }

    }

}
