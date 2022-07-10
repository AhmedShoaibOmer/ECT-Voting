package com.aso.ectvoting.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.aso.ectvoting.R;
import com.aso.ectvoting.databinding.ActivityHomeBinding;
import com.aso.ectvoting.ui.authentication.login.LoginActivity;
import com.aso.ectvoting.ui.authentication.register.RegisterActivity;
import com.aso.ectvoting.ui.vote.VoteActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.voteButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        binding.registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}