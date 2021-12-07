package com.aseemsethi.inventory.ui.inventory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.ui.home.HomeViewModel;

public class InventoryFragment extends Fragment {
    private HomeViewModel homeViewModel;
    final String TAG = "Inventory: inventory";

    private InventoryViewModel inventoryViewModel;
    private FragmentInventoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        inventoryViewModel =
                new ViewModelProvider(this).get(InventoryViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        if (homeViewModel.getLoggedin() == false) {
            Log.d(TAG, "Not logged in..");
            return null;
        } else {
            Log.d(TAG, "logged in..");
        }

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInventory;
        inventoryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
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