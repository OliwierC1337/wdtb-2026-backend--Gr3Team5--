package pl.wsb.systemobslugiautomyjni.notifications.entity;
import jakarta.persistence.*;
import pl.wsb.systemobslugiautomyjni.products.entity.products;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pl.wsb.systemobslugiautomyjni.employees.entity.employees;
import pl.wsb.systemobslugiautomyjni.usage.entity.usage;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private products product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_id", unique = true)
    private usage source_usage;

    @ManyToMany
    @JoinTable(name = "notifications_employees",
            joinColumns = @JoinColumn(name = "notification_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private List<employees> recipients = new ArrayList<>();

    protected notifications() {
    }

    public notifications(Long id, products product, usage source_usage, String type, String message, Boolean is_sent, LocalDateTime created_at, LocalDateTime sent_at) {
        this.id = id;
        this.product = product;
        this.source_usage = source_usage;
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
    public products getProduct() {
        return product; }

    public usage getSource_usage() {
        return source_usage;
    }

    public List<employees> getRecipients() {
        return recipients;
    }
}
