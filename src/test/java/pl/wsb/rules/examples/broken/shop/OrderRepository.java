package pl.wsb.rules.examples.broken.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Część celowo błędnego przykładu (patrz {@link Order}): repozytorium poza pakietem repository, zapytanie bez relacji. */
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(Order.Status status);
}
