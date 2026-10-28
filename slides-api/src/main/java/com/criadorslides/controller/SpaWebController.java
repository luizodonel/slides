package com.criadorslides.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaWebController {

    @GetMapping(value = {
            "/",
            "/{path:^(?!api|assets|favicon|error).*$}/**"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
