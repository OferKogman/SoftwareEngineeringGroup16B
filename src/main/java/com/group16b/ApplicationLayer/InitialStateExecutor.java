package com.group16b.ApplicationLayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Objects.DiscountPolicyTypes;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.ApplicationLayer.Records.EventRecord;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.group16b.ApplicationLayer.Exceptions.SystemStartupException;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;


@Component
public class InitialStateExecutor {

    private final EventService eventService;
    private final LotteryPolicyService lotteryPolicyService;
    private final DiscountPolicyService discountPolicyService;

    // stores IDs returned by commands (e.g. companyId, eventId)
    private final Map<String, Integer> idMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(InitialStateExecutor.class);

    private final UserLoginService userLoginService;
    private final UserService userService;
    private final ProductionCompanyService productionCompanyService;
    private final CompanyHierarchyService companyHierarchyService;

    // email -> token, populated as login commands are executed
    private final Map<String, String> tokenMap = new HashMap<>();

    public InitialStateExecutor(
            UserLoginService userLoginService,
            UserService userService,
            ProductionCompanyService productionCompanyService,
            CompanyHierarchyService companyHierarchyService, EventService eventService,
            LotteryPolicyService lotteryPolicyService,
            DiscountPolicyService discountPolicyService) {
        this.userLoginService = userLoginService;
        this.userService = userService;
        this.productionCompanyService = productionCompanyService;
        this.companyHierarchyService = companyHierarchyService;
        this.eventService = eventService;
        this.lotteryPolicyService = lotteryPolicyService;
        this.discountPolicyService = discountPolicyService;
    }

