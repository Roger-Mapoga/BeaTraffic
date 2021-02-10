package co.za.gmapssolutions.beatraffic.map;


import android.content.Context;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class MapTileFetcher implements Runnable{
    private static final String TAG = MapTileFetcher.class.getSimpleName();
    private final Context context;
    private TextView textView;

    private final MapView map;
    private final IMapController mapController;
    private Marker nodeMarker;
    private RoadNode node;

    public MapTileFetcher(Context context,MapView map,IMapController mapController){
        this.context = context;
        this.map = map;
        this.mapController = mapController;
    }
    @Override
    public void run(){
//      map.setTileSource(tileSource);
// map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        mapController.setZoom(20.0);
        map.invalidate();

    }


}
