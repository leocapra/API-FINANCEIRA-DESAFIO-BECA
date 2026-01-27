package br.com.beca.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ConsumerTransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerTransactionServiceApplication.class, args);
	}

}
