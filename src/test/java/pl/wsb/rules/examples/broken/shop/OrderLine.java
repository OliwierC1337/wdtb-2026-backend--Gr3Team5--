package pl.wsb.rules.examples.broken.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Część celowo błędnego przykładu (patrz {@link Order}). */
@Entity
public class OrderLine {

    @Id
    @GeneratedValue
    private Long id;

    private Integer quantity;
}
