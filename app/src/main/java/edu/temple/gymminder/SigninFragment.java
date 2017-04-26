package edu.temple.gymminder;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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
        View v = inflater.inflate(R.layout.fragment_signin, container, false);
        email = (EditText) v.findViewById(R.id.emailField);
        password = (EditText) v.findViewById(R.id.passwordField);
        v.findViewById(R.id.signinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            signin();
            }
        });
        v.findViewById(R.id.signupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().equals("")
                        || password.getText().toString().equals("")
                        || !email.getText().toString().contains("@")){
                    Toast.makeText(getActivity(), "Please enter a valid Email and Password.",
                            Toast.LENGTH_LONG).show();
                }
                else{
                auth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Auth", "Oh Yes");
                                    listener.goToWorkouts();
                                } else {

                                    Log.d("Auth", task.getException().getLocalizedMessage());
                                }
                            }
                        });}
            }
        });


        email.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            signin();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        password.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            signin();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        return v;
    }

    public void signin(){
        if(email.getText().toString().equals("")
                || password.getText().toString().equals("")
                || !email.getText().toString().contains("@")){
            Toast.makeText(getActivity(), "Please enter a valid Email and Password.",
                    Toast.LENGTH_LONG).show();
        }
        else{
            auth.signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("Auth", "Login complete");
                                listener.goToWorkouts();
                            } else {
                                Toast.makeText(getActivity(), "Incorrect Email or Password.",
                                        Toast.LENGTH_LONG).show();
                                Log.d("Auth", "Login failed");
                            }
                        }
                    });}
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        listener = (SigninListener) c;
    }

    public interface SigninListener {
        void goToWorkouts();
    }

}
