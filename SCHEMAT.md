# Schemat bazy danych

Plik generują reguły projektu (panel Maven → Lifecycle → verify). Nie edytujcie go ręcznie.

Tabele: 6 (w tym tabele łączące @ManyToMany: notifications_employees) · kolumny: 31 · relacje: 2 × @ManyToOne, 1 × @OneToMany, 2 × @OneToOne, 1 × @ManyToMany

| Tabela | Encja | Kolumny | Klucze obce |
|---|---|---|---|
| employees | employees | 6 | user_account_id |
| notifications | notifications | 8 | product_id, usage_id |
| products | products | 6 | — |
| usage | usage | 6 | product_id |
| user_account | UserAccount | 5 | — |

```mermaid
erDiagram
    employees {
        Long id PK
        String full_name
        LocalDateTime hired_at
        String job_title
        String phone
        Long user_account_id FK
    }
    notifications {
        Long id PK
        LocalDateTime created_at
        Boolean is_sent
        String message
        Long product_id FK
        LocalDateTime sent_at
        String type
        Long usage_id FK
    }
    products {
        Long id PK
        BigDecimal current_quantity
        BigDecimal min_threshold
        String name
        String unit
        LocalDateTime updated_at
    }
    usage {
        Long id PK
        Long product_id FK
        String product_name
        BigDecimal quantity_used
        String unit
        LocalDateTime used_at
    }
    user_account {
        Long id PK
        LocalDateTime created_at
        String display_name
        String email
        AccountRole role
    }
    notifications }o--o{ employees : "notifications_employees"
    products |o--o{ notifications : "product_id"
    products ||--o{ usage : "product_id"
    usage |o--o| notifications : "usage_id"
    user_account |o--o| employees : "user_account_id"
```
