package com.example.demo.service.impl;

import com.example.demo.dto.*;

import com.example.demo.entity.Customer;
import com.example.demo.entity.CustomerRole;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;

import com.example.demo.repository.CustomerRepository;

import com.example.demo.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    // ------------------------
    // SIGNUP SUCCESS
    // ------------------------
    @Test
    void signup_shouldCreateCustomerSuccessfully() {

        CustomerSignupRequest request = new CustomerSignupRequest();
        request.name = "John";
        request.email = "john@test.com";
        request.phone = "1234567890";
        request.address = "Delhi";
        request.setPassword("password123");
        request.setCustomerRole(CustomerRole.USER);

        when(customerRepository.findByEmail(request.email))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPass");

        Customer savedCustomer = Customer.builder()
                .name("John")
                .email("john@test.com")
                .password("encodedPass")
                .phone("1234567890")
                .address("Delhi")
                .customerRole(CustomerRole.USER)
                .build();

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(savedCustomer);

        CustomerResponse response = customerService.signup(request);

        assertEquals("John", response.name);
        assertEquals("john@test.com", response.email);
        assertEquals(CustomerRole.USER, response.role);

        verify(customerRepository).save(any(Customer.class));
    }

    // ------------------------
    // SIGNUP EMAIL EXISTS
    // ------------------------
    @Test
    void signup_shouldFailIfEmailAlreadyExists() {

        CustomerSignupRequest request = new CustomerSignupRequest();
        request.name = "John";
        request.email = "john@test.com";
        request.setPassword("password123");

        when(customerRepository.findByEmail(request.email))
                .thenReturn(Optional.of(new Customer()));

        assertThrows(BadRequestException.class,
                () -> customerService.signup(request));

        verify(customerRepository, never()).save(any());
    }

    // ------------------------
    // LOGIN SUCCESS
    // ------------------------
    @Test
    void login_shouldReturnTokenSuccessfully() {

        CustomerLoginRequest request = new CustomerLoginRequest();
        request.email = "john@test.com";
        request.setPassword("password123");

        Customer customer = Customer.builder()
                .email("john@test.com")
                .password("encodedPass")
                .customerRole(CustomerRole.USER)
                .build();

        when(customerRepository.findByEmail(request.email))
                .thenReturn(Optional.of(customer));

        when(passwordEncoder.matches("password123", "encodedPass"))
                .thenReturn(true);

        when(jwtService.generateToken(any(), eq(CustomerRole.USER)))
                .thenReturn("fake-jwt");

        AuthResponse response = customerService.login(request);

        assertEquals("fake-jwt", response.getToken());

        verify(jwtService).generateToken(any(), eq(CustomerRole.USER));
    }

    // ------------------------
    // LOGIN USER NOT FOUND
    // ------------------------
    @Test
    void login_shouldFailIfUserNotFound() {

        CustomerLoginRequest request = new CustomerLoginRequest();
        request.email = "missing@test.com";
        request.setPassword("pass");

        when(customerRepository.findByEmail(request.email))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> customerService.login(request));
    }

    // ------------------------
    // LOGIN WRONG PASSWORD
    // ------------------------
    @Test
    void login_shouldFailIfPasswordInvalid() {

        CustomerLoginRequest request = new CustomerLoginRequest();
        request.email = "john@test.com";
        request.setPassword("wrongpass");

        Customer customer = Customer.builder()
                .email("john@test.com")
                .password("encodedPass")
                .customerRole(CustomerRole.USER)
                .build();

        when(customerRepository.findByEmail(request.email))
                .thenReturn(Optional.of(customer));

        when(passwordEncoder.matches("wrongpass", "encodedPass"))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> customerService.login(request));
    }
}
