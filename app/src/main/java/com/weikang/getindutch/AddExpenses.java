package com.weikang.getindutch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddExpenses extends AppCompatActivity {

    private Spinner mPayeeSpinner;
    private Spinner mGroupSpinner;
    private EditText mExpense;
    private Button mButtonAdd;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserGroupsDatabaseReference;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mGroupSizeDatabaseReference;
    private DatabaseReference mMembersDatabaseReference;
    private ChildEventListener mChildEventListener;

    private String selectedGroup;

    private final String TAG = "AddExpensesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expenses_manual);

        //Firebase Auth variables
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        final String mUserId = mUser.getUid();

        //Firebase Database initialisation
        mFirebaseDatabase = FirebaseDatabase.getInstance();

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
        //create List of groups that user belongs to
        final List<String> groups = new ArrayList<String>();
        mUserGroupsDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mUserId).child("groups");
        mUserGroupsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getKey();
                    groups.add(groupName);
                }

                //create arrayadapter using the list above and a default spinner layout
                ArrayAdapter<String> groupSpinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, groups);
                groupSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mGroupSpinner.setAdapter(groupSpinnerAdapter);

                mGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView parent, View view, int position, long id){
                        selectedGroup = parent.getItemAtPosition(position).toString();
                    }
                    @Override
                    public void onNothingSelected(AdapterView parent){}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mButtonAdd.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final Float expense = Float.parseFloat(mExpense.getText().toString());

                //Firebase Database variables

                //get reference to the group that was selected
                mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(selectedGroup);
                mGroupSizeDatabaseReference = mGroupDatabaseReference.child("size");
                mMembersDatabaseReference = mGroupDatabaseReference.child("members");

                //get size of group: (sk: added size attribute to group in database for this)
                //use array of size1 to retrieve data from firebase cos of some variables problem
                final int[] sizeOfGroup = new int[1];
                mGroupDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int sizeX = Integer.valueOf(dataSnapshot.child("size").getValue().toString());
                        sizeOfGroup[0] = sizeX;

                        //Iterating through each user to add the expense
                        final Float expenseToAdd = expense / sizeOfGroup[0];

                        //iterating through each member in the group using childeventlistener
                        mMembersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.i(TAG, "start of onDataChange");
                                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                                    if (memberSnapshot.getKey().equals(mUserId)) {
                                        float newValue = Float.parseFloat(memberSnapshot.getValue().toString()) + expense - expenseToAdd;
                                        Log.i(TAG, expense.toString());
                                        mMembersDatabaseReference.child(memberSnapshot.getKey()).setValue(newValue);
                                    } else {
                                        float newValue = Float.parseFloat(memberSnapshot.getValue().toString()) - expenseToAdd;
                                        mMembersDatabaseReference.child(memberSnapshot.getKey()).setValue(newValue);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                        /*mChildEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                //if current member is the USER himself
                                if(dataSnapshot.getKey() == mUserId){
                                    float newValue = Integer.parseInt(dataSnapshot.getValue().toString()) + expense - expenseToAdd;
                                    mMembersDatabaseReference.child(dataSnapshot.getKey()).setValue(newValue);
                                }
                                else{
                                    float newValue = Integer.parseInt(dataSnapshot.getValue().toString()) - expenseToAdd;
                                    mMembersDatabaseReference.child(dataSnapshot.getKey()).setValue(newValue);
                                }
                            }
                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        };
                        mMembersDatabaseReference.addChildEventListener(mChildEventListener);
                        mMembersDatabaseReference.removeEventListener(mChildEventListener);//redundancy
                    }*/
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });


                //HARDCODED
                //mGroupDatabaseReference.child(mUserId).setValue(expense);
                //below is test1 userid
                //mGroupDatabaseReference.child("XIihVerGHgRgqKfAeL2vs9gbkf02").setValue(expense * -1);

                //TODO: retrieve size of group, divide expense, reduce users expense, add to other members expense
                //get original balances\
                //long userBalance =
                Toast.makeText(getApplicationContext(),"Expense added!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddExpenses.this,MainPage.class);
                startActivity(intent);
            }
        });
    }


}