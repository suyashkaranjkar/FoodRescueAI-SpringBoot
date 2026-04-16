package com.example.FoodProject.Controller;

import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.FoodStatus;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Model.NGO;
import com.example.FoodProject.Repository.FoodRepository;
import com.example.FoodProject.Repository.NGORepository;
import com.example.FoodProject.Repository.RegRepository;
import com.example.FoodProject.Service.FoodNotificationService;
import com.example.FoodProject.Service.RuleEngineService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailFlowControllerTest {

    @Mock
    private NGORepository ngoRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private RegRepository regRepository;

    @Mock
    private RuleEngineService ruleEngineService;

    @Mock
    private FoodNotificationService foodNotificationService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private NGOController ngoController;

    @InjectMocks
    private FoodController foodController;

    @Test
    void ngoAccept_shouldSendHotelNotificationMail() {
        NGO ngo = new NGO();
        ngo.setName("Helping NGO");
        ngo.setEmail("ngo@example.com");
        ngo.setMob("9999999999");
        ngo.setAddress("NGO Street");
        ngo.setCity("Pune");

        Food food = new Food();
        food.setId(1L);
        food.setPermid("H1");
        food.setStatus(FoodStatus.SAFE_FOR_DONATION);

        Form hotel = new Form();
        hotel.setPermid("H1");
        hotel.setEmail("hotel@example.com");

        when(session.getAttribute("ngo")).thenReturn(ngo);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));
        when(regRepository.findByPermid("H1")).thenReturn(hotel);

        String result = ngoController.accept(1L, session);

        assertEquals("redirect:/ngo/dashboard", result);
        verify(foodRepository).save(food);
        verify(foodNotificationService).sendFoodAcceptedNotification(hotel, ngo, food);
    }

    @Test
    void hotelDeliver_shouldSendNgoNotificationMail() {
        Form hotelUser = new Form();
        hotelUser.setPermid("H1");
        hotelUser.setEmail("hotel@example.com");

        Food food = new Food();
        food.setId(5L);
        food.setPermid("H1");
        food.setStatus(FoodStatus.CLAIMED);
        food.setAcceptedNgoEmail("ngo@example.com");

        when(session.getAttribute("form")).thenReturn(hotelUser);
        when(foodRepository.findById(5L)).thenReturn(Optional.of(food));

        String result = foodController.deliverFood(5L, session);

        assertEquals("redirect:/form/hotel/food", result);
        verify(foodRepository).save(food);
        verify(foodNotificationService).sendFoodDeliveredNotificationToNgo(hotelUser, food);
    }

    @Test
    void hotelDeliver_shouldNotSendMail_whenFoodNotClaimed() {
        Form hotelUser = new Form();
        hotelUser.setPermid("H1");

        Food food = new Food();
        food.setId(7L);
        food.setPermid("H1");
        food.setStatus(FoodStatus.SAFE_FOR_DONATION);

        when(session.getAttribute("form")).thenReturn(hotelUser);
        when(foodRepository.findById(7L)).thenReturn(Optional.of(food));

        foodController.deliverFood(7L, session);

        verify(foodNotificationService, never()).sendFoodDeliveredNotificationToNgo(any(), any());
    }
}
