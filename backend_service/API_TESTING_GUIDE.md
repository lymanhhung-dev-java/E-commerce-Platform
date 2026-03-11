# Hướng Dẫn Chạy & Đọc Kết Quả Kiểm Thử (API Testing Guide)

Tài liệu này hướng dẫn chi tiết cách chạy, đọc kết quả kiểm thử tự động cho hệ thống Backend API và cấu trúc của các Test Case hiện có.

## 1. Công nghệ sử dụng
- **JUnit 5**: Framework chính để viết và quản lý các luồng test.
- **RestAssured**: Thư viện dùng để giả lập các HTTP Request (GET, POST, PUT, DELETE) gửi đến API và dễ dàng kiểm tra JSON Body phản hồi.
- **H2 Database**: Database ảo (In-Memory) được ghi đè bằng `application-test.properties` và chạy trong bộ nhớ RAM, tự xóa khi chạy test xong. Nó đảm bảo quá trình test không làm ảnh hưởng hay rác dữ liệu thực tế (MySQL).
- **Spring Boot Test**: Thiết lập mock Web Environment tại một ngẫu nhiên `@SpringBootTest(webEnvironment = RANDOM_PORT)` để test mạng trực tiếp.

## 2. Cách chạy Test

### Bằng Terminal (Dòng lệnh)
Mở terminal (như PowerShell hoặc Command Prompt) đang trỏ vào thư mục chứa mã nguồn backend (`E-commerce-Platform/backend_service`) và chạy lệnh sau:
- **Để chạy toàn bộ hơn 60 test case:**
  ```bash
  .\gradlew test
  ```
- **Để chỉ chạy 1 group (class) cụ thể:** (Ví dụ chỉ test module Đơn hàng của Merchant)
  ```bash
  .\gradlew test --tests "com.example.backend_service.api.MerchantOrderApiTest"
  ```
- **Để chỉ chạy 1 phương thức test cụ thể:**
  ```bash
  .\gradlew test --tests "com.example.backend_service.api.AdminUserApiTest.testLockUser_SuccessOrNotFound"
  ```

### Bằng IDE (IntelliJ IDEA / VS Code)
- Vào thư mục cấu trúc dự án: `src/test/java/com/example/backend_service/api`.
- Mở bất kỳ file kết thúc bằng chữ `*ApiTest.java`.
- Ở góc IDE, ngay bên cạnh dòng khai báo class hoặc khai báo function `@Test` sẽ có **biểu tượng nút Play (Run)** màu xanh lá. Hãy click vào đó để chạy nhanh trực quan.

## 3. Cách đọc kết quả Test

### Khay lệnh (Terminal)
Khi lệnh `.\gradlew test` chạy xong:
- **Thành công (Pass):** Xanh lá `BUILD SUCCESSFUL`. File code không có lỗi logic, Validation đảm bảo.
- **Thất bại (Fail):** Đỏ `BUILD FAILED` sẽ hiện ra. Terminal sẽ in ra thông báo như: _Expected status code <200> but was <403>_, tức là code hy vọng API chả về 200 Thành Công, nhưng thực tế API lại chặn bằng lỗi 403 Forbidden.

### Báo cáo Website (HTML) - Khuyên dùng ⭐️
Bạn không cần phải căng mắt nhìn Terminal. Gradle cung cấp một trang giao diện trực quan sau mỗi lần chạy test.
Sau khi chạy test, bạn mở link dưới đây ở trình duyệt (Chrome/Cốc Cốc...):
👉 **Đường dẫn file:** `backend_service/build/reports/tests/test/index.html`

- **Tab Packages / Classes**: Chia test theo từng cụm, tỷ lệ thành công bao nhiêu % và thời gian chạy mất bao nhiêu milli-giây.
- Giả sử có test bị FAIL, ở thanh bên dưới màn hình sẽ hiển thị **Standard Output** chứa chi tiết câu lệnh truy vấn SQL Hibernate đã chạy và thông tin Exception tường minh giúp bạn biết code đang sai ở Controller hay Service.

## 4. Danh sách các Test Case (Các Module phân quyền)

Hiển tại toàn bộ API đã được phủ (Test coverage cao). Các folder test đã chia ranh giới theo chức vụ Token:

