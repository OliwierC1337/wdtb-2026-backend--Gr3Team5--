package pl.wsb.systemobslugiautomyjni.notifications.entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "is_sent", nullable = false)
    private Boolean is_sent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "sent_at")
    private LocalDateTime sent_at;


    protected notifications() {
    }

    public notifications(Long id, String type, String message, Boolean is_sent, LocalDateTime created_at, LocalDateTime sent_at) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.is_sent = is_sent;
        this.created_at = created_at;
        this.sent_at = sent_at;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getIs_sent() {
        return is_sent;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getSent_at() {
        return sent_at;
    }
}
