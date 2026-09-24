package cn.iocoder.yudao.module.gift.service.itinerary;

import java.util.List;

/** APP 地图数据服务。所有坐标均为高德 GCJ-02。 */
public interface ItineraryMapService {

    ExploreMap getExploreMap(ExploreMapRequest request);

    FootprintMap getFootprintMap(Long memberId, Integer year);

    ItineraryMap getItineraryMap(Long memberId, Long itineraryId);

    record ExploreMapRequest(Long cityId, Double latitude, Double longitude, String keyword, String category,
                             Integer pageNo, Integer pageSize) {
    }

    record ResolvedCity(Long cityId, String name, String adcode) {
    }

    record Viewport(Double centerLatitude, Double centerLongitude, Integer zoom, Double southWestLatitude,
                    Double southWestLongitude, Double northEastLatitude, Double northEastLongitude) {
    }

    record ExploreItem(String id, String poiId, String name, String category, Double latitude, Double longitude,
                       String address, String coverUrl, String summary, List<String> tags, Double distanceMeters,
                       Double score, Integer priority, String markerLabel, Boolean isFeatured) {
    }

    record ExploreMap(String coordinateSystem, ResolvedCity resolvedCity, Viewport viewport, List<ExploreItem> items) {
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
