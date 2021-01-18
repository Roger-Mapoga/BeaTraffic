package co.za.gmapssolutions.beatraffic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import co.za.gmapssolutions.beatraffic.Roads.DisplayForecast;
import co.za.gmapssolutions.beatraffic.Roads.DisplayRoutes;
import co.za.gmapssolutions.beatraffic.Roads.RoadFetcher;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.nominatim.ReverseGeoCoderNominatim;
import co.za.gmapssolutions.beatraffic.restClient.KafkaConsumerRestClient;
import co.za.gmapssolutions.beatraffic.restClient.KafkaProducerRestClient;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import co.za.gmapssolutions.beatraffic.services.location.BeatTrafficLocation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.json.JSONArray;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class mHandler extends Handler {
    //
    private static final String TAG = mHandler.class.getSimpleName();

    private RoadFetcher roadFetcher;
    private final Context context;
    private final MapView map;
    private final RoadManager roadManager;
    private final BeatTrafficLocation listner;
    private ReverseGeoCoderNominatim geocoderNominatim;
    private final GeocoderNominatim geocoder;
    private final User user;
    private final ThreadPoolExecutor backGroundThreadPoolExecutor;
    private final RestClient trafficRestClient,requestRestClient;
    private final ProgressBar progressBar;
    private final DisplayRoutes displayRoutes;
    private final BeaTrafficViewModel viewModel;
    private final BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    private final MyLocation myLocation;
    private final IMapController mapController;
    private final TextView tvTravelDetails;
    public mHandler(Context context, MapView map, IMapController mapController, RoadManager roadManager, BeatTrafficLocation listner,
                    GeocoderNominatim geocoder, User user, RestClient requestRestClient, RestClient trafficRestClient,
                    ProgressBar progressBar, ThreadPoolExecutor backGroundThreadPoolExecutor,
                    DisplayRoutes displayRoutes, MyLocation myLocation, BeaTrafficViewModel viewModel,
                    BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior, TextView tvTravelDetails){
        this.context = context;
        this.map = map;
        this.mapController = mapController;
        this.roadManager = roadManager;
        this.listner = listner;
//        this.geocoderNominatim = geocoderNominatim;
        this.geocoder = geocoder;
        this.user = user;
        this.requestRestClient = requestRestClient;
        this.trafficRestClient = trafficRestClient;
        this.progressBar = progressBar;
        this.backGroundThreadPoolExecutor = backGroundThreadPoolExecutor;
        this.displayRoutes = displayRoutes;
        this.myLocation = myLocation;
        this.viewModel = viewModel;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.tvTravelDetails = tvTravelDetails;
    }
    @Override
    public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        String nominatimDestination = bundle.getString("nominatim-destination");
        if(nominatimDestination != null){
            viewModel.getStartPoint().postValue(listner.getLastKnownLocation());
            geocoderNominatim = new ReverseGeoCoderNominatim(context
                        ,this,geocoder,listner.getLastKnownLocation(),nominatimDestination);
                backGroundThreadPoolExecutor.execute(geocoderNominatim);
        }
        String destination = bundle.getString("get-destination");
        String destinationError = bundle.getString("get-destination-error");

        if (destination != null && destination.equals("success")) {
            viewModel.getEndPoint().postValue(geocoderNominatim.getDestination());
            roadFetcher = new RoadFetcher(context, this, map,mapController,roadManager,
                    listner.getLastKnownLocation(), geocoderNominatim.getDestination(),displayRoutes);
            backGroundThreadPoolExecutor.execute(roadFetcher);
            Log.v(TAG, "Destination fetched");
        }else if(destinationError != null && destinationError.equals("error")){
            Toast.makeText(context,"destination error",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
        String roads = bundle.getString("get-roads");
        boolean showBottomSheet = false;
        if (roads != null && roads.equals("done")) {
            KafkaProducerRestClient producerRestClient = new KafkaProducerRestClient(requestRestClient, this,user,
                    geocoderNominatim.getDepartureAddress(),geocoderNominatim.getDestinationAddress(),
                    roadFetcher.getRoutes(),new JSONArray());
            viewModel.getRoutes().postValue(roadFetcher.getRoutes());
            backGroundThreadPoolExecutor.submit(producerRestClient);
            showBottomSheet = true;
//            viewModel.getShowBottomSheet().postValue(true);
        }
        int httpsPostResponse = bundle.getInt("http-post-status");
        if(httpsPostResponse == HttpURLConnection.HTTP_CREATED){
            //traffic-forecast
            Log.d(TAG, "Getting traffic forecast: " + httpsPostResponse);
            KafkaConsumerRestClient kafkaConsumerRestClient = new KafkaConsumerRestClient(trafficRestClient,this);
            backGroundThreadPoolExecutor.submit(kafkaConsumerRestClient);
        }else if (httpsPostResponse == HttpURLConnection.HTTP_NOT_ACCEPTABLE){
            Log.d(TAG,"Consumer Error: " + httpsPostResponse);
        }
        int trafficResponse = bundle.getInt("traffic-response");
        if(trafficResponse == HttpURLConnection.HTTP_OK){
            String trafficForecast = bundle.getString("traffic-forecast");
            DisplayForecast displayForecast = new DisplayForecast(context,map,trafficForecast);
            backGroundThreadPoolExecutor.submit(displayForecast);
            progressBar.setVisibility(View.INVISIBLE);
        }else if (trafficResponse == HttpURLConnection.HTTP_NO_CONTENT){
            Log.d(TAG, "No content: "+ trafficResponse);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(context,"No traffic in routes",Toast.LENGTH_LONG).show();
        }
        //bottom sheet
        if(showBottomSheet){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            List<GeoPoint> geoPointList = new ArrayList<>();
            geoPointList.add(listner.getLastKnownLocation());
            geoPointList.add(geocoderNominatim.getDestination());
            BoundingBox boundingBox = viewModel.getBoundingBox(geoPointList);
            map.zoomToBoundingBox(boundingBox,
                    true,250);
//            viewModel.getBoundingBox().postValue(boundingBox);
//            map.animate().start();
            map.postInvalidate();
            Log.d(TAG, "handleMessage: "+roadFetcher.getRoutes().length);

            tvTravelDetails.setText(String.format(context.getString(R.string.route_details), viewModel
                    .getTravelDuration(roadFetcher.getRoutes()[0].mDuration),roadFetcher.getRoutes()[0].mLength));
            viewModel.getBottomSheetState().postValue(bottomSheetBehavior.getState());
        }
    }


}
