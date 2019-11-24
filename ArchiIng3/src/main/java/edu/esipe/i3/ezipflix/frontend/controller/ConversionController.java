package edu.esipe.i3.ezipflix.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConversionController {

    @GetMapping("/")
    public String greeting() {
        return "index";
    }

}