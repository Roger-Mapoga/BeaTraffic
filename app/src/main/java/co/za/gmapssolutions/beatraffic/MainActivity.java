package co.za.gmapssolutions.beatraffic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import co.za.gmapssolutions.beatraffic.Roads.RoadFetcher;
import co.za.gmapssolutions.beatraffic.executor.DefaultExecutorSupplier;
import co.za.gmapssolutions.beatraffic.map.MapTileFetcher;
import co.za.gmapssolutions.beatraffic.nominatim.ReverseGeocoderNominatim;
import co.za.gmapssolutions.beatraffic.permissions.Permission;
import co.za.gmapssolutions.beatraffic.services.location.LocationReceiver;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static co.za.gmapssolutions.beatraffic.permissions.Permission.PERMISSIONS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    //Threads handler
    private DefaultExecutorSupplier defaultExecutorSupplier;
    private ThreadPoolExecutor backGroundThreadPoolExecutor;
    //Map
    private MapView map;
    private MapTileFetcher mapTile;
    private IMapController mapController;

    //location
    private LocationReceiver locationReceiver;
    //nominatim
    private ReverseGeocoderNominatim geocoderNominatim;
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
    private boolean nextAction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar  = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //register with osm
        Configuration.getInstance().setUserAgentValue(getPackageName());
        //
        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();
        progressBar.setVisibility(View.INVISIBLE);
        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Permission.hasPermissions(this,PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 112);
            }
        }
        //Thread executor manager
        defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
        backGroundThreadPoolExecutor = defaultExecutorSupplier.forBackgroundTasks();
        //map
        map = findViewById(R.id.map);
        //map.setMapOrientation(90f);

        mapController = map.getController();
        mapTile = new MapTileFetcher(this,map,mapController);
        backGroundThreadPoolExecutor.execute(mapTile);
        //location service
        Intent intent = new Intent(this, LocationService.class);
        locationReceiver = new LocationReceiver(new Handler(),this,map,mapController);
        intent.putExtra("currentLocation",locationReceiver);

        startService(intent);
        //reverse geocoder nominatim
        geocoder = new GeocoderNominatim(getPackageName());

        //roads
        roadManager = new OSRMRoadManager(this);

//        beatTrafficLocation location = new beatTrafficLocation(1,"toyota","kutlwano",
//                21.0,21.0);//
//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            URL url = new URL("http://192.168.1.106:8080/location");
//            KafkaProducerRestClient restClient = new KafkaProducerRestClient(url,location,jsonObject);
//            backGroundThreadPoolExecutor.submit(restClient);
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }


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

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
//
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("See routes with traffic ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Continue with navigation", dialogClickListener);
        final AlertDialog alertDialog = builder.create();
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                Bundle bundle = msg.getData();
                destination = bundle.getString("get-destination");
                if(destination != null && destination.equals("success")){
                    //get route
                    Toast.makeText(getApplicationContext(),"destination success",Toast.LENGTH_LONG).show();
                    roadFetcher = new RoadFetcher(getApplicationContext(),this, map,roadManager,
                            locationReceiver.getStartPoint(),geocoderNominatim.getDestination());
                    backGroundThreadPoolExecutor.execute(roadFetcher);
                    destination="";
                    Log.v(TAG,"Destination fetched");
                }
                roads = bundle.getString("get-roads");

                if(roads != null && roads.equals("done")){
                    alertDialog.show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        };
        if(nextAction) {

        }
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
                geocoderNominatim = new ReverseGeocoderNominatim(getApplicationContext()
                        ,handler,geocoder,s);
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

}
