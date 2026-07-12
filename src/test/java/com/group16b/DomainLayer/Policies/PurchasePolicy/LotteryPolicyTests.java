package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.contains;

import com.group16b.ApplicationLayer.Interfaces.INotifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LotteryPolicyTests {
    private INotifier notifier;
    

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
            lotteryPolicy.enrollInLottery(1, "1");
        });
    }

    @Test
    public void FailureEnrollInLotteryAfterDueDate() {
        LocalDateTime lotteryDate = LocalDateTime.now().plusSeconds(5);
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, lotteryDate);
            Thread.sleep(10000);
            lotteryPolicy.enrollInLottery(1, "1");
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("Lottery enrollment is closed since " + lotteryDate + "."));
        }
    }

    @Test
    public void FailureEnrollInLotteryAlreadyEnrolled() {
        try {
            LotteryPolicy lotteryPolicy = new LotteryPolicy(1, "Test Lottery", 5, LocalDateTime.now().plusDays(1));
            lotteryPolicy.enrollInLottery(1, "1");
            lotteryPolicy.enrollInLottery(1, "1");
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assert(e.getMessage().equals("User is already enrolled in the lottery."));
        }
    }

    @Test
    public void FailureValidateLotteryCodeBeenUsed() throws Exception {
        LotteryPolicy lotteryPolicy =
                new LotteryPolicy(
                        1,
                        "Test Lottery",
                        5,
                        LocalDateTime.now().plusMinutes(1));

        Field field =
                LotteryPolicy.class.getDeclaredField("winnersAndCodes");

        field.setAccessible(true);
        field.set(
                lotteryPolicy,
                new HashMap<>(Map.of("invalid_code", "1")));

        lotteryPolicy.validateLotteryCode("invalid_code");
        lotteryPolicy.useCode("invalid_code");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lotteryPolicy.validateLotteryCode("invalid_code"));

        assertEquals(
                "Lottery code has already been used.",
                exception.getMessage());
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

    @Test
    void lotteryPolicyConverter_roundTrip_preservesPolicy() {
        LotteryPolicy original = new LotteryPolicy(
                1,
                "test lottery",
                2,
                LocalDateTime.now().plusDays(1)
        );

        original.enrollInLottery(10, "user1");
        original.enrollInLottery(10, "user2");

        LotteryPolicyConverter converter = new LotteryPolicyConverter();

        String db = converter.convertToDatabaseColumn(original);
        assertFalse(db.contains("ConcurrentHashMap"));
        assertFalse(db.contains("KeySetView"));
        LotteryPolicy restored = converter.convertToEntityAttribute(db);

        assertEquals("test lottery", restored.getLotteryName());
        assertEquals(2, restored.getWinnerAmount());
        assertTrue(restored.getParticipants().contains("user1"));
        assertTrue(restored.getParticipants().contains("user2"));
    }

    private void forceLotteryDueDate(LotteryPolicy policy, LocalDateTime dueDate) throws Exception {
        var field = LotteryPolicy.class.getDeclaredField("lotteryRegistrationDueDate");
        field.setAccessible(true);
        field.set(policy, dueDate);
    }

    @Test
    void handleLotteryResults_beforeDueDate_fails() {
        LotteryPolicy policy = new LotteryPolicy(
                1,
                "test lottery",
                1,
                LocalDateTime.now().plusDays(1)
        );

        policy.enrollInLottery(10, "user1");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                policy::handleLotteryResults
        );

        assertEquals(
                "Cannot handle lottery results before the registration due time passed.",
                ex.getMessage()
        );
    }
}