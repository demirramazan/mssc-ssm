package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentServiceImplTest {
    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    public void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    public void testpreAuth() {
        Payment savepayment = paymentService.newPayment(payment);

        System.out.println("should be new!");
        System.out.println(savepayment.getState());

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savepayment.getId());

        System.out.println("should be pre_auth or pre_auth_error");
        System.out.println(stateMachine.getState());

        Payment preAuthPayment = paymentRepository.getOne(savepayment.getId());
        System.out.println(preAuthPayment);
    }

    @Transactional
    @RepeatedTest(10)
    public void testAuth() {
        Payment savepayment = paymentService.newPayment(payment);

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savepayment.getId());
        if (stateMachine.getState().getId() == PaymentState.PRE_AUTH) {
            System.out.println("payment is pre authhorized");

            StateMachine<PaymentState, PaymentEvent> authSm = paymentService.authPayment(savepayment.getId());
            System.out.println("result of auth: " + authSm.getState().getId());
        } else {
            System.out.println("Payment failed pre-auth...");
        }
    }
}