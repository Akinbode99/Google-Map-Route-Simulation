package com.example.mapapp;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener {

    private static final String TAG = "SUGGESTIONS";
    private MapView mMapView;
    private FloatingActionButton addActor,start;
    private GoogleMap googleMaps;
    private RelativeLayout formBackground;
    private Button submit, cancel;
    private PlacesClient placesClient;
    private Place PICKUP_PLACE;
    private Place DESTINATION_PLACE;
    private ArrayList<ActorPlace> suggestions;
    private EditText origin,destination;
    private RecyclerView sugestedplaces;
    private PlaceAdapter pLaceAdapter;
    private AutocompleteSessionToken token;
    private RectangularBounds bounds;
    private Runnable runnable;
    private DatabaseReference mDatabase;
    private Marker init,actorMarker;
    private ArrayList<LatLng> actorLocations;
    private double originLongitude,originLattitude,destinationLattitiude,destinationLongitude;
    private ArrayList<Actor> actors = new ArrayList<>();
    private boolean simulationHasStarted = false;
    private int count = 0;
    private LatLng markerPosition,markerDestination;
    private CountDownTimer countDownTimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.map);
        addActor = findViewById(R.id.add_actor);
        formBackground = findViewById(R.id.form_background);
        submit = findViewById(R.id.submit);
        cancel = findViewById(R.id.cancel);
        origin = findViewById(R.id.originlocation);
        destination = findViewById(R.id.destinationlocation);
        sugestedplaces = findViewById(R.id.sugestions_list);
        start = findViewById(R.id.start_simulation);
        suggestions = new ArrayList<ActorPlace>();
        actorLocations = new ArrayList<>();
        pLaceAdapter = new PlaceAdapter(suggestions);
        sugestedplaces.setHasFixedSize(true);
        sugestedplaces.setLayoutManager(new LinearLayoutManager(this));
        sugestedplaces.setAdapter(pLaceAdapter);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        String apiKey = getString(R.string.places_api_key);

        if (apiKey.equals("")) {
            Toast.makeText(MainActivity.this, getString(R.string.error_api_key),Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        placesClient = Places.createClient(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count ++;
                addNewActor(count);


            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formBackground.setVisibility(View.GONE);
            }
        });

        addActor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formBackground.setVisibility(View.VISIBLE);

            }
        });

        start.setOnClickListener(v -> {
            if(actors.isEmpty()){
                Toast.makeText(MainActivity.this,"Sorry, there are no actors on this map",Toast.LENGTH_LONG).show();
            }else{
                if(simulationHasStarted){
                 //   handler.removeCallbacks(runnable);
                    start.setImageResource(R.drawable.ic_play);
                    simulationHasStarted = false;
                    countDownTimer.cancel();
                }
                else{
                    start.setImageResource(R.drawable.ic_stop);
                    startSimulation();
                    simulationHasStarted = true;

                }


            }

        });


        token = AutocompleteSessionToken.newInstance();
        bounds = RectangularBounds.newInstance(
                new LatLng( 6.465422,  3.406448),
                new LatLng( 6.9098, 3.2584));

        origin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 0){
                    pLaceAdapter.UpdateList(suggestions);

                }
                else{
                    findPlaces(editable.toString(),origin);
                }



            }
        });

        destination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 0){
                    pLaceAdapter.UpdateList(suggestions);
                }
                else{
                    findPlaces(editable.toString(),destination);
                }



            }
        });

    }

    private void findPlaces(String Query, final EditText editText) {

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setCountry("ng")
                .setSessionToken(token)
                .setQuery(Query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            final ArrayList<ActorPlace> temp = new ArrayList<ActorPlace>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getFullText(null).toString());
                temp.add(new ActorPlace(prediction.getPrimaryText(null).toString(),
                        prediction.getSecondaryText(null).toString(), prediction.getPlaceId()));

                pLaceAdapter.UpdateList(temp);
                pLaceAdapter.setClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editText.getId() == R.id.originlocation){
                            if (TextUtils.isEmpty(origin.getText())) {
                                Snackbar.make(sugestedplaces, "Pick up bus stop can not be empty", Snackbar.LENGTH_SHORT).show();
                                //Toast.makeText(RequestBusRide.this,"PICK UP CAN NOT BE EMPTY!",Toast.LENGTH_LONG).show();
                            }
                            else{
                                int position = sugestedplaces.indexOfChild(view);
                                editText.setText(temp.get(position).getPrimary_place());
                                getLatLng(temp.get(position).getPlace_id(),R.id.originlocation);

                            }

                        }
                        else if (editText.getId() == R.id.destinationlocation){
                            int position = sugestedplaces.indexOfChild(view);
                            editText.setText(temp.get(position).getPrimary_place());
                            getLatLng(temp.get(position).getPlace_id(),R.id.destinationlocation);
                        }

                    }
                });

                sugestedplaces.setAdapter(pLaceAdapter);

            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + Query + apiException.getStatusCode());
            }
        });
    }

    private void getLatLng(String place_id, int calling_id){
        // Specify the fields to return (in this example all fields are returned).
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);
        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(place_id, placeFields).build();
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            if(calling_id == R.id.originlocation){
                PICKUP_PLACE = response.getPlace();
                originLongitude = PICKUP_PLACE.getLatLng().longitude;
               originLattitude = PICKUP_PLACE.getLatLng().latitude;
            }
            else {
                DESTINATION_PLACE = response.getPlace();
                destinationLongitude = DESTINATION_PLACE.getLatLng().longitude;
                destinationLattitiude = DESTINATION_PLACE.getLatLng().latitude;

            }

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this);
        googleMaps = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        showLocation();
    }


    public void showLocation(){
        LatLng eti_osa = new LatLng(6.4590, 3.6015);
        googleMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(
                eti_osa, 15f));
        init = googleMaps.addMarker(new MarkerOptions().
                position(eti_osa).draggable(true).title("Eti Osa").icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin)));
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addNewActor(int count){
            markerPosition = new LatLng(originLattitude,originLongitude);
            markerDestination = new LatLng(destinationLattitiude,destinationLongitude);
            getRoute(markerPosition,markerDestination);
            actorMarker  = googleMaps.addMarker(new MarkerOptions().
                    position(markerPosition).draggable(true).title(origin.getText().toString()).icon(bitmapDescriptorFromVector(this, R.drawable.ic_actor)));
         mDatabase.child("Actors").child("Actor "+ count).child("Location").child("lattitude").setValue(originLattitude);
         mDatabase.child("Actors").child("Actor "+ count).child("Location").child("longitude").setValue(originLongitude);

    }

    public void stretchMapToFit(Marker userMarker){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userMarker.getPosition());
        for(int i = 0; i <= actors.size()-1; i++){
            builder.include(actors.get(i).getMarker().getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMaps.moveCamera(cu);
    }

    public void startSimulation(){
      for(int i = 0; i <= actors.size() - 1; i++){
          actorLocations.add(actors.get(i).getMarker().getPosition());
         animateMarker(actors.get(i).getMarker(),actors.get(i).getCount(),actors.get(i).getRoute());
      }
      //reverseAnimation();
    }


    private void getRoute(LatLng origin, LatLng destination){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(origin, destination)
                .key(getString(R.string.places_api_key))
                .build();
        routing.execute();

    }

    public void animateMarker( Marker currentMarker, int count, Route route) {
        countDownTimer = new CountDownTimer(300000, 1000) {
            int i = 0;
            public void onTick(long millisUntilFinished) {
                    LatLng fromLocation = currentMarker.getPosition();
                    LatLng toLocation = route.getPoints().get(i);
                    mDatabase.child("Actors").child("Actor "+ count).child("Location").child("lattitude").setValue(toLocation.latitude);
                    mDatabase.child("Actors").child("Actor "+ count).child("Location").child("longitude").setValue(toLocation.longitude);
                    if (fromLocation != null) {
                        currentMarker.setPosition(fromLocation);
                        MarkerAnimation.animateMarkerToICS(currentMarker, toLocation, new LatLngInterpolator.Spherical());
                    }
                    i++;
            }
            public void onFinish() {
              /*  for(int i = 0; i <= actors.size() - 1; i++){
                    // animateMarker(actorLocations.get(i),actors.get(i).getMarker(),actors.get(i).getCount());
                }*/
            }
        }.start();


    }

    public void reverseAnimation(){

        CountDownTimer wait = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                for(int i = 0; i <= actors.size() - 1; i++){
                   // animateMarker(actorLocations.get(i),actors.get(i).getMarker(),actors.get(i).getCount());
                }
            }
        }.start();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        Route myroute = arrayList.get(0);
        Actor actor = new Actor(markerPosition,markerDestination,actorMarker,count,myroute);
        actors.add(actor);
        origin.getText().clear();
        destination.getText().clear();
        formBackground.setVisibility(View.GONE);
        stretchMapToFit(init);
    }

    @Override
    public void onRoutingCancelled() {

    }
}
