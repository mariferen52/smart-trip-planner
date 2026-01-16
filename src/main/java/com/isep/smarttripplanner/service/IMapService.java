package com.isep.smarttripplanner.service;

import com.isep.smarttripplanner.model.MapView;

public interface IMapService {
    MapView renderMap(double lat, double lon);

    String getInteractiveMapHtml(double lat, double lon, int zoom);
}
