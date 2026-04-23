# Technical Debt — Backend

> Cập nhật lần cuối: 2026-04-21
> Sau deep scan kiến trúc lần 5 + fix toàn bộ, rules 1-8 đều CLEAN.
> File này ghi lại các trade-off đã chấp nhận.

---

## 1. Accepted Pragmatic Tradeoffs (Không phải violations)

Các item dưới đây đã được đánh giá và chấp nhận là trade-off hợp lý trong context của project.

### 1.1 JPA trong Domain Layer
- Domain entities dùng `jakarta.persistence.*` annotations
- Domain repositories extend `JpaRepository`
- Domain entities dùng `@CreatedDate`, `@LastModifiedDate`
- **Lý do:** Tách hoàn toàn JPA ra khỏi domain yêu cầu mapper layer + repository adapter cho mỗi entity — chi phí không tương xứng với lợi ích cho một modular monolith.

### 1.2 `@Transactional` trong Application UseCases
- **Lý do:** Transaction management thuộc về application layer trong Hexagonal Architecture.

### 1.3 `Page`/`PageRequest` trong Application Layer
- `GetTransactionsUseCase`, `CleanupDeletedAccountsUseCase`, `AnalyzeTransactionUseCase` dùng Spring Data `Page`/`PageRequest`
- **Lý do:** Tạo custom Pageable abstraction chỉ để wrap Spring Data là over-engineering.

### 1.4 Domain Constants/Enums trong same-module Presentation Requests
- `IdentityValidationConstants` dùng trong `@Size` annotations (presentation request DTOs)
- `OtpPurpose` enum dùng trong `SendOtpRequest`, `VerifyOtpRequest`
- **Lý do:** Trong cùng module, presentation có thể reference domain constants/enums cho validation. Tạo String + converter cho mỗi enum trong cùng module là churn không cần thiết.

### 1.5 Controller trực tiếp construct Command/Query objects
- `PortfolioController` (và nhiều controllers khác) tạo `*Command`/`*Query` inline
- **Lý do:** Command/Query là application-layer input DTOs, controller construct chúng là pattern phổ biến và chấp nhận trong Spring Boot projects. Thêm 1 mapper layer giữa request → command cho mỗi endpoint là boilerplate.

### 1.6 `@AuthenticationPrincipal Jwt` trong Controllers
- Controllers dùng `Jwt` parameter để extract userId via `jwt.getSubject()`
- **Lý do:** Đây là standard Spring Security OAuth2 Resource Server pattern. Thay bằng custom principal type chỉ để decouple `Jwt` là over-abstraction.

### 1.7 `investment.common` shared utility package
- `StockSymbolUtils` được import trực tiếp bởi cả `investment.market_data` và `investment.portfolio` modules — không thông qua package `.api`
- Cùng pattern với `finance.common`
- **Lý do:** Accepted as shared investment utility. Tạo `.api` wrapper chỉ để re-export một utility class là over-engineering.

---

## 2. LOW-Priority Naming/Style Issues (Không warrant code changes)

Các item dưới đây là minor inconsistencies hoặc style issues. Không đủ tác động để justify churn.

### 2.1 `countByCategory_Id` trong TransactionRepository
- Method name dùng underscore (`category_Id`) thay vì `categoryId`
- **Thực tế:** Đây là JPA-valid syntax cho `@ManyToOne` field traversal — Spring Data hiểu và generate query đúng. Chỉ kém readable hơn explicit JPQL.
- **Verdict:** Correct code, minor readability issue. Không cần sửa.

### 2.2 `LoginUseCase` double DB lookup
- User được load hai lần: một lần bởi Spring Security `UserDetailsService`, một lần trong `LoginUseCase`
- **Thực tế:** Minor performance overhead (1 extra DB query per login). Không ảnh hưởng correctness hay security.
- **Verdict:** Acceptable for current scale. Có thể optimize bằng cách pass `UserDetails` vào UseCase nếu performance trở thành bottleneck.

### 2.3 `WealthSeedAccountTypesPort` naming inconsistency
- Port interface tên `WealthSeedAccountTypesPort` thay vì `SeedWealthAccountTypesPort` (convention của `SeedIdentityDataPort`, `SeedTransactionDataPort`)
- Thêm nữa: port này nhận `WealthSeedAccountTypesQuery` (empty record) thay vì no-arg như các seed ports khác
- **Files affected:** `WealthSeedAccountTypesPort.java`, `WealthSeedAccountTypesUseCase.java`, `WealthAccountTypeSeeder.java`, `WealthSeedAccountTypesQuery.java` (4 files)
- **Verdict:** Naming deviation noted. Rename deferred — low risk, low reward.

### 2.4 `DELETE /delete-account` returns HTTP 200 instead of 204
- `AuthController.deleteAccount()` returns `ResponseEntity.ok(new MessageResponse(...))` — HTTP 200 with body
- REST convention: successful DELETE should return 204 No Content
- Minor style issue, no functional impact. iOS client may depend on current response format.

### 2.5 DB connection held during external HTTP calls in read-only UseCases
- Several UseCases with `@Transactional(readOnly=true)` make external HTTP calls inside the transaction boundary, holding a DB connection for the entire duration:
  - `GetTransactionAnalyticsInsightsUseCase` — AI analytics HTTP call + Redis write
  - `GetDailyValuationSeriesUseCase` — Finfo API HTTP call
  - `GetPortfolioVsMarketUseCase` — stock ratios HTTP call
  - `GetPortfolioAssetsUseCase` — market price HTTP call
- **Risk:** Under high concurrency, long-running external calls can exhaust the DB connection pool
- **Fix direction:** Split DB reads into a short `@Transactional` method, then call external API outside tx (like `SendChatMessageUseCase` pattern). Deferred — current traffic volume doesn't warrant the refactor complexity.
