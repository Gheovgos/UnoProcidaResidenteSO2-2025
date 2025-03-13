package com.porfirio.orariprocida2011.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.porfirio.orariprocida2011.threads.alerts.AlertsService;
import com.porfirio.orariprocida2011.threads.companies.CompaniesService;
import com.porfirio.orariprocida2011.threads.companies.CompaniesUpdate;
import com.porfirio.orariprocida2011.threads.taxies.TaxisService;
import com.porfirio.orariprocida2011.threads.transports.TransportsService;
import com.porfirio.orariprocida2011.threads.transports.TransportsUpdate;
import com.porfirio.orariprocida2011.entity.Alert;
import com.porfirio.orariprocida2011.threads.alerts.AlertUpdate;
import com.porfirio.orariprocida2011.threads.weather.WeatherService;
import com.porfirio.orariprocida2011.utils.Analytics;
import com.porfirio.orariprocida2011.utils.AnalyticsApplication;
import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.threads.weather.WeatherUpdate;
import com.porfirio.orariprocida2011.dialogs.DettagliMezzoDialog;
import com.porfirio.orariprocida2011.dialogs.SegnalazioneDialog;
import com.porfirio.orariprocida2011.entity.Compagnia;
import com.porfirio.orariprocida2011.entity.Meteo;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.entity.Osservazione;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import android.content.ComponentName;
import android.os.IBinder;

public class OrariProcida2011Activity extends FragmentActivity {

    private static final String ANALYTICS_CATEGORY_APP_EVENT = "App Event";
    private static final String ANALYTICS_CATEGORY_UI_EVENT = "UI Event";
    private static final String ANALYTICS_CATEGORY_USER_EVENT = "User";

    private static FragmentManager fm;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WeatherService.LocalBinder binder = (WeatherService.LocalBinder) service;
            weatherService = binder.getService();
            isBound = true;

