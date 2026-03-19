package com.dingky.gis.ai.vectorcore;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.junit.Test;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: AppTest
 * Package: com.dingky.gis.ai.vectorcore
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/18 11:31
 * @Version 1.0
 **/
public class AppTest {
    @Test
    public void test() {
        ogr.RegisterAll();
        ogr.UseExceptions();
        String infile = "D:\\works\\codetest\\dingprojects\\collagingimages\\shp\\2__52e7bf55-0ca5-4fca-8c81-53e336113a94.shp";
        DataSource ds = ogr.Open(infile, 0);
        Layer layer = ds.GetLayer(0);
        System.out.println("feature count: "+ layer.GetFeatureCount());
        System.out.println();
    }
}
