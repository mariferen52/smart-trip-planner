package com.isep.smarttripplanner.model;

public class MapView {
    private String mapUrl;

    public MapView(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public String getMapUrl() { return mapUrl; }
    public void setMapUrl(String mapUrl) { this.mapUrl = mapUrl; }
}
