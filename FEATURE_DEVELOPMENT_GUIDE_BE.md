# 📖 Backend Development Guide - The Holy Bible

**Purpose:** Hướng dẫn phát triển tính năng mới ĐÚNG CHUẨN  
**Status:** Living Document - Nguyên tắc bất biến

---

## 🎯 Core Philosophy

> **UseCase chỉ có giá trị khi nó chứa LOGIC NGHIỆP VỤ thực sự.**  
> **Không tạo wrapper vô nghĩa.**

---

## 🌳 Decision Tree

```
Feature mới
    ↓
Logic có phức tạp không?
├─ Kết hợp 2+ repositories?
├─ Business rules phức tạp?
├─ Side effects (email, event)?
└─ Transaction spanning?
    ↓ CÓ              ↓ KHÔNG
┌─────────┐      ┌──────────────┐
│ UseCase │      │ Controller + │
│ Pattern │      │ Repository   │
└─────────┘      └──────────────┘
```

---

## 📋 Decision Matrix

| Tiêu chí | UseCase | Repository trực tiếp |
|----------|---------|---------------------|
| Simple CRUD (findAll, findById) | ❌ | ✅ |
| Kết hợp 2+ repositories | ✅ | ❌ |
| Complex validation/business rules | ✅ | ❌ |
| Side effects (email, event, analytics, token rotation) | ✅ | ❌ |
| Framework integration (Security, etc) | ✅ | ❌ |
| Transaction spanning multiple operations | ✅ | ❌ |

---

## 🏗️ Architecture Patterns

### Pattern 1: Simple CRUD → NO UseCase

**Structure:**
```
Controller → Repository → Database
```

**Example:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository repository;
    
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
```

**Lý do:** Chỉ 1 repository call, không có logic gì khác.

---

### Pattern 2: Complex Logic → UseCase

**Structure:**
```
Controller → UseCase → Repository(s) → Database
```

**Example: Multi-Repository Orchestration**
```java
@Component
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final EmailService emailService;
    
    @Transactional
    public Order execute(CreateOrderRequest request) {
        // 1. Validate products exist
        List<Product> products = productRepository
            .findAllById(request.getProductIds());
        
        // 2. Check inventory availability (business rule)
        for (Product product : products) {
            Inventory inventory = inventoryRepository
                .findByProductId(product.getId());
            if (inventory.getStock() < request.getQuantity(product.getId())) {
                throw new OutOfStockException(product.getName());
            }
        }
        
        // 3. Create order
        Order order = Order.builder()
            .customerId(request.getCustomerId())
            .products(products)
            .build();
        Order saved = orderRepository.save(order);
        
        // 4. Update inventory (business logic)
        for (Product product : products) {
            inventoryRepository.decreaseStock(
                product.getId(),
                request.getQuantity(product.getId())
            );
        }
        
        // 5. Side effect: Send confirmation email
        emailService.sendOrderConfirmation(saved);
        
        return saved;
    }
}
```

**Giá trị UseCase:**
- ✅ Orchestrate 3 repositories
- ✅ Business validation (stock check)
- ✅ Transaction boundary
- ✅ Side effect (email)

---

### Pattern 3: Infrastructure Service

**Khi nào dùng:**
- Scheduled tasks
- External system integration
- Framework requirements
- Technical concerns (không phải business logic)

**Example: Scheduled Cleanup**
```java
@Service
@RequiredArgsConstructor
public class DataCleanupService {
    private final ExpiredDataRepository repository;
    
    @Scheduled(cron = "0 0 2 * * *")  // 2 AM daily
    public void cleanupExpiredData() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = repository.deleteByCreatedAtBefore(cutoff);
        log.info("Cleaned up {} expired records", deleted);
    }
}
```

**Lý do dùng Service:** Infrastructure concern, không phải use case của user.

---

### Pattern 4: Domain Service

**Khi nào dùng:**
- Logic phức tạp cần reuse ở nhiều UseCases
- Thuật toán calculation phức tạp
- Domain rules không thuộc về 1 entity cụ thể

**Example: Pricing Engine**
```java
@Service
public class PricingService {
    
    public Money calculatePrice(
        List<LineItem> items,
        Customer customer,
        Promotion promotion
    ) {
        // Complex pricing algorithm
        Money subtotal = items.stream()
            .map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(Money.ZERO, Money::add);
        
        // Apply customer discount tier
        Money afterCustomerDiscount = applyCustomerDiscount(
            subtotal,
            customer.getTier()
        );
        
        // Apply promotion rules
        Money afterPromotion = applyPromotion(
            afterCustomerDiscount,
            promotion
        );
        
        // Add tax based on customer location
        Money tax = calculateTax(afterPromotion, customer.getLocation());
        
        return afterPromotion.add(tax);
    }
    
    private Money applyCustomerDiscount(Money amount, CustomerTier tier) {
        // Complex tier-based discount logic
        return switch(tier) {
            case GOLD -> amount.multiply(0.85);
            case SILVER -> amount.multiply(0.90);
            default -> amount;
        };
    }
    
