package co.za.gmapssolutions.beatraffic;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import co.za.gmapssolutions.beatraffic.Roads.RoadFetcher;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.executor.DefaultExecutorSupplier;
import co.za.gmapssolutions.beatraffic.map.MapTileFetcher;
import co.za.gmapssolutions.beatraffic.nominatim.ReverseGeoCoderNominatim;
import co.za.gmapssolutions.beatraffic.permissions.Permission;
import co.za.gmapssolutions.beatraffic.popup.Popup;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.services.AutoStart;
import co.za.gmapssolutions.beatraffic.services.location.LocationReceiver;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static co.za.gmapssolutions.beatraffic.permissions.Permission.PERMISSIONS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    //nominatim
    private final ReverseGeoCoderNominatim geocoderNominatim = null;
    private Future<?> geocoderNominatimFuture;

    private Future<?> roadFuture;

    //progess bar
    private ProgressBar progressBar;

    //main thread handler
    protected mHandler handler;


    //Detecting device in car
    private AutoStart detectedActivity;
    private final int activityType=-100;
    //rest api url
    private RestClient restClient = null;
    //traffic forecast
    private String trafficForecast;
    //permissions
    private void requestPermission(String[] permissions){
        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Permission.hasPermissions(this,permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trustAllCertificates(); //for running on emulator

        User user = new User(1, "car");//LOGIN

        Configuration.getInstance().setOsmdroidBasePath(new File(this.getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(this.getCacheDir(), "osmdroid/tiles"));

        setContentView(R.layout.activity_main);
        Toolbar toolbar  = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //register with osm
        Configuration.getInstance().setUserAgentValue(getPackageName());
        //
        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();
        progressBar.setVisibility(View.INVISIBLE);
        requestPermission(PERMISSIONS);

        //Thread executor manager
        //Threads handler
        DefaultExecutorSupplier defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
        ThreadPoolExecutor backGroundThreadPoolExecutor = defaultExecutorSupplier.forBackgroundTasks();
//        Executor runOnUiThread = defaultExecutorSupplier.forMainThreadTasks();
        //map
        //Map
        MapView map = findViewById(R.id.map);
        //map.setMapOrientation(90f);

        IMapController mapController = map.getController();
        MapTileFetcher mapTile = new MapTileFetcher(this, map, mapController);
        backGroundThreadPoolExecutor.execute(mapTile);

        //detected activity
        detectedActivity = new AutoStart();

        //location service
        Intent locationIntent = new Intent(this, LocationService.class);
        //location
        LocationReceiver locationReceiver = new LocationReceiver(new Handler(), this, map, mapController);
        locationIntent.putExtra("currentLocation", locationReceiver);
        startService(locationIntent);

        //reverse geocoder nominatim
        GeocoderNominatim geocoder = new GeocoderNominatim(getPackageName());

        //roads
        //Road
        RoadManager roadManager = new OSRMRoadManager(this);
        //rest api
        try {
            URL url = new URL("http://192.168.8.102:8080/location");
            restClient = new RestClient(url);
        } catch (IOException e) {
            e.printStackTrace();
//            System.exit(1);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//
        //alert
        Popup popup = new Popup(this, getResources().getString(R.string.traffic_routes),
                getResources().getString(R.string.yes), getResources().getString(R.string.continue_navigating));
        //destination handle
        AlertDialog alertDialog = popup.create();

        //Looper.getMainLooper()
        handler = new mHandler(this, map, roadManager, locationReceiver, geocoder,
                user,restClient,progressBar, backGroundThreadPoolExecutor);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem search_item = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) search_item.getActionView();
        searchView.setFocusable(false);
        searchView.setQueryHint("Destination...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                progressBar.setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putString("nominatim-destination",s);
                msg.setData(bundle);
                handler.sendMessage(msg);
//                geocoderNominatim = new ReverseGeoCoderNominatim(getApplicationContext()
//                        ,handler,geocoder,locationReceiver.getStartPoint(),s);
//                backGroundThreadPoolExecutor.execute(geocoderNominatim);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.search) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i : grantResults) {
            if(i == PackageManager.PERMISSION_DENIED){
                //this.finish();
                break;
            }
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
//        backGroundThreadPoolExecutor.execute(mapTile);
//        startService(LocationIntent);
        LocalBroadcastManager.getInstance(this).registerReceiver(detectedActivity,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }
    public void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }


                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

}