            // Osservare gli aggiornamenti meteo
            weatherService.getUpdates().observe(OrariProcida2011Activity.this, OrariProcida2011Activity.this::onWeatherUpdate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            weatherService = null;
            isBound = false;
        }
    };


    public Calendar c;
    public AlertDialog aboutDialog;
    public Meteo meteo;
    //    public AlertDialog meteoDialog;
    public ArrayList<Mezzo> transportList;
    private String[] ragioni = new String[100];

    private String nave;
    private String portoPartenza;
    private String portoArrivo;
    private ArrayAdapter<String> aalvMezzi;
    private TextView txtOrario;
    //public AlertDialog novitaDialog;
    private DettagliMezzoDialog dettagliMezzoDialog;
    private ArrayList<Mezzo> selectMezzi;
    private final ArrayList<Compagnia> listCompagnia = new ArrayList<>();
    private LocationManager myManager;
    private String BestProvider;
    private SegnalazioneDialog segnalazioneDialog;

    private AlertsService alertsService;
    private boolean isAlertsBound = false;
    private final ServiceConnection alertsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlertsService.LocalBinder binder = (AlertsService.LocalBinder) service;
            alertsService = binder.getService();
            isAlertsBound = true;

            Log.d("AlertsService", "Servizio alert connesso!");

            segnalazioneDialog = new SegnalazioneDialog(alertsService);


            // Osserviamo gli aggiornamenti sugli alert
            alertsService.getUpdates().observe(OrariProcida2011Activity.this, OrariProcida2011Activity.this::onAlertsUpdate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            alertsService = null;
            isAlertsBound = false;
        }
    };

    private Analytics analytics;

    private WeatherService weatherService;
    private boolean isBound = false;

    private TransportsService transportsService;
    private boolean isTransportsBound = false;

    private final ServiceConnection transportsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TransportsService.LocalBinder binder = (TransportsService.LocalBinder) service;

            transportsService = binder.getService();
            isTransportsBound = true;
            Log.d("TransportsService", "Servizio trasporti connesso all'activity");

            // Osserva gli aggiornamenti dei trasporti
            transportsService.getUpdates().observe(OrariProcida2011Activity.this, OrariProcida2011Activity.this::onTransportsUpdate);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            transportsService = null;
            isTransportsBound = false;
        }
    };

    private CompaniesService companiesService;
    private boolean isCompaniesBound = false;
    private final ServiceConnection companiesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("CompaniesService", "Service Companies connesso all'Activity");
            CompaniesService.LocalBinder binder = (CompaniesService.LocalBinder) service;
            companiesService = binder.getService();
            isCompaniesBound = true;

            companiesService.getUpdates().observe(OrariProcida2011Activity.this, OrariProcida2011Activity.this::onCompaniesUpdate);

            companiesService.requestUpdate();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            companiesService = null;
            isCompaniesBound = false;
        }
    };

    private TaxisService taxisService;
    private boolean isTaxisBound = false;
    private final ServiceConnection taxisConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TaxisService.LocalBinder binder = (TaxisService.LocalBinder) service;
            taxisService = binder.getService();
            isTaxisBound = true;

            if (alertsService != null) {
                dettagliMezzoDialog = new DettagliMezzoDialog(alertsService, taxisService);
                dettagliMezzoDialog.setDettagliMezzoDialog(fm, OrariProcida2011Activity.this, OrariProcida2011Activity.this, c);
                dettagliMezzoDialog.setAnalytics(analytics);
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("TaxisService", "Service Taxi disconnesso!");
            taxisService = null;
            isTaxisBound = false;
        }
    };



    private boolean hasReceivedWeather, hasReceivedCompanies, hasReceivedTransports, hasReceivedAlerts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // NOTE (2025-02-02):
        // this check breaks the reportFullyDrawn method somehow and the time never shows up in the logs
        // you can uncomment this if you don't intend to use it i guess idk the permission request shouldn't even be here
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Request Permission");
//            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 534534);
//        }

        analytics = new Analytics((AnalyticsApplication) getApplication());

        Intent intent = new Intent(this, WeatherService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Intent alertsIntent = new Intent(this, AlertsService.class);
        startService(alertsIntent);
        bindService(alertsIntent, alertsConnection, Context.BIND_AUTO_CREATE);

        Intent transportsIntent = new Intent(this, TransportsService.class);
        startService(transportsIntent);
        bindService(transportsIntent, transportsConnection, Context.BIND_AUTO_CREATE);

        Intent companiesIntent = new Intent(this, CompaniesService.class);
        startService(companiesIntent);
        bindService(companiesIntent, companiesConnection, Context.BIND_AUTO_CREATE);

        Intent taxisIntent = new Intent(this, TaxisService.class);
        startService(taxisIntent);
        bindService(taxisIntent, taxisConnection, Context.BIND_AUTO_CREATE);


        if (isTransportsBound && transportsService != null) {
            transportsService.getUpdates().observe(this, this::onTransportsUpdate);
        }

        fm = getSupportFragmentManager();
        myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        BestProvider = myManager.getBestProvider(criteria, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.disclaimer) + "\n" + getString(R.string.credits))
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
        aboutDialog = builder.create();


        ragioni = getResources().getStringArray(R.array.strRagioni);

        meteo = new Meteo();

        c = Calendar.getInstance(TimeZone.getDefault());
        txtOrario = findViewById(R.id.txtOrario);
        setTxtOrario(c);

        Button buttonMinusMinus = findViewById(R.id.btnConfermaOSmentisci);
        buttonMinusMinus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button --");
            c.add(Calendar.HOUR, -1);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonMinus = findViewById(R.id.button2);
        buttonMinus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button -");
            c.add(Calendar.MINUTE, -15);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonPlus = findViewById(R.id.button3);
        buttonPlus.setOnClickListener(v -> {
            //TODO L'unico problema residuo ? che problemi correttamente segnalati con pi? di 24 ore di anticipo vengono visualizzati solo a meno di 24h
            //Forse potrebbe essere risolto forzando un refresh quando si avanza di 24h rispetto all'orario corrente
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button +");
            c.add(Calendar.MINUTE, 15);
            setTxtOrario(c);
            aggiornaLista();
        });

        Button buttonPlusPlus = findViewById(R.id.button4);
        buttonPlusPlus.setOnClickListener(v -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Button ++");
            c.add(Calendar.HOUR, 1);
            setTxtOrario(c);
            aggiornaLista();
        });

        // spostato in avanti setSpinner();

        transportList = new ArrayList<>();
        ListView lvMezzi = findViewById(R.id.listMezzi);
        aalvMezzi = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvMezzi.setAdapter(aalvMezzi);

        //listener sul click di un item della lista
        lvMezzi.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Click Dettagli Mezzo");
            //aboutDialog.show();

            for (int i = 0; i < aalvMezzi.getCount(); i++) {
                if (selectMezzi.get(i).getOrderInList() == arg2)
                    dettagliMezzoDialog.setMezzo(selectMezzi.get(i));
            }


