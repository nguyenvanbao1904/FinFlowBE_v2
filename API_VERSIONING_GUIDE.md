# 📘 API Versioning - Complete Guide

## 🎯 Cơ chế hoạt động

### Mặc định - Không cần config gì

```bash
# Client không gửi header → Mặc định version 1
curl http://localhost:8080/api/users/my-profile

# Tương đương với:
curl -H "API-Version: 1" http://localhost:8080/api/users/my-profile
```

---

## 📂 Giải thích từng file trong `versioning/`

### 1️⃣ **ApiVersion.java** - Annotation đánh dấu version

**Công dụng:** Annotation để gắn lên Controller/Method xác định endpoint hỗ trợ version nào

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    String[] value();  // Mảng các version hỗ trợ: ["1"], ["2"], ["1","2"]
}
```

**Cách dùng:**

```java
// Endpoint chỉ hỗ trợ v1
@ApiVersion("1")
@GetMapping("/users")
public List<User> getUsersV1() { ... }

// Endpoint chỉ hỗ trợ v2
@ApiVersion("2")
@GetMapping("/users")
public List<UserV2> getUsersV2() { ... }

// Endpoint hỗ trợ cả v1 và v2
@ApiVersion({"1", "2"})
@GetMapping("/common")
public Response getCommon() { ... }
```

---

### 2️⃣ **ApiVersionInterceptor.java** - Kiểm tra header

**Công dụng:** Interceptor chạy trước mọi request, đọc header `API-Version` và validate

```java
@Component
@Slf4j
public class ApiVersionInterceptor implements HandlerInterceptor {
    // Danh sách version được hỗ trợ
    private static final Set<String> SUPPORTED = Set.of("1");
    private static final String DEFAULT = "1";
    private static final String ATTR = "apiVersion";

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {
        // 1. Đọc header API-Version
        String requested = request.getHeader("API-Version");

        // 2. Mặc định v1 nếu không có header
        String version = (requested == null || requested.isBlank())
            ? DEFAULT
            : requested;

        // 3. Kiểm tra version có được hỗ trợ không
        if (!SUPPORTED.contains(version)) {
            throw new AppException(CommonErrorCode.UNSUPPORTED_API_VERSION);
        }

        // 4. Lưu version vào request attribute để dùng sau
        request.setAttribute(ATTR, version);
        log.debug("API Version: {}", version);
        return true;
    }
}
```

**Luồng hoạt động:**

```
Client Request → ApiVersionInterceptor (preHandle)
                      ↓
                 Đọc header "API-Version"
                      ↓
          Không có? → Default "1"
          Có? → Validate trong SUPPORTED
                      ↓
          Không hợp lệ → throw UNSUPPORTED_API_VERSION (400)
          Hợp lệ → Tiếp tục đến Controller
```

---

### 3️⃣ **ApiVersionRequestCondition.java** - Logic matching version

**Công dụng:** Spring custom condition để match request với method theo version

```java
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {
    private final Set<String> versions;  // Versions mà endpoint hỗ trợ

    // Kiểm tra request có match với version của endpoint không
    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        String requested = (String) request.getAttribute("apiVersion");

        // Nếu request version nằm trong danh sách hỗ trợ → Match
        return this.versions.contains(requested)
            ? new ApiVersionRequestCondition(Set.of(requested))
            : null;
    }

    // So sánh 2 conditions để chọn method phù hợp nhất
    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        // Logic ưu tiên version cao hơn
        return other.versions.size() - this.versions.size();
    }
}
```

**Ví dụ:**

```java
// Có 2 methods:
@ApiVersion("1")
@GetMapping("/users")
public List<User> v1() { ... }  // versions = ["1"]

@ApiVersion("2")
@GetMapping("/users")
public List<UserV2> v2() { ... }  // versions = ["2"]

