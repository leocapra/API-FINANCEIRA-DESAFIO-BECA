package br.com.beca.transactionservice.domain.valueobject;

import java.math.BigDecimal;

public record Money(BigDecimal value, String currency) {
    public Money{
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Quantidade precisa ser maior que zero!");
        }
        if (currency == null || currency.isBlank()){
            throw new IllegalArgumentException("Tipo de moeda é obrigatório");
        }
    }
}
