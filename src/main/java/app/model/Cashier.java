package app.model;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cashier {

    private String name;
    private Balance cashBalanceBgn;
    private Balance cashBalanceEur;
}