package co.za.gmapssolutions.beatraffic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.Roads.DisplayForecast;
import co.za.gmapssolutions.beatraffic.Roads.RoadFetcher;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.nominatim.ReverseGeoCoderNominatim;
import co.za.gmapssolutions.beatraffic.restClient.KafkaConsumerRestClient;
import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.services.location.LocationReceiver;
import org.json.JSONArray;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.views.MapView;

import java.net.HttpURLConnection;
import java.util.concurrent.ThreadPoolExecutor;

public class mHandler extends Handler {
    //
    private static final String TAG = mHandler.class.getSimpleName();

    private RoadFetcher roadFetcher;
    private final Context context;
    private final MapView map;
    private final RoadManager roadManager;
    private final LocationReceiver locationReceiver;
    private ReverseGeoCoderNominatim geocoderNominatim;
    private final GeocoderNominatim geocoder;
    private final User user;
    private final ThreadPoolExecutor backGroundThreadPoolExecutor;
    private final RestClient restClient;
    private final ProgressBar progressBar;

    public mHandler(Context context,MapView map,RoadManager roadManager,LocationReceiver locationReceiver,
                    GeocoderNominatim geocoder,User user,RestClient restClient,
                    ProgressBar progressBar,ThreadPoolExecutor backGroundThreadPoolExecutor){
        this.context = context;
        this.map = map;
        this.roadManager = roadManager;
        this.locationReceiver = locationReceiver;
//        this.geocoderNominatim = geocoderNominatim;
        this.geocoder = geocoder;
        this.user = user;
        this.restClient = restClient;
        this.progressBar = progressBar;
        this.backGroundThreadPoolExecutor = backGroundThreadPoolExecutor;
    }
    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        String nominatimDestination = bundle.getString("nominatim-destination");
        if(nominatimDestination != null){
            geocoderNominatim = new ReverseGeoCoderNominatim(context
                        ,this,geocoder,locationReceiver.getStartPoint(),nominatimDestination);
                backGroundThreadPoolExecutor.execute(geocoderNominatim);
        }
        String destination = bundle.getString("get-destination");
        String destinationError = bundle.getString("get-destination-error");

        if (destination != null && destination.equals("success")) {
            roadFetcher = new RoadFetcher(context, this, map, roadManager,
                    locationReceiver.getStartPoint(), geocoderNominatim.getDestination());
            backGroundThreadPoolExecutor.execute(roadFetcher);
            destination = "";
            Log.v(TAG, "Destination fetched");
        }else if(destinationError != null && destinationError.equals("error")){
            Toast.makeText(context,"destination error",Toast.LENGTH_LONG).show();
        }
        String roads = bundle.getString("get-roads");
        if (roads != null && roads.equals("done")) {
            //alertDialog.show();
            //if(activityType == DetectedActivity.IN_VEHICLE){
            KafkaProducerRestClient producerRestClient = new KafkaProducerRestClient(restClient, this,user,
                    geocoderNominatim.getDepartureAddress(),geocoderNominatim.getDestinationAddress(),
                    roadFetcher.getRoutes(),new JSONArray());
            backGroundThreadPoolExecutor.submit(producerRestClient);
            //}
        }
        int httpsPostResponse = bundle.getInt("http-post-status");
        if(httpsPostResponse == HttpURLConnection.HTTP_CREATED){
            //traffic-forecast
            Log.d(TAG, String.valueOf(httpsPostResponse));
            KafkaConsumerRestClient kafkaConsumerRestClient = new KafkaConsumerRestClient(restClient,this);
            backGroundThreadPoolExecutor.submit(kafkaConsumerRestClient);
        }else if (httpsPostResponse == HttpURLConnection.HTTP_NOT_ACCEPTABLE){
            Log.d(TAG, String.valueOf(httpsPostResponse));
        }
        int trafficResponse = bundle.getInt("traffic-response");
        if(trafficResponse == HttpURLConnection.HTTP_OK){
            Log.d(TAG, String.valueOf(trafficResponse));

            String trafficForecast = bundle.getString("traffic-forecast");
            DisplayForecast displayForecast = new DisplayForecast(context,trafficForecast);
            backGroundThreadPoolExecutor.submit(displayForecast);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
