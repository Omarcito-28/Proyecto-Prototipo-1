package com.essencedetoi.dto;

import java.util.List;

public class UserRoleUpdateDto {

    private List<Long> roleIds;
    private boolean enabled;

    // Getters and Setters
    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
