package app.model;

import app.model.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Balance {

    private CurrencyType currency;
    private BigDecimal availableAmount;
    private Map<BigDecimal, Integer> denominations;
}