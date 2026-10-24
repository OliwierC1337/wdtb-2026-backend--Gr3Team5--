# WdTB-2026-Backend

Szablon projektu laboratoryjnego z przedmiotu **Wprowadzenie do technologii backendowych** (Uniwersytet WSB Merito Wrocław).

Materiały przygotował: **Wojciech Koszela**, prowadzący

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Wojciech%20Koszela-0A66C2?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/wkoszela/)
[![GitHub](https://img.shields.io/badge/GitHub-wkoszela-181717?logo=github&logoColor=white)](https://github.com/wkoszela)
[![E-mail](https://img.shields.io/badge/E--mail-wojciech.koszela%40wroclaw.merito.pl-C8102E)](mailto:wojciech.koszela@wroclaw.merito.pl)

**Dokumentacja: [wkoszela.github.io/WdTB-2026-Backend--projectname--](https://wkoszela.github.io/WdTB-2026-Backend--projectname--/)**
(instrukcje: [LAB 01](https://wkoszela.github.io/WdTB-2026-Backend--projectname--/lab01.html),
[LAB 02](https://wkoszela.github.io/WdTB-2026-Backend--projectname--/lab02.html)).
Są tam instrukcje do zajęć, wymagania, zasady zaliczenia i opis reguł projektu.
**Arkusz zespołów (Excel): [otwórz w SharePoint](https://m365ht-my.sharepoint.com/:x:/g/personal/wojciech_koszela_wroclaw_merito_pl/IQCEEPWN_2owS6zMeDfSfeG-AXpOGAC7AgOlj9SAudd8Y0w?e=S2s4H9)**, zakładka Waszej grupy.
Te same strony znajdują się w katalogu [`docs/`](docs/index.html); po sklonowaniu repozytorium można je otworzyć lokalnie w przeglądarce.

## W skrócie

- Zespół 3 osoby, jedno repozytorium: **fork** tego szablonu przemianowany na **`WdTB-2026-Backend-<nazwaprojektu>`**.
- Każdy zespół buduje backend własnego projektu: encje i relacje, repozytoria i zapytania, serwisy, REST API.
- **LAB 02 zaczyna się od prezentacji:** zespół uruchamia aplikację i pokazuje na http://localhost:8080 wypełnioną wizytówkę
  projektu (`src/main/resources/static/index.html`, bez `[uzupełnij]`). Wizytówkę wypełniacie na LAB 01.
- Wymagania każdego laboratorium sprawdzają automatyczne testy (reguły projektu) w GitHub Actions. Tag `lab-N` uruchamia reguły laboratorium N.

| Zajęcia | Temat |
|---|---|
| LAB 01 | Start: projekt, aplikacja i opis pomysłu (bez oceny) |
| LAB 02 | Encje JPA i dane startowe (ocena z pokazanego przebiegu w GitHub Actions) |
| LAB 03 | Repozytoria i zapytania |
| LAB 04 | Serwisy i komponenty |
| LAB 05 | Kontrolery REST |
| LAB 06 | Rozbudowa: zdarzenia, konfiguracja, zadania cykliczne |
| LAB 07 | Odrabianie |

## Fork: własne repozytorium zespołu

Robi to **jedna osoba z zespołu** (osoba hostująca), pozostałe osoby patrzą i dołączają w kroku 4.

1. **Fork.** Na stronie tego repozytorium kliknijcie **Fork** → *Owner*: Wasze konto → **Create fork**.
   Fork to Wasza kopia szablonu na Waszym koncie; w niej pracuje cały zespół.
2. **Zmiana nazwy.** W forku: *Settings → General → Repository name* → zamieńcie końcówkę `-projectname--`
   na nazwę projektu, np. `WdTB-2026-Backend-kawiarnia` → **Rename**. Nazwa projektu: jedno słowo, małe litery,
   bez polskich znaków. Fork możecie zrobić od razu, a nazwę zmieniacie, gdy zespół wybierze nazwę projektu (LAB 01, krok C3).
3. **Włączenie sprawdzania.** Zakładka **Actions** → *I understand my workflows, go ahead and enable them*.
   W forkach GitHub Actions jest domyślnie wyłączony; bez tego reguły nie będą się uruchamiać.
4. **Zespół.** *Settings → Collaborators → Add people*: pozostałe osoby z zespołu, żeby mogły wysyłać zmiany.
   Zaproszone osoby akceptują zaproszenie (e-mail albo https://github.com/notifications). Prowadzącego nie dodajecie:
   repozytorium jest publiczne, wystarczy link w arkuszu.
5. **[Arkusz zespołów](https://m365ht-my.sharepoint.com/:x:/g/personal/wojciech_koszela_wroclaw_merito_pl/IQCEEPWN_2owS6zMeDfSfeG-AXpOGAC7AgOlj9SAudd8Y0w?e=S2s4H9).** Jedna osoba wpisuje cały zespół w zakładce swojej grupy: nazwa projektu, link do repozytorium,
   **krótki opis** (1–2 zdania: jaki problem rozwiązujecie i dla kogo) i dane trzech osób.

O czym łatwo zapomnieć:

- **Nowe materiały.** Na początku każdych zajęć: na stronie forka **Sync fork → Update branch**, potem w IntelliJ
  *Git → Pull*.

## Projekt w IntelliJ IDEA

Wymagania: JDK 21 lub nowsze, IntelliJ IDEA (Maven i Git są wbudowane), konto na GitHubie. Docker nie jest potrzebny.

### Pobranie projektu

1. *File → New → Project from Version Control* (na ekranie powitalnym: **Clone Repository**).
2. URL: `https://github.com/<login>/WdTB-2026-Backend-<nazwaprojektu>.git` → **Clone** → **Trust Project**.
3. IntelliJ rozpozna projekt Maven i pobierze zależności (pasek postępu na dole okna, za pierwszym razem kilka minut).
4. Brak JDK? *File → Project Structure → Project → SDK → Download JDK* → wersja 21 lub nowsza.

### Uruchomienie aplikacji

1. Otwórzcie klasę `ProjektApplication` (po zmianie nazwy: `<NazwaProjektu>Application`).
2. Kliknijcie zieloną strzałkę obok metody `main` → *Run 'ProjektApplication'*.
3. W oknie *Run* na dole poczekajcie na `Started ProjektApplication in … seconds`.
4. Otwórzcie http://localhost:8080 (strona startowa) i http://localhost:8080/h2-console (baza danych:
   JDBC URL `jdbc:h2:file:./data/wdtb`, użytkownik `sa`, hasło puste).

Kolejne uruchomienia: przycisk *Run* (zielona strzałka) na górnym pasku. Po każdej zmianie w kodzie, `data.sql`
albo `application.properties` zrestartujcie aplikację: *Rerun* w oknie *Run*. Zatrzymanie: czerwony kwadrat.

Tabele tworzy Hibernate z encji przy każdym starcie, a potem wczytuje dane z `src/main/resources/data.sql`.
Dane dopisane ręcznie w konsoli H2 znikają po restarcie; to, co ma zostać, dopiszcie do `data.sql`.

| Problem | Rozwiązanie |
|---|---|
| `Port 8080 was already in use` | aplikacja już działa: zatrzymajcie poprzednie uruchomienie (czerwony kwadrat) |
| czerwone importy po zmianie `pom.xml` | panel *Maven* → *Reload All Maven Projects* (albo ikona *Load Maven Changes*) |
| po zmianie nazwy klasy startowej *Run* nie działa | uruchomcie nową klasę strzałką obok `main`; stara konfiguracja wskazuje nieistniejącą klasę |

### Testy i reguły projektu

| Co | Jak |
|---|---|
| testy i reguły dla laboratorium z `<lab>` w `pom.xml` | panel *Maven* (prawa krawędź okna) → *Lifecycle* → dwuklik na *verify* |

Wynik jest w oknie *Run*: przy każdej regule zielony albo czerwony znak, a przy czerwonej opis, co poprawić.
W GitHub Actions przebieg „Testy i reguły” ma dwa etapy: *Ocena 4 · reguły obowiązkowe* i *Ocena 5 · warunki dodatkowe*.

## Co zmodyfikować na LAB 01

Nazwę projektu w repozytorium, pakiecie, klasie startowej i `artifactId` zmieniacie, gdy zespół wybierze nazwę (LAB 01, krok C3).

| Plik | Zmiana | Reguła |
|---|---|---|
| pakiet `systemobslugiautomyjni` | *Refactor → Rename* na pakiecie → `pl.wsb.<nazwaprojektu>` | R1.01 |
| `ProjektApplication.java` | *Refactor → Rename* na klasie → `<NazwaProjektu>Application` | R1.01 |
| `pom.xml`, `<artifactId>` | `wdtb-2026-backend-<nazwaprojektu>` | R1.01 |
| `pom.xml`, `<version>` | `1.0.0-<nrindeksu1>-<nrindeksu2>-<nrindeksu3>` | R1.05 |
| `src/main/resources/data.sql` | konta wszystkich osób z zespołu (wzór w komentarzu na końcu pliku) | R1.03 |
| `src/main/resources/static/index.html` | wizytówka projektu: zastąpcie wszystkie `[uzupełnij]` | R1.04 |

Na LAB 01 wystarczy, że zmiany zacommituje jedna osoba z zespołu. Zmiany wysyłacie bezpośrednio na `main`
(*Git → Commit*, potem *Push*); przed pracą zróbcie *Git → Pull*.
Nie zmieniajcie katalogu `src/test/java/pl/wsb/rules` ani `.github/workflows`: tam są reguły, którymi sprawdzamy projekt.

Szczegóły krok po kroku: `docs/lab01.html`.

## Co zmodyfikować na LAB 02

Instrukcja krok po kroku: `docs/lab02.html`. Zespół pracuje razem **na jednym laptopie**.

| Plik | Zmiana | Reguła |
|---|---|---|
| `pom.xml`, `<lab>` | `1` → `2` (od razu commit i push) | włącza R2.xx |
| `pl.wsb.<projekt>.<moduł>.entity` | 3 własne encje (`@Entity`) bez relacji; enumy i klasy `@Embeddable` w tych samych pakietach | R2.01–R2.02 |
| typy pól | kwoty w `BigDecimal` (zalecenie) | — |
| kolumny | co najmniej 4 kolumny poza `id` w każdej tabeli, w tym co najmniej jedna z `nullable = false` | R2.04–R2.05 |
| `src/main/resources/data.sql` | sekcja per tabela, co najmniej 3 wiersze w każdej tabeli; sprawdzenie w konsoli H2 | R2.03 |
| na ocenę 5 | kolumna typu enum z `@Enumerated(EnumType.STRING)` i klasa `@Embeddable` użyta przez `@Embedded` | R2.06–R2.07 |
| `src/main/resources/static/index.html` | w liście zmian: co powstało na LAB 02 | — |

Na koniec zajęć zespół pokazuje prowadzącemu przebieg „Testy i reguły” w zakładce *Actions* dla ostatniego commita na `main`.
Przebieg można uruchomić ręcznie: *Actions* → *Testy i reguły* → *Run workflow* → numer laboratorium `2`.
Opcjonalnie zespół tworzy tag `lab-2` (okno *Git* → *Log* → prawy przycisk na commicie → *New Tag…*, potem *Git → Push* z *Push Tags*).

## Konwencja nazw

| Gdzie | Wzór |
|---|---|
| repozytorium (fork, po zmianie nazwy) | `WdTB-2026-Backend-<nazwaprojektu>` |
| pakiet Javy | `pl.wsb.<nazwaprojektu>` |
| `artifactId` w `pom.xml` | `wdtb-2026-backend-<nazwaprojektu>` |
| `version` w `pom.xml` | `1.0.0-<nrindeksu1>-<nrindeksu2>-<nrindeksu3>` |
