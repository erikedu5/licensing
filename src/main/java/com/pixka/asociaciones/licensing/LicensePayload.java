package com.pixka.asociaciones.licensing;

import java.time.Instant;
import java.util.List;

public class LicensePayload {
    private String customerId;
    private Instant notBefore;
    private Instant expiresAt;
    private List<String> allowedHwids;
    private List<String> features;
    private Integer maxNodes;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Instant getNotBefore() { return notBefore; }
    public void setNotBefore(Instant notBefore) { this.notBefore = notBefore; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public List<String> getAllowedHwids() { return allowedHwids; }
    public void setAllowedHwids(List<String> allowedHwids) { this.allowedHwids = allowedHwids; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
    public Integer getMaxNodes() { return maxNodes; }
    public void setMaxNodes(Integer maxNodes) { this.maxNodes = maxNodes; }
}
