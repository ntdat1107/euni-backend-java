package com.euni.backend.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    DISABLED("Disabled");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public static UserStatus fromString(String text) {
        for (UserStatus b : UserStatus.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return PENDING;
    }
}
