package com.pixka.asociaciones.licensing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

public class LicenseService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public String hwid() {
        return HardwareUtils.fingerprint();
    }

    public void genKeys(Path outDir) throws Exception {
        Files.createDirectories(outDir);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();
        Files.writeString(outDir.resolve("private.key"),
                Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));
        Files.writeString(outDir.resolve("public.key"),
                Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
    }

    public void sign(Path payloadJson, Path privateKeyFile, Path outLic) throws Exception {
        byte[] payloadBytes = Files.readAllBytes(payloadJson);
        PrivateKey privateKey = loadPrivateKey(privateKeyFile);
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(privateKey);
        sig.update(payloadBytes);
        byte[] signature = sig.sign();
        String lic = Base64.getEncoder().encodeToString(payloadBytes) + "." +
                Base64.getEncoder().encodeToString(signature);
        Files.writeString(outLic, lic);
    }

    public LicensePayload verify(Path licenseFile, Path publicKeyFile) throws Exception {
        String[] parts = Files.readString(licenseFile).trim().split("\\.");
        if (parts.length != 2) throw new RuntimeException("Formato de licencia inválido");
        byte[] payloadBytes = Base64.getDecoder().decode(parts[0]);
        byte[] signatureBytes = Base64.getDecoder().decode(parts[1]);
        PublicKey publicKey = loadPublicKey(publicKeyFile);
        Signature sig = Signature.getInstance("Ed25519");
        sig.initVerify(publicKey);
        sig.update(payloadBytes);
        if (!sig.verify(signatureBytes)) {
            throw new RuntimeException("Firma inválida");
        }
        LicensePayload payload = MAPPER.readValue(payloadBytes, LicensePayload.class);
        Instant now = Instant.now();
        if (now.isBefore(payload.getNotBefore()) || now.isAfter(payload.getExpiresAt())) {
            throw new RuntimeException("Licencia expirada o aún no válida");
        }
        String hwid = HardwareUtils.fingerprint();
        if (payload.getAllowedHwids() != null && !payload.getAllowedHwids().isEmpty()
                && !payload.getAllowedHwids().contains(hwid)) {
            throw new RuntimeException("HWID no autorizado");
        }
        return payload;
    }

    private PrivateKey loadPrivateKey(Path privateKeyFile) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(Files.readString(privateKeyFile).trim());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("Ed25519").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(Path publicKeyFile) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(Files.readString(publicKeyFile).trim());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("Ed25519").generatePublic(spec);
    }
}
