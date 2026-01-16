package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.MapView;

public class MapService implements IMapService {

    @Override
    public MapView renderMap(double lat, double lon) {
        String html = getInteractiveMapHtml(lat, lon, 10);
        String base64Html = java.util.Base64.getEncoder()
                .encodeToString(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return new MapView("data:text/html;base64," + base64Html);
    }

    @Override
    public String getInteractiveMapHtml(double lat, double lon, int zoom) {
        return "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>html, body { height: 100%; margin: 0; padding: 0; background: #e0e0e0; } #map { height: 100%; width: 100%; cursor: crosshair; }</style>"
                +
                "</head><body><div id='map'></div><script>" +
                "L.Browser.any3d = false;" +
                "L.Browser.retina = false;" +

                "var mapOptions = {" +
                "   zoomAnimation: false," +
                "   fadeAnimation: false," +
                "   markerZoomAnimation: false," +
                "   preferCanvas: true," +
                "   zoomControl: false" +
                "};" +
                "window.map = L.map('map', mapOptions).setView([" + lat + ", " + lon + "], " + zoom + ");" +
                "L.control.zoom({position: 'bottomright'}).addTo(window.map);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.de/{z}/{x}/{y}.png', {" +
                "   attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors',"
                +
                "   maxZoom: 20" +
                "}).addTo(window.map);" +
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
                "});" +

                "window.map.on('click', function(e) {" +
                "  var coord = e.latlng;" +
                "  if(selectionMarker) { selectionMarker.setLatLng(coord); }" +
                "  else { selectionMarker = L.marker(coord).addTo(map); }" +
                "  if(window.javaApp) {" +
                "    window.javaApp.onLocationSelected(coord.lat, coord.lng);" +
                "  }" +
                "});" +

                "setTimeout(function() { map.invalidateSize(); }, 500);" +
                "</script></body></html>";
    }
}
