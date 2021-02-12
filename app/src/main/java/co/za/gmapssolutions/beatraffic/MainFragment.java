package co.za.gmapssolutions.beatraffic;

import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.za.gmapssolutions.beatraffic.Roads.DisplayRoutes;
import co.za.gmapssolutions.beatraffic.adapter.ItemAdapter;
import co.za.gmapssolutions.beatraffic.adapter.RouteDetail;
import co.za.gmapssolutions.beatraffic.domain.User;
import co.za.gmapssolutions.beatraffic.executor.DefaultExecutorSupplier;
import co.za.gmapssolutions.beatraffic.map.MapTileFetcher;
import co.za.gmapssolutions.beatraffic.restClient.RestClient;
import co.za.gmapssolutions.beatraffic.services.AutoStart;
import co.za.gmapssolutions.beatraffic.services.MyLocation;
import co.za.gmapssolutions.beatraffic.services.location.BeatTrafficLocation;
import co.za.gmapssolutions.beatraffic.services.location.LocationReceiver;
import co.za.gmapssolutions.beatraffic.services.location.LocationService;
import co.za.gmapssolutions.beatraffic.utils.SortRoads;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements ItemAdapter.ItemClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //application
    private ProgressBar progressBar;
    private BeaTrafficViewModel viewModel;
    private RecyclerView recyclerView;
    private ItemAdapter mAdapter;
    private BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    private  String HOST;
    private RestClient requestRestClient;
    private RestClient trafficRestClient;
    private User user;
    private mHandler handler;
    private MapView map;
    //vars
    private final AtomicReference<GeoPoint> start = new AtomicReference<>();
    private final AtomicReference<GeoPoint> end = new AtomicReference<>();
    public MainFragment() {
        // Required empty public constructor
    }
    public MainFragment(String HOST,User user){
        this.HOST = HOST;
        this.user = user;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public MainFragment newInstance(BeaTrafficViewModel viewModel,String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        viewModel = new ViewModelProvider(this).get(BeaTrafficViewModel.class);
        //saveState logic
        if(viewModel.isNewlyCreated && savedInstanceState != null){
            Parcelable[] array = savedInstanceState.getParcelableArray(viewModel.requestedRoutes);
            if(array != null) {
                List<Road> roadList = new ArrayList<>();
                for (Parcelable p : array) {
                    roadList.add((Road) p);
                }
                Road[] routes = new Road[roadList.size()];
                roadList.toArray(routes);
                viewModel.getRoutes().setValue(routes);
            }
            viewModel.getMapZoomLevel().setValue(savedInstanceState.getDouble(viewModel.userMapZoomLevel));
            viewModel.getBottomSheetState().setValue(savedInstanceState.getInt(viewModel.userBottomSheetState));
            viewModel.getStartPoint().setValue(savedInstanceState.getParcelable(viewModel.userStartPoint));
            start.set(viewModel.getStartPoint().getValue());
            viewModel.getEndPoint().setValue(savedInstanceState.getParcelable(viewModel.userEndPoint));
            end.set(viewModel.getEndPoint().getValue());
            viewModel.getBottomSheetHidden().setValue(savedInstanceState.getBoolean(viewModel.bottomSheetHidden));
            viewModel.getRoute().setValue(savedInstanceState.getParcelable(viewModel.userRoute));
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show search button
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar  = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        //progressbar
        progressBar = view.findViewById(R.id.progressBar_cyclic);
        progressBar.bringToFront();
        progressBar.setVisibility(View.INVISIBLE);
        //recyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //threads
        DefaultExecutorSupplier defaultExecutorSupplier = DefaultExecutorSupplier.getInstance();
        ThreadPoolExecutor backGroundThreadPoolExecutor = defaultExecutorSupplier.forBackgroundTasks();
        //map fetcher
        map = view.findViewById(R.id.map);
        final ITileSource tileSource = new XYTileSource("Mapnik", 1, 20, 256,
                ".png", new String[]{HOST+":7071/tile/"});
        MapTileProviderBasic tileProvider = new MapTileProviderBasic(getActivity(),tileSource);
        map.setTileProvider(tileProvider);
        map.setTilesScaledToDpi(true);
        map.setTilesScaleFactor(1.5f);
        map.setZoomRounding(true);
        IMapController mapController = map.getController();
        MapTileFetcher mapTile = new MapTileFetcher(getActivity(), map, mapController);
        backGroundThreadPoolExecutor.execute(mapTile);
        //location update
        MyLocation myLocation = new MyLocation(map);
        DisplayRoutes displayRoutes = new DisplayRoutes(getActivity(), map,myLocation,viewModel);
        BeatTrafficLocation listener = new BeatTrafficLocation(getActivity(),displayRoutes);
        listener.enableLocation();
        //reverse geocoder nominatim
        GeocoderNominatim geocoder = new GeocoderNominatim(getActivity().getPackageName());
        geocoder.setService(HOST+":7070/");
        //Road
        OSRMRoadManager osrmRoadManager =  new OSRMRoadManager(getActivity());
        osrmRoadManager.setService(HOST+":5000/route/v1/driving/");
        try {
            URL requestUrl = new URL(HOST+":8080/request");
            URL trafficUrl = new URL(HOST+":8080/traffic");
            requestRestClient = new RestClient(requestUrl);
            trafficRestClient = new RestClient(trafficUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //bottomView
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //bottomSheet button
        view.findViewById(R.id.bottomSheetButton).setOnClickListener(v -> {
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            else
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                viewModel.getBottomSheetState().postValue(newState);
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        //handler interrupt
        handler = new mHandler(getActivity(), map,mapController, osrmRoadManager, listener, geocoder,
                user,requestRestClient,trafficRestClient,progressBar,backGroundThreadPoolExecutor,displayRoutes,
                myLocation,viewModel,bottomSheetBehavior);
        //view model observers
        //bottom sheet control
        final Observer<Integer> bottomSheetStateObserver = bottomSheetState ->{
             assert bottomSheetState != null;
             if(bottomSheetState == BottomSheetBehavior.SAVE_NONE){
                 bottomSheetBehavior.setHideable(true);
                 bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                 viewModel.getBottomSheetHidden().setValue(true);
             }else{
                 bottomSheetBehavior.setHideable(false);
                 bottomSheetBehavior.setState(bottomSheetState);
                 viewModel.getBottomSheetHidden().setValue(false);
             }
        };
        viewModel.getBottomSheetState().observe(getViewLifecycleOwner(),bottomSheetStateObserver);
        final Observer<Boolean> bottomSheetHiddenObserver = bottomSheetHidden ->{
            assert bottomSheetHidden != null;
            if(!bottomSheetHidden){
                mAdapter = new ItemAdapter(getRouteDetailList());
                mAdapter.setClickListener(this);
                recyclerView.setAdapter(mAdapter);
            }
        };
        viewModel.getBottomSheetHidden().observe(getViewLifecycleOwner(),bottomSheetHiddenObserver);
        //routes control
        final Observer<Road[]> roadsObserver = roads -> {
            assert roads != null;
            displayRoutes.show(roads,getActivity());
        };
        viewModel.getRoutes().observe(getViewLifecycleOwner(),roadsObserver);
        //start & end point control
        final Observer<GeoPoint> startPointObserver = startPoint ->{
            if(startPoint != null) {
                start.set(startPoint);
                displayRoutes.setStartPointIcon(startPoint);
            }
        };
        viewModel.getStartPoint().observe(getViewLifecycleOwner(),startPointObserver);
        final Observer<GeoPoint> endPointObserver = endPoint->{
            assert endPoint != null;
            end.set(endPoint);
        };
        viewModel.getEndPoint().observe(getViewLifecycleOwner(),endPointObserver);
        //route details control
        final Observer<RouteDetail> RouteObserver = route ->{
            List<GeoPoint> currGeoPoint = new ArrayList<>();
            if(route != null)
                if(route.getBtnRouteStateValue().equals("start")) {
                    currGeoPoint.add(start.get());
                    currGeoPoint.add(end.get());
                    zoomMapToBoundingBox(viewModel.getBoundingBox(currGeoPoint), false);
                    if(viewModel.getRoute().getValue() != null) {
                        mAdapter.setBtnAltStateValue(viewModel.getRoute().getValue().getId(), "start");
                        recyclerView.setAdapter(mAdapter);
                    }
                }else{
                    if(route.getBtnRouteStateValue().equals("cancel") && start.get() != null) {
                        currGeoPoint.add(start.get());
                        zoomMapToBoundingBox(viewModel.getBoundingBox(currGeoPoint), true);
                        if(viewModel.getRoute().getValue() != null){
                            mAdapter.setBtnAltStateValue(viewModel.getRoute().getValue().getId(),"cancel");
                            recyclerView.setAdapter(mAdapter);
                        }
                    }
                }
            if(viewModel.getBottomSheetState().getValue() != null)
                bottomSheetBehavior.setState(viewModel.getBottomSheetState().getValue());
        };
        viewModel.getRoute().observe(getViewLifecycleOwner(),RouteObserver);
        viewModel.isNewlyCreated = false;
        AutoStart detectedActivity = new AutoStart();
        //location service
        Intent locationIntent = new Intent(getContext(), LocationService.class);
        LocationReceiver locationReceiver = new LocationReceiver(new Handler(), getActivity(), map, mapController,myLocation);
        locationIntent.putExtra("locationReceiver", locationReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.getContext().startForegroundService(locationIntent);
        }else{
            view.getContext().startService(locationIntent);
        }
    }
    private void zoomMapToBoundingBox(BoundingBox currLocBb, boolean driving){
        if(driving) {
            map.zoomToBoundingBox(currLocBb, true, 10, 18.0, 10L);
        }else {
            map.zoomToBoundingBox(currLocBb,true,250);
        }
        map.postInvalidate();
    }
    @Override
    public void onItemClick(View view, int position) {
        if(viewModel.getRoute().getValue() != null ) {
            int prevClicked = viewModel.getRoute().getValue().getId();
            if (prevClicked != position) {
                mAdapter.getItem(prevClicked).setBtnRouteStateValue("start");
            }
        }
        if(mAdapter.getBtnAltStateValue(position).equals("start")) {
            mAdapter.setBtnAltStateValue(position, "cancel");
            mAdapter = new ItemAdapter(mAdapter.sortRoutes(position));
            mAdapter.setClickListener(this);
            viewModel.getRoute().setValue(mAdapter.getItem(0));
        }else {
            mAdapter = new ItemAdapter(getRouteDetailList());
            mAdapter.setClickListener(this);
            mAdapter.setBtnAltStateValue(position, "start");
            viewModel.getRoute().setValue(mAdapter.getItem(position));
        }
        recyclerView.setAdapter(mAdapter);
    }
    private List<RouteDetail> getRouteDetailList(){
        ArrayList<RouteDetail> routesDetails = new ArrayList<>();
        int i = 0;
        if(viewModel.getRoutes().getValue() != null) {
            Arrays.sort(viewModel.getRoutes().getValue(), new SortRoads());
            for (Road route : viewModel.getRoutes().getValue()) {
                RouteDetail routeDetail = new RouteDetail(i);
                routeDetail.setRouteDetails(String.format(this.getString(R.string.route_details), viewModel.getTravelDuration(route.mDuration)
                        , route.mLength));
                routeDetail.setBtnRouteStateValue("start");
                routesDetails.add(routeDetail);
                i++;
            }
        }
        return routesDetails;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu,inflater);
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
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(viewModel.getRoutes().getValue() != null)
            outState.putParcelableArray(viewModel.requestedRoutes, viewModel.getRoutes().getValue());
        if(viewModel.getMapZoomLevel().getValue() != null)
            outState.putDouble(viewModel.userMapZoomLevel,viewModel.getMapZoomLevel().getValue());
        if(viewModel.getBottomSheetState().getValue() != null)
            outState.putInt(viewModel.userBottomSheetState,viewModel.getBottomSheetState().getValue());
        if(viewModel.getStartPoint().getValue() != null)
            outState.putParcelable(viewModel.userStartPoint,viewModel.getStartPoint().getValue());
        if(viewModel.getEndPoint().getValue() != null)
            outState.putParcelable(viewModel.userEndPoint,viewModel.getEndPoint().getValue());
        if(viewModel.getRoute().getValue() != null)
            outState.putParcelable(viewModel.userRoute,viewModel.getRoute().getValue());
        if(viewModel.getBottomSheetHidden().getValue() != null)
            outState.putBoolean(viewModel.bottomSheetHidden,viewModel.getBottomSheetHidden().getValue());
    }

}