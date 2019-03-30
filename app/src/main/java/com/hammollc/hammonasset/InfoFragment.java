package com.hammollc.hammonasset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class InfoFragment extends Fragment {

    private ArrayList<Item> list;
    private ItemAdapter ad;
    private String uid;
    private String[] perInfo, conInfo;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info, container, false);

        list = new ArrayList<>();
        ListView listView = v.findViewById(R.id.infoView);
        ad = new ItemAdapter(getContext(), list);

        conInfo = new String[5];
        perInfo = new String[8];

        setItemListener(listView);
        listView.setAdapter(ad);
        populateList();

        return v;
    }

    private void populateList() {
        final int infResource = getResources().getIdentifier(
                "@drawable/red_info",
                "drawable",
                getActivity().getPackageName());

        final int defResource = getResources().getIdentifier(
                "@drawable/done",
                "drawable",
                getActivity().getPackageName());

        uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists() && dataSnapshot.getKey().equals("eContact")) {
                    String eml = dataSnapshot.child("email").getValue().toString();
                    String nme = dataSnapshot.child("name").getValue().toString();
                    String pPh = dataSnapshot.child("pPhone").getValue().toString();
                    String rel = dataSnapshot.child("relationship").getValue().toString();
                    String sPh = dataSnapshot.child("sPhone").getValue().toString();

                    conInfo[0] = eml;
                    conInfo[1] = nme;
                    conInfo[2] = pPh;
                    conInfo[3] = rel;
                    conInfo[4] = sPh;

                    if (rel.equals("") || nme.equals("") ||
                            pPh.equals("") || (eml.equals("") && sPh.equals(""))) {

                        Item newItem = new Item(
                                "Emergency Contact",
                                "Need to update\nEmergency Contact",
                                "con",
                                infResource);

                        list.add(newItem);
                    } else {
                        Item newItem = new Item(
                                "Emergency Contact Info",
                                "All information\nis up to date",
                                "con",
                                defResource);

                        list.add(newItem);
                    }

                    ad.notifyDataSetChanged();
                } else if (dataSnapshot.exists() && dataSnapshot.getKey().equals("info")) {
                    String add = dataSnapshot.child("address").getValue().toString();
                    String cty = dataSnapshot.child("city").getValue().toString();
                    String fnm = dataSnapshot.child("fName").getValue().toString();
                    String hdt = dataSnapshot.child("hireDate").getValue().toString();
                    String lnm = dataSnapshot.child("lName").getValue().toString();
                    String pho = dataSnapshot.child("phone").getValue().toString();
                    String ste = dataSnapshot.child("state").getValue().toString();
                    String zip = dataSnapshot.child("zip").getValue().toString();

                    perInfo[0] = add;
                    perInfo[1] = cty;
                    perInfo[2] = fnm;
                    perInfo[3] = hdt;
                    perInfo[4] = lnm;
                    perInfo[5] = pho;
                    perInfo[6] = ste;
                    perInfo[7] = zip;

                    if (add.equals("") ||
                            cty.equals("") || pho.equals("") || ste.equals("") || zip.equals("")) {

                        Item newItem = new Item(
                                "Personal Info",
                                "Need to update\nPersonal Information",
                                "per",
                                infResource);

                        list.add(newItem);
                    } else {
                        Item newItem = new Item(
                                "Personal Info",
                                "All information\nis up to date",
                                "per",
                                defResource);

                        list.add(newItem);
                    }

                    ad.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setItemListener(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String code = list.get(position).code;

                if(code.equals("con")) {
                    Intent i = new Intent(getContext(), EmergencyContact.class);
                    i.putExtra("uid", uid);
                    i.putExtra("conInfo", conInfo);
                    getContext().startActivity(i);
                }
                else if(code.equals("per")) {
                    Intent i = new Intent(getContext(), PersonalInfo.class);
                    i.putExtra("uid", uid);
                    i.putExtra("perInfo", perInfo);
                    getContext().startActivity(i);
                }
            }
        });
    }
}