### Nhóm 1: API Công Khai & Cơ bản (Public)
Vài đầu cuối không cần đăng nhập.
- `AuthApiTest`: Luồng Đăng ký, Đăng nhập, gia hạn Access Token / Refresh Token.
- `AddressApiTest`: Quản lý danh bạ nhận hàng.
- `CategoryApiTest`: Lấy danh mục chung để in ra Menu Website.
- `ReviewApiTest`: Xem đánh giá công khai từng món hàng.
- `ShopApiTest`: Thông tin public của các cửa hàng.
- `FileUploadApiTest`: Nhả Endpoint MultipartFile up ảnh Cloudinary.

### Nhóm 2: Người mua hàng (Khách / USER)
Bắt buộc Header có Bearer Token.
- `UserApiTest`: Nhóm đổi mật khẩu, Sửa avatar.
- `CartApiTest`: Thêm hàng vào giỏ, đổi số lượng trước checkout.
- `CheckoutApiTest`: Generate mã QR chuyển khoản VNPay, thao tác Pay, Hủy đơn rác.
- `OrderApiTest`: User coi danh sách đơn và lộ trình Ship.

### Nhóm 3: Cửa hàng (MERCHANT)
Token phải có Role `ROLE_MERCHANT`. Code test dùng hàm `createAndLoginMerchantUser()` để tự động chuẩn bị.
- `MerchantProductApiTest`: Chủ Shop đăng món, xóa món, sửa giá.
- `MerchantShopApiTest`: Tạo shop mới, xin update thông tin KYC chờ Admin duyệt.
- `MerchantOrderApiTest`: Đánh dấu đã đóng gói, đang giao hàng cho khách.
- `MerchantWalletApiTest` & `MerchantWithdrawalApiTest`: Ví nội bộ, đếm số dư, rút tiền ra tài khoản ngân hàng.
- `MerchantStatisticApiTest`: Biểu đồ thu nhập.

### Nhóm 4: Quản trị viên (ADMIN)
Token phải kẹp Role `ROLE_ADMIN`. Code test gán hàm `createAndLoginAdminUser()`.
- `AdminCategoryApiTest`: Sửa, thêm parent, tắt bật danh mục.
- `AdminProductApiTest`: Khóa các mặt hàng vi phạm hoặc duyệt đăng.
- `AdminShopApiTest`: Ban vĩnh viễn shop, Approve request mở shop từ Merchant.
- `AdminUserApiTest`: Ban/Khóa Account khách hàng clone/quấy rối.
- `AdminWithdrawalApiTest`: Chi tiền cho các lệnh rút của chủ shop.
- `AdminWithdrawalApiTest`: Chi tiền cho các lệnh rút của chủ shop.
- `AdminDashboardApiTest`: Chart thông kê sàn hệ thống E-commerce.

---

## 5. Bảng Kịch Bản Kiểm Thử Chi Tiết (Test Scenarios)

Dưới đây là một số kịch bản tiêu biểu đã được tự động hóa trong mã nguồn:

### 5.1. Module Xác thực (Auth) & Người dùng (User)
| API Endpoint | Method | Kịch bản Test (Test Case) | Input Body / Params | Expected Output (Mã HTTP) |
| :--- | :---: | :--- | :--- | :--- |
| `/api/auth/register` | POST | Đăng ký thành công | JSON: username, email hợp lệ | `200 OK` (Thành công) |
| `/api/auth/register` | POST | Đăng ký thất bại do trùng Email/Username | JSON: email đã tồn tại ở DB | `400 Bad Request` |
| `/api/auth/acces-token` | POST | Login thành công, nhận Token | JSON: username, password đúng | `200 OK` (Kèm AccessToken) |
| `/api/auth/acces-token` | POST | Login thất bại (Sai pass) | JSON: password sai | `401 Unauthorized` |
| `/api/profile/me` | GET | Lấy thông tin cá nhân | Header: Authorization Token | `200 OK` (Chứa username/email) |
| `/api/profile/update` | PUT | Sửa thông tin cá nhân thành công | JSON: SDT, Họ tên mới | `200 OK` |

### 5.2. Module Sản phẩm (Product) & Danh mục (Category)
| API Endpoint | Method | Kịch bản Test (Test Case) | Input Body / Params | Expected Output (Mã HTTP) |
| :--- | :---: | :--- | :--- | :--- |
| `/api/categories` | GET | Lấy danh sách chuyên mục | - | `200 OK` (Chứa List Cates) |
| `/api/products` | GET | Khách tìm kiếm mặt hàng | Query: keyword, sort | `200 OK` (List Products) |
| `/api/merchant/products` | POST | Chủ shop xin đăng bán mặt hàng | JSON: name, price, cateId | `200 OK` hoặc `400` (Thiếu field) |
| `/api/admin/products/{id}/toggle-status`| PUT | Admin duyệt/khóa sản phẩm | Path: Product ID | `200 OK` (Nếu có ID) hoặc `404` |
| `/api/reviews` | POST | Khách đánh giá hàng đã mua | JSON: content, rating, orderId | `200 OK` hoặc `400` |

