package app.service;

import app.model.*;
import app.model.enums.*;
import app.shared.exception.*;
import app.web.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;


@Slf4j
@Service
@AllArgsConstructor
public class CashService {

    private static final String TRANSACTION_HISTORY_FILE = "TRANSACTION-HISTORY.txt";
    private static final String BALANCE_FILE = "CASH-BALANCE.txt";

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.00");

    private final ModelMapper modelMapper;


    public String handleCashRequest(OperationRequest request) throws DomainException, IOException {
        log.info("Starting to process cash operation request...");

        Operation operation = modelMapper.map(request, Operation.class);
        log.debug("Mapped operation: {}", operation);

        validateOperation(operation);
        verifyAmountMatchesDenomination(operation);

        applyCashOperation(operation);
        appendTransactionToFile(operation);

        log.info("Cash operation processed successfully.");
        return "Cash operation completed successfully.";
    }


    private void validateOperation(Operation operation) {
        log.debug("Validating operation input...");

        if (operation.getCurrency() == null) {
            throw new IllegalArgumentException("Currency is required for the operation.");
        }

        if (operation.getType() == null) {
            throw new IllegalArgumentException("Transaction type must be provided.");
        }

        if (operation.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required.");
        }

        if (operation.getDenominations() == null || operation.getDenominations().isEmpty()) {
            throw new IllegalArgumentException("At least one denomination must be specified.");
        }

        log.debug("Validation successful.");
    }


    private void verifyAmountMatchesDenomination(Operation operation) {
        log.debug("Verifying that amount matches denominations...");

        BigDecimal calculatedSum = operation.getDenominations()
                .entrySet()
                .stream()
                .map(e -> e.getKey().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (operation.getAmount().compareTo(calculatedSum) != 0) {
            throw new IllegalArgumentException("Specified amount does not match the denomination total.");
        }

        log.debug("Amount matches denominations: {}", calculatedSum);
    }


    private void appendTransactionToFile(Operation operation) throws IOException {
        log.info("Appending transaction to file...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_HISTORY_FILE, true))) {
            writer.write("------------------------------------------\n");
            writer.write("Transaction Type : " + operation.getType() + "\n");
            writer.write("Currency         : " + operation.getCurrency() + "\n");
            writer.write("Amount           : " + AMOUNT_FORMAT.format(operation.getAmount()) + "\n");
            writer.write("Denominations    : " + renderDenominationList(operation.getDenominations()) + "\n");
            writer.write("------------------------------------------\n\n");
        }

        log.info("Transaction recorded successfully.");
    }


    public String renderDenominationList(Map<BigDecimal, Integer> denominations) {
        List<String> entries = new ArrayList<>();

        denominations.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String formatted = entry.getValue() + " x " + AMOUNT_FORMAT.format(entry.getKey());
                    entries.add(formatted);
                });

