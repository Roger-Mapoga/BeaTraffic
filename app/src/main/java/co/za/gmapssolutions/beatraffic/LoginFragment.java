package co.za.gmapssolutions.beatraffic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import co.za.gmapssolutions.beatraffic.security.SecurePreferences;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String userId = "userId";
    private static final String email = "email";
    private static final String password = "password";
    // TODO: Rename and change types of parameters
    private TextView userTv;
    private TextView passwordTv;
    private String HOST;
    private SecurePreferences preferences;
    public LoginFragment() {
        super(R.layout.fragment_login);
        // Required empty public constructor
    }
    public LoginFragment(SecurePreferences preferences,String HOST) {
        super(R.layout.fragment_login);
        this.preferences = preferences;
        this.HOST = HOST;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button loginBtn = view.findViewById(R.id.btn_login);
        userTv = view.findViewById(R.id.et_email);
        passwordTv = view.findViewById(R.id.et_password);
        AtomicReference<String> emailText = new AtomicReference<>();
        AtomicReference<String> passwordText = new AtomicReference<>();
        loginBtn.setOnClickListener(v ->
            {
                emailText.set(userTv.getText().toString());
                passwordText.set(passwordTv.getText().toString());
                if(!emailText.get().isEmpty() && !passwordText.get().isEmpty()) {
                    if(Patterns.EMAIL_ADDRESS.matcher(userTv.getText().toString()).matches()) {
                        Intent intent = new Intent();
                        intent.setAction("co.za.gmapssolutions.beatraffic.loginOrRegister");
                        intent.putExtra(email, emailText.get());
                        intent.putExtra(password, passwordText.get());
                        view.getContext().sendBroadcast(intent);
                        //close keyboard
                        InputMethodManager inputManager = (InputMethodManager)
                                view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }else {
                        Toast.makeText(view.getContext(),"Email address not valid",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(view.getContext(),"Email and password required to login or register",Toast.LENGTH_LONG).show();
                }
            }
        );
        IntentFilter intentFilter = new IntentFilter("co.za.gmapssolutions.beatraffic.loginOrRegisterError");
        Objects.requireNonNull(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String loginError = intent.getStringExtra("loginError");
                if(loginError != null) {
                    if (!loginError.isEmpty())
                        Toast.makeText(getActivity(), loginError, Toast.LENGTH_LONG).show();
                }
            }
        },intentFilter);
    }

}