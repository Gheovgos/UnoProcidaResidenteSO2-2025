package com.porfirio.orariprocida2011.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.threads.alerts.AlertsService;
import com.porfirio.orariprocida2011.threads.taxies.TaxisDAO;
import com.porfirio.orariprocida2011.threads.taxies.TaxisService;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class DettagliMezzoDialog extends DialogFragment implements OnClickListener {

    private final BiglietterieDialog biglietterieDialog = new BiglietterieDialog();
    private Mezzo mezzo;
    private Context callingContext;
    private TaxiDialog taxiDialog;
    private SegnalazioneDialog segnalazioneDialog;
    private Calendar calen;
    private OrariProcida2011Activity callingActivity;
    private FragmentManager fragmentManager;
    private ArrayList<Compagnia> lc;

    // private final AlertsDAO alertsDAO;

    private final AlertsService alertsService;
    private final TaxisService taxisService;
    private Analytics analytics;

    public DettagliMezzoDialog(AlertsService alertsService, TaxisService taxisService) {
        this.alertsService = Objects.requireNonNull(alertsService);
        this.taxisService = taxisService;
    }

    public void setDettagliMezzoDialog(FragmentManager fm, OrariProcida2011Activity a, Context context, Calendar cal) {
        fragmentManager = fm;
        callingActivity = a;
        callingContext = context;
        calen = cal;
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.dettaglimezzo, container);
        //setContentView(R.layout.dettaglimezzo);

        TextView txtMezzo = view.findViewById(R.id.txtMezzo);
        TextView txtPartenza = view.findViewById(R.id.txtPartenza);
        TextView txtArrivo = view.findViewById(R.id.txtArrivo);
//		txtOrario = (TextView) findViewById(R.id.txtOrario);
//		txtOraPartenza = (TextView) findViewById(R.id.txtOraPartenza);
//		txtOraArrivo = (TextView) findViewById(R.id.txtOraArrivo);
//		txtPortoPartenza = (TextView) findViewById(R.id.txtPortoPartenza);
//		txtPortoArrivo = (TextView) findViewById(R.id.txtPortoArrivo);
        TextView txtPeriodo = view.findViewById(R.id.txtPeriodo);
        txtPeriodo.setText("");
        TextView txtGiorniSettimana = view.findViewById(R.id.txtGiorniSettimana);
        txtGiorniSettimana.setText("");
//		txtNomeCompagnia = (TextView) findViewById(R.id.txtNomeCompagnia);
//		txtTelefonoCompagnia = (TextView) findViewById(R.id.txtTelefonoCompagnia);
        TextView txtCosto = view.findViewById(R.id.txtCosto);
        TextView txtAuto = view.findViewById(R.id.txtAuto);

        Button btnReturnToHome = view.findViewById(R.id.btnReturnToHome);
        btnReturnToHome.setOnClickListener(v -> dismiss());

        Button btnTaxi = view.findViewById(R.id.btnTaxi);
        btnTaxi.setOnClickListener(v -> {
            analytics.send("App Event", "Click Taxi Dialog");
            taxiDialog.show(fragmentManager, "fragment_edit_name");
        });

        Button btnBiglietterie = view.findViewById(R.id.btnBiglietterie);
        btnBiglietterie.setOnClickListener(v -> {
            analytics.send("App Event", "Click Biglietterie Dialog");
            biglietterieDialog.show(fragmentManager, "fragment_edit_name");
        });

        Button btnConfermaOSmentisci = view.findViewById(R.id.btnConfermaOSmentisci);
        btnConfermaOSmentisci.setOnClickListener(v -> {
            if (!callingActivity.isOnline())
                Toast.makeText(getContext(), callingActivity.getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
            else {
                analytics.send("App Event", "Click Segnalazione Dialog");
                segnalazioneDialog.show(fragmentManager, "fragment_edit_name");
            }
        });

        final String text = "    " + mezzo.nave + "    ";
        txtMezzo.setText(text);

        LocalDate departureDate = LocalDateTime.ofInstant(callingActivity.c.toInstant(), callingActivity.c.getTimeZone().toZoneId()).toLocalDate();
        LocalDate arrivalDate = LocalDateTime.ofInstant(callingActivity.c.toInstant(), callingActivity.c.getTimeZone().toZoneId()).toLocalDate();

        if (mezzo.getGiornoSeguente()) {
            departureDate = departureDate.plusDays(1);
            arrivalDate = arrivalDate.plusDays(1);
        }

        String s = callingContext.getString(R.string.parteAlle) + " " + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(mezzo.getDepartureTime());
        s += " " + callingContext.getString(R.string.del) + " " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(departureDate);
        s += " " + callingContext.getString(R.string.da) + " " + mezzo.portoPartenza;
        txtPartenza.setText(s);
        //s=new String();
        s = callingContext.getString(R.string.arrivaAlle) + " " + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(mezzo.getArrivalTime());
        s += " " + callingContext.getString(R.string.del) + " " + DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(arrivalDate);
        s += " " + callingContext.getString(R.string.a) + " " + mezzo.portoArrivo;
        txtArrivo.setText(s);


//		if (mezzo.isEsclusione())
//			txtPeriodo.setText(mezzo.inizioEsclusione.get(Calendar.DAY_OF_MONTH));
//		txtGiorniSettimana.setText(mezzo.giorniSettimana);

//        listNumeri = new ArrayList <String>();
//        lvNumeri=(ListView)findViewById(R.id.listViewNumeri);
//        aalvNumeri = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1);
//        lvNumeri.setAdapter(aalvNumeri);
//
        s = "";

        if (mezzo.getReducedPrice() > 0)
            s += callingContext.getString(R.string.costo) + " " + String.format(Locale.getDefault(), "%.2f", mezzo.getReducedPrice()) + " € ";

        if (mezzo.getFullPrice() > 0)
            s += callingContext.getString(R.string.residenteO) + " " + String.format(Locale.getDefault(), "%.2f", mezzo.getFullPrice()) + " € " + callingContext.getString(R.string.intero);

        txtCosto.setText(s);

        //trova compagnia c
        Compagnia c = null;
        for (int i = 0; i < lc.size(); i++) {
            if (mezzo.nave.contains(lc.get(i).getName()))
                c = lc.get(i);
        }

        //Aggiunto Aladino
        if (c != null) {
            if (c.getName().contains("Ippocampo") || c.getName().contentEquals("Procida Lines") || mezzo.nave.contains("Aliscafo") || mezzo.nave.contains("Aladino") || mezzo.nave.contains("Motonave") || mezzo.nave.contains("Scotto Line"))
                txtAuto.setText(callingContext.getString(R.string.trasportaSoloPasseggeri));
            else
                txtAuto.setText(callingContext.getString(R.string.trasportaAutoPasseggeri));

            //biglietterieDialog = new BiglietterieDialog(this.getContext());
            //FragmentManager fm = callingContext.getSupportFragmentManager();
            //BiglietterieDialog biglietterieDialog = new BiglietterieDialog();
            biglietterieDialog.setCompagnia(c);
        } else
            txtAuto.setText("");
        taxiDialog = new TaxiDialog();
        taxiDialog.setPorto(mezzo.portoPartenza);

        if (taxisService != null && taxiDialog != null) {
            taxisService.requestUpdate();
            taxisService.getUpdates().observe(this, update -> {
                if (update.isValid()) {
                    taxiDialog.setTaxis(update.getData());
                    Log.d("DettagliMezzoDialog", "Dati taxi aggiornati: " + update.getData().size());
                } else {
                    Log.e("DettagliMezzoDialog", "Errore nell'aggiornamento dei taxi");
                }
            });
        }



        segnalazioneDialog = new SegnalazioneDialog(alertsService);
        segnalazioneDialog.setOrarioRef(calen);
        segnalazioneDialog.setMezzo(mezzo);
        segnalazioneDialog.setCallingContext(this.getContext());
        segnalazioneDialog.setAnalytics(analytics);
        segnalazioneDialog.setListCompagnia(lc);
        //segnalazioneDialog.fill(lc);


        return view;
    }

    public void setMezzo(Mezzo m) {
        mezzo = m;
    }

    @Override
    public void onClick(View arg0) {
        this.dismiss();
    }

    public void setListCompagnia(ArrayList<Compagnia> listCompagnia) {
        lc = listCompagnia;
    }

}
