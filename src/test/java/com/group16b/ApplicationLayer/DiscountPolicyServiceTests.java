package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.group16b.DomainLayer.Event.IEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.*;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.DiscountPolicy.*;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;


public class DiscountPolicyServiceTests {

    private DiscountPolicyService discountPolicyService;
    private IAuthenticationService authService;
    private IProductionCompanyRepository productionCompanyRepo;
    private IRepository<User> userRepo;
    private IEventRepository eventRepository;
    private ProductionCompany testCompany;
    private User testAdmin;
    private static final String VALID_TOKEN = "validSessionToken";
    private static final String INVALID_TOKEN = "invalidSessionToken";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final int COMPANY_ID = 1;
    private static final int NONEXISTENT_COMPANY_ID = 999;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        authService = mock(IAuthenticationService.class);
        productionCompanyRepo = mock(IProductionCompanyRepository.class);
        userRepo = mock(IRepository.class);

        // Create test data
        testAdmin = new User(ADMIN_EMAIL, "password123");
        testCompany = ProductionCompany.createNewCompany("Test Company", ADMIN_EMAIL, COMPANY_ID);

        // Initialize service
        discountPolicyService = new DiscountPolicyService(
                authService,
                productionCompanyRepo,
                userRepo,
                eventRepository
        );
    }



    /**
     * Helper method to set up authentication mocks
     */
    private void setupValidAuth(String token, String email) {
        when(authService.validateToken(token)).thenReturn(true);
        when(authService.extractSubjectFromToken(token)).thenReturn(email);
    }

    /**
     * Helper method to set up invalid authentication mocks
     */
    private void setupInvalidAuth(String token) {
        when(authService.validateToken(token)).thenReturn(false);
    }

    // ==================== SET COMPANY DISCOUNT POLICY TESTS ====================

    @Nested
    @DisplayName("setCompanyDiscountPolicy - Success Cases")
    class SetCompanyDiscountPolicySuccess {

        @Test
        @DisplayName("Should create new SimpleDiscount policy when policy doesn't exist")
        void shouldCreateNewSimpleDiscountPolicy() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO dto = new RegularDiscountDTO(15.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess(), "Result should be successful");
            assertNotNull(result.getValue(), "DTO should not be null");
            assertEquals(15.0, ((RegularDiscountDTO) result.getValue()).getPercentage());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should update existing discount policy")
        void shouldUpdateExistingPolicy() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            testCompany.setDiscountPolicy(new SimpleDiscount(5.0));
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO newDto = new RegularDiscountDTO(20.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    newDto
            );

            // Assert
            assertTrue(result.isSuccess(), "Result should be successful");
            assertEquals(20.0, ((RegularDiscountDTO) result.getValue()).getPercentage());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should handle MinimumPurchaseDiscount creation")
        void shouldCreateMinimumPurchaseDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            MinimumPurchaseDiscountDTO dto = new MinimumPurchaseDiscountDTO(10.0, 100);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(MinimumPurchaseDiscountDTO.class, result.getValue());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should handle MaximumPurchaseDiscount creation")
        void shouldCreateMaximumPurchaseDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            MaximumPurchaseDiscountDTO dto = new MaximumPurchaseDiscountDTO(5.0, 500);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(MaximumPurchaseDiscountDTO.class, result.getValue());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should handle CouponCodeDiscount creation")
        void shouldCreateCouponCodeDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            LocalDateTime expirationDate = LocalDateTime.now().plusMonths(1);
            CouponCodeDiscountDTO dto = new CouponCodeDiscountDTO(12.5, "SUMMER2024", expirationDate);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(CouponCodeDiscountDTO.class, result.getValue());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should handle zero percent discount")
        void shouldHandleZeroPercentDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO dto = new RegularDiscountDTO(0.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0.0, ((RegularDiscountDTO) result.getValue()).getPercentage());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }

        @Test
        @DisplayName("Should handle high percent discount")
        void shouldHandleHighPercentDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO dto = new RegularDiscountDTO(99.9);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(99.9, ((RegularDiscountDTO) result.getValue()).getPercentage());
            verify(productionCompanyRepo).save(any(ProductionCompany.class));
        }
    }

    @Nested
    @DisplayName("setCompanyDiscountPolicy - Authentication Failures")
    class SetCompanyDiscountPolicyAuthenticationFailures {


        @Test
        @DisplayName("Should fail when token is null")
        void shouldFailWithNullToken() {
            // Arrange
            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    null,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertNotNull(result.getError(), "Error message should be present");
            verify(productionCompanyRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when token is empty string")
        void shouldFailWithEmptyToken() {
            // Arrange
            setupInvalidAuth("");
            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    "",
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            verify(productionCompanyRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("setCompanyDiscountPolicy - Company Not Found")
    class SetCompanyDiscountPolicyCompanyNotFound {

        @Test
        @DisplayName("Should fail when company doesn't exist")
        void shouldFailWhenCompanyNotFound() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(NONEXISTENT_COMPANY_ID)))
                    .thenReturn(null);

            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    NONEXISTENT_COMPANY_ID,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Company not found"),
                    "Error message should mention company not found");
            verify(productionCompanyRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when company repository throws exception")
        void shouldFailWhenCompanyRepositoryThrowsException() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenThrow(new IllegalArgumentException("Database error"));

            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Database error") ||
                    result.getError().contains("error"));
            verify(productionCompanyRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("setCompanyDiscountPolicy - Invalid Input")
    class SetCompanyDiscountPolicyInvalidInput {



        @Test
        @DisplayName("Should fail when company ID is negative")
        void shouldFailWithNegativeCompanyId() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    -1,
                    dto
            );

            // Assert
            // Either the method should handle this, or it should fail gracefully
            assertFalse(result.isSuccess());
            verify(productionCompanyRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when company ID is zero")
        void shouldFailWithZeroCompanyId() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    0,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess());
            verify(productionCompanyRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("setCompanyDiscountPolicy - Save Failures")
    class SetCompanyDiscountPolicySaveFailures {

        @Test
        @DisplayName("Should handle repository save failures gracefully")
        void shouldHandleSaveFailures() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);
            doThrow(new RuntimeException("Save failed"))
                    .when(productionCompanyRepo).save(any(ProductionCompany.class));

            RegularDiscountDTO dto = new RegularDiscountDTO(10.0);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    dto
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Save failed") ||
                    result.getError().toLowerCase().contains("error"));
        }
    }

    // ==================== GET COMPANY DISCOUNT POLICY TESTS ====================

    @Nested
    @DisplayName("getCompanyDiscountPolicy - Success Cases")
    class GetCompanyDiscountPolicySuccess {

        @Test
        @DisplayName("Should retrieve existing SimpleDiscount policy")
        void shouldRetrieveSimpleDiscountPolicy() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            SimpleDiscount policy = new SimpleDiscount(15.0);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess(), "Result should be successful");
            assertNotNull(result.getValue(), "Policy DTO should not be null");
            assertInstanceOf(RegularDiscountDTO.class, result.getValue());
            assertEquals(15.0, ((RegularDiscountDTO) result.getValue()).getPercentage());
            verify(productionCompanyRepo).findByID(String.valueOf(COMPANY_ID));
        }

        @Test
        @DisplayName("Should return null when no policy exists")
        void shouldReturnNullWhenNoPolicyExists() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            // testCompany has no policy set
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess(), "Result should be successful");
            assertNull(result.getValue(), "Policy DTO should be null when no policy exists");
        }

        @Test
        @DisplayName("Should retrieve MinimumPurchaseDiscount policy")
        void shouldRetrieveMinimumPurchaseDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            AmountRangeDiscount policy = new AmountRangeDiscount(100, null, 10.0);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(MinimumPurchaseDiscountDTO.class, result.getValue());
        }

        @Test
        @DisplayName("Should retrieve MaximumPurchaseDiscount policy")
        void shouldRetrieveMaximumPurchaseDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            AmountRangeDiscount policy = new AmountRangeDiscount(null, 500, 5.0);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(MaximumPurchaseDiscountDTO.class, result.getValue());
        }

        @Test
        @DisplayName("Should retrieve CouponCodeDiscount policy")
        void shouldRetrieveCouponCodeDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            LocalDateTime expirationDate = LocalDateTime.now().plusMonths(1);
            CouponCodeDiscount policy = new CouponCodeDiscount(12.5, "SUMMER2024", expirationDate, null);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess());
            assertNotNull(result.getValue());
            assertInstanceOf(CouponCodeDiscountDTO.class, result.getValue());
        }

        @Test
        @DisplayName("Should retrieve policy with zero percent discount")
        void shouldRetrieveZeroPercentPolicy() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            SimpleDiscount policy = new SimpleDiscount(0.0);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(0.0, ((RegularDiscountDTO) result.getValue()).getPercentage());
        }

        @Test
        @DisplayName("Should retrieve policy with high percent discount")
        void shouldRetrieveHighPercentPolicy() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            SimpleDiscount policy = new SimpleDiscount(99.9);
            testCompany.setDiscountPolicy(policy);

            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(result.isSuccess());
            assertEquals(99.9, ((RegularDiscountDTO) result.getValue()).getPercentage());
        }
    }

    @Nested
    @DisplayName("getCompanyDiscountPolicy - Authentication Failures")
    class GetCompanyDiscountPolicyAuthenticationFailures {

        @Test
        @DisplayName("Should fail when token is invalid")
        void shouldFailWithInvalidToken() {
            // Arrange
            setupInvalidAuth(INVALID_TOKEN);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    INVALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Authentication failed") ||
                            result.getError().toLowerCase().contains("invalid") ||
                            result.getError().toLowerCase().contains("token"),
                    "Error message should mention authentication failure");
            verify(productionCompanyRepo, never()).findByID(any());
        }

        @Test
        @DisplayName("Should fail when token is null")
        void shouldFailWithNullToken() {
            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    null,
                    COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertNotNull(result.getError(), "Error message should be present");
            verify(productionCompanyRepo, never()).findByID(any());
        }

        @Test
        @DisplayName("Should fail when token is empty string")
        void shouldFailWithEmptyToken() {
            // Arrange
            setupInvalidAuth("");

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    "",
                    COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            verify(productionCompanyRepo, never()).findByID(any());
        }
    }

    @Nested
    @DisplayName("getCompanyDiscountPolicy - Company Not Found")
    class GetCompanyDiscountPolicyCompanyNotFound {

        @Test
        @DisplayName("Should fail when company doesn't exist")
        void shouldFailWhenCompanyNotFound() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(NONEXISTENT_COMPANY_ID)))
                    .thenReturn(null);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    NONEXISTENT_COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Company not found") ||
                            result.getError().toLowerCase().contains("unexpected"),
                    "Error message should indicate company not found");
        }

        @Test
        @DisplayName("Should fail when company repository throws IllegalArgumentException")
        void shouldFailWhenRepositoryThrowsIllegalArgumentException() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenThrow(new IllegalArgumentException("Invalid company ID format"));

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("Illegal argument") ||
                            result.getError().contains("Invalid"),
                    "Error message should mention illegal argument");
        }

        @Test
        @DisplayName("Should fail when company repository throws generic Exception")
        void shouldFailWhenRepositoryThrowsGenericException() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenThrow(new RuntimeException("Database connection error"));

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertFalse(result.isSuccess(), "Result should fail");
            assertTrue(result.getError().contains("unexpected") ||
                            result.getError().contains("error") ||
                            result.getError().contains("Database"),
                    "Error message should indicate unexpected error");
        }
    }

    @Nested
    @DisplayName("getCompanyDiscountPolicy - Invalid Input")
    class GetCompanyDiscountPolicyInvalidInput {

        @Test
        @DisplayName("Should handle negative company ID")
        void shouldHandleNegativeCompanyId() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(-1)))
                    .thenReturn(null);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    -1
            );

            // Assert
            // Should either handle gracefully or return an error
            assertNotNull(result, "Result should not be null");
        }

        @Test
        @DisplayName("Should handle zero company ID")
        void shouldHandleZeroCompanyId() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(0)))
                    .thenReturn(null);

            // Act
            Result<DiscountPolicyDTO> result = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    0
            );

            // Assert
            assertNotNull(result, "Result should not be null");
        }
    }

    @Nested
    @DisplayName("Round-trip Conversion Tests (Set then Get)")
    class RoundTripConversionTests {

        @Test
        @DisplayName("Should successfully round-trip SimpleDiscount policy")
        void shouldRoundTripSimpleDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO originalDto = new RegularDiscountDTO(25.5);

            // Act - Set
            Result<DiscountPolicyDTO> setResult = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    originalDto
            );

            // Act - Get (using the updated company from set)
            Result<DiscountPolicyDTO> getResult = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(setResult.isSuccess());
            assertTrue(getResult.isSuccess());
            assertEquals(25.5, ((RegularDiscountDTO) getResult.getValue()).getPercentage());
        }

        @Test
        @DisplayName("Should successfully round-trip MinimumPurchaseDiscount policy")
        void shouldRoundTripMinimumPurchaseDiscount() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            MinimumPurchaseDiscountDTO originalDto = new MinimumPurchaseDiscountDTO(15.0, 200);

            // Act - Set
            Result<DiscountPolicyDTO> setResult = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    originalDto
            );

            // Act - Get
            Result<DiscountPolicyDTO> getResult = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );

            // Assert
            assertTrue(setResult.isSuccess());
            assertTrue(getResult.isSuccess());
            assertInstanceOf(MinimumPurchaseDiscountDTO.class, getResult.getValue());
        }

        @Test
        @DisplayName("Should verify policy persistence through multiple operations")
        void shouldVerifyPolicyPersistenceMultipleOperations() {
            // Arrange
            setupValidAuth(VALID_TOKEN, ADMIN_EMAIL);
            when(productionCompanyRepo.findByID(String.valueOf(COMPANY_ID)))
                    .thenReturn(testCompany);

            RegularDiscountDTO initialDto = new RegularDiscountDTO(10.0);
            RegularDiscountDTO updatedDto = new RegularDiscountDTO(20.0);

            // Act & Assert - Initial set
            Result<DiscountPolicyDTO> setResult1 = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    initialDto
            );
            assertTrue(setResult1.isSuccess());
            assertEquals(10.0, ((RegularDiscountDTO) setResult1.getValue()).getPercentage());

            // Act & Assert - Get after first set
            Result<DiscountPolicyDTO> getResult1 = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );
            assertTrue(getResult1.isSuccess());
            assertEquals(10.0, ((RegularDiscountDTO) getResult1.getValue()).getPercentage());

            // Act & Assert - Update
            Result<DiscountPolicyDTO> setResult2 = discountPolicyService.setCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID,
                    updatedDto
            );
            assertTrue(setResult2.isSuccess());
            assertEquals(20.0, ((RegularDiscountDTO) setResult2.getValue()).getPercentage());

            // Act & Assert - Get after update
            Result<DiscountPolicyDTO> getResult2 = discountPolicyService.getCompanyDiscountPolicy(
                    VALID_TOKEN,
                    COMPANY_ID
            );
            assertTrue(getResult2.isSuccess());
            assertEquals(20.0, ((RegularDiscountDTO) getResult2.getValue()).getPercentage());
        }
    }

    
}
