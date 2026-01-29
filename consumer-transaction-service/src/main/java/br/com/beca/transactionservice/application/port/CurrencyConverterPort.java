package br.com.beca.transactionservice.application.port;

import java.math.BigDecimal;

public interface CurrencyConverterPort {
    BigDecimal toBrl(BigDecimal amount, String currency);
    BigDecimal fxRate(String currency);
}
