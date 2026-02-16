package com.kiddo.ui.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.R;
import com.kiddo.controllers.AuthController;
import com.kiddo.utils.Storage;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountFragment extends Fragment {
    private String currentAvatar = "😊";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        JSONObject user = AuthController.getCurrentUser(requireContext());
        if (user == null) {
            requireActivity().onBackPressed();
            return;
        }
        TextInputEditText name = view.findViewById(R.id.accountName);
        TextView avatarPreview = view.findViewById(R.id.avatarPreview);
        name.setText(user.optString("name",""));
        currentAvatar = user.optString("avatar","😊");
        avatarPreview.setText(currentAvatar);
        LinearLayout avatarRow = view.findViewById(R.id.avatarRow);
        String[] options = new String[]{"😊","😎","🤩","🦄","🐱","🐶","🐼","🐸"};
        for (String opt : options) {
            TextView tv = new TextView(requireContext());
            tv.setText(opt);
            tv.setTextSize(24);
            tv.setPadding(12,12,12,12);
            tv.setOnClickListener(v -> {
                currentAvatar = opt;
                avatarPreview.setText(opt);
            });
            avatarRow.addView(tv);
        }
        MaterialButton save = view.findViewById(R.id.btnSave);
        save.setOnClickListener(v -> {
            String newName = String.valueOf(name.getText()).trim();
            if (TextUtils.isEmpty(newName) || newName.length() < 2) {
                Toast.makeText(requireContext(), "Nome muito curto", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject users = Storage.getUsers(requireContext());
            String email = user.optString("email");
            try {
                JSONObject u = users.getJSONObject(email);
                u.put("name", newName);
                u.put("avatar", currentAvatar);
                users.put(email, u);
                Storage.saveUsers(requireContext(), users);
                Toast.makeText(requireContext(), "Salvo", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Erro", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
