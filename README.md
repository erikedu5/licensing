# Licensing Shell Tool (v1.1.0)

Herramienta de licenciamiento en **Java 17 + Spring Shell** con comandos para:
- Generar llaves (`gen-keys`)
- Generar `payload.json` (`gen-payload`)
- Firmar licencias (`sign`)
- Verificar licencias (`verify`)
- Obtener el HWID (`hwid`)

## Requisitos
- Java 17+
- Maven 3.8+

## Compilar y ejecutar
```bash
mvn clean package
java -jar target/licensing-shell-1.1.0.jar
```

## Comandos

### 1) HWID
```shell
shell:> hwid
```

### 2) Generar llaves
```shell
shell:> gen-keys keys
```
Crea:
```
keys/private.key
keys/public.key
```

### 3) Generar `payload.json`
```shell
shell:> gen-payload payload.json CLIENTE-001 2025-07-01T00:00:00Z 2025-12-31T23:59:59Z 00A0C9140F12 moduleA,moduleB 1
```
**Parámetros (en orden):**
1. **out**: archivo de salida (ej. `payload.json`)
2. **customerId**
3. **notBefore** (ISO-8601 UTC)
4. **expiresAt** (ISO-8601 UTC)
5. **hwids** (coma separada, opcional; usa `""` para vacío)
6. **features** (coma separada, opcional; usa `""` para vacío)
7. **maxNodes** (entero, opcional por defecto 1)

Ejemplo sin HWIDs ni features:
```shell
shell:> gen-payload payload.json CLIENTE-002 2025-07-01T00:00:00Z 2026-07-01T00:00:00Z "" "" 5
```

### 4) Firmar la licencia
```shell
shell:> sign payload.json keys/private.key license.lic
```

### 5) Verificar la licencia
```shell
shell:> verify license.lic keys/public.key
```

---

## Ejecución no interactiva (pasando comandos desde la CLI)
```bash
java -jar target/licensing-shell-1.1.0.jar --spring.shell.interactive.enabled=false   --spring.shell.command=hwid
```

(Para múltiples comandos, puedes crear un script que los invoque secuencialmente).

---
