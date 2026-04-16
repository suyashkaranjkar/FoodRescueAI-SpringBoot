package com.example.FoodProject.Service;

import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Model.NGO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RuleEngineService {

    public boolean isWithinPreferredDistance(NGO ngo, Form hotel) {
        if (ngo == null || hotel == null) {
            return false;
        }
        if (ngo.getPreferredDistanceKm() == null || hotel.getDistanceInKm() == null) {
            return true;
        }
        return hotel.getDistanceInKm() <= ngo.getPreferredDistanceKm();
    }

    public boolean canAcceptFood(NGO ngo, Food food) {
        if (ngo == null || food == null) {
            return false;
        }
        if (ngo.getCapacity() == null || food.getQuantity() == null) {
            return true;
        }
        return ngo.getCapacity() >= food.getQuantity();
    }

    public int scoreFoodForNgo(Food food, Form hotel, NGO ngo) {
        int score = 0;
        long expiryDays = daysUntilExpiry(food);
        if (expiryDays >= 0 && expiryDays < 2) {
            score += 50;
        } else if (expiryDays >= 0 && expiryDays <= 5) {
            score += 20;
        }

        if (hotel != null && hotel.getDistanceInKm() != null) {
            if (ngo != null && ngo.getPreferredDistanceKm() != null && hotel.getDistanceInKm() <= ngo.getPreferredDistanceKm()) {
                score += 40;
            }
            score += Math.max(0, 50 - hotel.getDistanceInKm());
        }

        if (food.getFoodType() != null && food.getFoodType().equalsIgnoreCase("cooked")) {
            score += 30;
        }

        if (ngo != null && ngo.getCapacity() != null && food.getQuantity() != null) {
            score += Math.min(20, Math.max(0, ngo.getCapacity() - food.getQuantity()));
        }

        return score;
    }

    private long daysUntilExpiry(Food food) {
        if (food == null || food.getExpiryDate() == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), food.getExpiryDate());
    }
}
