package edu.esipe.i3.ezipflix.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping(value = {"/ui"})
public class ConversionController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

}