// Request: GET /users + Header "API-Version: 2"
// → Interceptor set attribute "apiVersion" = "2"
// → Condition của v1() check: "2" in ["1"]? NO → null
// → Condition của v2() check: "2" in ["2"]? YES → match
// → Spring route to v2()
```

---

### 4️⃣ **ApiVersionRequestMappingHandlerMapping.java** - Đăng ký condition

**Công dụng:** Custom HandlerMapping để Spring biết dùng `ApiVersionRequestCondition`

```java
public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        // Đọc @ApiVersion trên class
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        return apiVersion != null
            ? new ApiVersionRequestCondition(Set.of(apiVersion.value()))
            : null;
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        // Đọc @ApiVersion trên method
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        return apiVersion != null
            ? new ApiVersionRequestCondition(Set.of(apiVersion.value()))
            : null;
    }
}
```

**Vai trò:** "Dạy" Spring cách đọc `@ApiVersion` và tạo condition tương ứng

---

## 🚀 Ví dụ thực tế: Thêm v2 và v3

### Bước 1: Cập nhật SUPPORTED versions trong Interceptor

```java
// ApiVersionInterceptor.java
private static final Set<String> SUPPORTED = Set.of("1", "2", "3");  // Thêm v2, v3
```

### Bước 2: Tạo UserController với nhiều versions

```java
package com.finflow.backend.modules.identity;

import com.finflow.backend.common.versioning.ApiVersion;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // ============================================
    // VERSION 1 - Original API (deprecated)
    // ============================================

    @ApiVersion("1")
    @GetMapping("/my-profile")
    public UserResponseV1 getMyProfileV1() {
        return UserResponseV1.builder()
            .username("admin_test")
            .email("admin@test.com")
            .fullName("Admin Test")  // V1: Ghép firstName + lastName
            .build();
    }

    // ============================================
    // VERSION 2 - Improved (firstName, lastName riêng)
    // ============================================

    @ApiVersion("2")
    @GetMapping("/my-profile")
    public UserResponseV2 getMyProfileV2() {
        return UserResponseV2.builder()
            .username("admin_test")
            .email("admin@test.com")
            .firstName("Admin")      // V2: Tách riêng firstName
            .lastName("Test")        // V2: Tách riêng lastName
            .phone("+84123456789")   // V2: Thêm field phone
            .build();
    }

    // ============================================
    // VERSION 3 - Latest (thêm avatar, verified status)
    // ============================================

    @ApiVersion("3")
    @GetMapping("/my-profile")
    public UserResponseV3 getMyProfileV3() {
        return UserResponseV3.builder()
            .username("admin_test")
            .email("admin@test.com")
            .firstName("Admin")
            .lastName("Test")
            .phone("+84123456789")
            .avatarUrl("https://cdn.finflow.com/avatars/admin.jpg")  // V3: Thêm avatar
            .verified(true)          // V3: Thêm verified status
            .createdAt("2025-01-01") // V3: Thêm timestamp
            .build();
    }

    // ============================================
    // ENDPOINT HỖ TRỢ NHIỀU VERSIONS
    // ============================================

    @ApiVersion({"1", "2", "3"})  // Hỗ trợ tất cả versions
    @GetMapping("/ping")
    public Map<String, String> ping(HttpServletRequest request) {
        String version = (String) request.getAttribute("apiVersion");
        return Map.of(
            "status", "ok",
            "version", version,
            "message", "API v" + version + " is working"
        );
    }
}
```

### Bước 3: Tạo DTOs cho từng version

```java
// V1 DTO
@Data
@Builder
public class UserResponseV1 {
    private String username;
    private String email;
    private String fullName;  // Ghép firstName + lastName
}

// V2 DTO
@Data
@Builder
public class UserResponseV2 {
    private String username;
    private String email;
    private String firstName;  // Tách riêng
    private String lastName;   // Tách riêng
    private String phone;      // Field mới
}

// V3 DTO
@Data
@Builder
public class UserResponseV3 {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;  // Field mới
    private Boolean verified;  // Field mới
    private String createdAt;  // Field mới
}
```

---

## 🧪 Test các versions

### Test v1 (không gửi header - mặc định)

```bash
curl http://localhost:8080/api/users/my-profile

# Response:
{
  "username": "admin_test",
  "email": "admin@test.com",
  "fullName": "Admin Test"
}
```

### Test v2 (gửi header API-Version: 2)

```bash
curl -H "API-Version: 2" http://localhost:8080/api/users/my-profile

