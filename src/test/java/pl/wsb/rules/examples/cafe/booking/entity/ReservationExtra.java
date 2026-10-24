package pl.wsb.rules.examples.cafe.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Encja łącząca: relacja wiele-do-wielu rezerwacja ↔ dodatek z własnym atrybutem (ilość).
 * Dwie relacje @ManyToOne zamiast @ManyToMany, bo para ma własne dane.
 */
@Entity
@Table(name = "reservation_extra",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "extra_id"}))
public class ReservationExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "extra_id", nullable = false)
    private Extra extra;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "note", length = 120)
    private String note;

    protected ReservationExtra() {
    }

    public ReservationExtra(Reservation reservation, Extra extra, Integer quantity) {
        this.reservation = reservation;
        this.extra = extra;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Extra getExtra() {
        return extra;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
