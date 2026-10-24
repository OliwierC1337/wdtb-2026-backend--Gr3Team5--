package pl.wsb.rules.examples.cafe.booking.entity;

/** Cykl życia rezerwacji. W bazie zapisywany jako tekst (@Enumerated(EnumType.STRING)). */
public enum ReservationStatus {
    NEW,
    CONFIRMED,
    CANCELLED,
    DONE
}
