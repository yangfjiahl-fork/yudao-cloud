package cn.iocoder.yudao.module.gift.framework.trip.provider.route.amap;

import cn.iocoder.yudao.module.gift.framework.trip.provider.config.TripProviderProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AmapRouteQueryClientTest {

    private static final String WALKING_URL = "https://example.com/v3/direction/walking";
    private static final String TRANSIT_URL = "https://example.com/v3/direction/transit/integrated";

    private MockRestServiceServer server;
    private AmapRouteQueryClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        TripProviderProperties.Route config = new TripProviderProperties.Route()
                .setAmapKey("test-amap-key").setWalkingUrl(WALKING_URL).setTransitUrl(TRANSIT_URL);
        client = new AmapRouteQueryClient(restTemplate, config);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void query_shouldParseWalkingRoute() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(WALKING_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("key", "test-amap-key"))
                .andExpect(queryParam("origin", "120.1,30.2"))
                .andExpect(queryParam("destination", "120.2,30.3"))
                .andRespond(withSuccess("""
                        {"status":"1","infocode":"10000","route":{"paths":[{"distance":"840","duration":"720",
                        "steps":[{"polyline":"120.1,30.2;120.15,30.25"},{"polyline":"120.15,30.25;120.2,30.3"}]}]}}
                        """, MediaType.APPLICATION_JSON));

        AmapRouteQueryClient.Route route = client.query(new AmapRouteQueryClient.Request("杭州", "120.1,30.2", "120.2,30.3",
                AmapRouteQueryClient.Mode.WALKING));

        assertEquals(AmapRouteQueryClient.Mode.WALKING, route.mode());
        assertEquals(840, route.distanceMeters());
        assertEquals(720, route.durationSeconds());
        assertEquals(List.of(
                new AmapRouteQueryClient.RoutePoint(120.1D, 30.2D),
                new AmapRouteQueryClient.RoutePoint(120.15D, 30.25D),
                new AmapRouteQueryClient.RoutePoint(120.2D, 30.3D)), route.routePoints());
    }

    @Test
    void query_shouldParseTransitRoute() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(TRANSIT_URL)))
                .andExpect(queryParam("city", "%E6%9D%AD%E5%B7%9E"))
                .andExpect(queryParam("strategy", "0"))
                .andRespond(withSuccess("""
                        {"status":"1","infocode":"10000","route":{"transits":[{"distance":"6800","duration":"1800"}]}}
                        """, MediaType.APPLICATION_JSON));

        AmapRouteQueryClient.Route route = client.query(new AmapRouteQueryClient.Request("杭州", "120.1,30.2", "120.2,30.3",
                AmapRouteQueryClient.Mode.TRANSIT));

        assertEquals(AmapRouteQueryClient.Mode.TRANSIT, route.mode());
        assertEquals(6800, route.distanceMeters());
        assertEquals(1800, route.durationSeconds());
    }

    @Test
    void query_shouldRejectMissingApiKey() {
        client = new AmapRouteQueryClient(new RestTemplate(), new TripProviderProperties.Route().setAmapKey(""));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.query(new AmapRouteQueryClient.Request("杭州", "120.1,30.2", "120.2,30.3", AmapRouteQueryClient.Mode.WALKING)));

        assertEquals("高德路径规划 API Key 未配置", exception.getMessage());
    }
}
