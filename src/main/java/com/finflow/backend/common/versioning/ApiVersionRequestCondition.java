package com.finflow.backend.common.versioning;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * RequestCondition để Spring chọn handler theo version.
 * - Lấy version từ header "API-Version" (default "1" nếu thiếu).
 * - Match nếu version nằm trong danh sách annotation.
 * - Ưu tiên version cao hơn khi có nhiều match (chuẩn hơn khi có v2+).
 */
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private final TreeSet<String> versions;

    public ApiVersionRequestCondition(Set<String> versions) {
        this.versions = new TreeSet<>(Comparator.comparingInt(Integer::parseInt));
        this.versions.addAll(versions);
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        // method-level override class-level
        return new ApiVersionRequestCondition(other.versions);
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        String requested = request.getHeader("API-Version");
        if (requested == null || requested.isBlank()) {
            requested = "1";
        }
        return versions.contains(requested)
                ? new ApiVersionRequestCondition(Set.of(requested))
                : null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        // Prefer higher version if multiple match
        String thisMax = versions.last();
        String otherMax = other.versions.last();
        return Integer.compare(Integer.parseInt(otherMax), Integer.parseInt(thisMax));
    }
}

