package com.group16b.ApplicationLayer.DTOs;

public class AdminRegisterRequestDTO {
    private String newAdminID;
    private String newAdminUsername;
    private String newAdminPassword;
    private String newAdminEmail;
    
    public String getNewAdminID(){
        return newAdminID;
    }
    public void setNewAdminID(String newAdminID){
        this.newAdminID = newAdminID;
    }

    public String getNewAdminUsername(){
        return newAdminUsername;
    }
    public void setNewAdminUsername(String newAdminUsername){
        this.newAdminUsername = newAdminUsername;
    }

    public String getNewAdminPassword(){
        return newAdminPassword;
    }
    public void setNewAdminPassword(String newAdminPassword){
        this.newAdminPassword = newAdminPassword;
    }

    public String getNewAdminEmail(){
        return newAdminEmail;
    }
    public void setNewAdminEmail(String newAdminEmail){
        this.newAdminEmail = newAdminEmail;
    }
}
