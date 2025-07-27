package com.pixka.asociaciones.licensing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
public class LicenseCommands {

    private final LicenseService service = new LicenseService();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @ShellMethod("Muestra el HWID de esta máquina.")
    public String hwid() {
        return service.hwid();
    }

    @ShellMethod("Genera llaves pública/privada en un directorio.")
    public String genKeys(@ShellOption(help = "Directorio destino") String outDir) throws Exception {
        service.genKeys(Path.of(outDir));
        return "Llaves generadas en: " + Path.of(outDir).toAbsolutePath();
    }

    @ShellMethod("Firma una licencia a partir de un payload JSON.")
    public String sign(
            @ShellOption(help = "Archivo payload.json") String payloadJson,
            @ShellOption(help = "Archivo private.key") String privateKey,
            @ShellOption(help = "Salida license.lic") String outLic
    ) throws Exception {
        service.sign(Path.of(payloadJson), Path.of(privateKey), Path.of(outLic));
        return "Licencia firmada en: " + Path.of(outLic).toAbsolutePath();
    }

    @ShellMethod("Verifica una licencia con la llave pública.")
    public String verify(
            @ShellOption(help = "Archivo license.lic") String license,
            @ShellOption(help = "Archivo public.key") String publicKey
    ) throws Exception {
        LicensePayload payload = service.verify(Path.of(license), Path.of(publicKey));
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    @ShellMethod("Genera un payload.json de licencia.")
    public String genPayload(
            @ShellOption(help = "Archivo de salida, ej. payload.json") String out,
            @ShellOption(help = "ID del cliente, ej. CLIENTE-001") String customerId,
            @ShellOption(help = "Fecha inicio (ISO-8601), ej. 2025-07-01T00:00:00Z") String notBefore,
            @ShellOption(help = "Fecha fin (ISO-8601), ej. 2025-12-31T23:59:59Z") String expiresAt,
            @ShellOption(defaultValue = "", help = "HWIDs permitidos separados por coma") String hwids,
            @ShellOption(defaultValue = "", help = "Features separados por coma") String features,
            @ShellOption(defaultValue = "1", help = "Maximo de nodos") int maxNodes
    ) throws Exception {

        LicensePayload payload = new LicensePayload();
        payload.setCustomerId(customerId);
        payload.setNotBefore(Instant.parse(notBefore));
        payload.setExpiresAt(Instant.parse(expiresAt));

        if (!hwids.isBlank()) {
            List<String> list = Arrays.stream(hwids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            payload.setAllowedHwids(list);
        }

        if (!features.isBlank()) {
            List<String> list = Arrays.stream(features.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            payload.setFeatures(list);
        }

        payload.setMaxNodes(maxNodes);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        Files.writeString(Path.of(out), json);
        return "Payload generado en: " + Path.of(out).toAbsolutePath();
    }
}
