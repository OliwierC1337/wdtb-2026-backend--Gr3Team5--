package pl.wsb.rules.examples.cafe.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.rules.examples.cafe.booking.entity.Reservation;

import java.util.List;

/**
 * Repozytorium rezerwacji: interfejs bez implementacji, Spring Data buduje zapytania z nazw metod.
 * Nazwa {@code findByGuest_FullName} przechodzi przez relację {@code Reservation.guest} do kolumny {@code guest.full_name}.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByGuest_FullName(String fullName);

    long countByTable_Number(Integer number);
}
