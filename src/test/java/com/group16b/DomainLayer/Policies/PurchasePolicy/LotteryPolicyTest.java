package com.group16b.DomainLayer.Policies.PurchasePolicy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import net.bytebuddy.asm.Advice;

public class LotteryPolicyTest {
    

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
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now());
            lotteryPolicy.enrollInLottery(1, 1);
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery enrollment is closed since " + LocalDateTime.now() + "."));
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
}