package com.example.FoodProject.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/send")
public class MailController {

@Autowired
    private JavaMailSender mailSender;

    @GetMapping("/mail-form/{name}/{email}")
    public String openMailForm(@PathVariable String email,
                                @PathVariable String name, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("name", name);
        return "Mailsend";
    }

    @PostMapping("/send-mail")
    public String sendMail(@RequestParam String name,
                            @RequestParam String to,
                           @RequestParam String subject,
                           @RequestParam String message, Model model) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            String body = "Dear " + name + ",\n\n" + message;
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);

            mailSender.send(mail);
            model.addAttribute("success", "Email sent successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send email: " + e.getMessage());
        }
        return "Mailsend";
    }

}
