$(document).ready(function() {
    // Elementos do DOM
    const form = $('#loan-form');
    const calcularBtn = $('#calcular-btn');
    const limparBtn = $('#limpar-btn');
    const resultsSection = $('#results-section');
    const resultsTable = $('#results-table');
    const resultsTableBody = $('#results-tbody');
    const loading = $('#loading');
    const errorAlert = $('#error-alert');
    const successAlert = $('#success-alert');

    // Campos do formulário
    const dataInicial = $('#data-inicial');
    const dataFinal = $('#data-final');
    const primeiroPagamento = $('#primeiro-pagamento');
    const valorEmprestimo = $('#valor-emprestimo');
    const taxaJuros = $('#taxa-juros');

    // Validação em tempo real
    const campos = [dataInicial, dataFinal, primeiroPagamento, valorEmprestimo, taxaJuros];
    
    campos.forEach(campo => {
        campo.on('input change', function() {
            validateField($(this));
            checkFormValidity();
        });
    });

    // Validação específica para datas
    dataInicial.on('change', validateDates);
    dataFinal.on('change', validateDates);
    primeiroPagamento.on('change', validateDates);

    // Submit do formulário
    form.on('submit', function(e) {
        e.preventDefault();
        if (validateForm()) {
            calculateLoan();
        }
    });

    // Limpar formulário
    limparBtn.on('click', function() {
        clearForm();
    });

    // Fechar alertas
    $('.alert-close').on('click', function() {
        $(this).parent().hide();
    });

    // Funções de validação
    function validateField(field) {
        const fieldName = field.attr('name');
        const value = field.val();
        const errorElement = $(`#error-${field.attr('id')}`);
        
        let isValid = true;
        let errorMessage = '';

        // Validação de campo obrigatório
        if (!value || value.trim() === '') {
            isValid = false;
            errorMessage = 'Este campo é obrigatório';
        }
        // Validação de valores numéricos
        else if ((fieldName === 'valorEmprestimo' || fieldName === 'taxaJuros') && parseFloat(value) <= 0) {
            isValid = false;
            errorMessage = 'O valor deve ser maior que zero';
        }

        // Atualizar UI
        if (isValid) {
            field.removeClass('invalid').addClass('valid');
            errorElement.text('');
        } else {
            field.removeClass('valid').addClass('invalid');
            errorElement.text(errorMessage);
        }

        return isValid;
    }

    function validateDates() {
        const dataInicialVal = new Date(dataInicial.val());
        const dataFinalVal = new Date(dataFinal.val());
        const primeiroPagamentoVal = new Date(primeiroPagamento.val());

        let isValid = true;

        // Validar data final > data inicial
        if (dataInicial.val() && dataFinal.val() && dataFinalVal <= dataInicialVal) {
            $('#error-data-final').text('A data final deve ser maior que a data inicial');
            dataFinal.removeClass('valid').addClass('invalid');
            isValid = false;
        } else if (dataFinal.val()) {
            $('#error-data-final').text('');
            dataFinal.removeClass('invalid').addClass('valid');
        }

        // Validar primeiro pagamento entre data inicial e final
        if (dataInicial.val() && dataFinal.val() && primeiroPagamento.val()) {
            if (primeiroPagamentoVal <= dataInicialVal || primeiroPagamentoVal > dataFinalVal) {
                $('#error-primeiro-pagamento').text('A data de primeiro pagamento deve estar entre a data inicial e a data final');
                primeiroPagamento.removeClass('valid').addClass('invalid');
                isValid = false;
            } else {
                $('#error-primeiro-pagamento').text('');
                primeiroPagamento.removeClass('invalid').addClass('valid');
            }
        }

        return isValid;
    }

    function validateForm() {
        let isValid = true;
        
        campos.forEach(campo => {
            if (!validateField(campo)) {
                isValid = false;
            }
        });

        if (!validateDates()) {
            isValid = false;
        }

        return isValid;
    }

    function checkFormValidity() {
        const allFieldsFilled = campos.every(campo => campo.val().trim() !== '');
        const allFieldsValid = campos.every(campo => !campo.hasClass('invalid'));
        const datesValid = validateDates();
        
        calcularBtn.prop('disabled', !(allFieldsFilled && allFieldsValid && datesValid));
    }

    // Função para calcular empréstimo
    function calculateLoan() {
        hideAlerts();
        showLoading();

        const requestData = {
            dataInicial: dataInicial.val(),
            dataFinal: dataFinal.val(),
            primeiroPagamento: primeiroPagamento.val(),
            valorEmprestimo: parseFloat(valorEmprestimo.val()),
            taxaJuros: parseFloat(taxaJuros.val())
        };

        $.ajax({
            url: '/api/loan/calculate',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                hideLoading();
                console.log('Response received:', response);
                if (response.success && response.rows) {
                    displayResults(response.rows);
                    showSuccessAlert('Cálculo realizado com sucesso!');
                } else {
                    showErrorAlert(response.message || 'Erro no cálculo');
                }
            },
            error: function(xhr, status, error) {
                hideLoading();
                console.error('AJAX Error:', xhr, status, error);
                let errorMessage = 'Erro ao calcular empréstimo';
                
                try {
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMessage = xhr.responseJSON.message;
                    } else if (xhr.status === 0) {
                        errorMessage = 'Erro de conexão. Verifique se o servidor está rodando.';
                    } else {
                        errorMessage = `Erro ${xhr.status}: ${error}`;
                    }
                } catch (e) {
                    console.error('Error parsing response:', e);
                }
                
                showErrorAlert(errorMessage);
            }
        });
    }

    // Função para exibir resultados
    function displayResults(rows) {
        resultsTableBody.empty();
        
        rows.forEach(row => {
            const tr = $('<tr>');
            
            // Formatar data - tratar tanto string quanto array de data
            let dataFormatada;
            if (Array.isArray(row.dataCompetencia)) {
                // Se for array [ano, mês, dia]
                const [ano, mes, dia] = row.dataCompetencia;
                dataFormatada = new Date(ano, mes - 1, dia).toLocaleDateString('pt-BR');
            } else if (typeof row.dataCompetencia === 'string') {
                // Se for string ISO
                dataFormatada = new Date(row.dataCompetencia).toLocaleDateString('pt-BR');
            } else {
                dataFormatada = 'Data inválida';
            }
            
            tr.append(`<td>${dataFormatada}</td>`);
            tr.append(`<td>${formatCurrency(row.valorEmprestimo)}</td>`);
            tr.append(`<td>${formatCurrency(row.saldoDevedor)}</td>`);
            tr.append(`<td>${row.consolidada || ''}</td>`);
            tr.append(`<td>${formatCurrency(row.total)}</td>`);
            tr.append(`<td>${formatCurrency(row.amortizacao)}</td>`);
            tr.append(`<td>${formatCurrency(row.saldo)}</td>`);
            tr.append(`<td>${formatCurrency(row.provisao)}</td>`);
            tr.append(`<td>${formatCurrency(row.jurosAcumulado)}</td>`);
            tr.append(`<td>${formatCurrency(row.pago)}</td>`);
            
            resultsTableBody.append(tr);
        });
        
        resultsSection.show();
        
        // Scroll suave para os resultados
        $('html, body').animate({
            scrollTop: resultsSection.offset().top - 20
        }, 500);
    }

    // Funções utilitárias
    function formatCurrency(value) {
        if (value === null || value === undefined || value === 0) {
            return 'R$ 0,00';
        }
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(value);
    }

    function showLoading() {
        loading.show();
        resultsSection.hide();
    }

    function hideLoading() {
        loading.hide();
    }

    function showErrorAlert(message) {
        errorAlert.find('.alert-message').text(message);
        errorAlert.show();
        
        // Auto-hide após 5 segundos
        setTimeout(() => {
            errorAlert.hide();
        }, 5000);
    }

    function showSuccessAlert(message) {
        successAlert.find('.alert-message').text(message);
        successAlert.show();
        
        // Auto-hide após 3 segundos
        setTimeout(() => {
            successAlert.hide();
        }, 3000);
    }

    function hideAlerts() {
        errorAlert.hide();
        successAlert.hide();
    }

    function clearForm() {
        form[0].reset();
        campos.forEach(campo => {
            campo.removeClass('valid invalid');
            $(`#error-${campo.attr('id')}`).text('');
        });
        resultsSection.hide();
        hideAlerts();
        checkFormValidity();
    }

    // Inicialização
    checkFormValidity();
});

