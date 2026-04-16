package com.example.FoodProject.Model;

public enum FoodStatus {
    SAFE_FOR_DONATION,
    UNSAFE_FOR_DONATION,
    PENDING,
    CLAIMED,
    REJECTED,
    DELIVERED;

    public static FoodStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase()
                .replace(' ', '_')
                .replace("FOR_", "FOR_");
        try {
            return FoodStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            if ("SAFE FOR DONATION".equalsIgnoreCase(value)) {
                return SAFE_FOR_DONATION;
            } else if ("UNSAFE FOR DONATION".equalsIgnoreCase(value)) {
                return UNSAFE_FOR_DONATION;
            } else if ("ACCEPTED".equalsIgnoreCase(value) || value.toUpperCase().startsWith("ACCEPTED")) {
                return CLAIMED;
            } else if ("REJECTED".equalsIgnoreCase(value) || value.toUpperCase().startsWith("REJECTED")) {
                return REJECTED;
            }
            return PENDING;
        }
    }
}
