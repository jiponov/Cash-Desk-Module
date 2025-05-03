package app.initialize;

import app.model.*;
import app.model.enums.*;
import app.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.nio.file.*;
import java.util.*;


// Начално състояние на касата при първото стартиране на приложението. Създава файл със стойности, ако такъв още не съществува
@Slf4j
@AllArgsConstructor
@Component
public class CashDeskInitializer implements CommandLineRunner {

    private static final String BALANCE_FILE = "CASH-BALANCE.txt";

    private final CashService cashService;


    @Override
    public void run(String... args) {

        log.info("Starting CashDeskInitializer...");

        if (Files.notExists(Paths.get(BALANCE_FILE))) {
            log.info("Balance file not found. Initializing cashier with default data.");
            initializeCashierData();
        } else {
            log.info("Balance file already exists. Skipping initialization.");
        }
    }


    // Създава  начални стойности за касиер (BGN и EUR) и прави запис в .txt текстов файл
    private void initializeCashierData() {
        // Създаваме нов касиер с име
        Cashier cashier = new Cashier();
        cashier.setName("MARTINA");

        // BGN купюри и сума
        Map<BigDecimal, Integer> bgnNotes = new HashMap<>();
        bgnNotes.put(new BigDecimal("10.00"), 50);
        bgnNotes.put(new BigDecimal("50.00"), 10);

        Balance bgnBalance = Balance.builder()
                .currency(CurrencyType.BGN)
                .availableAmount(new BigDecimal("1000.00"))
                .denominations(bgnNotes)
                .build();

        // EUR купюри и сума
        Map<BigDecimal, Integer> eurNotes = new HashMap<>();
        eurNotes.put(new BigDecimal("10.00"), 100);
        eurNotes.put(new BigDecimal("50.00"), 20);

        Balance eurBalance = Balance.builder()
                .currency(CurrencyType.EUR)
                .availableAmount(new BigDecimal("2000.00"))
                .denominations(eurNotes)
                .build();

        // Добавяме баланси към касиера
        cashier.setCashBalanceBgn(bgnBalance);
        cashier.setCashBalanceEur(eurBalance);

        // Записваме в .txt файла
        cashService.saveBalancesToFile(cashier);

        log.info("Initial cashier data written to {} successfully.", BALANCE_FILE);
    }
}