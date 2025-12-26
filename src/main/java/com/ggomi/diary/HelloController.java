package com.ggomi.diary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    //
    @GetMapping("/")
    public String mainPage() {
        return "Hello World!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "안녕하세용";
    }
}
