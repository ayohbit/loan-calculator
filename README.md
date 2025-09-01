# Calculadora de Empréstimos - Spring Boot

## Descrição

Aplicação web desenvolvida em Spring Boot com frontend jQuery/HTML/CSS para cálculo de empréstimos. A aplicação implementa as fórmulas financeiras baseadas numa planilha financeira fornecida e oferece uma interface intuitiva para cálculo de empréstimos.

## Funcionalidades

- **Cálculo de Empréstimos**: Implementa as fórmulas da planilha Excel fornecida para cálculo preciso
- **Interface Responsiva**: Design responsivo compatível com desktop e mobile
- **Validações em Tempo Real**: Validação de campos e datas conforme regras de negócio
- **Grid de Resultados**: Exibição detalhada dos cálculos em formato tabular
- **API REST**: Backend robusto com endpoints para cálculo de empréstimos

## Regras de Negócio

- A data final sempre será um pagamento de parcela
- Todas as datas de fim de mês entre data inicial e final são exibidas na grid
- Pagamentos mensais no dia do primeiro pagamento
- Ajuste automático para fim de mês (ex: 31/01 → 29/02 em ano bissexto)

## Tecnologias Utilizadas

### Backend
- **Spring Boot 2.7.18**: Framework principal
- **Java 11**: Linguagem de programação
- **Maven**: Gerenciamento de dependências
- **Spring Web**: Para criação da API REST

### Frontend
- **HTML5**: Estrutura semântica
- **CSS3**: Estilização responsiva e moderna
- **jQuery 3.6.0**: Manipulação DOM e AJAX

## Como Executar

### Pré-requisitos
- Java 11 ou superior
- Maven 3.6 ou superior

### Passos para Execução

1. **Clone ou extraia o projeto**
   ```bash
   cd projeto-spring-boot/loan-calculator
   ```

2. **Execute a aplicação**
   ```bash
   ./mvnw spring-boot:run
   ```

   Ou no Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. **Acesse a aplicação**
    - Abra o navegador em: `http://localhost:8080`

### Endpoints da API

- **GET** `/api/loan/health` - Verificação de saúde da API
- **POST** `/api/loan/calculate` - Cálculo de empréstimo

## Características Técnicas

### Arquitetura
- **Padrão MVC**: Separação clara entre Model, View e Controller
- **DTOs**: Objetos de transferência de dados para API
- **Service Layer**: Lógica de negócio isolada
- **CORS Habilitado**: Permite requisições cross-origin

## Melhorias Futuras

- Testes unitários automatizados
- Persistência de dados (banco de dados)
- Exportação de resultados (PDF/Excel)
- Gráficos de evolução do empréstimo
- Múltiplos cenários de cálculo
- Autenticação e autorização