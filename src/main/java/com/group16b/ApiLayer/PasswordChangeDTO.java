package com.group16b.ApiLayer;

public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;

    //why is it in the api layer? shoudlnt it be wih all of its friends in ApplicationLayer/DTOs?
    public PasswordChangeDTO() {
    }

    public PasswordChangeDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
