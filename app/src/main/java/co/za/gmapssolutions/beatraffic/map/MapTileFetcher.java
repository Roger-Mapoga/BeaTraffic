package co.za.gmapssolutions.beatraffic.map;


import android.content.Context;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class MapTileFetcher implements Runnable {
    private static final String TAG = MapTileFetcher.class.getSimpleName();
    private Context context;
    private TextView textView;

    private MapView map;
    private IMapController mapController;
    private Marker nodeMarker;
    private RoadNode node;

    public MapTileFetcher(Context context,MapView map,IMapController mapController){
        this.context = context;
        this.map = map;
        this.mapController = mapController;
    }
    @Override
    public void run(){

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        map.setMultiTouchControls(true);

        mapController.setZoom(20.0);

        map.invalidate();

    }


}
