package com.example.FoodProject.Controller;

import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.FoodStatus;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Repository.FoodRepository;
import com.example.FoodProject.Service.FoodNotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/form/hotel/food")
public class FoodController {
    private static final String FOOD_UPLOAD_DIR = "uploads/";
    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);
    
    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private FoodNotificationService foodNotificationService;
    
    @GetMapping
    public String showFoodList(HttpSession session, Model model) {
        Form user = currentUser(session);
        
        if (user == null) {
            return "redirect:/form/hotel/login";
        }
        
        String permid = user.getPermid();
        
        model.addAttribute("user", user);
        
        List<Food> foodList = foodRepository.findByPermid(permid);

        LocalDate today = LocalDate.now();
        for (Food food : foodList) {
            if (food.getFreshUntil() == null && food.getExpiryDate() != null) {
                food.setFreshUntil(food.getExpiryDate());
            }

            if (food.getFreshUntil() != null && food.getFreshUntil().isBefore(today)) {
                if (food.getStatus() != FoodStatus.UNSAFE_FOR_DONATION && food.getStatus() != FoodStatus.CLAIMED && food.getStatus() != FoodStatus.REJECTED && food.getStatus() != FoodStatus.DELIVERED) {
                    food.setStatus(FoodStatus.UNSAFE_FOR_DONATION);
                    food.setExpiryDate(food.getFreshUntil());
                    foodRepository.save(food);
                }
            } else {
                if (food.getStatus() == null) {
                    food.setStatus(FoodStatus.SAFE_FOR_DONATION);
                    food.setExpiryDate(food.getFreshUntil());
                    foodRepository.save(food);
                }
            }
        }

        List<Food> wasteList = foodList.stream()
                .filter(item -> item.getStatus() == FoodStatus.UNSAFE_FOR_DONATION)
                .collect(Collectors.toList());

        List<Food> inventoryList = foodList.stream()
                .filter(item -> item.getStatus() == FoodStatus.SAFE_FOR_DONATION)
                .collect(Collectors.toList());

        List<Food> pendingStatsList = foodList.stream()
                .filter(item -> item.getStatus() == FoodStatus.CLAIMED || item.getStatus() == FoodStatus.REJECTED || item.getStatus() == FoodStatus.DELIVERED)
                .collect(Collectors.toList());

        model.addAttribute("foodList", inventoryList);
        model.addAttribute("wasteList", wasteList);
        model.addAttribute("pendingStatsList", pendingStatsList);
        model.addAttribute("food", new Food());

        int claimedCount = (int) pendingStatsList.stream().filter(item -> item.getStatus() == FoodStatus.CLAIMED).count();
        int rejectedCount = (int) pendingStatsList.stream().filter(item -> item.getStatus() == FoodStatus.REJECTED).count();
        int deliveredCount = (int) pendingStatsList.stream().filter(item -> item.getStatus() == FoodStatus.DELIVERED).count();

        model.addAttribute("claimedCount", claimedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("deliveredCount", deliveredCount);

        return "FoodList";
    }

    @GetMapping("/claims")
    public String claimedFoodList(HttpSession session, Model model) {
        Form user = currentUser(session);
        if (user == null) {
            return "redirect:/form/hotel/login";
        }

        List<Food> claimedFoods = foodRepository.findByPermid(user.getPermid()).stream()
                .filter(item -> item.getStatus() == FoodStatus.CLAIMED || item.getStatus() == FoodStatus.REJECTED || item.getStatus() == FoodStatus.DELIVERED)
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
                    if (a.getUpdatedAt() == null) return 1;
                    if (b.getUpdatedAt() == null) return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("claimedFoods", claimedFoods);
        model.addAttribute("claimedCount", claimedFoods.size());

        return "FoodClaims";
    }

    @PostMapping("/add")
    public String addFood(@ModelAttribute Food food, @RequestParam(value = "photo", required = false) MultipartFile photo, HttpSession session, Model model) {
        Form user = currentUser(session);
        
        if (user == null) {
            return "redirect:/form/hotel/login";
        }

        food.setPermid(user.getPermid());

        if (photo != null && !photo.isEmpty()) {
            try {
                String filename = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                Path path = Paths.get(FOOD_UPLOAD_DIR).resolve(filename);
                Files.createDirectories(path.getParent());
                Files.write(path, photo.getBytes());
                food.setPhotoFilename(filename);
            } catch (IOException e) {
                logger.warn("Failed to store uploaded food photo for new food entry", e);
            }
        }

        if (food.getFreshUntil() != null) {
            food.setExpiryDate(food.getFreshUntil());
            if (food.getFreshUntil().isBefore(LocalDate.now())) {
                food.setStatus(FoodStatus.UNSAFE_FOR_DONATION);
            } else {
                food.setStatus(FoodStatus.SAFE_FOR_DONATION);
            }
        } else {
            food.setStatus(FoodStatus.SAFE_FOR_DONATION);
        }

        food.setCreatedAt(java.time.LocalDateTime.now());
        food.setUpdatedAt(java.time.LocalDateTime.now());

        foodRepository.save(food);
        
        return "redirect:/form/hotel/food";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteFood(@PathVariable Long id, HttpSession session) {
        Form user = currentUser(session);
        
        if (user == null) {
            return "redirect:/form/hotel/login";
        }
        
        Food food = foodRepository.findById(id).orElse(null);
        if (food != null && food.getPermid().equals(user.getPermid())) {
            foodRepository.deleteById(id);
        }
        
        return "redirect:/form/hotel/food";
    }

    @PostMapping("/deliver/{id}")
    public String deliverFood(@PathVariable Long id, HttpSession session) {
        Form user = currentUser(session);
        if (user == null) {
            return "redirect:/form/hotel/login";
        }

        Food food = foodRepository.findById(id).orElse(null);
        if (food != null && food.getPermid().equals(user.getPermid()) && food.getStatus() == FoodStatus.CLAIMED) {
            food.setStatus(FoodStatus.DELIVERED);
            food.setHotelRejectionReason(null);
            food.setUpdatedAt(java.time.LocalDateTime.now());
            foodRepository.save(food);

            try {
                foodNotificationService.sendFoodDeliveredNotificationToNgo(user, food);
            } catch (Exception ex) {
                logger.warn("Food marked delivered but NGO notification email failed for foodId={}: {}", id, ex.getMessage());
            }
        }

        return "redirect:/form/hotel/food";
    }

    @PostMapping("/ngoreject/{id}")
    public String hotelRejectClaim(@PathVariable Long id,
                                   @RequestParam("reason") String reason,
                                   HttpSession session) {
        Form user = currentUser(session);
        if (user == null) {
            return "redirect:/form/hotel/login";
        }

        Food food = foodRepository.findById(id).orElse(null);
        if (food != null && food.getPermid().equals(user.getPermid()) && food.getStatus() == FoodStatus.CLAIMED) {
            String sanitizedReason = reason == null ? "" : reason.trim();
            if (sanitizedReason.isEmpty()) {
                return "redirect:/form/hotel/food";
            }

            food.setStatus(FoodStatus.REJECTED);
            food.setHotelRejectionReason(sanitizedReason);
            food.setUpdatedAt(java.time.LocalDateTime.now());
            foodRepository.save(food);

            try {
                foodNotificationService.sendFoodRejectedNotificationToNgo(user, food, sanitizedReason);
            } catch (Exception ex) {
                logger.warn("Food rejected but NGO notification email failed for foodId={}: {}", id, ex.getMessage());
            }
        }

        return "redirect:/form/hotel/food";
    }
    
    @GetMapping("/edit/{id}")
    public String editFoodForm(@PathVariable Long id, HttpSession session, Model model) {
        Form user = currentUser(session);
        
        if (user == null) {
            return "redirect:/form/hotel/login";
        }
        
        Food food = foodRepository.findById(id).orElse(null);
        if (food == null || !food.getPermid().equals(user.getPermid())) {
            return "redirect:/form/hotel/food";
        }
        
        if (food.getFreshUntil() == null && food.getExpiryDate() != null) {
            food.setFreshUntil(food.getExpiryDate());
        }
        if (food.getStatus() == null) {
            food.setStatus(FoodStatus.SAFE_FOR_DONATION);
        }

        model.addAttribute("user", user);
        model.addAttribute("food", food);
        model.addAttribute("isEdit", true);
        
        List<Food> foodList = foodRepository.findByPermid(user.getPermid());
        List<Food> wasteList = foodList.stream()
                .filter(item -> item.getStatus() == FoodStatus.UNSAFE_FOR_DONATION)
                .collect(Collectors.toList());
        List<Food> safeList = foodList.stream()
                .filter(item -> item.getStatus() != FoodStatus.UNSAFE_FOR_DONATION)
                .collect(Collectors.toList());

        model.addAttribute("foodList", safeList);
        model.addAttribute("wasteList", wasteList);
        
        return "FoodList";
    }
    
    @PostMapping("/update/{id}")
    public String updateFood(@PathVariable Long id, @ModelAttribute Food updatedFood, @RequestParam(value = "photo", required = false) MultipartFile photo, HttpSession session) {
        Form user = currentUser(session);
        
        if (user == null) {
            return "redirect:/form/hotel/login";
        }
        
        Food existingFood = foodRepository.findById(id).orElse(null);
        if (existingFood != null && existingFood.getPermid().equals(user.getPermid())) {
            existingFood.setFoodName(updatedFood.getFoodName());
            existingFood.setFoodType(updatedFood.getFoodType());
            existingFood.setQuantity(updatedFood.getQuantity());
            existingFood.setQuantityUnit(updatedFood.getQuantityUnit());
            existingFood.setCookedWhen(updatedFood.getCookedWhen());
            existingFood.setFreshUntil(updatedFood.getFreshUntil());

            if (photo != null && !photo.isEmpty()) {
                try {
                    String filename = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
                    Path path = Paths.get(FOOD_UPLOAD_DIR).resolve(filename);
                    Files.createDirectories(path.getParent());
                    Files.write(path, photo.getBytes());
                    existingFood.setPhotoFilename(filename);
                } catch (IOException e) {
                    logger.warn("Failed to store uploaded food photo for foodId={}", id, e);
                }
            }

            if (existingFood.getFreshUntil() != null) {
                existingFood.setExpiryDate(existingFood.getFreshUntil());
                if (existingFood.getFreshUntil().isBefore(LocalDate.now())) {
                    if (existingFood.getStatus() != FoodStatus.CLAIMED && existingFood.getStatus() != FoodStatus.REJECTED && existingFood.getStatus() != FoodStatus.DELIVERED) {
                        existingFood.setStatus(FoodStatus.UNSAFE_FOR_DONATION);
                    }
                } else {
                    if (existingFood.getStatus() != FoodStatus.CLAIMED && existingFood.getStatus() != FoodStatus.REJECTED && existingFood.getStatus() != FoodStatus.DELIVERED) {
                        existingFood.setStatus(FoodStatus.SAFE_FOR_DONATION);
                    }
                }
            } else {
                if (existingFood.getStatus() != FoodStatus.CLAIMED && existingFood.getStatus() != FoodStatus.REJECTED && existingFood.getStatus() != FoodStatus.DELIVERED) {
                    existingFood.setStatus(FoodStatus.SAFE_FOR_DONATION);
                }
            }

            existingFood.setUpdatedAt(java.time.LocalDateTime.now());
            foodRepository.save(existingFood);
        }
        
        return "redirect:/form/hotel/food";
    }

    private Form currentUser(HttpSession session) {
        return (Form) session.getAttribute("form");
    }
}
