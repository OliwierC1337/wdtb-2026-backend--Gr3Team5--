package pl.wsb.systemobslugiautomyjni.products.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true , length = 80)
    private String name;

    @Column(name = "unit", nullable = false, length = 10)
    private String unit;

    @Column(name = "current_quantity", nullable = false, precision = 10 , scale = 2)
    private BigDecimal current_quantity;

    @Column(name = "min_threshold", nullable = false, precision=10 , scale = 2)
    private BigDecimal min_threshold;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;


    protected products() {
    }

    public products(Long id, String name, String unit, BigDecimal current_quantity, BigDecimal min_threshold, LocalDateTime updated_at) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.current_quantity = current_quantity;
        this.min_threshold = min_threshold;
        this.updated_at = updated_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public BigDecimal getMin_threshold() {
        return min_threshold;
    }

    public BigDecimal getCurrent_quantity() {
        return current_quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
