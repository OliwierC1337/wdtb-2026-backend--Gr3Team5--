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
-- – ===== LAB 02 · products =====
INSERT INTO products (name, unit, current_quantity, min_threshold, updated_at)
VALUES
('Szampon samochodowy', 'litr', 25.50, 5.00, '2026-10-24 08:00:00'),
('Wosk ochronny', 'litr', 12.00, 3.00, '2026-10-24 09:00:00'),
('Płyn do szyb', 'litr', 2.50, 5.00, '2026-10-24 10:00:00');

INSERT INTO usage (product_id, product_name, quantity_used, unit, used_at)
VALUES
(1, 'Szampon samochodowy', 1.50, 'litr', '2026-10-24 08:30:00'),
(2, 'Wosk ochronny', 0.30, 'litr', '2026-10-24 09:15:00'),
(3, 'Płyn do szyb', 0.50, 'litr', '2026-10-24 10:00:00');

INSERT INTO notifications (product_id, usage_id, type, message, is_sent, created_at, sent_at)
VALUES
    (NULL, NULL, 'INFO', 'Myjnia została uruchomiona.', TRUE, '2026-10-24 08:30:00', '2026-10-24 08:30:05'),
    (3, 3, 'WARNING', 'Niski stan: płyn do szyb (2.5 l).', FALSE, '2026-10-24 09:15:00', NULL),
    (NULL, NULL, 'SUCCESS', 'Mycie pojazdu zostało zakończone.', TRUE, '2026-10-24 10:00:00', '2026-10-24 10:00:10');

INSERT INTO employees (full_name, job_title, phone, hired_at, user_account_id)
VALUES
    ('Tomasz Chrzanowski', 'Operator myjni', '600100200', '2026-09-01 08:00:00', 5),
    ('Oliwier Ciszek', 'Serwisant', NULL, '2026-09-15 08:00:00', 6),
    ('Patryk Bednarek', 'Kierownik zmiany', '600500600', '2026-10-01 08:00:00', NULL);

INSERT INTO notifications_employees (notification_id, employee_id)
VALUES
    (1, 1),
    (2, 1),
    (2, 2),
    (3, 3);