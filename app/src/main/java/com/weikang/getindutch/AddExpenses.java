package com.weikang.getindutch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddExpenses extends AppCompatActivity {

    private Spinner mPayeeSpinner;
    private Spinner mGroupSpinner;
    private EditText mExpense;
    private Button mButtonAdd;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseReference;
    private ChildEventListener mChildEventListener;

    private String selectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expenses_manual);

        //Firebase Auth variables
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        final String mUserId = mUser.getUid();

        mExpense = findViewById(R.id.edittext_Expense);
        //initialise button
        mButtonAdd = findViewById(R.id.button_add);


        //Configuring Payee Spinner
        mPayeeSpinner = findViewById(R.id.spinner_payee);

        //create arrayadapter using String array and a default spinner layout
        ArrayAdapter<CharSequence> payeeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.payee_array, android.R.layout.simple_spinner_item);
        payeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPayeeSpinner.setAdapter(payeeSpinnerAdapter);

        //Configuring Group Spinner
        mGroupSpinner = findViewById(R.id.spinner_group);
        //create arrayadapter using String array and a default spinner layout
        ArrayAdapter<CharSequence> groupSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.group_array, android.R.layout.simple_spinner_item);
        groupSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGroupSpinner.setAdapter(groupSpinnerAdapter);

        mGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id){
                selectedGroup = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView parent){

            }
        });

        mButtonAdd.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Float expense = Float.parseFloat(mExpense.getText().toString());

                //Firebase Database variables
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                //get reference to the group that was selected
                mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(selectedGroup).child("members");

                //get size of group: (sk: added size attribute to group in database for this)
                //use array of size1 to retrieve data from firebase cos of some variables problem
                final int[] sizeOfGroup = new int[1];
                mGroupDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int sizeX = Integer.parseInt(dataSnapshot.child("size").getValue().toString());
                        sizeOfGroup[0] = sizeX;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
                Float expenseToAdd = expense / sizeOfGroup[0];
                //HARDCODED
                mGroupDatabaseReference.child(mUserId).setValue(expense);
                //below is test1 userid
                mGroupDatabaseReference.child("XIihVerGHgRgqKfAeL2vs9gbkf02").setValue(expense * -1);

                //TODO: retrieve size of group, divide expense, reduce users expense, add to other members expense
                //get original balances\
                //long userBalance =

                Intent intent = new Intent(AddExpenses.this,MainPage.class);
                startActivity(intent);
            }
        });
    }


}