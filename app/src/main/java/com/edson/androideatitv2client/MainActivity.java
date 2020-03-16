package com.edson.androideatitv2client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.edson.androideatitv2client.Common.Common;
import com.edson.androideatitv2client.Model.UserModel;
import com.edson.androideatitv2client.Remote.ICloudFunctions;
import com.edson.androideatitv2client.Remote.RetrofitCloudClient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171; //qualquer numero
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunctions cloudFunctions;
    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {

        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES);

        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions.class);
        listener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {

                //já logado
                // Toast.makeText(MainActivity.this, "Já está logado!", Toast.LENGTH_SHORT).show();

                checkUserFromFirebase(user);


            } else {


                phoneLogin();
            }


        };


    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();

        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);

                        } else {
                            Log.d("Log", "onDataChange: "+ user.getPhoneNumber());
                            showRegisterDialog(user);
                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please Fill Information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);

        Log.d("Log2", "onDataChange: "+ user.getPhoneNumber());

        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_address = itemView.findViewById(R.id.edt_address);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        //set

        edt_phone.setText(user.getPhoneNumber());
        Log.d("Log3", "onDataChange: "+ user.getPhoneNumber());


        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();

        });
        builder.setPositiveButton("REGISTER", (dialog, which) -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())) {


                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(edt_address.getText().toString())) {

                Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
                return;
            }

            UserModel userModel = new UserModel();
            userModel.setUid(user.getUid());
            userModel.setName(edt_name.getText().toString());
            userModel.setAddress(edt_address.getText().toString());
            userModel.setPhone(edt_phone.getText().toString());

            userRef.child(user.getUid()).setValue(userModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            dialog.dismiss();

                            Toast.makeText(this, "Congratulations! Register success", Toast.LENGTH_SHORT).show();
                            goToHomeActivity(userModel);
                        }
                    });

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToHomeActivity(UserModel userModel) {
        Common.currentUser = userModel;
    }

    private void phoneLogin() {


        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers).build(),
                APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {

                Toast.makeText(this, "failed to sign in!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
