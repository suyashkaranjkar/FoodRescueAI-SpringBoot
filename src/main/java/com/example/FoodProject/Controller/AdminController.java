package com.example.FoodProject.Controller;

import com.example.FoodProject.Repository.RegRepository;
import com.example.FoodProject.Repository.FoodRepository;
import com.example.FoodProject.Repository.NGORepository;
import com.example.FoodProject.Model.Food;
import com.example.FoodProject.Model.FoodStatus;
import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Model.NGO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/set")
public class AdminController {

    @Autowired
    private RegRepository regRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private NGORepository ngoRepository;

    @GetMapping("/login")
    public String adminLogin()
    {
        return "AdminLogin";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (username.equalsIgnoreCase("admin") && password.equals("admin123")) {
            session.setAttribute("form", "FORM");
            return "redirect:/set/dashboard";
        } else {
            model.addAttribute("error", "Invalid Admin Credentials");
            return "AdminLogin";
        }
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdminAuthenticated(session)) {
            return "redirect:/set/login";
        }

        model.addAttribute("form", regRepository.findAll());
        model.addAttribute("ngos", ngoRepository.findAll());
        return "AdminDash";
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) throws IOException
    {
        Path filePath = Paths.get("uploads").resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if(!resource.exists())
        {
            throw new RuntimeException("File not found or not readable");
        }
        String contentType= Files.probeContentType(filePath);
        if(contentType == null)
        {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return "redirect:/set/login";
        }
        regRepository.deleteById(id);
        return "redirect:/set/dashboard";
    }

    @GetMapping("/delete-ngo/{id}")
    public String deleteNGO(@PathVariable Long id, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return "redirect:/set/login";
        }
        ngoRepository.deleteById(id);
        return "redirect:/set/dashboard";
    }

     @GetMapping("/logout")
     public  String adminLogout(HttpSession session)
     {
         session.invalidate();
         return "redirect:/set/login";
     }

     @GetMapping("/hotel-food/{permid}")
     public String viewHotelFood(@PathVariable String permid, HttpSession session, Model model) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }

         model.addAttribute("foodList", foodRepository.findByPermid(permid));
         model.addAttribute("permid", permid);
         return "AdminFoodView";
     }

     @GetMapping("/hotel-food/edit/{id}")
     public String editHotelFood(@PathVariable Long id, HttpSession session, Model model) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }

         Food food = foodRepository.findById(id).orElse(null);
         if (food == null) {
             return "redirect:/set/dashboard";
         }

         model.addAttribute("food", food);
         model.addAttribute("statusLocked", food.getStatus() == FoodStatus.CLAIMED || food.getStatus() == FoodStatus.DELIVERED);
         return "AdminFoodEdit";
     }

     @PostMapping("/hotel-food/update/{id}")
     public String updateHotelFood(@PathVariable Long id,
                                   @ModelAttribute Food editedFood,
                                   HttpSession session,
                                   Model model) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }

         Food existingFood = foodRepository.findById(id).orElse(null);
         if (existingFood == null) {
             return "redirect:/set/dashboard";
         }

         existingFood.setFoodName(editedFood.getFoodName());
         existingFood.setFoodType(editedFood.getFoodType());
         existingFood.setQuantity(editedFood.getQuantity());
         existingFood.setQuantityUnit(editedFood.getQuantityUnit());
         existingFood.setCookedWhen(editedFood.getCookedWhen());
         existingFood.setFreshUntil(editedFood.getFreshUntil());
         existingFood.setExpiryDate(editedFood.getFreshUntil());

         // Preserve workflow state for already claimed or delivered foods.
         if (existingFood.getStatus() != FoodStatus.CLAIMED && existingFood.getStatus() != FoodStatus.DELIVERED) {
             if (editedFood.getFreshUntil() != null && editedFood.getFreshUntil().isBefore(LocalDate.now())) {
                 existingFood.setStatus(FoodStatus.UNSAFE_FOR_DONATION);
             } else {
                 existingFood.setStatus(FoodStatus.SAFE_FOR_DONATION);
             }
         }

         existingFood.setUpdatedAt(java.time.LocalDateTime.now());
         foodRepository.save(existingFood);

         String permid = existingFood.getPermid();
         if (permid == null || permid.isBlank()) {
             return "redirect:/set/dashboard";
         }
         return "redirect:/set/hotel-food/" + permid;
     }

     @GetMapping("/ngo-food/{id}")
     public String viewNGOFood(@PathVariable Long id, HttpSession session, Model model) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }

         NGO ngo = ngoRepository.findById(id).orElse(null);
         if (ngo == null) {
             return "redirect:/set/dashboard";
         }

         List<Food> acceptedFoods = foodRepository.findByAcceptedNgoName(ngo.getName());

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
         return "AdminNGOFoods";
     }

     @GetMapping("/delete-food/{id}")
     public String deleteFood(@PathVariable Long id, HttpSession session) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }
         foodRepository.deleteById(id);
         return "redirect:/set/dashboard"; // or back to the ngo-food page, but since we don't know ngo id, redirect to dashboard
     }

     @GetMapping("/undo-food/{id}")
     public String undoFood(@PathVariable Long id, HttpSession session) {
         if (!isAdminAuthenticated(session)) {
             return "redirect:/set/login";
         }
         Food food = foodRepository.findById(id).orElse(null);
         if (food != null) {
             food.setStatus(FoodStatus.SAFE_FOR_DONATION);
             food.setAcceptedByNgo(null);
             food.setAcceptedNgoName(null);
             food.setAcceptedNgoMobile(null);
             food.setAcceptedNgoEmail(null);
             food.setAcceptedNgoAddress(null);
             food.setAcceptedNgoCity(null);
             food.setHotelRejectionReason(null);
             foodRepository.save(food);
         }
         return "redirect:/set/dashboard";
     }

    @GetMapping("/search/hotels")
    @ResponseBody
    public List<Form> searchHotels(@RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return regRepository.findAll();
        }
        return regRepository.searchHotels(keyword.trim());
    }

    @GetMapping("/search/ngos")
    @ResponseBody
    public List<NGO> searchNgos(@RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ngoRepository.findAll();
        }
        return ngoRepository.searchNgos(keyword.trim());
    }

    private boolean isAdminAuthenticated(HttpSession session) {
        return session.getAttribute("form") != null;
    }

}
