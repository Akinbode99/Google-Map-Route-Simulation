package com.example.mapapp;

public class ActorPlace {
    private String primary_place;
    private String secondary_place;
    private String place_id;

    public ActorPlace(String primary_place, String secondary_place, String place_id) {
        this.primary_place = primary_place;
        this.secondary_place = secondary_place;
        this.place_id = place_id;
    }

    public String getPrimary_place() {
        return primary_place;
    }

    public String getSecondary_place() {
        return secondary_place;
    }

    public String getPlace_id() {
        return place_id;
    }
}

