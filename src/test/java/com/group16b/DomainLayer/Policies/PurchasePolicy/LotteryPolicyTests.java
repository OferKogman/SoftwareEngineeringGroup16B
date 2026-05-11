package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class LotteryPolicyTests {
    

    @Test
    public void testLotteryPolicyCreation() {
        assertDoesNotThrow(() -> {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusDays(1));
        });
    }

    @Test
    public void testLotteryPolicyCreationWithPastDate() {
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().minusDays(1));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery registration due date cannot be in the past."));
        }
    }

    @Test
    public void SuccessfulsetLotteryRegistrationDueDate() {
        assertDoesNotThrow(() -> {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusDays(1));
            lotteryPolicy.setLotteryRegistrationDueDate(LocalDateTime.now().plusHours(1));
        });
    }

    @Test
    public void FailuresetLotteryRegistrationDueDateInPast() {
        try {
            LocalDateTime lotteryDate = LocalDateTime.now().plusDays(1);
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, lotteryDate);
            lotteryPolicy.setLotteryRegistrationDueDate(LocalDateTime.now().minusHours(1));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery registration due date cannot be in the past."));
        }
    }

    @Test
    public void SuccessfulEnrollInLottery(){
        assertDoesNotThrow(() -> {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusDays(1));
            lotteryPolicy.enrollInLottery(1, 1);
        });
    }

    @Test
    public void FailureEnrollInLotteryAfterDueDate() {
        LocalDateTime lotteryDate = LocalDateTime.now().plusSeconds(5);
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, lotteryDate);
            Thread.sleep(10000);
            lotteryPolicy.enrollInLottery(1, 1);
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery enrollment is closed since " + lotteryDate + "."));
        }
    }

    @Test
    public void FailureEnrollInLotteryAlreadyEnrolled() {
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusDays(1));
            lotteryPolicy.enrollInLottery(1, 1);
            lotteryPolicy.enrollInLottery(1, 1);
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("User is already enrolled in the lottery."));
        }
    }

    @Test 
    public void FailureValidateLotteryCodeBeenUsed() {
        try{
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusMinutes(1));
            Field field = LotteryPolicy.class.getDeclaredField("winnersAndCodes");
            field.setAccessible(true);
            field.set(lotteryPolicy, Map.of("invalid_code", 1));
            lotteryPolicy.validateLotteryCode("invalid_code");
            lotteryPolicy.validateLotteryCode("invalid_code");
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery code has already been used."));
        }
    }

    public void FailureValidateLotteryInvalidLotteryCode() {
        try{
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusMinutes(1));
            lotteryPolicy.validateLotteryCode("invalid_code");
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Invalid lottery code."));
        }
    }
}