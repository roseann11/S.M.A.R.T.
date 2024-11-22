package com.example.smokeapplicationapppro.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smokeapplicationapppro.databinding.FragmentProfilesBinding;

public class ProfilesFragment extends Fragment {

    private FragmentProfilesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfilesViewModel profilesViewModel =
                new ViewModelProvider(this).get(ProfilesViewModel.class);

        binding = FragmentProfilesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textProfiles;
        profilesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}