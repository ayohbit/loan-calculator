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

    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {
        try {
            // Validações
            String validationError = validateRequest(request);
            if (validationError != null) {
                return new LoanCalculationResponse(null, validationError, false);
            }

            List<LoanCalculationRow> rows = new ArrayList<>();

            // Gerar todas as datas que devem aparecer na grid
            List<LocalDate> allDates = generateAllDates(request);

            // Gerar datas de pagamento
            List<LocalDate> paymentDates = generatePaymentDates(request.getPrimeiroPagamento(), request.getDataFinal());

            // Inicializar variáveis para cálculo interativo
            double saldoAnterior = request.getValorEmprestimo();
            double jurosAcumuladoAnterior = 0.0;
            LocalDate dataAnterior = request.getDataInicial();
            int parcelaAtual = 0;  // contador de parcelas

            for (int i = 0; i < allDates.size(); i++) {
                LocalDate dataCompetencia = allDates.get(i);
                LoanCalculationRow row = new LoanCalculationRow();

                row.setDataCompetencia(dataCompetencia);
                row.setValorEmprestimo(i == 0 ? request.getValorEmprestimo() : 0.0);

                // Verificar se é data de pagamento
                boolean isPaymentDate = paymentDates.contains(dataCompetencia);

                // Calcular amortização
                double amortizacao = 0.0;
                if (isPaymentDate && request.getQuantidadeParcelas() != null && request.getQuantidadeParcelas() > 0) {
                    amortizacao = request.getValorEmprestimo() / request.getQuantidadeParcelas();
                }
                row.setAmortizacao(amortizacao);

                // Calcular provisão
                double provisao = 0.0;
                if (i > 0) {
                    long daysDiff = ChronoUnit.DAYS.between(dataAnterior, dataCompetencia);
                    double taxaDecimal = request.getTaxaJuros() / 100.0;
                    double exponent = (double) daysDiff / LoanCalculationRequest.BASE_DIAS;
                    provisao = (Math.pow(1 + taxaDecimal, exponent) - 1) * (saldoAnterior + jurosAcumuladoAnterior);
                }
                row.setProvisao(provisao);

                // Calcular pago
                double pago = 0.0;
                if (isPaymentDate) {
                    pago = jurosAcumuladoAnterior + provisao;
                }
                row.setPago(pago);

                // Calcular juros acumulado
                double jurosAcumulado = jurosAcumuladoAnterior + provisao - pago;
                row.setJurosAcumulado(jurosAcumulado);

                // Calcular saldo devedor
                double saldoDevedor = saldoAnterior + jurosAcumulado;
                row.setSaldoDevedor(saldoDevedor);

                // Calcular saldo
                double saldo = saldoDevedor - amortizacao;
                row.setSaldo(saldo);

                // Calcular total
                double total = amortizacao + pago;
                row.setTotal(total);

                // Definir consolidada com contador incremental
                if (isPaymentDate) {
                    parcelaAtual++;
                    row.setConsolidada(String.format("%d/%d", parcelaAtual, request.getQuantidadeParcelas()));
                } else {
                    row.setConsolidada("");
                }

                rows.add(row);

                // Atualizar variáveis para próxima interação
                saldoAnterior = saldo;
                jurosAcumuladoAnterior = jurosAcumulado;
                dataAnterior = dataCompetencia;
            }

            return new LoanCalculationResponse(rows, "Cálculo realizado com sucesso", true);

        } catch (Exception e) {
            return new LoanCalculationResponse(null, "Erro interno no cálculo: " + e.getMessage(), false);
        }
    }

    private List<LocalDate> generateAllDates(LoanCalculationRequest request) {
        List<LocalDate> dates = new ArrayList<>();

        // Adicionar data inicial
        dates.add(request.getDataInicial());

        // Adicionar todos os últimos dias dos meses entre data inicial e final
        LocalDate current = request.getDataInicial().withDayOfMonth(1).plusMonths(1);
        while (!current.isAfter(request.getDataFinal().withDayOfMonth(1))) {
            LocalDate lastDayOfMonth = current.withDayOfMonth(current.lengthOfMonth());
            if (!dates.contains(lastDayOfMonth) && !lastDayOfMonth.isBefore(request.getDataInicial())) {
                dates.add(lastDayOfMonth);
            }
            current = current.plusMonths(1);
        }

        // Adicionar datas de pagamento
        List<LocalDate> paymentDates = generatePaymentDates(request.getPrimeiroPagamento(), request.getDataFinal());
        for (LocalDate paymentDate : paymentDates) {
            if (!dates.contains(paymentDate)) {
                dates.add(paymentDate);
            }
        }

        dates.sort(LocalDate::compareTo);
        return dates;
    }

    private List<LocalDate> generatePaymentDates(LocalDate primeiroPagamento, LocalDate dataFinal) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = primeiroPagamento;

        while (!current.isAfter(dataFinal)) {
            dates.add(current);
            if (current.equals(dataFinal)) break;
            current = getNextPaymentDate(current);
        }

        return dates;
    }

    private LocalDate getNextPaymentDate(LocalDate currentPaymentDate) {
        LocalDate nextMonth = currentPaymentDate.plusMonths(1);
        int targetDay = currentPaymentDate.getDayOfMonth();

        if (targetDay > nextMonth.lengthOfMonth()) {
            return nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
        }

        return nextMonth.withDayOfMonth(targetDay);
    }

    private String validateRequest(LoanCalculationRequest request) {
        if (request.getDataInicial() == null) return "Data inicial é obrigatória";
        if (request.getDataFinal() == null) return "Data final é obrigatória";
        if (request.getPrimeiroPagamento() == null) return "Primeiro pagamento é obrigatório";
        if (request.getValorEmprestimo() == null || request.getValorEmprestimo() <= 0) return "Valor do empréstimo deve ser maior que zero";
        if (request.getTaxaJuros() == null || request.getTaxaJuros() <= 0) return "Taxa de juros deve ser maior que zero";
        if (request.getQuantidadeParcelas() == null || request.getQuantidadeParcelas() <= 0) return "Quantidade de parcelas deve ser maior que zero";
        if (request.getDataFinal().isBefore(request.getDataInicial())) return "Data final deve ser maior que a data inicial";
        if (request.getPrimeiroPagamento().isBefore(request.getDataInicial()) || request.getPrimeiroPagamento().isAfter(request.getDataFinal()))
            return "Data do primeiro pagamento deve estar entre a data inicial e final";
        return null;
    }
}
