package com.totvs.loan_calculator.dto;

import java.time.LocalDate;

public class LoanCalculationRequest {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private LocalDate primeiroPagamento;
    private Double valorEmprestimo;
    private Double taxaJuros;

    // Constructors
    public LoanCalculationRequest() {}

    public LoanCalculationRequest(LocalDate dataInicial, LocalDate dataFinal, LocalDate primeiroPagamento, 
                                 Double valorEmprestimo, Double taxaJuros) {
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.primeiroPagamento = primeiroPagamento;
        this.valorEmprestimo = valorEmprestimo;
        this.taxaJuros = taxaJuros;
    }

    // Getters and Setters
    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public LocalDate getPrimeiroPagamento() {
        return primeiroPagamento;
    }

    public void setPrimeiroPagamento(LocalDate primeiroPagamento) {
        this.primeiroPagamento = primeiroPagamento;
    }

    public Double getValorEmprestimo() {
        return valorEmprestimo;
    }

    public void setValorEmprestimo(Double valorEmprestimo) {
        this.valorEmprestimo = valorEmprestimo;
    }

    public Double getTaxaJuros() {
        return taxaJuros;
    }

    public void setTaxaJuros(Double taxaJuros) {
        this.taxaJuros = taxaJuros;
    }
}