//				problema: clicco sulla lista ma ho solo la stringa, non il mezzo corrispondente
//				soluzione: mantenere una variabile ordine che abbini lvMezzi con Mezzi
//				altra soluzione: trovare il mezzo dalla stringa
            //dettagliMezzoDialog.fill(listCompagnia);
            dettagliMezzoDialog.setListCompagnia(listCompagnia);
            dettagliMezzoDialog.show(fm, "fragment_edit_name");

        });
        lvMezzi.setLongClickable(true);
        lvMezzi.setOnItemLongClickListener((arg0, arg1, arg2, arg3) -> {
            analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "LongClick DettagliMezzo");

            if (!isOnline())
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.soloOnline), Toast.LENGTH_SHORT).show();
            else {
                for (int i = 0; i < aalvMezzi.getCount(); i++) {
                    if (selectMezzi.get(i).getOrderInList() == arg2)
                        segnalazioneDialog.setMezzo(selectMezzi.get(i));
                }


                // problema: clicco sulla lista ma ho solo la stringa, non il mezzo corrispondente
                // soluzione: mantenere una variabile ordine che abbini lvMezzi con Mezzi
                // altra soluzione: trovare il mezzo dalla stringa
                segnalazioneDialog.setOrarioRef(c);
                segnalazioneDialog.setCallingContext(getApplicationContext());
                segnalazioneDialog.setAnalytics(analytics);
                segnalazioneDialog.setListCompagnia(listCompagnia);

                //segnalazioneDialog.fill(listCompagnia);
                segnalazioneDialog.show(fm, "fragment_edit_name");
                //segnalazioneDialog.show();
                aggiornaLista(); //TODO Capire come si fa ad aggiornare dopo una segnalazione (oppure scrivere che prossimamente verr? aggiunta)
            }
            return true;
        });


        //aggiungere onlongclick su lvMezzi che faccia partire il dialog di segnalazione
        //che ha due funzioni: segnala un cambiamento (interazione con mail)
        //esegui un cambiamento (richiede una password che conosco solo io)
        //il cambiamento si ottiene andando ad aggiornare un file esclusioni giornaliere
        //ad ogni cambiamento si apre il file, si riscrivono le righe di oggi, si aggiunge la riga della segnalazione
        //bisogna cambiare anche la lettura dei mezzi prevedendo la lettura di questo file con la conseguente
        //eliminazione delle corse indicate

        setSpinner();

        if (!portoPartenza.equals(getString(R.string.tutti)))
            Toast.makeText(this, (getString(R.string.secondoMeVuoiPartireDa) + " " + portoPartenza), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // NOTE:
        // LiveData should automatically remove destroyed observers but let's do it for clarity's sake
        alertsService.getUpdates().removeObservers(this);
        companiesService.getUpdates().removeObservers(this);
        transportsService.getUpdates().removeObservers(this);
        // transportsDAO.getUpdates().removeObservers(this);
        // weatherDAO.getUpdates().removeObservers(this);
        // weatherDAO.close();

        // Pulisce il service per il Meteo
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }

        // Pulisce il service per l'Alert
        if (isAlertsBound) {
            unbindService(alertsConnection);
            isAlertsBound = false;
        }

        // Pulisce il service per i trasporti
        if (isTransportsBound) {
            unbindService(transportsConnection);
            isTransportsBound = false;
        }

        // Pulisce il service per le compagnie
        if (isCompaniesBound) {
            unbindService(companiesConnection);
            isCompaniesBound = false;
        }

        // Pulisce il service per i taxi
        if (isTaxisBound) {
            unbindService(taxisConnection);
            isTaxisBound = false;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Open Menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case (R.id.about):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "About");
                aboutDialog.show();
                return true;
            // cambiata semantica pulsante: se scelgo, allora carico esplicitamente da web
            case (R.id.updateWeb):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Update Orari da Web da Menu");
                if (isTransportsBound && transportsService != null) {
                    transportsService.getUpdates().observe(this, this::onTransportsUpdate);
                }

                return true;
            case (R.id.meteo):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Update Meteo da Menu");

                if (isBound && weatherService != null) {
                    Log.d("WeatherService", "Menu: forzo aggiornamento meteo");

                    // Forza un aggiornamento manuale prendendo il valore attuale del LiveData
                    weatherService.getUpdates().observe(this, this::onWeatherUpdate);
                } else {
                    Log.e("WeatherService", "Service non connesso, impossibile aggiornare il meteo");
                    Toast.makeText(this, "Servizio meteo non disponibile", Toast.LENGTH_SHORT).show();
                }
                return true;
            case (R.id.esci):
                analytics.send(ANALYTICS_CATEGORY_UI_EVENT, "Exit da Menu");
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setTxtOrario(Calendar c) {
        String s = getString(R.string.dalle) + " ";
        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            s += "0";
        s += c.get(Calendar.HOUR_OF_DAY) + ":";
        if (c.get(Calendar.MINUTE) < 10)
            s += "0";
        s += c.get(Calendar.MINUTE) + " " + getString(R.string.del) + " ";
        s += c.get(Calendar.DAY_OF_MONTH) + "/";
        s += (c.get(Calendar.MONTH) + 1) + "";
        txtOrario.setText(s);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void aggiornaLista() {
        if (!hasReceivedWeather || !hasReceivedTransports || !hasReceivedCompanies || !hasReceivedAlerts)
            return;

        analytics.send(ANALYTICS_CATEGORY_APP_EVENT, "Aggiorna Lista");

        selectMezzi = new ArrayList<>();

        aalvMezzi.clear();

        String expandedTransportName = espandiNave(nave);
        String expandedDepartureLocation = espandiPorto(portoPartenza);
        String expandedArrivalLocation = espandiPorto(portoArrivo);

        LocalDateTime selectedDate = LocalDateTime.ofInstant(c.toInstant(), c.getTimeZone().toZoneId());
        LocalDateTime oraLimite = selectedDate.plusDays(1);

        for (Mezzo mezzo : transportList) {
            LocalDateTime oraNave = selectedDate.toLocalDate().atTime(mezzo.getDepartureTime());

            if (oraNave.isBefore(selectedDate))
                oraNave = oraNave.plusDays(1);

            if (isNaveCompatibile(expandedTransportName, mezzo) &&
                    isPortoCompatibile(portoPartenza, expandedDepartureLocation, mezzo.portoPartenza) &&
                    isPortoCompatibile(portoArrivo, expandedArrivalLocation, mezzo.portoArrivo) &&
                    mezzo.isDateInExclusion(oraNave.toLocalDate()) &&
                    mezzo.isActiveOnDay(oraNave.getDayOfWeek()) &&
                    oraNave.isBefore(oraLimite)) {

                mezzo.setGiornoSeguente(!oraNave.toLocalDate().equals(selectedDate.toLocalDate()));

                selectMezzi.add(mezzo);
            }
        }

        selectMezzi.sort((m1, m2) -> {
            if (m1.getGiornoSeguente() == m2.getGiornoSeguente())
                return m1.getDepartureTime().compareTo(m2.getDepartureTime());
            else if (m1.getGiornoSeguente())
                return 1;
            else if (m2.getGiornoSeguente())
                return -1;

            return 0;
        });

        for (int i = 0; i < selectMezzi.size(); i++) {
            Mezzo route = selectMezzi.get(i);

            route.setOrderInList(i);

            String s = formatMezzoInfo(route);
            aalvMezzi.add(s);
        }

        reportFullyDrawn();
    }

    private String espandiNave(String nave) {
        if (nave.contains(getString(R.string.traghetti)))
            return "Traghetto Caremar Medmar Ippocampo Ippocampo(da Chiaiolella) Ippocampo(a Chiaiolella) Traghetto LazioMar";
        if (nave.contains(getString(R.string.aliscafi)))
            return "Aliscafo Caremar Aliscafo SNAV Scotto Line Aliscafo Alilauro";
        if (nave.equals("Ippocampo"))
            return "Ippocampo Ippocampo(da Chiaiolella) Ippocampo(a Chiaiolella)";
        if (nave.contains("Gestur"))
            return "Motonave Gestur Traghetto Gestur";
        return nave;
    }

    private String espandiPorto(String porto) {
        switch (porto) {
            case "Napoli" -> {
                return "Napoli Porta di Massa o Napoli Beverello";
            }
            case "Napoli o Pozzuoli" -> {
                return "Napoli Porta di Massa o Napoli Beverello o Pozzuoli";
            }
            case "Ischia" -> {
                return "Ischia Porto o Casamicciola";
            }
            case "Monte di Procida" -> {
                return "Monte di Procida";
            }
            default -> {
                return porto;
            }
        }
    }

    private boolean isNaveCompatibile(String naveEspanso, Mezzo mezzo) {
        return naveEspanso.contains(mezzo.nave) || nave.equals(getString(R.string.tutti));
    }

    private boolean isPortoCompatibile(String porto, String portoEspanso, String portoMezzo) {
        return portoMezzo.equals(porto) || portoEspanso.contains(portoMezzo) || porto.equals(getString(R.string.tutti));
    }

    private String formatMezzoInfo(Mezzo route) {
        StringBuilder s = new StringBuilder(route.nave + " - " + route.portoPartenza + " - " + route.portoArrivo + " - ");
        s.append(route.getDepartureTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));

        String spc = "";
        if (route.segnalazionePiuComune() > -1)
            spc = ragioni[route.segnalazionePiuComune()];
        if (route.tot > 0 || route.conferme > 0) {
            if (route.tot > 0) {
                if (route.conc) {
                    s.append(" - ").append(route.tot).append(route.tot == 1 ? " " + getString(R.string.segnalazione) : " " + getString(R.string.segnalazioni));
                    s.append(" ").append(getString(R.string.diProblemi)).append(" (").append(spc).append(")");
                } else {
                    s.append(" - ").append(getString(R.string.possibiliProblemi)).append(" (").append(route.tot);
                    s.append(route.tot == 1 ? " " + getString(R.string.segnalazione) + ")" : " " + getString(R.string.segnalazioni) + ")");
                    s.append(", ").append(getString(R.string.inParticolare)).append(" ").append(spc);
                }
            }
            if (route.conferme > 0) {
                s.append(" - ").append(route.conferme).append(route.conferme == 1 ? " " + getString(R.string.utenteDice) : " " + getString(R.string.utentiDicono));
                s.append(" ").append(getString(R.string.cheLaCorsaERegolare));
            }
        }

        return s.toString();
    }

    private void setSpinner() {
        Spinner spnNave = findViewById(R.id.spnNave);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.strMezzi, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spnNave.setAdapter(adapter);

        nave = getString(R.string.tutti);
        portoPartenza = getString(R.string.tutti);
        portoArrivo = getString(R.string.tutti);

        spnNave.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                nave = parent.getItemAtPosition(pos).toString();
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner spnPortoPartenza = findViewById(R.id.spnPortoPartenza);
        final ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.strPorti, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        spnPortoPartenza.setAdapter(adapter2);

        //controllo e setto tramite algoritmo di set con gps
        portoPartenza = setPortoPartenza();

        setSpnPortoPartenza(spnPortoPartenza, adapter2);

        final Spinner spnPortoArrivo = findViewById(R.id.spnPortoArrivo);
        final ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.strPorti, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(R.layout.spinner_item);
        spnPortoArrivo.setAdapter(adapter3);

        if (!portoPartenza.contentEquals("Procida") || portoPartenza.contentEquals(getString(R.string.tutti))) {
            portoArrivo = "Procida";
            setSpnPortoArrivo(spnPortoArrivo, adapter3);
        }

        spnPortoPartenza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                portoPartenza = parent.getItemAtPosition(pos).toString();
                if (portoPartenza.contentEquals("Procida") && portoArrivo.contentEquals("Procida")) {
                    portoArrivo = getString(R.string.tutti);
                    setSpnPortoArrivo(spnPortoArrivo, adapter2);
                    setSpnPortoPartenza(spnPortoPartenza, adapter3);
                } else if (!portoPartenza.contentEquals("Procida") || portoPartenza.contentEquals(getString(R.string.tutti))) {
                    portoArrivo = "Procida";
                    setSpnPortoArrivo(spnPortoArrivo, adapter2);
                    setSpnPortoPartenza(spnPortoPartenza, adapter3);
                }
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        spnPortoArrivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                portoArrivo = parent.getItemAtPosition(pos).toString();
                if (portoArrivo.contentEquals("Procida") && portoPartenza.contentEquals("Procida")) {
                    portoPartenza = getString(R.string.tutti);
                    setSpnPortoPartenza(spnPortoPartenza, adapter2);
                    setSpnPortoArrivo(spnPortoArrivo, adapter3);
                } else if (!portoArrivo.contentEquals("Procida") || portoArrivo.contentEquals(getString(R.string.tutti))) {
                    portoPartenza = "Procida";
                    setSpnPortoPartenza(spnPortoPartenza, adapter2);
                    setSpnPortoArrivo(spnPortoArrivo, adapter3);
                }
                aggiornaLista();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setSpnPortoArrivo(Spinner spnPortoArrivo, final ArrayAdapter<CharSequence> adapter3) {
        for (int i = 0; i < spnPortoArrivo.getCount(); i++) {
            if (Objects.equals(adapter3.getItem(i), portoArrivo)) {
                spnPortoArrivo.setSelection(i);
            }
        }
    }

    private void setSpnPortoPartenza(Spinner spnPortoPartenza, ArrayAdapter<CharSequence> adapter2) {
        for (int i = 0; i < spnPortoPartenza.getCount(); i++) {
            if (Objects.equals(adapter2.getItem(i), portoPartenza)) {
                spnPortoPartenza.setSelection(i);
            }
        }
    }

    private String setPortoPartenza() {
        // Trova il porto pi? vicino a quello di partenza
        Location l = null;


        if (ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OrariProcida2011Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //System.exit(0);
            return getString(R.string.tutti);
        }

        // l'accesso al GPS potrebbe non essere garantito per ragioni legate a Google Play
        // Bisogna gestire l'eccezione e restituire un valore di default che si potrà settare in altro punto dell'app
        try {
            l = myManager.getLastKnownLocation(BestProvider);
            Log.d("ACTIVITY", "Posizione:" + (l != null ? l.getLongitude() : 0) + "," + (l != null ? l.getLatitude() : 0));
        } catch (Exception e) {
            Log.e("Activity", "GPS: ", e);
        }
        if (l == null)
            return getString(R.string.tutti);
        //Coordinate angoli Procida
        if ((l.getLatitude() > 40.7374) && (l.getLatitude() < 40.7733) && (l.getLongitude() > 13.9897) && (l.getLongitude() < 14.0325)) {
            analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Procida");
            return "Procida";
        }
        //Coordinate angoli Isola d'Ischia
        if ((l.getLatitude() > 40.6921) && (l.getLatitude() < 40.7626) && (l.getLongitude() > 13.8465) && (l.getLongitude() < 13.9722))
            //Isola d'Ischia
            if (calcolaDistanza(l, 13.9063, 40.7496) > calcolaDistanza(l, 13.9602, 40.7319)) {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Ischia");
                return "Ischia";
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Casamicciola");
                return "Casamicciola";
            }
        //Inserire coordinate Napoli (media porti) e Pozzuoli
        double distNapoli = calcolaDistanza(l, 14.2575, 40.84);
        Log.d("OrariProcida", "d(Napoli)=" + distNapoli);
        double distPozzuoli = calcolaDistanza(l, 14.1179, 40.8239);
        Log.d("OrariProcida", "d(Pozzuoli)=" + distPozzuoli);
        double distMonteProcida = calcolaDistanza(l, 14.05, 40.8);
        if (distMonteProcida < 1500) {
            analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Monte di Procida");
            return "Monte di Procida";
        }
        if (distPozzuoli < distNapoli) {
            if (distPozzuoli < 15000) {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Pozzuoli");
                return "Pozzuoli";
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli o Pozzuoli");
                return "Napoli o Pozzuoli";
            }
        } else {
            if (distNapoli < 15000) {
                if (distNapoli > 1000) {
                    analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli");
                    return "Napoli";
                } else {
                    if (calcolaDistanza(l, 14.2548, 40.8376) < calcolaDistanza(l, 14.2602, 40.8424)) {
                        analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli Beverello");
                        return "Napoli Beverello";
                    } else {
                        analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli Porta di Massa");
                        return "Napoli Porta di Massa";
                    }
                }
            } else {
                analytics.send(ANALYTICS_CATEGORY_USER_EVENT, "From Napoli o Pozzuoli");
                return "Napoli o Pozzuoli";
            }
        }
    }

    private double calcolaDistanza(Location location, double lon, double lat) {
        //calcola distanza da obiettivo
        double deltaLong = Math.abs(lon - location.getLongitude());
        double deltaLat = Math.abs(lat - location.getLatitude());
        double delta = (Math.sqrt(deltaLong * deltaLong + deltaLat * deltaLat));
        delta = delta * 60 * 1852;
        return Math.ceil(delta);
    }

    private void onAlertsUpdate(AlertUpdate update) {
        boolean showToast = hasReceivedAlerts;
        hasReceivedAlerts = true;

        if (update.isValid()) {
            List<Alert> alerts = update.getData();

            Log.d("AlertsService", "Aggiornamento alert ricevuto:");
            for (Alert alert : alerts) {
                Log.d("AlertsService", "Alert: " + alert.getDetails() + " (Motivo: " + alert.getReason() + ")");
            }

            aggiornaLista(); // Aggiorna la UI se necessario

            // DEBUG ONLY: Si può modificare
            if (showToast) {
                Toast.makeText(this, "Alert aggiornati!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("AlertsService", "Errore nell'aggiornamento alert", update.getError());
            Toast.makeText(this, getString(R.string.error_update_alerts), Toast.LENGTH_SHORT).show();
        }
    }

    private void onCompaniesUpdate(CompaniesUpdate update) {
        hasReceivedCompanies = true;

        if (update.isValid()) {
            listCompagnia.clear();
            listCompagnia.addAll(update.getData());
            Log.d("CompaniesService", "Dati compagnie aggiornati, aggiornamento UI...");
            aggiornaLista();
        } else {
            Log.e("MainActivity", "OnCompaniesUpdate: ", update.getError());
            Toast.makeText(this, getString(R.string.error_update_companies), Toast.LENGTH_SHORT).show();
        }
    }

    // private void onTaxisUpdate(TaxisUpdate update)

    private void onTransportsUpdate(TransportsUpdate update) {
        boolean showToast = hasReceivedTransports;
        hasReceivedTransports = true;

        if (update.isValid()) {
            transportList.clear();
            transportList.addAll(update.getData());
            aggiornaLista();

            Log.d("TransportsService", "Aggiornamento trasporti ricevuto! Numero di mezzi: " + update.getData());
            // alertsDAO.requestUpdate();
            alertsService.getUpdates();

            if (showToast)
                Toast.makeText(this, getString(R.string.orariAggiornatiAl) + " " + DateTimeFormatter.ISO_LOCAL_DATE.format(update.getUpdateTime()), Toast.LENGTH_SHORT).show();
        } else {
            // TODO: handle exception
            Log.e("MainActivity", "OnTransportsUpdate: ", update.getError());
            Toast.makeText(this, getString(R.string.error_update_transports), Toast.LENGTH_SHORT).show();
        }
    }

    private void onWeatherUpdate(WeatherUpdate update) {
        boolean showToast = hasReceivedWeather;
        hasReceivedWeather = true;

        if (update.isValid()) {
            List<Osservazione> forecasts = update.getData();
            meteo.setForecasts(forecasts);
            aggiornaLista();

            Osservazione currentWeather = forecasts.get(0);

            Log.d("WeatherService", "Aggiornamento meteo ricevuto:");
            Log.d("WeatherService", "Vento: " + currentWeather.getWindSpeed() + " km/h");
            Log.d("WeatherService", "Direzione: " + getWindDirectionString(currentWeather));
            Log.d("WeatherService", "Orario rilevazione: " + currentWeather.getTime().toString());

            if (showToast)
                showWeatherUpdateMessage(currentWeather);
        } else {
            Log.e("WeatherService", "Errore nell'aggiornamento meteo", update.getError());
            Toast.makeText(this, getString(R.string.error_update_weather), Toast.LENGTH_SHORT).show();
        }
    }



    private void showWeatherUpdateMessage(Osservazione forecast) {
        String message = getString(R.string.updated) + " " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(forecast.getTime()) + "\n" +
                getString(R.string.condimeteo) + " " + getWindBeaufortString(forecast) +
                " (" + (int) forecast.getWindSpeed() + " km/h) " + getString(R.string.da) + " " + getWindDirectionString(forecast);

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getWindBeaufortString(Osservazione forecast) {
        return getWindBeaufortString((int) forecast.getWindBeaufort());
    }

    private String getWindBeaufortString(int force) {
        switch (force) {
            case 0 -> {
                return getString(R.string.calma);
            }
            case 1 -> {
                return getString(R.string.bavaDiVento);
            }
            case 2 -> {
                return getString(R.string.brezzaLeggera);
            }
            case 3 -> {
                return getString(R.string.brezzaTesa);
            }
            case 4 -> {
                return getString(R.string.ventoModerato);
            }
            case 5 -> {
                return getString(R.string.ventoTeso);
            }
            case 6 -> {
                return getString(R.string.ventoFresco);
            }
            case 7 -> {
                return getString(R.string.ventoForte);
            }
            case 8 -> {
                return getString(R.string.burrasca);
            }
            case 9 -> {
                return getString(R.string.burrascaForte);
            }
            case 10 -> {
                return getString(R.string.tempesta);
            }
            case 11 -> {
                return getString(R.string.fortunale);
            }
            case 12 -> {
                return getString(R.string.uragano);
            }
        }

        return getString(R.string.errore);
    }

    private String getWindDirectionString(Osservazione forecast) {
        return getWindDirectionString(forecast.getWindDirection());
    }

    private String getWindDirectionString(Osservazione.Direction direction) {
        switch (direction) {
            case N -> {
                return getString(R.string.nord);
            }
            case NW -> {
                return getString(R.string.nordOvest);
            }
            case NE -> {
                return getString(R.string.nordEst);
            }
            case E -> {
                return getString(R.string.est);
            }
            case SE -> {
                return getString(R.string.sudEst);
            }
            case S -> {
                return getString(R.string.sud);
            }
            case SW -> {
                return getString(R.string.sudOvest);
            }
            case W -> {
                return getString(R.string.ovest);
            }
            default -> {
                return null; // NOTE: it can't happen, here just to make the compiler happy
            }
        }
    }

}

