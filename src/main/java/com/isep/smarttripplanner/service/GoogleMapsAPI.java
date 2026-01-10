package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.MapView;

public class GoogleMapsAPI implements IMapService {
    private static final String API_KEY = "YOUR_API_KEY";

    @Override
    public MapView renderMap(double lat, double lon) {
        if (API_KEY.equals("YOUR_API_KEY")) {
            // Demo Mode: Use interactive Leaflet map instead of static embed
            return new MapView("data:text/html," + getInteractiveMapHtml(lat, lon));
        }
        String mapUrl = "https://www.google.com/maps/embed/v1/view?key=" + API_KEY + "&center=" + lat + "," + lon
                + "&zoom=12";
        return new MapView(mapUrl);
    }

    @Override
    public String getInteractiveMapHtml(double lat, double lon) {
        return "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>html, body { height: 100%; margin: 0; padding: 0; } #map { height: 100%; width: 100%; cursor: crosshair; }</style>"
                +
                "</head><body><div id='map'></div><script>" +
                "var map = L.map('map').setView([" + lat + ", " + lon + "], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "var marker = L.marker([" + lat + ", " + lon + "]).addTo(map);" +
                "map.on('click', function(e) {" +
                "  var coord = e.latlng;" +
                "  marker.setLatLng(coord);" +
                "  if(window.javaApp) { window.javaApp.onLocationSelected(coord.lat, coord.lng); }" +
                "});" +
                "setTimeout(function() { map.invalidateSize(); }, 500);" +
                "</script></body></html>";
    }
}
