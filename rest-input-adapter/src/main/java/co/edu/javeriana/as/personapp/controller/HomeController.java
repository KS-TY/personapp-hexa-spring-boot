package co.edu.javeriana.as.personapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index.html";
    }
    
    @GetMapping("/home")
    public String homeAlias() {
        return "index.html";
    }
}