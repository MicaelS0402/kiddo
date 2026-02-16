package com.kiddo.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.kiddo.app.R;
import com.kiddo.app.models.User;
import com.kiddo.app.utils.AppState;
import com.kiddo.app.utils.StorageManager;
import java.util.Map;

public class AuthFragment extends Fragment {
    private LinearLayout loginForm, registerForm;
    private Button btnLoginTab, btnRegisterTab, btnLogin, btnRegister;
    private EditText loginEmail, loginPassword, registerName, registerEmail, registerPassword, registerPasswordConfirm;
    private CheckBox rememberMe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        loginForm = v.findViewById(R.id.loginForm);
        registerForm = v.findViewById(R.id.registerForm);
        btnLoginTab = v.findViewById(R.id.btnLoginTab);
        btnRegisterTab = v.findViewById(R.id.btnRegisterTab);
        btnLogin = v.findViewById(R.id.btnLogin);
        btnRegister = v.findViewById(R.id.btnRegister);
        loginEmail = v.findViewById(R.id.loginEmail);
        loginPassword = v.findViewById(R.id.loginPassword);
        registerName = v.findViewById(R.id.registerName);
        registerEmail = v.findViewById(R.id.registerEmail);
        registerPassword = v.findViewById(R.id.registerPassword);
        registerPasswordConfirm = v.findViewById(R.id.registerPasswordConfirm);
        rememberMe = v.findViewById(R.id.rememberMe);

        btnLoginTab.setOnClickListener(view -> showTab(true));
        btnRegisterTab.setOnClickListener(view -> showTab(false));
        btnLogin.setOnClickListener(view -> handleLogin());
        btnRegister.setOnClickListener(view -> handleRegister());
    }

    private void showTab(boolean login) {
        loginForm.setVisibility(login ? View.VISIBLE : View.GONE);
        registerForm.setVisibility(login ? View.GONE : View.VISIBLE);
    }

    private String hashPassword(String password) {
        String str = password + "kiddo_salt_2024";
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            int c = str.charAt(i);
            hash = ((hash << 5) - hash) + c;
            hash = hash & hash;
        }
        return Integer.toString(Math.abs(hash), 36);
    }

    private void handleRegister() {
        String name = value(registerName);
        String email = value(registerEmail).toLowerCase();
        String pass = value(registerPassword);
        String conf = value(registerPasswordConfirm);
        if (name.length() < 2) {
            toast("Nome muito curto");
            return;
        }
        if (email.length() < 3) {
            toast("E-mail/usuário muito curto");
            return;
        }
        if (pass.length() < 4) {
            toast("Senha deve ter no mínimo 4 caracteres");
            return;
        }
        if (!pass.equals(conf)) {
            toast("As senhas não conferem");
            return;
        }
        Map<String, User> users = StorageManager.getUsers();
        if (users.containsKey(email)) {
            toast("Este e-mail/usuário já está cadastrado");
            return;
        }
        String hash = hashPassword(pass);
        User u = new User(name, email, hash, null);
        users.put(email, u);
        StorageManager.saveUsers(users);
        AppState.get().setCurrentUser(u, false);
        toast("Conta criada");
        goHome();
    }

    private void handleLogin() {
        String email = value(loginEmail).toLowerCase();
        String pass = value(loginPassword);
        boolean remember = rememberMe.isChecked();
        Map<String, User> users = StorageManager.getUsers();
        if (!users.containsKey(email)) {
            toast("Usuário não encontrado");
            return;
        }
        User u = users.get(email);
        if (!hashPassword(pass).equals(u.passwordHash)) {
            toast("Senha incorreta");
            return;
        }
        AppState.get().setCurrentUser(u, remember);
        goHome();
    }

    private void goHome() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private String value(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(String m) {
        Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }
}
