package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SellerRestaurantDishEditor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_restaurant_dish_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.new_dish);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellerRestaurantDishEditor.this, InsertNewDish.class);
                startActivity(intent);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dishRef = FirebaseDatabase.getInstance().getReference("sellers").child(user.getUid())
                .child("Dishes");
        dishRef.addValueEventListener(new ValueEventListener() {
            List<SellerRestaurantDishEditorData> items = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Dish dish = dataSnapshot1.getValue(Dish.class);
                    SellerRestaurantDishEditorData sellerRestaurantDishEditorData = new SellerRestaurantDishEditorData();
                    sellerRestaurantDishEditorData.cost = dish.DishPrice;
                    sellerRestaurantDishEditorData.dish_name = dish.DishName;
                    items.add(sellerRestaurantDishEditorData);
                }

                RecyclerView editor_List = findViewById(R.id.restaurant_editor_list);
                editor_List.setHasFixedSize(true);
                editor_List.setClickable(true);


                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                editor_List.setLayoutManager(linearLayoutManager);

                SellerRestaurantDishEditorDataAdapter editorDataAdapter = new SellerRestaurantDishEditorDataAdapter(items);
                editor_List.setAdapter(editorDataAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /*
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    */

}