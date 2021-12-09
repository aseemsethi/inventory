package com.aseemsethi.inventory.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aseemsethi.inventory.databinding.FragmentSettingsBinding;
import com.aseemsethi.inventory.ui.home.HomeViewModel;

public class SettingsFragment extends Fragment {
    final String TAG = "Inventory: settings";
    private SettingsViewModel settingsViewModel;
    private HomeViewModel homeViewModel;
    private FragmentSettingsBinding binding;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        if (homeViewModel.getLoggedin() == false) {
            Log.d(TAG, "Not logged in..");
            if (isAdded())
                Toast.makeText(getContext(),"Please login first...",
                    Toast.LENGTH_SHORT).show();
            return null;
        } else {
            Log.d(TAG, "logged in..");
        }

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences sharedPref = getActivity().
                getPreferences(Context.MODE_PRIVATE);
        String nm = sharedPref.getString("cid", "10000");
        binding.customerId.setText(nm);
        settingsViewModel.cid = nm;

        final Button btn = binding.savePref;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                String nm = binding.customerId.getText().toString();
                Log.d(TAG, "Save CustomerID: " + nm);
                settingsViewModel.cid = nm;

                SharedPreferences sharedPref = getActivity().getPreferences(
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("cid", nm);
                editor.apply();
            }
        });
        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}