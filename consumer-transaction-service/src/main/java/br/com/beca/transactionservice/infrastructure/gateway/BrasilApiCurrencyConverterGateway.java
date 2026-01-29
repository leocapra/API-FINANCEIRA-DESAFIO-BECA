package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.CurrencyConverterPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BrasilApiCurrencyConverterGateway implements CurrencyConverterPort {

    private final RestClient client;
    private final Map<String, CachedRate> cache = new ConcurrentHashMap<>();

    public BrasilApiCurrencyConverterGateway() {
        this.client = RestClient.builder().baseUrl("https://brasilapi.com.br").build();
    }

    @Override
    public BigDecimal toBrl(BigDecimal amount, String currency) {
        if (amount == null) throw new IllegalArgumentException("amount é obrigatório");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency é obrigatória");

        String c = currency.toUpperCase();

        if ("BRL".equals(c)) {
            return amount.setScale(2, RoundingMode.HALF_EVEN);
        }

        LocalDate today = exchangeDate(LocalDate.now());
        BigDecimal rate = getBrlPerUnit(c, today);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

    private BigDecimal getBrlPerUnit(String currency, LocalDate date) {
        String key = currency + ":" + date;

        CachedRate cached = cache.get(key);
        if (cached != null) return cached.rate();

        JsonNode json = client.get().uri("/api/cambio/v1/cotacao/{currency}/{date}", currency, date).header("Accept", "application/json").retrieve().body(JsonNode.class);

        if (json == null) {
            throw new RuntimeException("BrasilAPI retornou resposta vazia");
        }

        JsonNode cotacoes = json.get("cotacoes");

        if (cotacoes == null || !cotacoes.isArray() || cotacoes.isEmpty()) {
            throw new RuntimeException("Nenhuma cotação encontrada na BrasilAPI");
        }

        JsonNode ultimaCotacao = cotacoes.get(cotacoes.size() - 1);

        JsonNode venda = ultimaCotacao.get("cotacao_venda");

        if (venda == null) {
            throw new RuntimeException("Campo cotacao_venda não encontrado na última cotação");
        }

        BigDecimal rate = new BigDecimal(venda.asText());

        cache.put(key, new CachedRate(rate));

        return rate;
    }

    private LocalDate exchangeDate(LocalDate now) {
        LocalDate date = now.minusDays(1);

        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date;
    }


    private record CachedRate(BigDecimal rate) {
    }


}