    public void execute(String filePath) {
        logger.info("InitialStateExecutor.execute: Reading initial state file: {}", filePath);
        tokenMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                logger.info("InitialStateExecutor.execute: Line {}: {}", lineNumber, line);
                executeLine(line, lineNumber);
            }
        } catch (IOException e) {
            throw new SystemStartupException("Failed to read initial state file: " + filePath, e);
        }
        logger.info("InitialStateExecutor.execute: Initial state file executed successfully.");
    }

    private void executeLine(String line, int lineNumber) {
        if (!line.endsWith(";"))
            throw new SystemStartupException("Line " + lineNumber + ": missing semicolon: " + line, null);

        line = line.substring(0, line.length() - 1).trim();
        int parenOpen = line.indexOf('(');
        int parenClose = line.lastIndexOf(')');

        if (parenOpen == -1 || parenClose == -1 || parenClose < parenOpen)
            throw new SystemStartupException("Line " + lineNumber + ": invalid syntax: " + line, null);

        String command = line.substring(0, parenOpen).trim();
        String argsStr = line.substring(parenOpen + 1, parenClose).trim();
        List<String> args = parseArgs(argsStr);

        dispatch(command, args, lineNumber);
    }

    private List<String> parseArgs(String argsStr) {
        List<String> args = new ArrayList<>();
        if (argsStr.isEmpty()) return args;
        for (String arg : argsStr.split(",")) {
            args.add(arg.trim());
        }
        return args;
    }

    // If arg is a known email with a stored token, return the token.
    // Otherwise return the arg as-is.
    private String resolveToken(String arg) {
        return tokenMap.getOrDefault(arg, arg);
    }

    private String getGuestToken() {
        Result<String> res = userLoginService.ensureGuestSession(null);
        if (!res.isSuccess())
            throw new SystemStartupException("Failed to create guest session for initial state", null);
        return res.getValue();
    }

    private void dispatch(String command, List<String> args, int lineNumber) {
        try {
            switch (command) {

                case "guest-registration" -> {
                    // guest-registration(email, password);
                    require(args, 2, command, lineNumber);
                    Result<?> res = userService.registerUser(args.get(0), args.get(1));
                    checkResult(res, command, lineNumber);
                }

                case "login" -> {
                    // login(email, password);
                    require(args, 2, command, lineNumber);
                    String guestToken = getGuestToken();
                    Result<String> res = userLoginService.loginMember(
                            args.get(0), args.get(1), guestToken);
                    checkResult(res, command, lineNumber);
                    tokenMap.put(args.get(0), res.getValue());
                    logger.info("InitialStateExecutor: Stored token for {}", args.get(0));
                }

                case "open-production-company" -> {
                    // open-production-company(email, companyName);
                    require(args, 2, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    Result<?> res = productionCompanyService.createProductionCompany(token, args.get(1));
                    checkResult(res, command, lineNumber);
                }

                case "appoint-manager" -> {
                    // appoint-manager(email, companyId, targetEmail, PERMISSION1, PERMISSION2, ...);
                    // at least 4 args required (email, companyId, targetEmail, at least one permission)
                    if (args.size() < 4)
                        throw new SystemStartupException(
                                "Line " + lineNumber + ": 'appoint-manager' needs at least 4 args", null);
                    String token = resolveToken(args.get(0));
                    int companyId = Integer.parseInt(args.get(1));
                    Set<ManagerPermissions> perms = new HashSet<>();
                    for (int i = 3; i < args.size(); i++) {
                        perms.add(ManagerPermissions.valueOf(args.get(i)));
                    }
                    Result<?> res = companyHierarchyService.assignManagerToCompany(
                            companyId, args.get(2), perms, token);
                    checkResult(res, command, lineNumber);
                }

                case "appoint-owner" -> {
                    // appoint-owner(email, companyId, targetEmail);
                    require(args, 3, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int companyId = Integer.parseInt(args.get(1));
                    Result<?> res = companyHierarchyService.assignOwnerToCompany(
                            companyId, args.get(2), token);
                    checkResult(res, command, lineNumber);
                }

                case "accept-invite" -> {
                    // accept-invite(email, companyId, assignerEmail);
                    require(args, 3, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int companyId = Integer.parseInt(args.get(1));
                    Result<?> res = companyHierarchyService.acceptInviteToCompany(
                            companyId, args.get(2), token);
                    checkResult(res, command, lineNumber);
                }

                case "create-event" -> {
                    // create-event(email, venueId, name, artist, category, companyId);
                    require(args, 6, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    LocalDateTime start = LocalDateTime.now().plusDays(7);
                    LocalDateTime end = start.plusHours(3);
                    EventRecord record = new EventRecord(
                            args.get(1),           // venueId
                            args.get(2),           // name
                            start, end,
                            args.get(3),           // artist
                            args.get(4),           // category
                            Integer.parseInt(args.get(5)), // companyId
                            0.0);
                    Result<EventDTO> res = eventService.createEvent(record, token);
                    checkResult(res, command, lineNumber);
                    // store the event ID so later commands can reference it by name
                    idMap.put(args.get(2), res.getValue().getEventID());
                    logger.info("InitialStateExecutor: Created event '{}' with ID {}", args.get(2), res.getValue().getEventID());
                }

                case "set-prices" -> {
                    // set-prices(email, eventName, segmentId, price);
                    require(args, 4, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int eventId = resolveId(args.get(1), lineNumber);
                    Map<String, Double> prices = new HashMap<>();
                    prices.put(args.get(2), Double.parseDouble(args.get(3)));
                    Result<?> res = eventService.addEventPrices(eventId, prices, token);
                    checkResult(res, command, lineNumber);
                }

                case "activate-event" -> {
                    // activate-event(email, eventName);
                    require(args, 2, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int eventId = resolveId(args.get(1), lineNumber);
                    Result<?> res = eventService.activateEvent(eventId, token);
                    checkResult(res, command, lineNumber);
                }

                case "add-company-discount" -> {
                    // add-company-discount(email, companyId, type, percentage, couponCode);
                    require(args, 5, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int companyId = Integer.parseInt(args.get(1));
                    DiscountPolicyTypes type = DiscountPolicyTypes.valueOf(args.get(2));
                    DiscountPolicyRecord record = new DiscountPolicyRecord(
                            type,                                     // type enum
                            Double.parseDouble(args.get(3)),          // discountPercentage
                            null, null, null, null,                   // minTickets, maxTickets, startDate, endDate
                            args.get(4),                              // couponCode
                            LocalDateTime.now().plusYears(1),         // expiryDate — 1 year from now
                            null,                                     // maxUsages
                            null, null);                              // left, right
                    Result<?> res = discountPolicyService.createCompanyDiscountPolicy(token, companyId, record);
                    checkResult(res, command, lineNumber);
                }

                case "create-lottery" -> {
                    // create-lottery(email, eventName, lotteryId, lotteryName, winnerAmount, minutesFromNow);
                    require(args, 6, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    int eventId = resolveId(args.get(1), lineNumber);
                    int lotteryId = Integer.parseInt(args.get(2));
                    String lotteryName = args.get(3);
                    int winnerAmount = Integer.parseInt(args.get(4));
                    int minutes = Integer.parseInt(args.get(5));
                    LocalDateTime dueDate = LocalDateTime.now().plusMinutes(minutes);
                    Result<?> res = lotteryPolicyService.createLotteryPolicy(
                            eventId, lotteryId, lotteryName, winnerAmount, dueDate, token);
                    checkResult(res, command, lineNumber);
                }

                case "logout" -> {
                    // logout(email);
                    require(args, 1, command, lineNumber);
                    String token = resolveToken(args.get(0));
                    Result<?> res = userLoginService.logOutMember(token);
                    checkResult(res, command, lineNumber);
                }

                default -> throw new SystemStartupException(
                        "Line " + lineNumber + ": unknown command: " + command, null);
            }
        } catch (SystemStartupException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemStartupException(
                    "Line " + lineNumber + ": '" + command + "' threw: " + e.getMessage(), e);
        }
    }

    private void checkResult(Result<?> res, String command, int lineNumber) {
        if (!res.isSuccess())
            throw new SystemStartupException(
                    "Line " + lineNumber + ": '" + command + "' failed: " + res.getError(), null);
    }

    private void require(List<String> args, int count, String command, int lineNumber) {
        if (args.size() != count)
            throw new SystemStartupException(
                    "Line " + lineNumber + ": '" + command + "' needs " + count + " args, got " + args.size(), null);
    }

    private int resolveId(String name, int lineNumber) {
        if (idMap.containsKey(name)) return idMap.get(name);
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            throw new SystemStartupException(
                    "Line " + lineNumber + ": cannot resolve ID for '" + name + "'", null);
        }
    }
}