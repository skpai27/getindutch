package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainPage extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    public static final String GUEST = "guest";
    public static final String TAG = "MainPage";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ProgressBar mProgressBar;
    private String mUsername;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private Dialog mDialogAddpopup;
    private Dialog mDialogCreateGroup;
    private Dialog mDialogAddFriend;
    private Users currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mUsername = GUEST;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDialogAddpopup = new Dialog(this);
        mDialogCreateGroup = new Dialog(this);
        mDialogAddFriend = new Dialog(this);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPage(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

        //initialising buttons
        mToolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    mUsername = user.getDisplayName();
                    onSignedInInitialise(user.getUid());
                } else {
                    //user is signed out
                    //TODO: onSignedOutCleanup method
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    public void onSignedInInitialise(String userUid){
        currentUser = new Users(mUsername);
        final DatabaseReference currDataRef = mUsersDatabaseReference.child(userUid);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //create new user
                    currDataRef.setValue(currentUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        currDataRef.addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainpagemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sign_out_btn:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuth != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void setupViewPage(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new AllPage(), "All");
        adapter.addFragment(new SummaryPage(),"Summary");
        adapter.addFragment(new FriendsPage(),"Friends");
        viewPager.setAdapter(adapter);
    }

    public void showAddPopup(View view){
        TextView textclose;
        Button manualAdd;
        Button receiptScan;
        mDialogAddpopup.setContentView(R.layout.add_pop_up);
        textclose = (TextView) mDialogAddpopup.findViewById(R.id.text_close);
        manualAdd = (Button) mDialogAddpopup.findViewById(R.id.manual_add);
        receiptScan = (Button) mDialogAddpopup.findViewById(R.id.scanner_receipt);
        textclose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogAddpopup.dismiss();
            }
        });
        mDialogAddpopup.show();
        mDialogAddpopup.setCancelable(true);
        manualAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainPage.this, AddExpenses.class);
                startActivity(intent);
            }
        });
    }

    public void showFriendsAddPopup(View view){
        TextView cancelBtn;
        TextView nextBtn;
        final EditText userName;
        mDialogAddFriend.setContentView(R.layout.add_friends_popup);
        cancelBtn = (TextView) mDialogAddFriend.findViewById(R.id.cancelBtn2);
        nextBtn = (TextView) mDialogAddFriend.findViewById(R.id.nextBtn2);
        userName = (EditText) mDialogAddFriend.findViewById(R.id.userName);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogAddFriend.dismiss();
            }
        });
        mDialogAddFriend.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currUserName = userName.getText().toString();
                if (currUserName.isEmpty()){
                    Toast.makeText(MainPage.this,"Please enter a username!", Toast.LENGTH_SHORT).show();
                } else {
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                //create new user
                                Toast.makeText(MainPage.this,currUserName + " is not found! Please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                DatabaseReference userRef = mUsersDatabaseReference.child(mUsername).child("friends").child(currUserName);
                                ValueEventListener eventListener2 = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists()) {
                                            //create new user
                                            mUsersDatabaseReference.child(mUsername).child("friends").child(currUserName).setValue(true);
                                            mDialogAddFriend.dismiss();
                                        } else {
                                            Toast.makeText(MainPage.this,currUserName + " is already your friend!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                };
                                userRef.addListenerForSingleValueEvent(eventListener2);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    mUsersDatabaseReference.child(currUserName).addListenerForSingleValueEvent(eventListener);

                }
            }
        });

    }

    public void showCreateGroup(View view){
        TextView cancelBtn;
        TextView nextBtn;
        final EditText groupName;
        mDialogCreateGroup.setContentView(R.layout.create_group_popup);
        cancelBtn = (TextView) mDialogCreateGroup.findViewById(R.id.cancelBtn);
        nextBtn = (TextView) mDialogCreateGroup.findViewById(R.id.nextBtn);
        groupName = (EditText) mDialogCreateGroup.findViewById(R.id.groupName);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogCreateGroup.dismiss();
            }
        });
        mDialogCreateGroup.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currGroupName = groupName.getText().toString();
                if (currGroupName.isEmpty()){
                    Toast.makeText(MainPage.this,"Please enter a group name!", Toast.LENGTH_SHORT).show();
                } else {
                    final Groups newGroup = new Groups(currGroupName); //not really sure why it has to be final
                    final DatabaseReference groupRef = mGroupDatabaseReference.child(currGroupName);
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                //create new user
                                mDialogCreateGroup.dismiss();
                                mDialogAddpopup.dismiss();
                                groupRef.setValue(newGroup);
                                mUsersDatabaseReference.child(mUsername).child("groups").child(currGroupName).setValue(true);
                                newGroup.addMembers(currentUser, mGroupDatabaseReference.child(currGroupName).child("members"));
                                Toast.makeText(MainPage.this,currGroupName + " has been successfully created!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainPage.this, "Name of group has been taken. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    groupRef.addListenerForSingleValueEvent(eventListener);

                }
            }
        });

    }
}
