package com.pixka.asociaciones.licensing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;

public class HardwareUtils {
    public static String fingerprint() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String mac = getFirstMac();
            String diskSerial = getDiskSerial();

            String combined = hostname + "-" + mac + "-" + diskSerial;
            return sha256(combined);
        } catch (Exception e) {
            throw new RuntimeException("HWID generation error", e);
        }
    }

    private static String getFirstMac() throws Exception {
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
        return "UNKNOWN_MAC";
    }

    private static String getDiskSerial() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec(new String[]{"wmic", "diskdrive", "get", "SerialNumber"});
                return readFirstOutputLine(process);
            } else if (os.contains("linux")) {
                Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "udevadm info --query=all --name=/dev/sda | grep ID_SERIAL_SHORT"});
                return readFirstOutputLine(process);
            }
        } catch (Exception ignored) {
        }
        return "UNKNOWN_DISK";
    }

    private static String readFirstOutputLine(Process process) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.toLowerCase().contains("serial")) {
                    return line.replaceAll("\\s+", "");
                }
            }
        }
        return "UNKNOWN";
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
