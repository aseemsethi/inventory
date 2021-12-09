package com.aseemsethi.inventory.ui.inventory;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aseemsethi.inventory.R;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.ui.home.HomeViewModel;
import com.aseemsethi.inventory.ui.settings.SettingsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InventoryFragment extends Fragment {
    private HomeViewModel homeViewModel;
    final String TAG = "Inventory: inventory";
    FirebaseFirestore db;
    String cid;

    private InventoryViewModel inventoryViewModel;
    private SettingsViewModel settingsViewModel;
    private FragmentInventoryBinding binding;
    String currentDate;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        if (homeViewModel.getLoggedin() == false) {
            Log.d(TAG, "Not logged in..");
            Toast.makeText(getContext(),"Please login first...",
                    Toast.LENGTH_SHORT).show();
            return null;
        } else {
            Log.d(TAG, "logged in..");
        }

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*
        final TextView textView = binding.textInventory;
        inventoryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */
        SharedPreferences sharedPref = getActivity().
                getPreferences(Context.MODE_PRIVATE);
        String nm = sharedPref.getString("cid", "10000");
        cid = nm;

        db = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yyyy",
                Locale.getDefault()).format(new Date());
        Log.d(TAG, "Date: " + currentDate + " cid: " + cid);

        final Button btn = binding.saveItem;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                hideKeyboard(getActivity());
                Log.d(TAG, "Item no: " + binding.itemNo.getText().toString());
                String str = binding.itemNo.getText().toString();
                if(str == null || str.trim().isEmpty()) {
                    if (isAdded())
                        Toast.makeText(getContext(),"Enter Quantity",Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Integer val = Integer.valueOf(binding.itemNo.getText().toString());
                        addData(binding.itemID.getText().toString(), val);
                    } catch(Exception e) {
                        if (isAdded())
                            Toast.makeText(getContext(), "Input error",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return root;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Database looks like
    //      /cid/date/<items:count>
    //      /100/08-12-2021/<items, count>
    public void addData(String itemName, int num) {
        DocumentReference docref = db.collection(cid).document(currentDate);
        Map<String, Object> data = new HashMap<>();
        //data.put("item", itemName);
        //data.put("count", num);
        data.put(itemName, num);
        Log.d(TAG, "Adding: " + itemName + ", num: " + num + " to cid: " +  cid);

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Document exists!");
                        docref.update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Doc successfully written!");
                                        if (isAdded())
                                            Toast.makeText(getContext(), "Write ok..",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                        if (isAdded())
                                            Toast.makeText(getContext(), "Write failed..",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d(TAG, "Document does not exist!");
                        docref
                                //.collection("items")
                                //.add(data)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "New Doc successfully written!");
                                        if (isAdded())
                                            Toast.makeText(getContext(),"New Doc Write ok..",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                        if (isAdded())
                                            Toast.makeText(getContext(),"New Doc Write failed..",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    });
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                    if (isAdded())
                        Toast.makeText(getContext(),"DB Access Error",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}