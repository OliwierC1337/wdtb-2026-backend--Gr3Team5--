package pl.wsb.rules.examples.cafe.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uproszczona kopia konta użytkownika z szablonu. Przykład nie może wskazywać na klasę z kodu produkcyjnego,
 * bo każdy zespół ma ją w innym pakiecie (pl.wsb.&lt;projekt&gt;.account.domain).
 */
@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    protected UserAccount() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
