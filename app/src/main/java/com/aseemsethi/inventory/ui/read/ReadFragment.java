package com.aseemsethi.inventory.ui.read;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.CollapsibleActionView;
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

import com.aseemsethi.inventory.R;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentReadBinding;
import com.aseemsethi.inventory.ui.home.HomeViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReadFragment extends Fragment {
    private HomeViewModel homeViewModel;
    final String TAG = "Inventory: read";
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);
    String cid;
    FirebaseFirestore db;
    String currentDate;

    private ReadViewModel readViewModel;
    private FragmentReadBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        readViewModel =
                new ViewModelProvider(this).get(ReadViewModel.class);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        if (homeViewModel.getLoggedin() == false) {
            Log.d(TAG, "Not logged in..");
            Toast.makeText(getContext(),"Please login first...",
                    Toast.LENGTH_SHORT).show();
            return null;
        } else {
            Log.d(TAG, "logged in..");
        }
        SharedPreferences sharedPref = getActivity().
                getPreferences(Context.MODE_PRIVATE);
        String nm = sharedPref.getString("cid", "10000");
        cid = nm;
        db = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yyyy",
                Locale.getDefault()).format(new Date());

        binding = FragmentReadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final Button btn = binding.downloadDaily;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                getDailyData();
            }
        });
        final Button btn2 = binding.downloadAll;
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                getAllData();
            }
        });
        return root;
    }

    public void getDailyData() {
        Log.d(TAG, "getData for cid: " + cid);
        db.collection(cid).document(currentDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                binding.itemOut1.setText(document.getData().toString());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void getAllData() {
        List<String> list = new ArrayList<>();
        Log.d(TAG, "getData for cid: " + cid);
        db.collection(cid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Success...");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                list.add(document.getId());
                                list.add(document.getData().toString());
                            }
                        } else {
                            Log.w(TAG, "DB Access Error", task.getException());
                        }
                        Log.d(TAG, list.toString());
                        binding.itemOut1.setText(list.toString());

                    }
                });
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}