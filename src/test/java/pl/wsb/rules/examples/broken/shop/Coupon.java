package pl.wsb.rules.examples.broken.shop;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Część celowo błędnego przykładu (patrz {@link Order}): tabela bez żadnej relacji. */
@Entity
public class Coupon {

    @Id
    @GeneratedValue
    private Long id;

    private String code;
}
