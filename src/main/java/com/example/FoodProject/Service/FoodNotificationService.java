package com.example.FoodProject.Service;

import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Model.NGO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class FoodNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public FoodNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendFoodAcceptedNotification(Form hotel, NGO ngo, Food food) {
        if (hotel == null || hotel.getEmail() == null || hotel.getEmail().isBlank()) {
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(hotel.getEmail());
        mail.setSubject("Food Claim Update: " + safe(food.getFoodName()) + " accepted by NGO");
        if (fromEmail != null && !fromEmail.isBlank()) {
            mail.setFrom(fromEmail);
        }

        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(safe(hotel.getName())).append(",\n\n");
        message.append("Your food donation has been accepted by an NGO.\n\n");

        message.append("Food details:\n");
        message.append("- Food Name: ").append(safe(food.getFoodName())).append("\n");
        message.append("- Food Type: ").append(safe(food.getFoodType())).append("\n");
        message.append("- Quantity: ").append(food.getQuantity() == null ? "N/A" : food.getQuantity())
                .append(" ").append(safe(food.getQuantityUnit())).append("\n");
        message.append("- Cooked When: ").append(food.getCookedWhen() == null ? "N/A" : food.getCookedWhen()).append("\n");
        message.append("- Fresh Until: ").append(food.getFreshUntil() == null ? "N/A" : food.getFreshUntil()).append("\n");
        message.append("- Current Status: ").append(food.getStatus() == null ? "N/A" : food.getStatus()).append("\n\n");

        message.append("Accepted NGO details:\n");
        message.append("- NGO Name: ").append(safe(ngo.getName())).append("\n");
        message.append("- NGO Contact Number: ").append(safe(ngo.getMob())).append("\n");
        message.append("- NGO Email: ").append(safe(ngo.getEmail())).append("\n");
        message.append("- NGO Address: ").append(safe(ngo.getAddress())).append("\n");
        message.append("- NGO City: ").append(safe(ngo.getCity())).append("\n\n");

        message.append("You can now contact the NGO directly for pickup and delivery coordination.\n\n");
        message.append("Regards,\nFoodRescueAI Team");

        mail.setText(message.toString());
        mailSender.send(mail);
    }

    public void sendFoodDeliveredNotificationToNgo(Form hotel, Food food) {
        String ngoEmail = food.getAcceptedNgoEmail();
        if (ngoEmail == null || ngoEmail.isBlank()) {
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(ngoEmail);
        mail.setSubject("Delivery Update: " + safe(food.getFoodName()) + " marked as delivered");
        if (fromEmail != null && !fromEmail.isBlank()) {
            mail.setFrom(fromEmail);
        }

        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(safe(food.getAcceptedNgoName())).append(",\n\n");
        message.append("The hotel has marked your accepted food as delivered.\n\n");

        appendCommonFoodDetails(message, food);

        message.append("Hotel details:\n");
        message.append("- Hotel Name: ").append(safe(hotel == null ? null : hotel.getName())).append("\n");
        message.append("- Hotel Contact Number: ").append(safe(hotel == null ? null : hotel.getMob())).append("\n");
        message.append("- Hotel Email: ").append(safe(hotel == null ? null : hotel.getEmail())).append("\n");
        message.append("- Hotel Address: ").append(safe(hotel == null ? null : hotel.getAddress())).append("\n");
        message.append("- Hotel City: ").append(safe(hotel == null ? null : hotel.getCity())).append("\n\n");

        message.append("Regards,\nFoodRescueAI Team");

        mail.setText(message.toString());
        mailSender.send(mail);
    }

    public void sendFoodRejectedNotificationToNgo(Form hotel, Food food, String rejectionReason) {
        String ngoEmail = food.getAcceptedNgoEmail();
        if (ngoEmail == null || ngoEmail.isBlank()) {
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(ngoEmail);
        mail.setSubject("Claim Update: " + safe(food.getFoodName()) + " was rejected by hotel");
        if (fromEmail != null && !fromEmail.isBlank()) {
            mail.setFrom(fromEmail);
        }

        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(safe(food.getAcceptedNgoName())).append(",\n\n");
        message.append("The hotel has rejected this accepted food request.\n\n");

        appendCommonFoodDetails(message, food);

        message.append("Rejection reason provided by hotel:\n");
        message.append("- Reason: ").append(safe(rejectionReason)).append("\n\n");

        message.append("Hotel details:\n");
        message.append("- Hotel Name: ").append(safe(hotel == null ? null : hotel.getName())).append("\n");
        message.append("- Hotel Contact Number: ").append(safe(hotel == null ? null : hotel.getMob())).append("\n");
        message.append("- Hotel Email: ").append(safe(hotel == null ? null : hotel.getEmail())).append("\n");
        message.append("- Hotel Address: ").append(safe(hotel == null ? null : hotel.getAddress())).append("\n");
        message.append("- Hotel City: ").append(safe(hotel == null ? null : hotel.getCity())).append("\n\n");

        message.append("Regards,\nFoodRescueAI Team");

        mail.setText(message.toString());
        mailSender.send(mail);
    }

    private void appendCommonFoodDetails(StringBuilder message, Food food) {
        message.append("Food details:\n");
        message.append("- Food Name: ").append(safe(food.getFoodName())).append("\n");
        message.append("- Food Type: ").append(safe(food.getFoodType())).append("\n");
        message.append("- Quantity: ").append(food.getQuantity() == null ? "N/A" : food.getQuantity())
                .append(" ").append(safe(food.getQuantityUnit())).append("\n");
        message.append("- Cooked When: ").append(food.getCookedWhen() == null ? "N/A" : food.getCookedWhen()).append("\n");
        message.append("- Fresh Until: ").append(food.getFreshUntil() == null ? "N/A" : food.getFreshUntil()).append("\n");
        message.append("- Current Status: ").append(food.getStatus() == null ? "N/A" : food.getStatus()).append("\n\n");
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value;
    }
}
