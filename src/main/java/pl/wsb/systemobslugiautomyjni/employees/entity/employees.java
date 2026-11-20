package pl.wsb.systemobslugiautomyjni.employees.entity;

import jakarta.persistence.*;
import pl.wsb.systemobslugiautomyjni.account.domain.UserAccount;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class employees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 80)
    private String full_name;

    @Column(name = "job_title", nullable = false, length = 40)
    private String job_title;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "hired_at", nullable = false)
    private LocalDateTime hired_at;

    // relacja do konta użytkownika (R3.06), opcjonalna
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount account;

    protected employees() {
    }

    public employees(Long id, String full_name, String job_title, String phone,
                     LocalDateTime hired_at, UserAccount account) {
        this.id = id;
        this.full_name = full_name;
        this.job_title = job_title;
        this.phone = phone;
        this.hired_at = hired_at;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getJob_title() {
        return job_title;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getHired_at() {
        return hired_at;
    }

    public UserAccount getAccount() {
        return account;
    }
}