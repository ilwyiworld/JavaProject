package com.yiworld.controller;

import com.yiworld.entity.PersonForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Valid;

@Controller
public class ValidFormController extends WebMvcConfigurerAdapter {

    @GetMapping("/Valid")
    public String showForm(Model model) {
        model.addAttribute("personForm", new PersonForm());
        return "form";
    }

    @PostMapping("/Valid")
    public String checkPersonInfo(@Valid @ModelAttribute PersonForm personForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "form";
        }
        return "results";
    }
}
