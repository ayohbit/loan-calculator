package com.totvs.loan_calculator.service;

import com.totvs.loan_calculator.dto.LoanCalculationRequest;
import com.totvs.loan_calculator.dto.LoanCalculationResponse;
import com.totvs.loan_calculator.dto.LoanCalculationRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanCalculationService {

    private static final int BASE_DAYS = 360; // F2 fixo
    
    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {
        try {
            // Validações
            String validationError = validateRequest(request);
            if (validationError != null) {
                return new LoanCalculationResponse(null, validationError, false);
            }

            List<LoanCalculationRow> rows = new ArrayList<>();
            
            // Calcular quantidade de parcelas
            int quantidadeParcelas = calculateInstallments(request.getPrimeiroPagamento(), request.getDataFinal());
            
            // Gerar datas de competência
            List<LocalDate> datasCompetencia = generateCompetencyDates(request.getDataInicial(), request.getDataFinal());
            
            // Gerar datas de pagamento
            List<LocalDate> datasPagamento = generatePaymentDates(request.getPrimeiroPagamento(), request.getDataFinal());
            
            // Inicializar variáveis para cálculo iterativo
            double saldoAnterior = request.getValorEmprestimo();
            double jurosAcumuladoAnterior = 0.0;
            
            for (int i = 0; i < datasCompetencia.size(); i++) {
                LocalDate dataCompetencia = datasCompetencia.get(i);
                LoanCalculationRow row = new LoanCalculationRow();
                
                row.setDataCompetencia(dataCompetencia);
                row.setValorEmprestimo(i == 0 ? request.getValorEmprestimo() : 0.0);
                
                // Verificar se é data de pagamento
                boolean isPaymentDate = datasPagamento.contains(dataCompetencia);
                row.setConsolidada(isPaymentDate ? "1/120" : "");
                
                // Calcular Amortização (F)
                double amortizacao = 0.0;
                if (isPaymentDate) {
                    amortizacao = request.getValorEmprestimo() / quantidadeParcelas;
                }
                row.setAmortizacao(amortizacao);
                
                // Calcular Provisão (H)
                double provisao = 0.0;
                if (i > 0) {
                    LocalDate dataAnterior = datasCompetencia.get(i - 1);
                    long daysDiff = ChronoUnit.DAYS.between(dataAnterior, dataCompetencia);
                    provisao = (Math.pow(1 + request.getTaxaJuros() / 100, (double) daysDiff / BASE_DAYS) - 1) * 
                              (saldoAnterior + jurosAcumuladoAnterior);
                }
                row.setProvisao(provisao);
                
                // Calcular Pago (J)
                double pago = 0.0;
                if (isPaymentDate) {
                    pago = jurosAcumuladoAnterior + provisao;
                }
                row.setPago(pago);
                
                // Calcular Juros Acumulado (I)
                double jurosAcumulado = jurosAcumuladoAnterior + provisao - pago;
                row.setJurosAcumulado(jurosAcumulado);
                
                // Calcular Saldo (G)
                double saldo = saldoAnterior - amortizacao;
                row.setSaldo(saldo);
                
                // Calcular Saldo Devedor (C)
                double saldoDevedor = saldo + jurosAcumulado;
                row.setSaldoDevedor(saldoDevedor);
                
                // Calcular Total (E)
                double total = amortizacao + pago;
                row.setTotal(total);
                
                rows.add(row);
                
                // Atualizar variáveis para próxima iteração
                saldoAnterior = saldo;
                jurosAcumuladoAnterior = jurosAcumulado;
            }
            
            return new LoanCalculationResponse(rows, "Cálculo realizado com sucesso", true);
            
        } catch (Exception e) {
            return new LoanCalculationResponse(null, "Erro no cálculo: " + e.getMessage(), false);
        }
    }
    
    private String validateRequest(LoanCalculationRequest request) {
        if (request.getDataFinal().isBefore(request.getDataInicial())) {
            return "A data final deve ser maior que a data inicial";
        }
        
        if (request.getPrimeiroPagamento().isBefore(request.getDataInicial()) || 
            request.getPrimeiroPagamento().isAfter(request.getDataFinal())) {
            return "A data de primeiro pagamento deve estar entre a data inicial e a data final";
        }
        
        if (request.getValorEmprestimo() <= 0) {
            return "O valor do empréstimo deve ser maior que zero";
        }
        
        if (request.getTaxaJuros() <= 0) {
            return "A taxa de juros deve ser maior que zero";
        }
        
        return null;
    }
    
    private int calculateInstallments(LocalDate primeiroPagamento, LocalDate dataFinal) {
        int parcelas = 1;
        LocalDate currentDate = primeiroPagamento;
        
        while (currentDate.isBefore(dataFinal)) {
            currentDate = getNextPaymentDate(currentDate);
            parcelas++;
        }
        
        return parcelas;
    }
    
    private List<LocalDate> generateCompetencyDates(LocalDate dataInicial, LocalDate dataFinal) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = dataInicial;
        
        while (!current.isAfter(dataFinal)) {
            dates.add(current);
            
            if (current.equals(dataFinal)) {
                break;
            }
            
            // Próxima data é o último dia do mês seguinte
            current = current.plusMonths(1).withDayOfMonth(current.plusMonths(1).lengthOfMonth());
            
            if (current.isAfter(dataFinal)) {
                current = dataFinal;
            }
        }
        
        return dates;
    }
    
    private List<LocalDate> generatePaymentDates(LocalDate primeiroPagamento, LocalDate dataFinal) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = primeiroPagamento;
        
        while (!current.isAfter(dataFinal)) {
            dates.add(current);
            if (current.equals(dataFinal)) {
                break;
            }
            current = getNextPaymentDate(current);
        }
        
        return dates;
    }
    
    private LocalDate getNextPaymentDate(LocalDate currentPaymentDate) {
        LocalDate nextMonth = currentPaymentDate.plusMonths(1);
        int dayOfMonth = currentPaymentDate.getDayOfMonth();
        int lastDayOfNextMonth = nextMonth.lengthOfMonth();
        
        // Se o dia do pagamento atual for maior que o último dia do próximo mês,
        // usar o último dia do próximo mês
        if (dayOfMonth > lastDayOfNextMonth) {
            return nextMonth.withDayOfMonth(lastDayOfNextMonth);
        } else {
            return nextMonth.withDayOfMonth(dayOfMonth);
        }
    }
}

