package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.MapView;

public class GoogleMapsAPI implements IMapService {
    private static final String API_KEY = "YOUR_API_KEY";

    @Override
    public MapView renderMap(double lat, double lon) {
        if (API_KEY.equals("YOUR_API_KEY")) {

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
                "window.map = L.map('map').setView([" + lat + ", " + lon + "], 5);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(window.map);" +
                "var markers = [];" +
                "var routeLine = null;" +
                "window.selectionMarker = L.marker([" + lat + ", " + lon + "]).addTo(window.map);" +

                "function clearMap() {" +
                "  if(routeLine) window.map.removeLayer(routeLine);" +
                "  markers.forEach(m => window.map.removeLayer(m));" +
                "  markers = [];" +
                "}" +

                "function addMarker(lat, lon, title) {" +
                "  var m = L.marker([lat, lon]).addTo(window.map).bindPopup(title);" +
                "  markers.push(m);" +
                "}" +

                "function fitBounds() {" +
                "  if(markers.length > 0) {" +
                "    var group = new L.featureGroup(markers);" +
                "    window.map.fitBounds(group.getBounds().pad(0.1));" +
                "  }" +
                "}" +

                "function drawRoute(points) {" +

                "  if(routeLine) window.map.removeLayer(routeLine);" +
                "  routeLine = L.polyline(points, {color: 'blue', weight: 4}).addTo(window.map);" +
                "}" +

                "// Click handler (Standard)" +
                "document.addEventListener('click', function(e) {" +
                "  console.log('Native click at ' + e.clientX + ',' + e.clientY);" +
                "});" +

                "window.map.on('click', function(e) {" +
                "  var coord = e.latlng;" +
                "  if(selectionMarker) { selectionMarker.setLatLng(coord); }" +
                "  else { selectionMarker = L.marker(coord).addTo(map); }" +
                "  if(window.javaApp) {" +
                "    window.javaApp.log('Map clicked at: ' + coord);" +
                "    window.javaApp.onLocationSelected(coord.lat, coord.lng);" +
                "  } else {" +
                "    alert('CRITICAL ERROR: Java Bridge (javaApp) NOT FOUND. Map clicks will not work.');" +
                "  }" +
                "});" +

                "setTimeout(function() { map.invalidateSize(); }, 500);" +
                "</script></body></html>";
    }
}
