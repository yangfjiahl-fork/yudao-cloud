package cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.gaode;

import cn.iocoder.yudao.module.gift.service.itinerary.provider.geo.core.AmapPlaceSearchClient;
import cn.iocoder.yudao.module.gift.service.itinerary.provider.scenic.ScenicSpotQueryClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GaodeScenicSpotQueryClientTest {

    @Test
    void queryAdaptsSharedAmapResult() {
        AmapPlaceSearchClient amapClient = mock(AmapPlaceSearchClient.class);
        AmapPlaceSearchClient.Place place = new AmapPlaceSearchClient.Place(
                "poi-1", "西湖", "杭州市西湖区", new BigDecimal("120.1"), new BigDecimal("30.2"),
                "风景名胜", "110000", "浙江省", "杭州市", "西湖区", "330106", null,
                "0571-12345678", "https://example.com/west-lake.jpg", "4.8", "0", "自然风光", "全天开放");
        when(amapClient.search(any())).thenReturn(new AmapPlaceSearchClient.SearchResult(1L, List.of(place)));
        GaodeScenicSpotQueryClient client = new GaodeScenicSpotQueryClient(amapClient);

        ScenicSpotQueryClient.Response response = client.query(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setRegion("杭州市").setPage(2));

        assertTrue(response.getSuccess());
        assertEquals("西湖", response.getData().path("list").get(0).path("name").asText());
        assertEquals("4.8", response.getData().path("list").get(0).path("business").path("rating").asText());
        ArgumentCaptor<AmapPlaceSearchClient.SearchRequest> captor =
                ArgumentCaptor.forClass(AmapPlaceSearchClient.SearchRequest.class);
        verify(amapClient).search(captor.capture());
        assertEquals("110000", captor.getValue().typeCode());
        assertEquals("杭州市", captor.getValue().region());
        assertEquals(2, captor.getValue().pageNo());
    }

}
