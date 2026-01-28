package com.finflow.backend.common.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.ReportingPolicy;

@org.mapstruct.MapperConfig(
        componentModel = "spring",           // Tự động biến Mapper thành Bean của Spring (@Component)
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // Bỏ qua nếu thiếu trường (đỡ báo lỗi rác)
        injectionStrategy = InjectionStrategy.CONSTRUCTOR // Inject mapper khác vào bằng Constructor (chuẩn Spring mới)
)
public interface MapperConfig {
    // File này để trống, chỉ dùng để chứa annotation config thôi
}