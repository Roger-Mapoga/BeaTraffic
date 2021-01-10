package co.za.gmapssolutions.beatraffic;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.osmdroid.bonuspack.routing.Road;

public class BeaTrafficViewModel extends ViewModel {
    private String TAG = BeaTrafficViewModel.class.getSimpleName();
    public boolean isNewlyCreated = true;
    public String requestedRoutes = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.routes";
//    public Road[] routes;
    public String userMapZoomLevel = "co.za.gmapssolutions.beatraffic.BeaTrafficViewModel.mapZoomLevel";
//    public Double mapZoomLevel;

    private MutableLiveData<Road[]> routes;
    private MutableLiveData<Double> mapZoomLevel;
    public MutableLiveData<Road[]> getRoutes(){
        if(routes == null){
            routes = new MutableLiveData<>();
        }
        return routes;
    }
    public MutableLiveData<Double> getMapZoomLevel(){
        if(mapZoomLevel == null){
            mapZoomLevel = new MutableLiveData<>();
        }
        return mapZoomLevel;
    }


}
