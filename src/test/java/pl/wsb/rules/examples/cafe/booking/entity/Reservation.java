package pl.wsb.rules.examples.cafe.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.wsb.rules.examples.cafe.seating.entity.CafeTable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Rezerwacja stolika: obiekt ze statusem, dwie relacje @ManyToOne (gość, stolik), kwota w BigDecimal. */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private CafeTable table;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "deposit", nullable = false, precision = 10, scale = 2)
    private BigDecimal deposit;

    protected Reservation() {
    }

    public Reservation(Guest guest, CafeTable table, LocalDateTime startsAt, Integer partySize, BigDecimal deposit) {
        this.guest = guest;
        this.table = table;
        this.startsAt = startsAt;
        this.partySize = partySize;
        this.status = ReservationStatus.NEW;
        this.deposit = deposit;
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public CafeTable getTable() {
        return table;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }
}
