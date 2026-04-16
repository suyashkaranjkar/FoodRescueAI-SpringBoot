package com.example.FoodProject.Controller;


import com.example.FoodProject.Model.Form;
import com.example.FoodProject.Repository.RegRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/form")

public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private RegRepository regRepository;

    @GetMapping("/show")
    public String showForm() {
        return "HomePage";
    }

    @GetMapping("/hotel/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("jk", new Form());
        return "RegForm";
    }

    @PostMapping("/submit")
    public String submitForm(@Valid @ModelAttribute Form form, BindingResult result, @RequestParam("file") MultipartFile file, Model model) {
        if (result.hasErrors()) {
            return "RegForm";
        }
        try {
            storeUploadedFile(form, file);
            regRepository.save(form);
            model.addAttribute("submitted", true);
            model.addAttribute("form", form);
        } catch (Exception e) {
            logger.warn("File upload failed while submitting hotel registration", e);
            model.addAttribute("message", "File upload failed !");
            return "RegForm";
        }
        return "Complete";
    }

    @GetMapping("/hotel/login")
    public String showLogin(Model model) {
        return "UserLogin";
    }

    @PostMapping("/afterlogin")
    public String afterLogin(@RequestParam String permid, @RequestParam String mob, Model model, HttpSession session) {
        Form form = regRepository.findByPermidAndMob(permid, mob);
        if (form != null) {
            session.setAttribute("form", form);
            return "redirect:/form/hotel/details";
        } else {
            model.addAttribute("error", "Invalid Credentials");
            return "UserLogin";
        }
    }

    @GetMapping("/hotel/details")
    public String welcome(Model model, HttpSession session) {
        Form form = currentForm(session);
        if (form == null) {
            return "redirect:/form/hotel/login";
        }

        model.addAttribute("form", form);
        return "UserProfile";
    }

    @GetMapping("/edit")
    public String editPage(Model model, HttpSession session) {
        Form form = currentForm(session);
        if (form == null) {
            return "redirect:/form/hotel/login";
        }

        model.addAttribute("form", form);
        return "Update";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("form") Form form, BindingResult result,
                         @RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        Form oldForm = currentForm(session);

        if (result.hasErrors()) {
            model.addAttribute("form", form);
            return "Update";
        }

        try {
            if (!storeUploadedFile(form, file) && oldForm != null) {
                form.setFilepath(oldForm.getFilepath());
                form.setFilename(oldForm.getFilename());
            }

            regRepository.save(form);
            session.setAttribute("form", form);
        } catch (Exception e) {
            logger.warn("Hotel profile update failed", e);
            model.addAttribute("message", "Update failed");
            return "Update";
        }
        return "redirect:/form/hotel/details";
    }

    private Form currentForm(HttpSession session) {
        return (Form) session.getAttribute("form");
    }

    private boolean storeUploadedFile(Form form, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return false;
        }

        Path path = Paths.get(UPLOAD_DIR).resolve(file.getOriginalFilename());
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        form.setFilepath(path.toString());
        form.setFilename(file.getOriginalFilename());
        return true;
    }
}

