package pl.wsb.rules.examples.cafe.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.wsb.rules.examples.cafe.account.domain.UserAccount;

/** Gość kawiarni: dane biznesowe osoby, powiązane z kontem logowania (UserAccount). */
@Entity
@Table(name = "guest")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id")
    private UserAccount account;

    @Embedded
    private Address address;

    protected Guest() {
    }

    public Guest(String fullName, String phone, UserAccount account) {
        this.fullName = fullName;
        this.phone = phone;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public UserAccount getAccount() {
        return account;
    }
}