        return String.join(", ", entries);
    }


    private void applyCashOperation(Operation operation) throws DomainException {
        log.info("Starting application of cash operation...");

        Cashier cashier = loadCashierFromFile();
        log.debug("Cashier data loaded from file: {}", cashier.getName());

        CurrencyType currency = operation.getCurrency();
        Balance balance;

        if (currency == CurrencyType.BGN) {
            balance = cashier.getCashBalanceBgn();
            log.debug("Selected BGN balance for operation.");
        } else if (currency == CurrencyType.EUR) {
            balance = cashier.getCashBalanceEur();
            log.debug("Selected EUR balance for operation.");
        } else {
            log.error("Unsupported currency type: {}", currency);
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        Map<BigDecimal, Integer> balanceDenoms = balance.getDenominations();
        Map<BigDecimal, Integer> inputDenoms = operation.getDenominations();

        if (operation.getType() == TransactionType.DEPOSIT) {
            log.info("Processing DEPOSIT operation...");

            depositAmount(balance, operation);
            addDenominations(balanceDenoms, inputDenoms);

            log.debug("Deposit completed. Amount: {}", operation.getAmount());
        }

        if (operation.getType() == TransactionType.WITHDRAWAL) {
            log.info("Processing WITHDRAWAL operation...");

            withdrawAmount(balance, operation);
            subtractDenominations(balanceDenoms, inputDenoms);

            log.debug("Withdrawal completed. Amount: {}", operation.getAmount());
        }

        saveBalancesToFile(cashier);

        log.info("Updated balances saved successfully for cashier: {}", cashier.getName());
    }


    private void depositAmount(Balance balance, Operation operation) {
        log.debug("Depositing amount: {}", operation.getAmount());
        balance.setAvailableAmount(balance.getAvailableAmount().add(operation.getAmount()));
    }


    private void withdrawAmount(Balance balance, Operation operation) throws DomainException {
        log.debug("Attempting withdrawal of amount: {}", operation.getAmount());

        if (balance.getAvailableAmount().compareTo(operation.getAmount()) < 0) {
            throw new DomainException("Insufficient funds for this withdrawal.");
        }

        balance.setAvailableAmount(balance.getAvailableAmount().subtract(operation.getAmount()));
    }


    private void addDenominations(Map<BigDecimal, Integer> current, Map<BigDecimal, Integer> additions) {
        additions.forEach((denom, count) ->
                current.merge(denom, count, Integer::sum)
        );
    }


    private void subtractDenominations(Map<BigDecimal, Integer> current, Map<BigDecimal, Integer> toSubtract) {

        for (Map.Entry<BigDecimal, Integer> entry : toSubtract.entrySet()) {

            BigDecimal denom = entry.getKey();
            int count = entry.getValue();

            int available = current.getOrDefault(denom, 0);

            if (available < count) {
                throw new IllegalArgumentException("Not enough banknotes of: " + denom);
            }

            current.put(denom, available - count);

            if (current.get(denom) == 0) {
                current.remove(denom);
            }
        }

    }


    public void saveBalancesToFile(Cashier cashier) {
        log.info("Saving current cashier balances to file...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BALANCE_FILE))) {
            writer.write("============= CASHIER DAILY BALANCE =============\n");
            writer.write("Cashier Name     : " + cashier.getName() + "\n");
            writer.write("--------------------------------------------------\n");

            writer.write("Currency         : BGN\n");
            writer.write("Available Amount : " + AMOUNT_FORMAT.format(cashier.getCashBalanceBgn().getAvailableAmount()) + "\n");
            writer.write("Denominations    : " + renderDenominationList(cashier.getCashBalanceBgn().getDenominations()) + "\n\n");

            writer.write("Currency         : EUR\n");
            writer.write("Available Amount : " + AMOUNT_FORMAT.format(cashier.getCashBalanceEur().getAvailableAmount()) + "\n");
            writer.write("Denominations    : " + renderDenominationList(cashier.getCashBalanceEur().getDenominations()) + "\n");
            writer.write("==================================================\n");

        } catch (IOException e) {
            log.error("Error saving balances to file", e);
            throw new RuntimeException("Failed to save balance summary.", e);
        }

    }


    public String printCashierOverview() {
        log.info("Loading cashier balance overview...");

        Cashier cashier = loadCashierFromFile();

        return "Cashier: " + cashier.getName() + System.lineSeparator() +
                cashier.getCashBalanceBgn().getCurrency().name() + "; " +
                AMOUNT_FORMAT.format(cashier.getCashBalanceBgn().getAvailableAmount()) + "; " +
                renderDenominationList(cashier.getCashBalanceBgn().getDenominations()) + System.lineSeparator() +
                cashier.getCashBalanceEur().getCurrency().name() + "; " +
                AMOUNT_FORMAT.format(cashier.getCashBalanceEur().getAvailableAmount()) + "; " +
                renderDenominationList(cashier.getCashBalanceEur().getDenominations()) + System.lineSeparator();
    }


    private Cashier loadCashierFromFile() {
        log.info("Reading cashier data from balance file...");

        Cashier cashier = new Cashier();
        Balance bgn = new Balance();
        bgn.setCurrency(CurrencyType.BGN);

        Balance eur = new Balance();
        eur.setCurrency(CurrencyType.EUR);

        cashier.setCashBalanceBgn(bgn);
        cashier.setCashBalanceEur(eur);


        try (BufferedReader reader = new BufferedReader(new FileReader(BALANCE_FILE))) {

            reader.readLine();

            String nameLine = reader.readLine();

            if (nameLine == null || !nameLine.contains(":")) {
                throw new RuntimeException("Balance file format invalid: cashier name missing.");
            }

            cashier.setName(nameLine.split(":")[1].trim());

            reader.readLine();
            reader.readLine();

            BigDecimal bgnAmount = new BigDecimal(reader.readLine().split(":")[1].trim());
            Map<BigDecimal, Integer> bgnDenoms = extractDenominations(reader.readLine());

            reader.readLine();
            reader.readLine();

            BigDecimal eurAmount = new BigDecimal(reader.readLine().split(":")[1].trim());
            Map<BigDecimal, Integer> eurDenoms = extractDenominations(reader.readLine());

            cashier.getCashBalanceBgn().setAvailableAmount(bgnAmount);
            cashier.getCashBalanceBgn().setDenominations(bgnDenoms);

            cashier.getCashBalanceEur().setAvailableAmount(eurAmount);
            cashier.getCashBalanceEur().setDenominations(eurDenoms);

        } catch (IOException e) {
            log.error("Failed to read cashier data.", e);
            throw new RuntimeException("Unable to read cashier balance file.", e);
        }

        log.info("Cashier data successfully loaded.");
        return cashier;
    }


    private Map<BigDecimal, Integer> extractDenominations(String line) {

        Map<BigDecimal, Integer> denominations = new HashMap<>();

        if (line.contains(":")) {
            String[] breakdowns = line.split(":")[1].trim().split(", ");

            for (String part : breakdowns) {
                String[] pieces = part.split(" x ");
                int count = Integer.parseInt(pieces[0].trim());
                BigDecimal value = new BigDecimal(pieces[1].trim());
                denominations.put(value, count);
            }

        }

        return denominations;
    }

}