package com.example.mapapp;

import com.directions.route.Route;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Actor {

    private LatLng origin,destination;
    private Marker marker;
    private int count;
    private Route route;

    public Actor(LatLng origin, LatLng destination, Marker marker, int count, Route route) {
        this.origin = origin;
        this.destination = destination;
        this.marker = marker;
        this.count = count;
        this.route = route;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public Marker getMarker() {
        return marker;
    }

    public int getCount(){
        return  count;
    }

    public Route getRoute(){
        return route;
    }
}
