package com.example.backend_service.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import com.example.backend_service.model.auth.Role;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.product.Category;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.repository.CategoryRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.RoleRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    protected String jwtToken;
    protected String testUsername;
    protected String testEmail;
    protected String testPassword = "Password123@";

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected ShopRepository shopRepository;
    @Autowired
    protected ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Ensure default roles exist in the empty H2 database
        createRoleIfNotFound("USER");
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("MERCHANT");

        seedMockData();
    }

    private void seedMockData() {
        Category category;
        if (categoryRepository.findAll().isEmpty()) {
            category = new Category();
            category.setName("Mock Category");
            category = categoryRepository.save(category);
        } else {
            category = categoryRepository.findAll().get(0);
        }

        User owner = userRepository.findByUsername("global_mock_owner");
        if (owner == null) {
            owner = new User();
            owner.setUsername("global_mock_owner");
            owner.setEmail("global@mock.com");
            owner.setFullName("Global Mock Owner");
            owner.setPhoneNumber("0000000000");
            owner.setPassword(testPassword);
            Role merchantRole = roleRepository.findByName("MERCHANT").get();
            owner.setRoles(Set.of(merchantRole));
            owner = userRepository.save(owner);
        }

        Shop shop;
        if (shopRepository.findAll().isEmpty()) {
            shop = new Shop();
            shop.setOwner(owner);
            shop.setShopName("Global Mock Shop");
            shop = shopRepository.save(shop);
        } else {
            shop = shopRepository.findAll().get(0);
        }

        if (productRepository.findAll().isEmpty()) {
            Product product = new Product();
            product.setShop(shop);
            product.setCategory(category);
            product.setName("Global Mock Product");
            product.setPrice(new BigDecimal("1000.0"));
            product.setStockQuantity(100);
            productRepository.save(product);
        }
    }

    private void createRoleIfNotFound(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }

    /**
     * Hàm dùng cho các API yêu cầu bảo mật.
     * Tự động đăng ký 1 user mới (random) và đăng nhập để lấy JWT Token và gán vào biến jwtToken.
     */
    protected void createAndLoginTestUser() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        this.testUsername = "testuser_" + randomSuffix;
        this.testEmail = "testuser_" + randomSuffix + "@example.com";

        // 1. Đăng ký
        Map<String, String> regPayload = new HashMap<>();
        regPayload.put("username", testUsername);
        regPayload.put("email", testEmail);
        regPayload.put("phone", "0987654321");
        regPayload.put("fullName", "Test User");
        regPayload.put("password", testPassword);
        regPayload.put("confirmPassword", testPassword);

        given()
            .contentType(ContentType.JSON)
            .body(regPayload)
        .when()
            .post("/api/auth/register");
        
        // 2. Đăng nhập để lấy token
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", testUsername); // LoginRequest dùng username, không phải email
        loginPayload.put("password", testPassword);

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
        .when()
            .post("/api/auth/acces-token");

        this.jwtToken = loginResponse.jsonPath().getString("accessToken");
    }

    protected void createAndLoginAdminUser() {
        createAndLoginTestUserWithRole("ADMIN");
    }

    protected void createAndLoginMerchantUser() {
        createAndLoginTestUserWithRole("MERCHANT");
    }

    private void createAndLoginTestUserWithRole(String roleName) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        this.testUsername = roleName.toLowerCase() + "_" + randomSuffix;
        this.testEmail = this.testUsername + "@example.com";

        // 1. Register
        Map<String, String> regPayload = new HashMap<>();
        regPayload.put("username", testUsername);
        regPayload.put("email", testEmail);
        regPayload.put("phone", "0987654321");
        regPayload.put("fullName", "Test " + roleName);
        regPayload.put("password", testPassword);
        regPayload.put("confirmPassword", testPassword);

        given()
            .contentType(ContentType.JSON)
            .body(regPayload)
        .when()
            .post("/api/auth/register");
        
        // 2. Assign Role directly
        User user = userRepository.findByUsername(testUsername);
        if (user != null) {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(roleName);
                return roleRepository.save(newRole);
            });
            Set<Role> roles = user.getRoles();
            roles.add(role);
            user.setRoles(roles);
            user = userRepository.save(user);

            if ("MERCHANT".equals(roleName)) {
                Shop merchantShop = new Shop();
                merchantShop.setOwner(user);
                merchantShop.setShopName(testUsername + " Shop");
                shopRepository.save(merchantShop);
            }
        }

        // 3. Login to get token
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", testUsername);
        loginPayload.put("password", testPassword);

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body(loginPayload)
        .when()
            .post("/api/auth/acces-token");

        this.jwtToken = loginResponse.jsonPath().getString("accessToken");
    }
}
