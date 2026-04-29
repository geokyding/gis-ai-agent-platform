package com.dingky.gis.demo;

import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: GeoServerDemo
 * Package: com.dingky.gis.demo
 * Description:
 * https://repo.osgeo.org/?spm=5176.28103460.0.0.50b929883pu34i#browse/search=keyword%3Dgeoserver:NX.coreui.model.Component-3
 * @Author: ding
 * @Create 2026/4/27 13:55
 * @Version 1.0
 **/
public class GeoServerDemo {
    public static void main(String[] args) {
        String url = "http://localhost:8181/geoserver/demo/wms?service=WMS&version=1.3.0&request=GetCapabilities";
        String username = "admin";
        String password = "geoserver";
        try {
            GeoServerRESTManager manager = new GeoServerRESTManager(new URL(url), username, password);
            System.out.println(manager.getReader().existGeoserver());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
