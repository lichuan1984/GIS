package com.arcgis.until;

import com.esri.arcgisruntime.arcgisservices.LevelOfDetail;
import com.esri.arcgisruntime.arcgisservices.TileInfo;
import com.esri.arcgisruntime.data.TileKey;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.WebTiledLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GoogleMapLayer{

    private static final int DPI = 96;
    private static final int minZoomLevel = 0;
    private static final int maxZoomLevel = 19;
    private static final int tileWidth = 256;
    private static final int tileHeight = 256;

    // 枚举
    public enum MapType {
        ROADMAP,        //矢量标注地图
        SATELLITE        //影像地图
    }


    private MapType mMapType;

    private static final List<String> SubDomain = Arrays.asList(new String[]{"mt0", "mt1", "mt2", "mt3", "mt4"});
    //102100是esri内部使用id 与epsg：3857 是对应的
    private static final SpatialReference SRID_MERCATOR = SpatialReference.create(102113);
    private static final Point ORIGIN_MERCATOR = new Point(-20037508.3427892, 20037508.3427892, SRID_MERCATOR);
    private static final Envelope ENVELOPE_MERCATOR = new Envelope(-22041257.773878,
            -32673939.6727517, 22041257.773878, 20851350.0432886, SRID_MERCATOR);

    private static double[] SCALES = new double[] { 591657527.591555,
            295828763.79577702, 147914381.89788899, 73957190.948944002,
            36978595.474472001, 18489297.737236001, 9244648.8686180003,
            4622324.4343090001, 2311162.217155, 1155581.108577, 577790.554289,
            288895.277144, 144447.638572, 72223.819286, 36111.909643,
            18055.954822, 9027.9774109999998, 4513.9887049999998, 2256.994353,
            1128.4971760000001 };

    private static double[] iRes = new double[] { 156543.03392800014,
            78271.516963999937, 39135.758482000092, 19567.879240999919,
            9783.9396204999593, 4891.9698102499797, 2445.9849051249898,
            1222.9924525624949, 611.49622628138, 305.748113140558,
            152.874056570411, 76.4370282850732, 38.2185141425366,
            19.1092570712683, 9.55462853563415, 4.7773142679493699,
            2.3886571339746849, 1.1943285668550503, 0.59716428355981721,
            0.29858214164761665 };

    private String getMapUrl(TileKey tileKey) {
        String iResult = null;
        Random iRandom = null;
        int level = tileKey.getLevel();
        int col = tileKey.getColumn();
        int row = tileKey.getRow();
        iResult = "http://mt";
        iRandom = new Random();
        iResult = iResult + iRandom.nextInt(4);
        switch (this.mMapType) {
            case ROADMAP:
                iResult = iResult + ".google.cn/vt/lyrs=m@212000000&hl=zh-CN&gl=CN&src=app&x=" + col + "&y=" + row + "&z=" + level + "&s==Galil";
                break;
            case SATELLITE:
                iResult = iResult + ".google.cn/vt/imgtp=png32&lyrs=h@212000000&hl=zh-CN&gl=CN&src=app&x=" + col + "&y=" + row + "&z=" + level + "&s==Galil";
                break;
            default:
                return null;
        }
        return iResult;
    }

    public static WebTiledLayer CreateGoogleLayer(MapType layerType) {
        WebTiledLayer webTiledLayer = null;
        String mainUrl = "";
        String mainName = "Google";
        TileInfo mainTileInfo = null;
        Envelope mainEnvelope = null;
        try {
            switch (layerType) {
                case ROADMAP:
                    mainUrl = "http://{subDomain}.google.cn/vt/lyrs=m@225000000&hl=zh-CN&gl=CN&src=app&s=G&x={col}&y={row}&z={level}";
                    break;
                case SATELLITE:
                    mainUrl = "http://{subDomain}.google.cn/vt/lyrs=y&scale=2&hl=zh-CN&gl=cn&x={col}&y={row}&z={level}";
                    //mainUrl = "http://bjtljlkj.in.3322.org:33077/imgserver/date/L{level}/R{col}/C{row}.jpg";
                    break;
            }
            List<LevelOfDetail> mainLevelOfDetail = new ArrayList<LevelOfDetail>();
            Point mainOrigin = null;
            for (int i = minZoomLevel; i <= maxZoomLevel; i++) {
                LevelOfDetail item = new LevelOfDetail(i, iRes[i], SCALES[i]);
                mainLevelOfDetail.add(item);
            }
            mainEnvelope = ENVELOPE_MERCATOR;
            mainOrigin = ORIGIN_MERCATOR;

            mainTileInfo = new TileInfo(
                    DPI,
                    TileInfo.ImageFormat.PNG,
                    mainLevelOfDetail,
                    mainOrigin,
                    mainOrigin.getSpatialReference(),
                    tileHeight,
                    tileWidth
            );
            webTiledLayer = new WebTiledLayer(
                    mainUrl,
                    SubDomain,
                    mainTileInfo,
                    mainEnvelope);
            webTiledLayer.setName(mainName);
            webTiledLayer.loadAsync();
        } catch (Exception e) {
            e.getCause();
        }
        return webTiledLayer;
    }
}