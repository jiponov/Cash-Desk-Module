package app.web.dto;

import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;


@Data
public class OperationRequest {

    @NotBlank(message = "Operation type must not be blank")
    private String type;

    @NotBlank(message = "Currency must not be blank")
    private String currency;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotEmpty(message = "Denominations must not be empty")
    private Map<
            @NotNull(message = "Denomination value must not be null")
                    BigDecimal,

            @NotNull(message = "Count must not be null")
            @Min(value = 1, message = "Each denomination count must be at least 1")
                    Integer> denominations;
}