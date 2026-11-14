package pl.wsb.rules.examples.broken.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

/** Część celowo błędnego przykładu (patrz {@link Order}): relacja @OneToOne bez @JoinColumn. */
@Entity
public class LoyaltyCard {

    @Id
    @GeneratedValue
    private Long id;

    private Integer points;

    @OneToOne
    private Customer customer;

    /** Poprawna relacja jednokierunkowa: klucz obcy loyalty_card_id w tabeli podrzędnej, bez tabeli łączącej. */
    @OneToMany
    @JoinColumn(name = "loyalty_card_id")
    private java.util.List<OrderLine> rewardedLines;
}