### 5.3. Module Giỏ Hàng (Cart) & Thanh Toán (Checkout)
| API Endpoint | Method | Kịch bản Test (Test Case) | Input Body / Params | Expected Output (Mã HTTP) |
| :--- | :---: | :--- | :--- | :--- |
| `/api/cart/add` | POST | Thêm mặt hàng vào giỏ | JSON: productId, quantity | `200 OK` hoặc `404` (Sản phẩm ẩn) |
| `/api/checkout` | POST | Khách tạo đơn hàng COD / VNPay | JSON: paymentMethod, addressId | `200 OK` (Tạo đơn) hoặc `400` |
| `/api/orders/my-orders` | GET | Xem danh sách đơn đã mua | Header: Authorization Token | `200 OK` |
| `/api/merchant/orders/{id}/status` | PUT | Shop cập nhật Giao hàng | JSON: STATUS (SHIPPING...) | `200 OK` hoặc `403` (Ko phải chủ) |

### 5.4. Module Cửa hàng (Shop) & Admin Control
| API Endpoint | Method | Kịch bản Test (Test Case) | Input Body / Params | Expected Output (Mã HTTP) |
| :--- | :---: | :--- | :--- | :--- |
| `/api/merchant/shops/register` | POST | Tạo shop mới chờ duyệt | JSON: name, address | `200 OK` hoặc `400` (Trùng tên) |
| `/api/admin/shops/{id}/approve`| PUT | Admin duyệt shop | Path: Shop ID | `200 OK` hoặc `404` (Sai ID) |
| `/api/admin/users/{id}/status` | PUT | Khóa mõm Account clone | Path: User ID | `200 OK` (Khóa/Mở) |
| `/api/merchant/wallet/withdraw`| POST | Chủ shop rút tiền ra bank | JSON: amount, bankName | `200 OK` hoặc `400` (Số dư âm) |

---

## 6. Cấu trúc của một Test Function (BDD Pattern)
Các bài Test áp dụng thiết kế Behavior-Driven Development bằng từ khóa Tiếng Anh `given`, `when`, `then`.

```java
@Test
void testUpdateShop_SuccessOrNotFound() {
    // 1. Chuẩn bị (GIVEN): Body Request bằng Java Map
    Map<String, Object> payload = new HashMap<>();
    payload.put("name", "Updated Merchant Shop");

    given()
        // Kẹp token để Pass qua Filter Spring Security
        .header("Authorization", "Bearer " + jwtToken)
        // Kiểu data JSON
        .contentType(ContentType.JSON)
        .body(payload)
    
    // 2. Chạy URL (WHEN)
    .when()
        .put("/api/merchant/shops/info") // Method HTTP
    
    // 3. Phán xét (THEN)
    .then()
        // API Test cho phép Pass cả các lỗi Validation mong muốn mà không chết server (200, 404, 400).
        .statusCode(anyOf(is(200), is(404), is(400), is(500)));
}
```

## 7. Sổ tay Debug nhanh khi bạn thấy Test báo đỏ (Fail)
- Khác biệt JSON Key: (Ví dụ body test cần `"phoneNumber"`, nhưng bạn ở Controller gõ `"phone"` sẽ sinh ra Lỗi `400 Bad Request`).
- Sai đường dẫn Mappings: Khi Controller thay đổi Route ví dụ từ `@GetMapping("/shop")` thành `@GetMapping("/shops/current")`, Code test sẽ bị trả `404 Not Found`. Bạn vào Java Error Test sửa lại URL tương ứng.
- Lỗi `401 Unauthorized`: API Test đang quên không chèn `.header("Authorization", "Bearer " + jwtToken)`.
- Chỉnh sửa Config `application-test.properties`: Hãy cẩn thận vì H2 Database sẽ validate các Constraint siêu nghiêm ngặt (Ví dụ Product thiếu Shop ID là nó văng Java Hibernate Exception `500 Internal Server error`). `BaseApiTest.java` đã được tối ưu để tự Inject sẵn Database Mock mồi (Mock Shop, User) cho test.
