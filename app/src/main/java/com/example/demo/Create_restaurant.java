package com.example.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.StringReader;

public class Create_restaurant extends AppCompatActivity {
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restaurant);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button create = (Button) findViewById(R.id.Create_restaurant);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText restaurantName = (EditText) findViewById(R.id.enter_restaurant_name);
                EditText restaurantStreet = findViewById(R.id.enter_street);
                EditText restaurantBlk = findViewById(R.id.enter_blk);
                EditText restaurantPostcode= findViewById(R.id.enter_postcode);
                String Name = restaurantName.getText().toString();
                String Street = restaurantStreet.getText().toString();
                String Blk = restaurantBlk.getText().toString();
                String Postcode = restaurantPostcode.getText().toString();
                if(Name.isEmpty() || Street.isEmpty() || Blk.isEmpty() || Postcode.isEmpty()){
                    // do something here
                }else{
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String userID = user.getUid();
                    String name = restaurantName.getText().toString();
                    String email = user.getEmail();
                    StringBuilder addressBuilder = new StringBuilder();
                    addressBuilder.append(Street+" ").append(Blk+ " ").append(Postcode);
                    String address = addressBuilder.toString();
                    Seller seller = new Seller(email, name, address);
                    mDatabase.child("sellers").child(userID).setValue(seller);
                    Intent intent = new Intent(Create_restaurant.this, InitialActivity.class);
                    startActivity(intent);
                }
            }
        });

    }
}