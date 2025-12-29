package com.example.demo.client;

import com.example.demo.dto.PaymentInitiateRequest;
import com.example.demo.dto.PaymentInitiateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import org.json.JSONObject;
import com.razorpay.*;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final WebClient paymentWebClient;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String getRazorpayKeySecret;

    private RazorpayClient client;
//    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallback")
//    @Retry(name = "paymentGateway")
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) throws RazorpayException {

//        return paymentWebClient.post()
//                .uri("/payments/initiate")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(PaymentInitiateResponse.class)
//                .block();


        JSONObject objectReq=new JSONObject();
        BigDecimal amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100));

        objectReq.put("amount", amountInPaise.intValueExact());
        objectReq.put("currency","INR");
        objectReq.put("receipt",request.getEmail());

        this.client=new RazorpayClient(razorpayKeyId,getRazorpayKeySecret);


      //remember this is the razorpay order
        Order razorpayOrder=client.orders.create(objectReq);

        PaymentInitiateResponse  res=new PaymentInitiateResponse();

        res.setExternalOrderId(razorpayOrder.get("id"));
        res.setAmount(amountInPaise.intValueExact());








        return res;





    }

    private PaymentInitiateResponse fallback(
            PaymentInitiateRequest request,
            Throwable ex
    ) {
        throw new RuntimeException("Gateway unavailable", ex);
    }
}


