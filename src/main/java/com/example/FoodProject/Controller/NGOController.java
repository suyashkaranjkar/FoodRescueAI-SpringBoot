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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/ngo")
public class NGOController {

    private static final Logger logger = LoggerFactory.getLogger(NGOController.class);

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private RegRepository regRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private FoodNotificationService foodNotificationService;

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("ngo", new NGO());
        return "NGORegister";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute NGO ngo, Model model) {
        // Check if email already exists
        if (ngoRepository.findByEmailAndPassword(ngo.getEmail(), null) != null) {
            model.addAttribute("error", "Email already registered");
            return "NGORegister";
        }
        ngo.setVerified(true); // For demo, auto-verify
        if (ngo.getPreferredDistanceKm() == null) {
            ngo.setPreferredDistanceKm(10);
        }
        if (ngo.getCapacity() == null) {
            ngo.setCapacity(100);
        }
        ngoRepository.save(ngo);

        model.addAttribute("message", "Registration successful! Please login.");
        return "NGOLogin";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "NGOLogin";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        NGO ngo = ngoRepository.findByEmailAndPassword(email, password);
        if (ngo != null && ngo.isVerified()) {
            session.setAttribute("ngo", ngo);
            return "redirect:/ngo/dashboard";
        } else {
            model.addAttribute("error", "Invalid credentials or not verified");
            return "NGOLogin";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }
        // Get all safe foods
        List<Food> safeFoods = foodRepository.findByStatus(FoodStatus.SAFE_FOR_DONATION);
        List<Food> nearbyFoods = new ArrayList<>();

        for (Food food : safeFoods) {
            Form hotel = regRepository.findByPermid(food.getPermid());
            if (hotel == null) {
                continue;
            }
            boolean inSameCity = hotel.getCity() != null && hotel.getCity().equalsIgnoreCase(ngo.getCity());
            boolean withinDistance = ruleEngineService.isWithinPreferredDistance(ngo, hotel);
            if (!inSameCity && !withinDistance) {
                continue;
            }
            food.setHotelName(hotel.getName());
            food.setHotelMobile(hotel.getMob());
            food.setHotelAddress(hotel.getAddress());
            food.setHotelDistanceKm(hotel.getDistanceInKm());
            nearbyFoods.add(food);
        }

        nearbyFoods.sort((food1, food2) -> {
            Form hotel1 = regRepository.findByPermid(food1.getPermid());
            Form hotel2 = regRepository.findByPermid(food2.getPermid());
            return Integer.compare(ruleEngineService.scoreFoodForNgo(food2, hotel2, ngo), ruleEngineService.scoreFoodForNgo(food1, hotel1, ngo));
        });

        List<Food> suggestedFoods = nearbyFoods.stream()
                .filter(food -> {
                    Form hotel = regRepository.findByPermid(food.getPermid());
                    return ruleEngineService.isWithinPreferredDistance(ngo, hotel);
                })
                .sorted((food1, food2) -> {
                    Form hotel1 = regRepository.findByPermid(food1.getPermid());
                    Form hotel2 = regRepository.findByPermid(food2.getPermid());
                    return Integer.compare(ruleEngineService.scoreFoodForNgo(food2, hotel2, ngo), ruleEngineService.scoreFoodForNgo(food1, hotel1, ngo));
                })
                .collect(Collectors.toList());

        List<Food> availableFoods = new ArrayList<>(nearbyFoods);
        availableFoods.removeAll(suggestedFoods);

        model.addAttribute("suggestedFoods", suggestedFoods);
        model.addAttribute("foods", availableFoods);
        model.addAttribute("ngo", ngo);
        return "NGODashboard";
    }

    @GetMapping("/accepted")
    public String acceptedFoods(HttpSession session, Model model) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }
        List<Food> acceptedFoods = foodRepository.findByAcceptedNgoNameIgnoreCase(ngo.getName());
        if (acceptedFoods == null || acceptedFoods.isEmpty()) {
            acceptedFoods = foodRepository.findByAcceptedNgoEmailIgnoreCase(ngo.getEmail());
        }

        List<Map<String, Object>> foodWithHotel = new ArrayList<>();
        for (Food food : acceptedFoods) {
            Map<String, Object> item = new HashMap<>();
            item.put("food", food);
            Form hotel = regRepository.findByPermid(food.getPermid());
            item.put("hotel", hotel);
            foodWithHotel.add(item);
        }

        model.addAttribute("ngo", ngo);
        model.addAttribute("foodWithHotel", foodWithHotel);
        return "NGOFoodStatus";
    }

    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }
        model.addAttribute("ngo", ngo);
        return "NGOEdit";
    }

    @PostMapping("/edit")
    public String updateNgo(@ModelAttribute NGO formNgo, HttpSession session, Model model) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }

        ngo.setName(formNgo.getName());
        ngo.setMob(formNgo.getMob());
        ngo.setCity(formNgo.getCity());
        ngo.setAddress(formNgo.getAddress());
        ngo.setPreferredDistanceKm(formNgo.getPreferredDistanceKm());
        ngo.setCapacity(formNgo.getCapacity());
        if (formNgo.getPassword() != null && !formNgo.getPassword().isEmpty()) {
            ngo.setPassword(formNgo.getPassword());
        }

        ngoRepository.save(ngo);
        session.setAttribute("ngo", ngo);

        model.addAttribute("ngo", ngo);
        model.addAttribute("message", "NGO details updated successfully.");
        return "NGOEdit";
    }

    @PostMapping("/accept/{id}")
    public String accept(@PathVariable Long id, HttpSession session) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }
        Food food = foodRepository.findById(id).orElse(null);
        if (food != null && food.getStatus() == FoodStatus.SAFE_FOR_DONATION) {
            Form hotel = regRepository.findByPermid(food.getPermid());

            // Assign to NGO or change status
            food.setStatus(FoodStatus.CLAIMED);
            String ngoContact = ngo.getName();
            if (ngo.getMob() != null && !ngo.getMob().isEmpty()) {
                ngoContact += " (" + ngo.getMob() + ")";
                food.setAcceptedNgoMobile(ngo.getMob());
            }
            if (ngo.getEmail() != null && !ngo.getEmail().isEmpty()) {
                ngoContact += " - " + ngo.getEmail();
                food.setAcceptedNgoEmail(ngo.getEmail());
            }
            food.setAcceptedByNgo(ngoContact);
            food.setAcceptedNgoName(ngo.getName());
            food.setAcceptedNgoAddress(ngo.getAddress());
            food.setAcceptedNgoCity(ngo.getCity());
            food.setHotelRejectionReason(null);
            foodRepository.save(food);

            try {
                foodNotificationService.sendFoodAcceptedNotification(hotel, ngo, food);
            } catch (Exception ex) {
                // Do not block claim if email delivery fails.
                logger.warn("Food accepted but failed to send hotel notification email for foodId={}: {}", id, ex.getMessage());
            }
        }
        return "redirect:/ngo/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, HttpSession session) {
        NGO ngo = currentNgo(session);
        if (ngo == null) {
            return "redirect:/ngo/login";
        }
        Food food = foodRepository.findById(id).orElse(null);
        if (food != null && food.getStatus() == FoodStatus.SAFE_FOR_DONATION) {
            food.setStatus(FoodStatus.REJECTED);
            foodRepository.save(food);
        }
        return "redirect:/ngo/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/ngo/login";
    }

    private NGO currentNgo(HttpSession session) {
        return (NGO) session.getAttribute("ngo");
    }
}