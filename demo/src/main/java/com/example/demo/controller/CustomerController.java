package com.example.demo.controller;

import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.entity.Customer;
import com.example.demo.dto.CustomerLoginRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.CustomerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // SIGNUP
    @PostMapping("/signup")
    public CustomerResponse signup(@RequestBody CustomerSignupRequest request) {
        return customerService.signup(request);
    }

    // LOGIN
//    HttpServletResponse is an interface in Java (Servlet API) that represents
//    the HTTP response sent from your server back to the client (browser, mobile app,
//    Postman, frontend, etc.).
//            "This object lets you modify what you want to send back to the client —
//            headers, cookies, status code, body, content type, etc."
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody CustomerLoginRequest request,
                                              HttpServletResponse response) {

        AuthResponse auth = customerService.login(request);

        // ----------------------------------------
        // Create HTTP-ONLY COOKIE for JWT token
        // ----------------------------------------
        Cookie cookie = new Cookie("jwt_token", auth.getToken());
        cookie.setHttpOnly(false);                     // Cannot be accessed by JS
        cookie.setSecure(false);                      // true in production (HTTPS)
        cookie.setPath("/");                          // Cookie valid for all routes
        cookie.setMaxAge(7 * 24 * 60 * 60);           // 7 days

        response.addCookie(cookie);

        return ResponseEntity.ok(auth);
    }

}
