package app.model;

import app.model.enums.*;
import lombok.*;

import java.math.*;
import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation {

    private TransactionType type;
    private CurrencyType currency;
    private BigDecimal amount;
    private Map<BigDecimal, Integer> denominations;
}