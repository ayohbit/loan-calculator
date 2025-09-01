$(document).ready(function() {
    // Elementos do DOM
    const form = $('#loan-form');
    const calcularBtn = $('#calcular-btn');
    const limparBtn = $('#limpar-btn');
    const resultsSection = $('#results-section');
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
    const quantidadeParcelas = $('#quantidade-parcelas');

    const campos = [dataInicial, dataFinal, primeiroPagamento, valorEmprestimo, taxaJuros, quantidadeParcelas];

    campos.forEach(campo => {
        campo.on('input change', function() {
            validateField($(this));
            checkFormValidity();
        });
    });

    dataInicial.on('change', validateDates);
    dataFinal.on('change', validateDates);
    primeiroPagamento.on('change', validateDates);

    form.on('submit', function(e) {
        e.preventDefault();
        if (validateForm()) {
            calculateLoan();
        }
    });

    limparBtn.on('click', function() {
        clearForm();
    });

    $('.alert-close').on('click', function() {
        $(this).parent().hide();
    });

    // Validação de campos
    function validateField(field) {
        const fieldName = field.attr('name');
        const value = field.val();
        const errorElement = $(`#error-${field.attr('id')}`);

        let isValid = true;
        let errorMessage = '';

        if (!value || value.trim() === '') {
            isValid = false;
            errorMessage = 'Este campo é obrigatório';
        } else if ((fieldName === 'valorEmprestimo' || fieldName === 'taxaJuros') && parseFloat(value) <= 0) {
            isValid = false;
            errorMessage = 'O valor deve ser maior que zero';
        }

        if (isValid) {
            field.removeClass('invalid').addClass('valid');
            errorElement.text('');
        } else {
            field.removeClass('valid').addClass('invalid');
            errorElement.text(errorMessage);
        }

        return isValid;
    }

    // Função para parsear datas do input sem deslocamento
    function parseDateInput(value) {
        if (!value) return null;
        const [ano, mes, dia] = value.split('-').map(Number);
        return new Date(ano, mes - 1, dia); // evita problema de fuso UTC
    }

    function validateDates() {
        const dataInicialVal = parseDateInput(dataInicial.val());
        const dataFinalVal = parseDateInput(dataFinal.val());
        const primeiroPagamentoVal = parseDateInput(primeiroPagamento.val());

        let isValid = true;

        if (dataInicialVal && dataFinalVal) {
            if (dataFinalVal <= dataInicialVal) {
                $('#error-data-final').text('A data final deve ser maior que a data inicial');
                dataFinal.removeClass('valid').addClass('invalid');
                isValid = false;
            } else {
                $('#error-data-final').text('');
                dataFinal.removeClass('invalid').addClass('valid');
            }
        }

        if (dataInicialVal && dataFinalVal && primeiroPagamentoVal) {
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
            if (!validateField(campo)) isValid = false;
        });
        if (!validateDates()) isValid = false;
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
            taxaJuros: parseFloat(taxaJuros.val()),
            quantidadeParcelas: parseInt(quantidadeParcelas.val())
        };

        $.ajax({
            url: '/api/loan/calculate',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function(response) {
                hideLoading();
                if (response.success && response.rows) {
                    displayResults(response.rows);
                    showSuccessAlert('Cálculo realizado com sucesso!');
                } else {
                    showErrorAlert(response.message || 'Erro no cálculo');
                }
            },
            error: function(xhr, status, error) {
                hideLoading();
                let errorMessage = 'Erro ao calcular empréstimo';
                try {
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        errorMessage = xhr.responseJSON.message;
                    } else if (xhr.status === 0) {
                        errorMessage = 'Erro de conexão. Verifique se o servidor está rodando.';
                    } else {
                        errorMessage = `Erro ${xhr.status}: ${error}`;
                    }
                } catch (e) {}
                showErrorAlert(errorMessage);
            }
        });
    }

    // Exibir resultados
    function displayResults(rows) {
        resultsTableBody.empty();

        rows.forEach(row => {
            const tr = $('<tr>');

            // Formatar data de competência corretamente
            let dataFormatada = 'Data inválida';
            if (row.dataCompetencia) {
                const dateParts = row.dataCompetencia.split('-');
                const ano = parseInt(dateParts[0], 10);
                const mes = parseInt(dateParts[1], 10);
                const dia = parseInt(dateParts[2], 10);
                dataFormatada = `${String(dia).padStart(2,'0')}/${String(mes).padStart(2,'0')}/${ano}`;
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

        $('#results-section').show();
        $('html, body').animate({ scrollTop: $('#results-section').offset().top - 20 }, 500);
    }

    function formatCurrency(value) {
        if (!value) return 'R$ 0,00';
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
    }

    function showLoading() { loading.show(); $('#results-section').hide(); }
    function hideLoading() { loading.hide(); }
    function showErrorAlert(msg) { errorAlert.find('.alert-message').text(msg); errorAlert.show(); setTimeout(()=>errorAlert.hide(),5000); }
    function showSuccessAlert(msg) { successAlert.find('.alert-message').text(msg); successAlert.show(); setTimeout(()=>successAlert.hide(),3000); }
    function hideAlerts() { errorAlert.hide(); successAlert.hide(); }

    function clearForm() {
        form[0].reset();
        campos.forEach(campo => { campo.removeClass('valid invalid'); $(`#error-${campo.attr('id')}`).text(''); });
        $('#results-section').hide();
        hideAlerts();
        checkFormValidity();
    }

    checkFormValidity();
});