# Response:
{
  "username": "admin_test",
  "email": "admin@test.com",
  "firstName": "Admin",
  "lastName": "Test",
  "phone": "+84123456789"
}
```

### Test v3 (gửi header API-Version: 3)

```bash
curl -H "API-Version: 3" http://localhost:8080/api/users/my-profile

# Response:
{
  "username": "admin_test",
  "email": "admin@test.com",
  "firstName": "Admin",
  "lastName": "Test",
  "phone": "+84123456789",
  "avatarUrl": "https://cdn.finflow.com/avatars/admin.jpg",
  "verified": true,
  "createdAt": "2025-01-01"
}
```

### Test version không hỗ trợ (v99)

```bash
curl -H "API-Version: 99" http://localhost:8080/api/users/my-profile

# Response: HTTP 400
{
  "type": "/api/error/1008",
  "title": "Unsupported API version",
  "status": 400,
  "detail": "Unsupported API version",
  "code": 1008
}
```

---

## 🎯 Luồng hoạt động đầy đủ

```
1. Client Request
   ↓
   GET /api/users/my-profile
   Header: API-Version: 2

2. WebConfig → ApiVersionInterceptor.preHandle()
   ↓
   - Đọc header "API-Version" = "2"
   - Check "2" in SUPPORTED? YES
   - Set request.attribute("apiVersion", "2")

3. Spring HandlerMapping
   ↓
   - Tìm tất cả methods match path "/my-profile"
   - Method 1: @ApiVersion("1") → Condition check: "2" in ["1"]? NO
   - Method 2: @ApiVersion("2") → Condition check: "2" in ["2"]? YES ✅
   - Method 3: @ApiVersion("3") → Condition check: "2" in ["3"]? NO

4. Route to getMyProfileV2()
   ↓
   Return UserResponseV2
```

---

## 📋 Best Practices

### 1. Deprecation Strategy

```java
// Đánh dấu v1 deprecated, hỗ trợ đến 31/12/2026
@ApiVersion("1")
@Deprecated(since = "2026-01-01", forRemoval = true)
@GetMapping("/old-endpoint")
public Response oldApi() {
    log.warn("API v1 deprecated - please upgrade to v2");
    // ...
}
```

### 2. Shared Logic cho nhiều versions

```java
@RestController
public class UserController {

    // Shared service
    private final UserService userService;

    @ApiVersion("1")
    @GetMapping("/users")
    public List<UserV1> getUsersV1() {
        return userService.getUsers()
            .stream()
            .map(this::toV1)  // Convert to V1 format
            .toList();
    }

    @ApiVersion("2")
    @GetMapping("/users")
    public List<UserV2> getUsersV2() {
        return userService.getUsers()
            .stream()
            .map(this::toV2)  // Convert to V2 format
            .toList();
    }
}
```

### 3. Version trong response header

```java
@ApiVersion("2")
@GetMapping("/users")
public ResponseEntity<List<UserV2>> getUsers(HttpServletRequest request) {
    String version = (String) request.getAttribute("apiVersion");

    return ResponseEntity.ok()
        .header("API-Version", version)  // Echo back version
        .body(users);
}
```

---

## ✅ Tóm tắt

| File                                            | Công dụng                    | Thời điểm chạy                 |
| ----------------------------------------------- | ---------------------------- | ------------------------------ |
| **ApiVersion.java**                             | Annotation đánh dấu version  | Compile time                   |
| **ApiVersionInterceptor.java**                  | Đọc header, validate version | Mỗi request (trước controller) |
| **ApiVersionRequestCondition.java**             | Logic match request → method | Mỗi request (routing)          |
| **ApiVersionRequestMappingHandlerMapping.java** | Đăng ký condition vào Spring | Application startup            |

**Đơn giản hóa:**

1. **Interceptor** = "Cổng kiểm tra" - Ai vào cũng phải qua
2. **Condition** = "Bảng chỉ dẫn" - Request này đi đường nào
3. **HandlerMapping** = "Quản lý bảng chỉ dẫn" - Đọc @ApiVersion và tạo bảng
4. **@ApiVersion** = "Biển báo" - Endpoint này hỗ trợ version nào

**Không config gì = v1 mặc định ✅**
