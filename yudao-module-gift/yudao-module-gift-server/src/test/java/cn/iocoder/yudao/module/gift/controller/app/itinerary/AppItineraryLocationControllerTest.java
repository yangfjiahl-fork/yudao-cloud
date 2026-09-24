package cn.iocoder.yudao.module.gift.controller.app.itinerary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppItineraryLocationControllerTest {

    @Test
    void isPrivateOrLocalIp() {
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("10.0.0.1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("172.16.0.1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("192.168.1.1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("127.0.0.1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("169.254.1.1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("::1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("fc00::1"));
        assertTrue(AppItineraryLocationController.isPrivateOrLocalIp("invalid-ip"));
        assertFalse(AppItineraryLocationController.isPrivateOrLocalIp("114.114.114.114"));
    }

}
