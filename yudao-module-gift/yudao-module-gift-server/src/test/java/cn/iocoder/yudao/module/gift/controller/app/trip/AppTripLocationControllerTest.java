package cn.iocoder.yudao.module.gift.controller.app.trip;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTripLocationControllerTest {

    @Test
    void isPrivateOrLocalIp() {
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("10.0.0.1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("172.16.0.1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("192.168.1.1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("127.0.0.1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("169.254.1.1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("::1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("fc00::1"));
        assertTrue(AppTripLocationController.isPrivateOrLocalIp("invalid-ip"));
        assertFalse(AppTripLocationController.isPrivateOrLocalIp("114.114.114.114"));
    }

}
