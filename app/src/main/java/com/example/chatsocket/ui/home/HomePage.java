package com.example.chatsocket.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatsocket.R;
import com.example.chatsocket.databinding.FragmentHomePageBinding;


public class HomePage extends Fragment {

    private FragmentHomePageBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomePageBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.clientPage.setOnClickListener(v -> {
            navController.navigate(R.id.action_homePage_to_clientPage);
        });
        binding.serverPage.setOnClickListener(v -> {
            navController.navigate(R.id.action_homePage_to_serverPage);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}