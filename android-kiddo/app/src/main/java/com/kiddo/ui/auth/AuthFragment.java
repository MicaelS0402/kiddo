package com.kiddo.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.R;
import com.kiddo.controllers.AuthController;
import com.kiddo.ui.home.HomeFragment;

public class AuthFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TabLayout tabs = view.findViewById(R.id.tabLayout);
        tabs.addTab(tabs.newTab().setText(getString(R.string.login)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.register)));

        View loginForm = view.findViewById(R.id.loginForm);
        View registerForm = view.findViewById(R.id.registerForm);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                boolean isLogin = tab.getPosition() == 0;
                loginForm.setVisibility(isLogin ? View.VISIBLE : View.GONE);
                registerForm.setVisibility(isLogin ? View.GONE : View.VISIBLE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TextInputEditText loginEmail = view.findViewById(R.id.loginEmail);
        TextInputEditText loginPassword = view.findViewById(R.id.loginPassword);
        CheckBox remember = view.findViewById(R.id.rememberMe);
        MaterialButton btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            try {
                String email = String.valueOf(loginEmail.getText());
                String pass = String.valueOf(loginPassword.getText());
                AuthController.login(requireContext(), email, pass, remember.isChecked());
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new HomeFragment())
                        .commit();
            } catch (Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        TextInputEditText registerName = view.findViewById(R.id.registerName);
        TextInputEditText registerEmail = view.findViewById(R.id.registerEmail);
        TextInputEditText registerPassword = view.findViewById(R.id.registerPassword);
        TextInputEditText registerPasswordConfirm = view.findViewById(R.id.registerPasswordConfirm);
        MaterialButton btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String name = String.valueOf(registerName.getText());
            String email = String.valueOf(registerEmail.getText());
            String pass = String.valueOf(registerPassword.getText());
            String conf = String.valueOf(registerPasswordConfirm.getText());
            if (!pass.equals(conf)) {
                Toast.makeText(requireContext(), "As senhas não conferem", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                AuthController.register(requireContext(), name, email, pass);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new HomeFragment())
                        .commit();
            } catch (Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
