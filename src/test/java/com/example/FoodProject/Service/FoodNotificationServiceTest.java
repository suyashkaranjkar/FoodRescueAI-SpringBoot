package com.example.FoodProject.Service;

import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Model.NGO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FoodNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private FoodNotificationService foodNotificationService;

    @Test
    void sendFoodAcceptedNotification_sendsEmailToHotel() {
        Form hotel = new Form();
        hotel.setName("Hotel One");
        hotel.setEmail("hotel@example.com");

        NGO ngo = new NGO();
        ngo.setName("Helping NGO");
        ngo.setEmail("ngo@example.com");
        ngo.setMob("9999999999");

        Food food = new Food();
        food.setFoodName("Biryani");
        food.setFoodType("Veg");

        foodNotificationService.sendFoodAcceptedNotification(hotel, ngo, food);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertEquals("hotel@example.com", sent.getTo()[0]);
        assertTrue(sent.getSubject().contains("accepted by NGO"));
        assertTrue(sent.getText().contains("Accepted NGO details"));
    }

    @Test
    void sendFoodDeliveredNotificationToNgo_sendsEmailToAcceptedNgo() {
        Form hotel = new Form();
        hotel.setName("Hotel One");
        hotel.setEmail("hotel@example.com");

        Food food = new Food();
        food.setFoodName("Rice");
        food.setAcceptedNgoName("Helping NGO");
        food.setAcceptedNgoEmail("ngo@example.com");

        foodNotificationService.sendFoodDeliveredNotificationToNgo(hotel, food);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertEquals("ngo@example.com", sent.getTo()[0]);
        assertTrue(sent.getSubject().contains("marked as delivered"));
        assertTrue(sent.getText().contains("Hotel details"));
    }

    @Test
    void sendFoodDeliveredNotificationToNgo_skipsWhenNgoEmailMissing() {
        Food food = new Food();
        food.setFoodName("Rice");
        food.setAcceptedNgoEmail("");

        foodNotificationService.sendFoodDeliveredNotificationToNgo(new Form(), food);

        verifyNoInteractions(mailSender);
    }
}
