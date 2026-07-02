package com.group16b.ApplicationLayer.Interfaces;

import java.util.Collection;

public interface IBusinessNotificationService {
    void generic(String userID, String message);
    void genericMany(Collection<String> userIDs, String message);

    void orderCompleted(String userID, String orderID);
    void orderCancelled(String userID, String orderID);

    void companyOwnerInvite(String targetID, int companyID, String companyName, String inviterID);
    void companyManagerInvite(String targetID, int companyID, String companyName, String inviterID);
    void companyInviteAccepted(String assignerID, String targetID, int companyID, String companyName);
    void companyInviteRejected(String assignerID, String targetID, int companyID, String companyName);
    void companyMembershipRemoved(String targetID, int companyID, String companyName);
    void companyPermissionsChanged(String targetID, int companyID, String companyName);

    void eventCreated(String userID, int eventID, String eventName);
    void eventActivated(String userID, int eventID, String eventName);
    void eventDeactivated(String userID, int eventID, String eventName);

    void lotteryEnrolled(String userID, int eventID);
    void lotteryResultsHandled(String userID, int eventID);
}