package co.za.gmapssolutions.beatraffic;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
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
import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import co.za.gmapssolutions.beatraffic.services.location.LocationReceiver;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import co.za.gmapssolutions.beatraffic.transition.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static co.za.gmapssolutions.beatraffic.permissions.Permission.PERMISSIONS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    //Threads handler
    private DefaultExecutorSupplier defaultExecutorSupplier;
    private ThreadPoolExecutor backGroundThreadPoolExecutor;
    private Executor runOnUiThread;
    //Map
    private MapView map;
    private MapTileFetcher mapTile;
    private IMapController mapController;

    //location
    private LocationReceiver locationReceiver;
    private Intent LocationIntent;
    //nominatim
    private ReverseGeoCoderNominatim geocoderNominatim;
    private GeocoderNominatim geocoder;
    private Future<?> geocoderNominatimFuture;

    //Road
    private RoadManager roadManager;
    private RoadFetcher roadFetcher;
    private Future<?> roadFuture;

    //progess bar
    private ProgressBar progressBar;

    //main thread handler
    private Handler handler;

    //destination
    private String destination= "";
    private String roads="none";
    //Detecting device in car
    private BroadcastReceiver detectedActivity;
    private int activityType=-100;
    //alert
    private Popup popup;
    private AlertDialog alertDialog;
    //rest api url
    private URL url = null;

    //permissions
    private boolean runApp = false;
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
        defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
        backGroundThreadPoolExecutor = defaultExecutorSupplier.forBackgroundTasks();
        runOnUiThread = defaultExecutorSupplier.forMainThreadTasks();
        //map
        map = findViewById(R.id.map);
        //map.setMapOrientation(90f);

        mapController = map.getController();
        mapTile = new MapTileFetcher(this, map, mapController);
        backGroundThreadPoolExecutor.execute(mapTile);

        //detected activity
        detectedActivity = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    activityType = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);

                    Toast.makeText(context, "Activity type: " + activityType + " , confidence : " + confidence, Toast.LENGTH_LONG).show();

                }
            }
        };

        //location service
        LocationIntent = new Intent(MainActivity.this, LocationService.class);
        locationReceiver = new LocationReceiver(new Handler(), this, map, mapController);
        LocationIntent.putExtra("currentLocation", locationReceiver);
        startService(LocationIntent);

        //reverse geocoder nominatim
        geocoder = new GeocoderNominatim(getPackageName());

        //roads
        roadManager = new OSRMRoadManager(this);
        //rest api
        try {
            url = new URL("http://192.168.1.106:8080/location");
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
        popup = new Popup(this, getResources().getString(R.string.traffic_routes),
                getResources().getString(R.string.yes), getResources().getString(R.string.continue_navigating));
        //destination handle
        alertDialog = popup.create();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                destination = bundle.getString("get-destination");
                if (destination != null && destination.equals("success")) {
                    //get traffic
                    if(activityType != -100){
                        User user = new User(1,"car");
                        KafkaProducerRestClient producerRestClient = new KafkaProducerRestClient(url, user,
                                geocoderNominatim.getDepartureAddress(),geocoderNominatim.getDestinationAddress().iterator().next(),new JSONArray());
                        backGroundThreadPoolExecutor.submit(producerRestClient);
                    }

                    //autoStart = new AutoStart(backGroundThreadPoolExecutor,url,geocoderNominatim.getDepartureAddress(),geocoderNominatim.getDestinationAddress().iterator().next());
                   // Log.v(TAG,geocoderNominatim.getDepartureAddress().toString());
                    //Toast.makeText(getApplicationContext(), geocoderNominatim.getDepartureAddress().getLocality().toString(), Toast.LENGTH_LONG).show();
//                    roadFetcher = new RoadFetcher(getApplicationContext(), this, map, roadManager,
//                            locationReceiver.getStartPoint(), geocoderNominatim.getDestination());
                    //backGroundThreadPoolExecutor.execute(roadFetcher);
                    destination = "";
                    Log.v(TAG, "Destination fetched");
                }
                roads = bundle.getString("get-roads");

                if (roads != null && roads.equals("done")) {
                    //alertDialog.show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        };
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
                geocoderNominatim = new ReverseGeoCoderNominatim(getApplicationContext()
                        ,handler,geocoder,locationReceiver.getStartPoint(),s);
                backGroundThreadPoolExecutor.execute(geocoderNominatim);
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
}
