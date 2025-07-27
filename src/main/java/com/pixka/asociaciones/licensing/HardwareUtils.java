package com.pixka.asociaciones.licensing;

import java.net.NetworkInterface;
import java.util.Collections;

public class HardwareUtils {
    public static String fingerprint() {
        try {
            StringBuilder sb = new StringBuilder();
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener HWID", e);
        }
    }
}
