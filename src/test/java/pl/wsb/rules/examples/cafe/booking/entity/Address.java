package pl.wsb.rules.examples.cafe.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Klasa wbudowana: kolumny adresu w tabeli gościa (bez własnej tabeli). */
@Embeddable
public class Address {

    @Column(name = "street", length = 80)
    private String street;

    @Column(name = "city", length = 40)
    private String city;

    @Column(name = "postal_code", length = 6)
    private String postalCode;

    protected Address() {
    }
}
