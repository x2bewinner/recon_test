package com.example.financial;

import com.example.financial.model.UsageData;
import com.example.financial.repository.UsageDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class FinancialReconciliationApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialReconciliationApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(UsageDataRepository usageDataRepository) {
		return (args) -> {
			// Seed some data for testing
			UsageData data1 = new UsageData();
			data1.setDeviceId("DEVICE-001");
			data1.setDate(LocalDate.now());
			data1.setTransactionCount(100L);
			data1.setTotalAmount(new BigDecimal("5000.00"));
			usageDataRepository.save(data1);

			System.out.println("Seeded Usage Data: " + data1);
		};
	}

}
