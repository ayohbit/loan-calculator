package com.totvs.loan_calculator.dto;

import java.time.LocalDate;

public class LoanCalculationRow {
    private LocalDate dataCompetencia;
    private Double valorEmprestimo;
    private Double saldoDevedor;
    private String consolidada;
    private Double total;
    private Double amortizacao;
    private Double saldo;
    private Double provisao;
    private Double jurosAcumulado;
    private Double pago;

    // Constructors
    public LoanCalculationRow() {}

    public LoanCalculationRow(LocalDate dataCompetencia, Double valorEmprestimo, Double saldoDevedor,
                             String consolidada, Double total, Double amortizacao, Double saldo,
                             Double provisao, Double jurosAcumulado, Double pago) {
        this.dataCompetencia = dataCompetencia;
        this.valorEmprestimo = valorEmprestimo;
        this.saldoDevedor = saldoDevedor;
        this.consolidada = consolidada;
        this.total = total;
        this.amortizacao = amortizacao;
        this.saldo = saldo;
        this.provisao = provisao;
        this.jurosAcumulado = jurosAcumulado;
        this.pago = pago;
    }

    // Getters and Setters
    public LocalDate getDataCompetencia() {
        return dataCompetencia;
    }

    public void setDataCompetencia(LocalDate dataCompetencia) {
        this.dataCompetencia = dataCompetencia;
    }

    public Double getValorEmprestimo() {
        return valorEmprestimo;
    }

    public void setValorEmprestimo(Double valorEmprestimo) {
        this.valorEmprestimo = valorEmprestimo;
    }

    public Double getSaldoDevedor() {
        return saldoDevedor;
    }

    public void setSaldoDevedor(Double saldoDevedor) {
        this.saldoDevedor = saldoDevedor;
    }

    public String getConsolidada() {
        return consolidada;
    }

    public void setConsolidada(String consolidada) {
        this.consolidada = consolidada;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getAmortizacao() {
        return amortizacao;
    }

    public void setAmortizacao(Double amortizacao) {
        this.amortizacao = amortizacao;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public Double getProvisao() {
        return provisao;
    }

    public void setProvisao(Double provisao) {
        this.provisao = provisao;
    }

    public Double getJurosAcumulado() {
        return jurosAcumulado;
    }

    public void setJurosAcumulado(Double jurosAcumulado) {
        this.jurosAcumulado = jurosAcumulado;
    }

    public Double getPago() {
        return pago;
    }

    public void setPago(Double pago) {
        this.pago = pago;
    }
}

