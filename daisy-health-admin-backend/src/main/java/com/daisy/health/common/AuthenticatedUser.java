package com.daisy.health.common;

public class AuthenticatedUser {
    private final Long accountId;
    private final String roleType;
    private final String phone;

    public AuthenticatedUser(Long accountId, String roleType, String phone) {
        this.accountId = accountId;
        this.roleType = roleType;
        this.phone = phone;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getRoleType() {
        return roleType;
    }

    public String getPhone() {
        return phone;
    }
}
