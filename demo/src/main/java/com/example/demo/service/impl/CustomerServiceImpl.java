package com.example.demo.service.impl;
import jakarta.servlet.http.Cookie;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.CustomerLoginRequest;
import com.example.demo.dto.CustomerResponse;
import com.example.demo.dto.CustomerSignupRequest;
import com.example.demo.entity.Customer;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.entity.CustomerRole;
import com.example.demo.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Override
    public CustomerResponse signup(CustomerSignupRequest request) {

        // -----------------------------
        // 1. Required field validation
        // -----------------------------
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {

            throw new BadRequestException("Email and Password cannot be empty");
        }

        // Name check
        if ( request.name.isBlank()) {
            throw new BadRequestException("Name cannot be empty");
        }

        // Address blank check (null ok, blank not ok)
        if (request.address != null && request.address.isBlank()) {
            throw new BadRequestException("Address cannot be blank");
        }

        // -----------------------------
        // 2. Validate email format
        // -----------------------------
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Invalid email format");
        }

        // -----------------------------
        // 3. Check if email already exists
        // -----------------------------
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered!");
        }

        // -----------------------------
        // 4. Validate password strength
        // -----------------------------
        if (request.getPassword().length() <= 6) {
            throw new BadRequestException("Password must be greater than 6 characters");
        }

        // -----------------------------
        // 5. Validate role (acceptable values only)
        // -----------------------------
        if (request.getCustomerRole() != null &&
                request.getCustomerRole() != CustomerRole.ADMIN &&
                request.getCustomerRole() != CustomerRole.USER) {

            throw new BadRequestException("Invalid role! Allowed: ADMIN, USER");
        }

        // -----------------------------
        // 6. Validate phone (only if provided)
        // -----------------------------
        if (request.phone != null && !request.phone.matches("\\d{10}")) {
            throw new BadRequestException("Phone number must be 10 digits");
        }


        // -----------------------------
        // 7. CUSTOMER CREATION
        // -----------------------------
        Customer customer = Customer.builder()
                .name(request.name)
                .email(request.email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.phone)
                .address(request.address)
                .customerRole(
                        request.getCustomerRole() != null
                                ? request.getCustomerRole()
                                : CustomerRole.USER // default USER
                )
                .build();

        Customer saved = customerRepository.save(customer);

        // -----------------------------
        // 8. Create Response DTO
        // -----------------------------
        CustomerResponse resp = new CustomerResponse();
        resp.customerUuid = saved.getCustomerUuid();
        resp.name = saved.getName();
        resp.email = saved.getEmail();
        resp.phone = saved.getPhone();
        resp.address = saved.getAddress();
        resp.role = saved.getCustomerRole();

        return resp;
    }





    @Override
    public AuthResponse login(CustomerLoginRequest request) {

        // -----------------------------
        // 1. Check empty fields
        // -----------------------------
        if (request.email == null || request.email.isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {

            throw new BadRequestException("Email and Password cannot be empty");
        }

        // -----------------------------
        // 2. Validate email format
        // -----------------------------
        if (!request.email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadRequestException("Invalid email format");
        }

        // -----------------------------
        // 3. Find user
        // -----------------------------
        Customer customer = customerRepository.findByEmail(request.email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // -----------------------------
        // 4. Validate password
        // -----------------------------
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        // -----------------------------
        // 5. Optional checks
        // -----------------------------
        // example: if you add fields later
    /*
    if (!customer.isActive()) {
        throw new BadRequestException("Account is disabled");
    }
    */

        // -----------------------------
        // 6. Generate JWT
        // -----------------------------
        String token = jwtService.generateToken(
                customer.getUuid(),
                customer.getCustomerRole()
        );

        // -----------------------------
        // 7. Response DTO
        // -----------------------------
        return new AuthResponse(token, customer.getUuid());
    }


    @Override
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    public Void logout(HttpServletResponse response) {

        // Clear SecurityContext
        SecurityContextHolder.clearContext();

        // Delete JWT cookie
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // prod
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return null;
    }


}

