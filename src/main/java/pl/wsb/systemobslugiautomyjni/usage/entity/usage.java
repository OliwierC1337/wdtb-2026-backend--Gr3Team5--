package pl.wsb.systemobslugiautomyjni.usage.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage")
public class usage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 80)
    private String product_name;

    @Column(name = "quantity_used", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity_used;

    @Column(name = "unit", nullable = false, length = 10)
    private String unit;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime used_at;




    protected usage() {
    }

    public usage(Long id, String product_name, BigDecimal quantity_used, String unit, LocalDateTime used_at) {
        this.id = id;
        this.product_name = product_name;
        this.quantity_used = quantity_used;
        this.unit = unit;
        this.used_at = used_at;
    }

    public Long getId() {
        return id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public BigDecimal getQuantity_used() {
        return quantity_used;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDateTime getUsed_at() {
        return used_at;
    }
}