    private Money applyPromotion(Money amount, Promotion promo) {
        // Complex promotion rules
    }
    
    private Money calculateTax(Money amount, Location location) {
        // Tax calculation by location
    }
}
```

**Lý do dùng Service:**
- Logic tính giá phức tạp, nhiều UseCases cần dùng
- Checkout, Invoice, Quote đều cần pricing
- Domain logic không phải technical concern

---

## ⚠️ Anti-Patterns

### ❌ Wrapper Vô Nghĩa

```java
// ❌ SAI - UseCase chỉ forward
@Component
public class GetUserUseCase {
    private final UserRepository repository;
    
    public User execute(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
```

**Vấn đề:** Không có giá trị gì! Controller có thể gọi trực tiếp Repository.

**Cách sửa:**
```java
// ✅ ĐÚNG - Controller gọi Repository trực tiếp
@RestController
public class UserController {
    private final UserRepository repository;
    
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
```

---

### ❌ Business Logic trong Controller

```java
// ❌ SAI - Logic nằm trong Controller
@RestController
public class OrderController {
    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        // Validation logic
        // Multi-repository operations
        // Email sending
        // All in Controller! ❌
    }
}
```

**Vấn đề:**
- Controller quá nặng
- Không test được business logic riêng
- Khó reuse

**Cách sửa:**
```java
// ✅ ĐÚNG - Logic vào UseCase
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        return createOrderUseCase.execute(request);
    }
}
```

---

## 📚 Real-World Examples

### Example 1: Simple Query

**Requirement:** Get all categories

**Decision:**
- Kết hợp nhiều repo? ❌
- Business rules? ❌
- Side effects? ❌

**Solution:** Repository trực tiếp

```java
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository repository;
    
    @GetMapping
    public List<Category> getAll() {
        return repository.findAll();
    }
}
```

---

### Example 2: Create with Validation

**Requirement:** Create budget với validation amount > 0, check duplicate

**Decision:**
- Logic đơn giản có thể xử lý trong Controller
- Chỉ 1 repository
- Không có side effects

**Solution:** Controller + Repository

```java
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetRepository repository;
    
    @PostMapping
    public Budget create(@RequestBody CreateBudgetRequest request) {
        // Simple validation
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        
        // Check duplicate
        if (repository.existsByUserIdAndCategory(
            getCurrentUserId(),
            request.getCategory()
        )) {
            throw new DuplicateException("Budget already exists");
        }
        
        // Create and save
        Budget budget = Budget.from(request);
        return repository.save(budget);
    }
}
```

---

### Example 3: Complex Transaction

**Requirement:** Create payment với:
- Validate account balance
- Create transaction record
- Update account balance
- Create notification
- Send email confirmation

**Decision:**
- Kết hợp nhiều repo? ✅ (Payment, Account, Notification)
- Business rules? ✅ (balance check)
- Side effects? ✅ (email)
- Transaction? ✅

**Solution:** UseCase

```java
@Component
@RequiredArgsConstructor
public class CreatePaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    @Transactional
    public Payment execute(CreatePaymentRequest request) {
        // 1. Get account
        Account account = accountRepository.findById(request.getAccountId())
            .orElseThrow(() -> new NotFoundException("Account not found"));
        
        // 2. Business rule: Check balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                "Balance: " + account.getBalance() +
                ", Required: " + request.getAmount()
            );
        }
        
        // 3. Create payment
        Payment payment = Payment.builder()
            .accountId(request.getAccountId())
            .amount(request.getAmount())
            .description(request.getDescription())
            .status(PaymentStatus.PENDING)
            .build();
        Payment saved = paymentRepository.save(payment);
        
        // 4. Update account balance
        account.decreaseBalance(request.getAmount());
        accountRepository.save(account);
        
        // 5. Create notification
        Notification notification = Notification.builder()
            .userId(account.getUserId())
            .message("Payment of " + request.getAmount() + " processed")
            .build();
        notificationRepository.save(notification);
        
        // 6. Side effect: Send email
        emailService.sendPaymentConfirmation(
            account.getEmail(),
            saved
        );
        
        return saved;
    }
}
```

**Giá trị UseCase:**
- ✅ Orchestrate 3 repositories
- ✅ Complex business validation
- ✅ Transaction boundary clear
- ✅ Side effect handled

---

---

## 6. Modular Clean Architecture Rules (BẮT BUỘC)

Backend sử dụng kiến trúc **Modular Monolith** kết hợp **Pragmatic Clean Architecture**.

### 🏗️ Cấu trúc Module
Mỗi module (ví dụ `identity`) phải tuân thủ cấu trúc gói sau:

```
com.finflow.backend.modules.[module_name]
├── api             # Public interfaces (DTOs, Events) cho module khác gọi
├── domain          # Domain Layer (Core - KHÔNG phụ thuộc Framework/Internal)
│   ├── entity      # JPA Entities (@Entity accepted pragmatic)
│   └── repository  # Repository Interfaces (extend JpaRepository)
├── internal        # Private implementation (Infrastructure)
│   ├── security    # Security configs, specialized services
│   └── ...
├── usecase         # Application Layer (Business Logic)
│   └── [Name]UseCase.java
└── [Module]Controller.java
```

### 📏 Rules Bất Biến
1.  **Dependency Direction**:
    -   `UseCase` -> `Domain` (Entities/Repositories)
    -   `Internal` -> `Domain`
    -   `Controller` -> `UseCase` HOẶC `Domain` (Repository)
    -   ❌ CẤM: `UseCase` phụ thuộc trực tiếp vào `Internal`
2.  **Domain Layer**:
    -   Chứa: Entities, Market interfaces (Repository Interfaces).
    -   Pragmatic Exception: JPA entities được coi là Domain Entities (chấp nhận `@Entity` annotations).
3.  **UseCases**:
    -   Chỉ import classes từ `domain` packages.
    -   Không import từ `internal`.

---

## 🚀 Development Workflow

### Checklist khi develop feature mới:

1. **Phân tích requirement:**
   - Cần query data gì?
   - Có business rules phức tạp không?
   - Cần update nhiều tables?
   - Có side effects không?

2. **Chạy qua Decision Matrix:**
   - Đánh dấu ✅ các tiêu chí phù hợp
   - Nếu có >= 2 ✅ trong cột UseCase → Dùng UseCase
   - Nếu tất cả ❌ → Repository trực tiếp

3. **Implement:**
   - Simple → Controller + Repository
   - Complex → Controller + UseCase + Repository(s)

4. **Review:**
   - UseCase có logic thực sự không?
   - Có thể đơn giản hóa không?
   - Có duplicate code không?

5. **Refactor nếu cần:**
   - UseCase wrapper vô nghĩa → Xóa, gọi Repository trực tiếp
   - Logic phức tạp reuse → Extract Domain Service
   - Technical concerns → Extract Infrastructure Service

---

## 📖 Key Principles

### 1. KISS (Keep It Simple, Stupid)
- Default: Simple nhất có thể
- Chỉ thêm complexity khi CẦN THIẾT

### 2. YAGNI (You Aren't Gonna Need It)
- Không tạo UseCase "for future"
- Không tạo abstraction "just in case"

### 3. Explicit Over Implicit
- Logic rõ ràng > "clever code"
- Tên class/method phản ánh đúng ý nghĩa

### 4. Single Responsibility
- 1 UseCase = 1 business operation
- 1 Repository = 1 entity

---

## 🎯 Summary

| Scenario | Pattern | Example |
|----------|---------|---------|
| **Simple query** | Repository trực tiếp | `repository.findAll()` |
| **Simple create với validation đơn giản** | Controller + Repository | Create budget |
| **Complex orchestration** | UseCase | Create order với inventory check |
| **Side effects** | UseCase | Payment với email confirmation |
| **Scheduled tasks** | Infrastructure Service | Daily cleanup |
| **Shared complex logic** | Domain Service | Pricing calculation |

---

## 🔄 Token Refresh Pattern

**Khi nào dùng UseCase cho refresh token?**
- Cần validate refresh token (signature, expiry, blacklist)
- Cần phân biệt access vs refresh (`claim type`)
- Cần rotate refresh token và blacklist token cũ
- Cần rebuild scope từ user/roles để cấp access mới

**Flow chuẩn**
```
POST /api/auth/refresh
    ↓
