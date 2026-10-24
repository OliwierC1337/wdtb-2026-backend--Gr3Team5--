package pl.wsb.rules.examples.broken.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.List;

/**
 * Celowo błędna encja do autotestu reguł: zły pakiet (brak .domain), @ManyToOne bez @JoinColumn,
 * enum zapisany jako numer, kwota w double, @OneToMany bez mappedBy.
 */
@Entity
public class Order {

    public enum Status { NEW, PAID }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Customer customer;

    @Enumerated
    private Status status;

    private double total;

    @OneToMany
    private List<OrderLine> lines;
}
