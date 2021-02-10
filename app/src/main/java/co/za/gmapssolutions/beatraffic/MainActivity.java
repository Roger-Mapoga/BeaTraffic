package co.za.gmapssolutions.beatraffic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import co.za.gmapssolutions.beatraffic.domain.AuthenticationPagerAdapter;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.executor.DefaultExecutorSupplier;
import co.za.gmapssolutions.beatraffic.permissions.Permission;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.security.SecurePreferences;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ThreadPoolExecutor;

import static co.za.gmapssolutions.beatraffic.permissions.Permission.PERMISSIONS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String HOST = "http://192.168.8.106"; //"http://taungcareerdevelopment.co.za";
    //permissions
    private void requestPermission(String[] permissions){
        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Permission.hasPermissions(this,permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }
    private MainFragment mainFragment;
    private RestClient loginOrRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission(PERMISSIONS);
        trustAllCertificates(); //for running on emulator
        //login logic
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
        AuthenticationPagerAdapter  authenticationPagerAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager(),
                getLifecycle());
        SecurePreferences preferences = new SecurePreferences(this, "user-info",
                "YourSecurityKey", true);
        //mapstorage osmdroid
        Configuration.getInstance().setOsmdroidBasePath(new File(this.getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(this.getCacheDir(), "osmdroid/tiles"));

        if(preferences.getString("email") == null && preferences.getString("password") == null) {
            authenticationPagerAdapter.addFragment(new LoginFragment(preferences, HOST));
            viewPager.setAdapter(authenticationPagerAdapter);
            IntentFilter intentFilter = new IntentFilter("co.za.gmapssolutions.beatraffic.loginOrRegister");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String email = intent.getStringExtra("email");
                    String password = intent.getStringExtra("password");
                    //thread
                    DefaultExecutorSupplier defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
                    ThreadPoolExecutor backGroundThreadPoolExecutor = defaultExecutorSupplier.forLightWeightBackgroundTasks();
                    backGroundThreadPoolExecutor.execute(new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            Intent loginStatus = new Intent().setAction("co.za.gmapssolutions.beatraffic.loginOrRegisterError");
                            try {
                                URL url = new URL(HOST+":8080/loginOrRegister");
                                loginOrRegister = new RestClient(url);
                                JSONObject userDetails = new JSONObject();
                                userDetails.put("email",email);
                                userDetails.put("password",password);
                                HttpURLConnection con = loginOrRegister.post(userDetails.toString());
                                if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    long Id = Long.parseLong(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
                                    preferences.put("userId",String.valueOf(Id));
                                    preferences.put("email",email);
                                    preferences.put("password",password);
                                    loginStatus.putExtra("loginSuccess","success");
                                }else{
                                    loginStatus.putExtra("loginError",new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
                                }
                                sendBroadcast(loginStatus);
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, intentFilter);
            IntentFilter intentFilter1 = new IntentFilter("co.za.gmapssolutions.beatraffic.loginOrRegisterError");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String loginSuccess = intent.getStringExtra("loginSuccess");
                    Log.i(TAG, "onReceive: "+loginSuccess);
                    assert loginSuccess != null;
                    if(!loginSuccess.isEmpty() && loginSuccess.equals("success")){
                        User user = new User(Long.parseLong(preferences.getString("userId")), "car");
                        authenticationPagerAdapter.clear();
                        mainFragment = new MainFragment(HOST, user);
                        authenticationPagerAdapter.addFragment(mainFragment);
                        viewPager.setAdapter(authenticationPagerAdapter);
                    }
                }
            },intentFilter1);
        }else{
            User user = new User(Long.parseLong(preferences.getString("userId")), "car");
            authenticationPagerAdapter.clear();
            mainFragment = new MainFragment(HOST,user);
            authenticationPagerAdapter.addFragment(mainFragment);
            viewPager.setAdapter(authenticationPagerAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        getSupportFragmentManager().putFragment(outState,"MainFragment",mainFragment);
    }
    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        assert savedInstanceState != null;
        mainFragment = (MainFragment) getSupportFragmentManager().getFragment(savedInstanceState, "MainFragment");
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
////        if (id == R.id.search) {
////            return true;
////        }
//
//        return super.onOptionsItemSelected(item);
//    }

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

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        for(int i : grantResults) {
//            if(i == PackageManager.PERMISSION_DENIED){
//                //this.finish();
//                break;
//            }
//        }
//
//    }
    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(detectedActivity,
//                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        getApplicationContext().getSharedPreferences("test",MODE_PRIVATE).edit().putBoolean("isActive", false).commit();
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
