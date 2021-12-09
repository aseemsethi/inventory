package com.aseemsethi.inventory.ui.read;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.aseemsethi.inventory.MainActivity;
import com.aseemsethi.inventory.R;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentInventoryBinding;
import com.aseemsethi.inventory.databinding.FragmentReadBinding;
import com.aseemsethi.inventory.ui.home.HomeViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ReadFragment extends Fragment {
    private HomeViewModel homeViewModel;
    final String TAG = "Inventory: read";
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);
    String cid;
    FirebaseFirestore db;
    String currentDate;
    TableLayout stk;
    Integer rowNum;

    private ReadViewModel readViewModel;
    private FragmentReadBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        readViewModel =
                new ViewModelProvider(this).get(ReadViewModel.class);

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
        SharedPreferences sharedPref = getActivity().
                getPreferences(Context.MODE_PRIVATE);
        String nm = sharedPref.getString("cid", "10000");
        cid = nm;
        db = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yyyy",
                Locale.getDefault()).format(new Date());

        binding = FragmentReadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final Button btn = binding.viewBtn;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                int selectedId = binding.radioGroup.getCheckedRadioButtonId();
                Log.d(TAG, "Selected: " + selectedId);
                RadioButton rb = root.findViewById(selectedId);
                String duration = rb.getText().toString();
                if (duration.equals("Day")) {
                    getDailyData();
                } else {
                    getAllData();
                }
            }
        });
        final Button btn2 = binding.downloadBtn;
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                int selectedId = binding.radioGroup.getCheckedRadioButtonId();
                Log.d(TAG, "Selected: " + selectedId);
                RadioButton rb = root.findViewById(selectedId);
                String duration = rb.getText().toString();
                if (duration.equals("Day")) {
                    getDailyData();
                } else {
                    getAllData();
                }
            }
        });
        return root;
    }

    public void getDailyData() {
        Log.d(TAG, "getData for cid: " + cid);
        binding.tableD.removeAllViews();
        setupTable();
        db.collection(cid).document(currentDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "Doc data: " + document.getData());
                                List<String> list = new ArrayList<>();
                                String date = document.getId();
                                list.add(document.getData().toString());
                                parseData(date, list);
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
        binding.tableD.removeAllViews();
        setupTable();
        Log.d(TAG, "getData for cid: " + cid);
        db.collection(cid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Success...");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<String> list = new ArrayList<>();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String date = document.getId();
                                list.add(document.getData().toString());
                                parseData(date, list);
                            }
                        } else {
                            Log.w(TAG, "DB Access Error", task.getException());
                        }
                    }
                });
    }

    public void parseData(String date, List<String> list) {
        Map<String, String> data = new HashMap<>();
        data = new Gson().fromJson(list.get(0),
                new TypeToken<HashMap<String, String>>(){}.getType());
        for (Map.Entry<String,String> entry : data.entrySet()) {
            Log.d(TAG, "Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
            addToTable(date, entry.getKey(), entry.getValue());
        }
    }

    public void addToTable(String date, String key, String value) {
        TableRow tbrow = new TableRow(getContext());
        TextView t1v = new TextView(getContext());
        t1v.setText(rowNum.toString());
        t1v.setTextColor(Color.WHITE);
        t1v.setGravity(Gravity.CENTER);
        tbrow.addView(t1v);
        TextView t2v = new TextView(getActivity());
        t2v.setText(key);
        t2v.setTextColor(Color.WHITE);
        t2v.setGravity(Gravity.CENTER);
        tbrow.addView(t2v);
        TextView t3v = new TextView(getActivity());
        t3v.setText(value);
        t3v.setTextColor(Color.WHITE);
        t3v.setGravity(Gravity.CENTER);
        tbrow.addView(t3v);
        TextView t4v = new TextView(getActivity());
        t4v.setText(date);
        t4v.setTextColor(Color.WHITE);
        t4v.setGravity(Gravity.CENTER);
        tbrow.addView(t4v);
        stk.addView(tbrow);
    }

    public void setupTable() {
        TableRow tbrow0;
        rowNum = 1;
        stk = binding.tableD;
        tbrow0 = new TableRow(getContext());
        TextView tv0 = new TextView(getActivity());
        tv0.setText(" Sl.No ");
        tv0.setTextColor(Color.WHITE);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(getActivity());
        tv1.setText(" Product ");
        tv1.setTextColor(Color.WHITE);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(getActivity());
        tv2.setText(" Qty ");
        tv2.setTextColor(Color.WHITE);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(getActivity());
        tv3.setText(" Date ");
        tv3.setTextColor(Color.WHITE);
        tbrow0.addView(tv3);
        stk.addView(tbrow0);
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}