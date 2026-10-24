-- Dane startowe aplikacji. Tabele tworzy Hibernate z encji, a ten plik Spring Boot uruchamia zaraz potem.
-- LAB 01: dopiszcie tu konta wszystkich osób z zespołu (reguła R1.03).
-- LAB 02: dopiszcie dane do swoich tabel (kolejność: najpierw tabele, na które wskazują klucze obce).
insert into user_account (email, display_name, role, created_at) values
    ('ola@student.merito.pl',   'Ola',   'USER',    current_timestamp),
    ('kuba@student.merito.pl',   'Kuba',   'USER',    current_timestamp),
    ('firma@example.com',   'Firma',   'PARTNER',    current_timestamp),
    ('admin@example.com',   'Admin',   'ADMIN',    current_timestamp),
    ('tomasz.chrzanowski@student.merito.pl',   'Tomasz',   'USER',    current_timestamp),
    ('oliwier.ciszek@student.merito.pl',  'Oliwier',  'USER',    current_timestamp),
    ('patryk.bednarek@student.merito.pl',       'Patryk', 'USER', current_timestamp);


-- LAB 01: Wasze konta (zmieńcie na swoje dane, rola USER)
-- insert into user_account (email, display_name, role, created_at) values
--     ('imie.nazwisko@student.merito.pl', 'Imię', 'USER', current_timestamp);
-- ===== LAB 02 · <tabela 1> =====
INSERT INTO `usage` (product_name, quantity_used, unit, used_at)
VALUES
    ('Szampon samochodowy', 1.50, 'litr', '2026-10-24 08:30:00'),
    ('Wosk ochronny', 0.30, 'litr', '2026-10-24 09:15:00'),
    ('Płyn do szyb', 0.50, 'litr', '2026-10-24 10:00:00');
-- ===== LAB 02 · <tabela 2> =====
INSERT INTO products (name, unit, current_quantity, min_threshold, updated_at)
VALUES
    ('Szampon samochodowy', 'litr', 25.50, 5.00, '2026-10-24 08:00:00'),
    ('Wosk ochronny', 'litr', 12.00, 3.00, '2026-10-24 09:00:00'),
    ('Płyn do szyb', 'litr', 2.50, 5.00, '2026-10-24 10:00:00');
-- ===== LAB 02 · <tabela 3> =====
INSERT INTO notifications (type, message, is_sent, created_at, sent_at)
VALUES
    ('INFO', 'Myjnia została uruchomiona.', TRUE, '2026-10-24 08:30:00', '2026-10-24 08:30:05'),
    ('WARNING', 'Brak środka czyszczącego w systemie.', FALSE, '2026-10-24 09:15:00', NULL),
    ('SUCCESS', 'Mycie pojazdu zostało zakończone.', TRUE, '2026-10-24 10:00:00', '2026-10-24 10:00:10');