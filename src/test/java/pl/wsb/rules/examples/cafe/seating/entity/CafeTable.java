package pl.wsb.rules.examples.cafe.seating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import pl.wsb.rules.examples.cafe.booking.entity.Reservation;

import java.util.ArrayList;
import java.util.List;

/** Stolik w kawiarni: encja bez relacji „w dół” (klucze obce trzymają rezerwacje). */
@Entity
@Table(name = "cafe_table")
public class CafeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true)
    private Integer number;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "room", nullable = false, length = 40)
    private String room;

    /** Strona odwrotna: nie tworzy kolumny, tylko pozwala przejść od stolika do jego rezerwacji. */
    @OneToMany(mappedBy = "table")
    private List<Reservation> reservations = new ArrayList<>();

    protected CafeTable() {
    }

    public CafeTable(Integer number, Integer seats, String room) {
        this.number = number;
        this.seats = seats;
        this.room = room;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getSeats() {
        return seats;
    }

    public String getRoom() {
        return room;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
