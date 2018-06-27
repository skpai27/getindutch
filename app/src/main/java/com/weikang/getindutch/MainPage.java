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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainPage extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    public static final String GUEST = "guest";
    public static final String TAG = "MainPage";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mFriendsDatabaseReference;
    private ProgressBar mProgressBar;
    private String mUsername;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private Dialog mDialogAddpopup;
    private Dialog mDialogCreateGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mUsername = GUEST;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDialogAddpopup = new Dialog(this);
        mDialogCreateGroup = new Dialog(this);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPage(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

        //initialising buttons
        mToolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    mUsername = user.getDisplayName();
                } else {
                    //user is signed out
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
                String currGroupName = groupName.getText().toString();
                if (currGroupName.isEmpty()){
                    Toast.makeText(MainPage.this,"Please enter a group name!", Toast.LENGTH_SHORT).show();
                } else {
                    mDialogCreateGroup.dismiss();
                    mDialogAddpopup.dismiss();
                    Groups newGroup = new Groups(currGroupName);
                    mGroupDatabaseReference.push().setValue(newGroup);
                    Toast.makeText(MainPage.this,currGroupName + " has been successfully created!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
