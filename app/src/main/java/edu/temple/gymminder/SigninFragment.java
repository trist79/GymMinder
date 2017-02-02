package edu.temple.gymminder;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SigninFragment extends Fragment {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText email;
    EditText password;
    SigninListener listener;

    public SigninFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_signin, container, false);
        email = (EditText) v.findViewById(R.id.emailField);
        password = (EditText) v.findViewById(R.id.passwordField);
        v.findViewById(R.id.signinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signInWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Log.d("Auth", "Login complete");
                                    listener.goToMain();
                                } else {
                                    Log.d("Auth", "Login failed");
                                }
                            }
                        });
            }
        });
        v.findViewById(R.id.signupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Auth", "Oh Yes");
                                    listener.goToMain();
                                } else {
                                    Log.d("Auth", "Oh no");
                                }

                            }
                        });

            }
        });
        return v;
    }

    @Override
    public void onAttach(Context c){
        super.onAttach(c);
        listener = (SigninListener) c;
    }

}
