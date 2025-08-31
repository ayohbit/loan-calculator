package com.totvs.loan_calculator.controller;

import com.totvs.loan_calculator.dto.LoanCalculationRequest;
import com.totvs.loan_calculator.dto.LoanCalculationResponse;
import com.totvs.loan_calculator.service.LoanCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
@CrossOrigin(origins = "*")
public class LoanCalculationController {

    @Autowired
    private LoanCalculationService loanCalculationService;

    @PostMapping("/calculate")
    public ResponseEntity<LoanCalculationResponse> calculateLoan(@RequestBody LoanCalculationRequest request) {
        LoanCalculationResponse response = loanCalculationService.calculateLoan(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan Calculator API is running");
    }
}

