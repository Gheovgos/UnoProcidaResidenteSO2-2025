package com.porfirio.orariprocida2011.dialogs;


import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.entity.Taxi;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TaxiDialog extends DialogFragment {

    private String porto;
    private List<Taxi> taxis;

    public void setPorto(String porto) {
        this.porto = porto;
    }

    public void setTaxis(List<Taxi> taxis) {
        this.taxis = taxis;

        if (getView() != null) {
            aggiornaUI();
        }
    }

    private void aggiornaUI() {
        View view = getView();
        if (view == null) return; // Se la vista non è ancora pronta, esci

        TextView tn1 = view.findViewById(R.id.tn1);
        TextView tn2 = view.findViewById(R.id.tn2);
        TextView tn3 = view.findViewById(R.id.tn3);
        TextView tn4 = view.findViewById(R.id.tn4);
        TextView tn5 = view.findViewById(R.id.tn5);
        TextView tn6 = view.findViewById(R.id.tn6);

        tn1.setText(null);
        tn2.setText(null);
        tn3.setText(null);
        tn4.setText(null);
        tn5.setText(null);
        tn6.setText(null);

        ArrayList<Taxi> taxiPortoList = new ArrayList<>();

        if (taxis != null) {
            for (Taxi taxi : taxis) {
                if (porto.contains(taxi.getPorto()) &&
                        !(porto.contentEquals("Monte di Procida") && taxi.getPorto().contentEquals("Procida"))) {
                    taxiPortoList.add(taxi);
                }
            }
        }

        if (!taxiPortoList.isEmpty()) {
            tn1.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(0).getCompagnia(), taxiPortoList.get(0).getNumero()));
            Linkify.addLinks(tn1, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 2) {
            tn2.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(1).getCompagnia(), taxiPortoList.get(1).getNumero()));
            Linkify.addLinks(tn2, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 3) {
            tn3.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(2).getCompagnia(), taxiPortoList.get(2).getNumero()));
            Linkify.addLinks(tn3, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 4) {
            tn4.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(3).getCompagnia(), taxiPortoList.get(3).getNumero()));
            Linkify.addLinks(tn4, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 5) {
            tn5.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(4).getCompagnia(), taxiPortoList.get(4).getNumero()));
            Linkify.addLinks(tn5, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 6) {
            tn6.setText(MessageFormat.format("{0} : {1}", taxiPortoList.get(5).getCompagnia(), taxiPortoList.get(5).getNumero()));
            Linkify.addLinks(tn6, Linkify.PHONE_NUMBERS);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.taxi, container);

        TextView tn1 = view.findViewById(R.id.tn1);
        tn1.setText(null);
        TextView tn2 = view.findViewById(R.id.tn2);
        tn2.setText(null);
        TextView tn3 = view.findViewById(R.id.tn3);
        tn3.setText(null);
        TextView tn4 = view.findViewById(R.id.tn4);
        tn4.setText(null);
        TextView tn5 = view.findViewById(R.id.tn5);
        tn5.setText(null);
        TextView tn6 = view.findViewById(R.id.tn6);
        tn6.setText(null);

        Button btnBack = view.findViewById(R.id.btnBackTaxi);
        btnBack.setOnClickListener(v -> dismiss());

        ArrayList<Taxi> taxiPortoList = new ArrayList<>();

        if (taxis != null) {
            for (Taxi taxi : taxis) {
                if (porto.contains(taxi.getPorto()) &&
                        !(porto.contentEquals("Monte di Procida") && taxi.getPorto().contentEquals("Procida"))) {
                    taxiPortoList.add(taxi);
                } else {
                    Log.d("TaxiDialog", "Taxi escluso: " + taxi.getCompagnia() + " - Porto: " + taxi.getPorto());
                }
            }
        }


        if (!taxiPortoList.isEmpty()) {
            final String text = taxiPortoList.get(0).getCompagnia() + " : " + taxiPortoList.get(0).getNumero();
            tn1.setText(text);
            Linkify.addLinks(tn1, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 2) {
            final String text = taxiPortoList.get(1).getCompagnia() + " : " + taxiPortoList.get(1).getNumero();
            tn2.setText(text);
            Linkify.addLinks(tn2, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 3) {
            final String text = taxiPortoList.get(2).getCompagnia() + " : " + taxiPortoList.get(2).getNumero();
            tn3.setText(text);
            Linkify.addLinks(tn3, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 4) {
            final String text = taxiPortoList.get(3).getCompagnia() + " : " + taxiPortoList.get(3).getNumero();
            tn4.setText(text);
            Linkify.addLinks(tn4, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 5) {
            final String text = taxiPortoList.get(4).getCompagnia() + " : " + taxiPortoList.get(4).getNumero();
            tn5.setText(text);
            Linkify.addLinks(tn5, Linkify.PHONE_NUMBERS);
        }
        if (taxiPortoList.size() >= 6) {
            final String text = taxiPortoList.get(5).getCompagnia() + " : " + taxiPortoList.get(5).getNumero();
            tn6.setText(text);
            Linkify.addLinks(tn6, Linkify.PHONE_NUMBERS);
        }

        aggiornaUI();

        return view;
    }


}
