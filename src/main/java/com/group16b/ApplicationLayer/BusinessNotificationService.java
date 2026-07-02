package com.group16b.ApplicationLayer;

import com.group16b.ApplicationLayer.Interfaces.IBusinessNotificationService;
import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class BusinessNotificationService implements IBusinessNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessNotificationService.class);

    private final INotificationService notificationService;

    public BusinessNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void generic(String userID, String message) {
        safeNotify(userID, message);
    }

    @Override
    public void genericMany(Collection<String> userIDs, String message) {
        if (userIDs == null || userIDs.isEmpty()) {
            return;
        }

        for (String userID : userIDs) {
            safeNotify(userID, message);
        }
    }

    @Override
    public void orderCompleted(String userID, String orderID) {
        safeNotify(userID, "Your order " + orderID + " was completed successfully.");
    }

    @Override
    public void orderCancelled(String userID, String orderID) {
        safeNotify(userID, "Your order " + orderID + " was cancelled.");
    }

    @Override
    public void companyOwnerInvite(String targetID, int companyID, String companyName, String inviterID) {
        safeNotify(targetID, "You were invited to become an owner of " + displayCompany(companyName, companyID) + ".");
    }

    @Override
    public void companyManagerInvite(String targetID, int companyID, String companyName, String inviterID) {
        safeNotify(targetID, "You were invited to become a manager of " + displayCompany(companyName, companyID) + ".");
    }

    @Override
    public void companyInviteAccepted(String assignerID, String targetID, int companyID, String companyName) {
        safeNotify(assignerID, targetID + " accepted your invite to " + displayCompany(companyName, companyID) + ".");
    }

    @Override
    public void companyInviteRejected(String assignerID, String targetID, int companyID, String companyName) {
        safeNotify(assignerID, targetID + " rejected your invite to " + displayCompany(companyName, companyID) + ".");
    }

    @Override
    public void companyMembershipRemoved(String targetID, int companyID, String companyName) {
        safeNotify(targetID, "Your role in " + displayCompany(companyName, companyID) + " was removed.");
    }

    @Override
    public void companyPermissionsChanged(String targetID, int companyID, String companyName) {
        safeNotify(targetID, "Your permissions in " + displayCompany(companyName, companyID) + " were changed.");
    }

    @Override
    public void eventCreated(String userID, int eventID, String eventName) {
        safeNotify(userID, "Event created successfully: " + displayEvent(eventName, eventID) + ".");
    }

    @Override
    public void eventActivated(String userID, int eventID, String eventName) {
        safeNotify(userID, "Event activated: " + displayEvent(eventName, eventID) + ".");
    }

    @Override
    public void eventDeactivated(String userID, int eventID, String eventName) {
        safeNotify(userID, "Event deactivated: " + displayEvent(eventName, eventID) + ".");
    }

    @Override
    public void lotteryEnrolled(String userID, int eventID) {
        safeNotify(userID, "You enrolled in the lottery for event " + eventID + ".");
    }

    @Override
    public void lotteryResultsHandled(String userID, int eventID) {
        safeNotify(userID, "Lottery results were handled for event " + eventID + ".");
    }

    private void safeNotify(String userID, String message) {
        if (userID == null || userID.isBlank() || message == null || message.isBlank()) {
            return;
        }

        try {
            notificationService.notify(userID, message);
        } catch (Exception e) {
            logger.warn("BusinessNotificationService.safeNotify: failed to notify user {}: {}", userID, e.getMessage());
        }
    }

    private String displayCompany(String companyName, int companyID) {
        if (companyName == null || companyName.isBlank()) {
            return "company " + companyID;
        }

        return companyName + " company";
    }

    private String displayEvent(String eventName, int eventID) {
        if (eventName == null || eventName.isBlank()) {
            return "event " + eventID;
        }

        return eventName;
    }
}