AuthController.refresh(RefreshTokenRequest)
    ↓
RefreshTokenUseCase.execute(refreshToken)
    ├─ 1. Decode & validate JWT (signature, expiry, blacklist)
    ├─ 2. Enforce type == "refresh"
    ├─ 3. Load user + roles → scope
    ├─ 4. Blacklist refresh cũ (jti, expiry)
    ├─ 5. Issue access + refresh mới (rotate)
    ↓
Return AuthResponse (token, refreshToken, expiresIn, username, email)
```

**Controller guideline**
- `AuthController` thêm endpoint `/api/auth/refresh`
- Request: `RefreshTokenRequest.refreshToken` (@NotBlank)
- Response: `AuthResponse` chứa `token`, `refreshToken`, `expiresIn`, `username`, `email`

**UseCase guideline**
- Validate token bằng `JwtDecoder` (đã gắn blacklist validator trong `SecurityConfig`)
- Kiểm tra claim `type == refresh`
- Blacklist token cũ (`invalidated_tokens`)
- Sinh token mới bằng `JwtEncoder`:
  - Access: expiry ~1h, claim `type=access`
  - Refresh: expiry dài hơn (ví dụ 7 ngày), claim `type=refresh`
- Scope: join roles/authorities (ví dụ `"ROLE_USER ROLE_ADMIN"`)

**Security/Blacklist**
- `SecurityConfig` validator check blacklist qua `InvalidatedTokenRepository`
- `TokenCleanupService` dọn dẹp token hết hạn (scheduled)

**Contract FE/BE**
- FE gửi refresh token hiện tại; BE rotate và trả refresh mới
- FE nên thay refresh token sau mỗi lần refresh

---

*"Simplicity is the ultimate sophistication." - Leonardo da Vinci*

---

*Last updated: 07/01/2026*  
*Status: The Holy Bible - Follow strictly* 📖
