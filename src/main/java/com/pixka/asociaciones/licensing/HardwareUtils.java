package com.pixka.asociaciones.licensing;

import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collections;

public class HardwareUtils {

    private static final Path STORAGE_PATH = Paths.get(System.getProperty("user.home"), ".bitacora-fingerprint");

    public static String fingerprint() {
        try {
            if (Files.exists(STORAGE_PATH)) {
                return Files.readAllLines(STORAGE_PATH).get(0).trim();
            }

            String mac = getFirstMacAddress();
            String hwid = sha256(mac);
            Files.write(STORAGE_PATH, hwid.getBytes(StandardCharsets.UTF_8));
            return hwid;

        } catch (Exception e) {
            throw new RuntimeException("Error generando HWID", e);
        }
    }

    private static String getFirstMacAddress() throws Exception {
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
            byte[] mac = ni.getHardwareAddress();
            if (mac != null && mac.length == 6) {
                StringBuilder sb = new StringBuilder();
                for (byte b : mac) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            }
        }
        throw new IOException("No se encontró una dirección MAC válida.");
    }

    private static String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
