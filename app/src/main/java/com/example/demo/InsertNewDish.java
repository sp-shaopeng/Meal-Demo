package com.example.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import org.w3c.dom.Text;

import java.util.UUID;

import io.opencensus.common.ServerStatsFieldEnums;

public class InsertNewDish extends AppCompatActivity implements View.OnClickListener {
    private EditText dishName;
    private EditText dishPrice;
    private EditText dishDescription;
    private EditText dishPreparationDuration;
    private TextView imagePrompt;
    private DatabaseReference mDatabase;
    private ImageView imageCapture1;
    private Uri imageUri;
    private ProgressBar progressBar;
    private Uri uploadedImageUri;
    private String dishID;
    private Dish dish;


    //TODO: ACKNOWLEDGEMENT: https://github.com/ArthurHub/Android-Image-Cropper/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_new_dish);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        imagePrompt = findViewById(R.id.ImagePrompt);
        dishName = (EditText) findViewById(R.id.enter_dish_name);
        dishPrice = (EditText) findViewById((R.id.enter_price));
        dishDescription = (EditText) findViewById(R.id.enter_description) ;
        dishPreparationDuration = (EditText) findViewById(R.id.enter_preparation_duration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageCapture1 = (ImageView) findViewById(R.id.photo1);

        if(extras != null){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            dishID = extras.getString("dishID");
            DatabaseReference dishRef = FirebaseDatabase.getInstance().getReference("sellers").child(user.getUid()).child("Dishes").child(dishID);
            dishRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dish = dataSnapshot.getValue(Dish.class);
                    dishName.setText(dish.DishName);
                    dishDescription.setText(dish.DishDescription);
                    dishPrice.setText(dish.DishPrice);
                    dishPreparationDuration.setText(dish.PrepDuration);
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(dish.imageUri);
                    GlideApp.with(getApplicationContext() /* context */)
                            .load(storageRef)
                            .into(imageCapture1);
                    imagePrompt.setText("Tap to change photo");
                    Button button = findViewById(R.id.Post);
                    button.setText("Update dish");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
//        FloatingActionButton takePhoto = findViewById(R.id.take_photo);
//        takePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                CropImage.activity()
//                        .setFixAspectRatio(true)
//                        .setAspectRatio(1,1)
//                        .setMinCropResultSize(128,128)
//                        .setInitialCropWindowPaddingRatio(0)
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setCropMenuCropButtonTitle("Submit")
//                        .start(InsertNewDish.this);
//            }
//        });

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
                                    imagePrompt.setText("Tap to delete");

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

                }else{
                    CropImage.activity()
                            .setFixAspectRatio(true)
                            .setAspectRatio(1,1)
                            .setMinCropResultSize(128,128)
                            .setInitialCropWindowPaddingRatio(0)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropMenuCropButtonTitle("Submit")
                            .start(InsertNewDish.this);

                }
            }
        });


        dishPreparationDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPicker Np = new NumberPicker(InsertNewDish.this);


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
                        imageUri = result.getUri();
                        // get the cropped bitmap
                        Bitmap thePic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imageCapture1.setImageBitmap(thePic);
                        imagePrompt.setText("Tap to delete");
                    }
                }catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
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
            if(dish == null){
                DatabaseReference DishRef = mDatabase.child("sellers").child(userID).child("Dishes").push();
                String imageRef = "dish_images/" + UUID.randomUUID().toString();
                StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference(imageRef);
                imageStorageReference.putFile(imageUri);
                Dish dish = new Dish(name,desc,price, DishRef.getKey(), imageRef, preparationDuration);
                DishRef.setValue(dish).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InsertNewDish.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InsertNewDish.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                dish.DishName = name;
                dish.DishPrice = price;
                dish.DishDescription = desc;
                dish.PrepDuration = preparationDuration;
                if(imageUri != null){
                    FirebaseStorage.getInstance().getReference().child("dish_images/" + dish.imageUri).delete();
                    String imageRef = "dish_images/" + UUID.randomUUID().toString();
                    StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference(imageRef);
                    imageStorageReference.putFile(imageUri);
                    dish.imageUri = imageRef;
                }
                DatabaseReference DishRef = mDatabase.child("sellers").child(userID).child("Dishes").child(dishID);
                DishRef.setValue(dish);
            }


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