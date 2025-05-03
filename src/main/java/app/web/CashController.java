package app.web;

import app.service.CashService;
import app.shared.config.AppProperties;
import app.web.dto.OperationRequest;
import lombok.extern.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/*
REST контролер за операции на касата
Работи с POST заявки за депозити и тегления (deposits and withdrawals) и GET заявка за текущо състояние
*/
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class CashController {

    private final CashService cashService;
    private final AppProperties appProperties;


    public CashController(CashService cashService, AppProperties appProperties) {
        this.cashService = cashService;
        this.appProperties = appProperties;
    }


    // POST заявка за обработка на касова операция (deposits and withdrawals)
    // Валидира API ключ и входящите данни, и изпраща отговор
    @PostMapping("/cash-operation")
    public ResponseEntity<String> handleCashOperation(
            @RequestHeader("FIB-X-AUTH") String apiKey,
            @Valid @RequestBody OperationRequest operationRequest) {

        log.info("Received cash operation request: Type={}, Currency={}, Amount={}",
                operationRequest.getType(),
                operationRequest.getCurrency(),
                operationRequest.getAmount());

        // Проверка за валиден API ключ
        if (!apiKey.equals(appProperties.getKey())) {
            log.warn("Unauthorized attempt with invalid API key.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Access denied: Invalid API key.");
        }


        try {
            // Извършване на операцията
            String resultMessage = cashService.handleCashRequest(operationRequest);

            log.info("Cash operation processed successfully.");

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success: " + resultMessage);


        } catch (Exception ex) {
            log.error("Error during cash operation: {}", ex.getMessage());

            // Грешка при обработка
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Operation failed: " + ex.getMessage());
        }
    }


    // GET заявка за преглед на текущия баланс на касата
    @GetMapping("/cash-balance")
    public ResponseEntity<String> getCashBalance(
            @RequestHeader("FIB-X-AUTH") String apiKey) {

        log.info("Received request for current cashier balance.");

        // Проверка на API ключ
        if (!apiKey.equals(appProperties.getKey())) {
            log.warn("Unauthorized balance request attempt.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Please check your API key.");
        }

        // Извличане на инфо за баланс
        String overview = cashService.printCashierOverview();
        log.info("Cashier balance successfully retrieved.");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(overview);
    }

}