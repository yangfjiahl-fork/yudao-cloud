package cn.iocoder.yudao.module.gift.service.itinerary;

import java.util.List;

/** APP 地图数据服务。所有坐标均为高德 GCJ-02。 */
public interface ItineraryMapService {

    FootprintMap getFootprintMap(Long memberId, Integer year);

    ItineraryMap getItineraryMap(Long memberId, Long itineraryId);

    record Viewport(Double centerLatitude, Double centerLongitude, Integer zoom, Double southWestLatitude,
                    Double southWestLongitude, Double northEastLatitude, Double northEastLongitude) {
    }

    record FootprintSummary(Integer cityCount) {
    }

    record FootprintCluster(String scope, String name, Double latitude, Double longitude, Integer cityCount,
                            Integer visitCount, Long latestVisitAt) {
    }

    record FootprintMap(String coordinateSystem, FootprintSummary summary, Viewport viewport,
                        List<FootprintCluster> clusters) {
    }

    record ItineraryStop(String slot, Integer order, String poiId, String name, Double latitude, Double longitude,
                         String address, String arrival, String departure, String timeLabel, boolean hasCoordinate) {
    }

    record ItineraryDay(Integer day, List<ItineraryStop> stops) {
    }

    record ItineraryMap(String coordinateSystem, Long itineraryId, Viewport viewport, List<ItineraryDay> days) {
    }

}
