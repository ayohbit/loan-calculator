package com.totvs.loan_calculator.dto;

import java.util.List;

public class LoanCalculationResponse {
    private List<LoanCalculationRow> rows;
    private String message;
    private boolean success;

    // Constructors
    public LoanCalculationResponse() {}

    public LoanCalculationResponse(List<LoanCalculationRow> rows, String message, boolean success) {
        this.rows = rows;
        this.message = message;
        this.success = success;
    }

    // Getters and Setters
    public List<LoanCalculationRow> getRows() {
        return rows;
    }

    public void setRows(List<LoanCalculationRow> rows) {
        this.rows = rows;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

