package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import pl.wsb.rules.core.JpaModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprawdzenia reguł LAB 03 (relacje między encjami, pierwsze repozytorium) na modelu {@link JpaModel}.
 * Oddzielone od klas testowych, żeby ten sam kod działał na projekcie zespołu i na przykładach
 * w {@code pl.wsb.rules.examples}. R3.02 (dane powiązane) uruchamia aplikację i jest w {@link Lab03ProjectRulesTest}.
 */
final class Lab03Checks {

    /** Minimum na ocenę 3: jedna relacja. */
    static final int MIN_MANY_TO_ONE = 1;
    /** Na ocenę 4: dwie relacje wiele-do-jednego. */
    static final int MIN_MANY_TO_ONE_GRADE_FOUR = 2;
    static final int MIN_TABLES = 5;
    static final String REPOSITORY_LAYER = "repository";
    static final String SPRING_DATA_REPOSITORY = "org.springframework.data.repository.Repository";

    /** Nazwa metody zapytania pochodnego Spring Data: prefiks, opcjonalny dodatek (All, First…), „By”, warunek. */
    private static final Pattern DERIVED_QUERY =
            Pattern.compile("^(find|read|get|query|search|stream|count|exists|delete|remove)(\\w*?)By([A-Z]\\w*)$");

    private Lab03Checks() {
    }

    /** R3.01 (minimum = 1, ocena 3) i R3.03 (minimum = 2, ocena 4). */
    static void manyToOneWithJoinColumn(JpaModel model, int minimum) {
        List<JavaField> manyToOne = model.ownFieldsAnnotatedWith(ManyToOne.class);
        List<String> withoutJoinColumn = manyToOne.stream()
                .filter(f -> f.tryGetAnnotationOfType(JoinColumn.class).map(j -> j.name().isBlank()).orElse(true))
                .map(JpaModel::where)
                .toList();
        assertThat(withoutJoinColumn)
                .withFailMessage("Każde pole @ManyToOne ma @JoinColumn(name = \"<tabela_nadrzędna>_id\"), żeby kolumna klucza obcego "
                        + "miała czytelną nazwę w bazie. Dopiszcie @JoinColumn w: %s", withoutJoinColumn)
                .isEmpty();
        assertThat(manyToOne)
                .withFailMessage("Wymagane minimum relacji @ManyToOne we własnych encjach: %d, jest %d: %s. "
                                + "Relacja wiele-do-jednego to pole typu encji nadrzędnej z @ManyToOne i @JoinColumn, np. w rezerwacji pole stolika: "
                                + "@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = \"table_id\", nullable = false) private CafeTable table;",
                        minimum, manyToOne.size(), manyToOne.stream().map(JpaModel::where).toList())
                .hasSizeGreaterThanOrEqualTo(minimum);
    }

    /**
     * R3.04 (na ocenę 4): relacja jeden-do-wielu bez niezaplanowanej tabeli łączącej. Dwa poprawne zapisy:
     * strona odwrotna relacji dwukierunkowej ({@code mappedBy}) albo relacja jednokierunkowa
     * z kluczem obcym w tabeli podrzędnej ({@code @JoinColumn(name = "…_id")} przy liście).
     */
    static void oneToManyIsInverseSide(JpaModel model) {
        List<JavaField> oneToMany = model.ownFieldsAnnotatedWith(OneToMany.class);
        List<String> offenders = oneToMany.stream()
                .filter(f -> f.getAnnotationOfType(OneToMany.class).mappedBy().isBlank())
                .filter(f -> f.tryGetAnnotationOfType(JoinColumn.class).map(c -> c.name().isBlank()).orElse(true))
                .map(JpaModel::where)
                .toList();
        assertThat(offenders)
                .withFailMessage("@OneToMany bez mappedBy i bez @JoinColumn tworzy dodatkową tabelę łączącą, której nie planowaliście. "
                        + "W: %s dopiszcie mappedBy = \"<nazwa pola @ManyToOne w encji podrzędnej>\" (relacja dwukierunkowa) "
                        + "albo @JoinColumn(name = \"<tabela_nadrzędna>_id\") przy liście (relacja jednokierunkowa, "
                        + "gdy encja podrzędna nie ma pola @ManyToOne).",
                        offenders)
                .isEmpty();
        assertThat(oneToMany)
                .withFailMessage("Potrzeba co najmniej jednej relacji @OneToMany. W encji nadrzędnej dopiszcie listę wierszy podrzędnych, "
                        + "np. w stoliku: @OneToMany(mappedBy = \"table\") private List<Reservation> reservations = new ArrayList<>(); "
                        + "Wartość mappedBy to nazwa pola @ManyToOne w encji podrzędnej.")
                .isNotEmpty();
    }

    /** R3.05 (na ocenę 4) */
    static void oneToOneWithJoinColumn(JpaModel model) {
        List<JavaField> oneToOne = model.ownFieldsAnnotatedWith(OneToOne.class);
        assertThat(oneToOne)
                .withFailMessage("Potrzeba co najmniej jednej relacji @OneToOne: dwa obiekty, które występują zawsze w parze jeden do jednego, "
                        + "np. gość i jego konto użytkownika: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = \"user_account_id\") "
                        + "private UserAccount account;")
                .isNotEmpty();
        List<String> withoutJoinColumn = oneToOne.stream()
                .filter(f -> f.getAnnotationOfType(OneToOne.class).mappedBy().isBlank())
                .filter(f -> f.tryGetAnnotationOfType(JoinColumn.class).map(j -> j.name().isBlank()).orElse(true))
                .map(JpaModel::where)
                .toList();
        assertThat(withoutJoinColumn)
                .withFailMessage("Pole @OneToOne po stronie z kluczem obcym ma @JoinColumn(name = \"<tabela_nadrzędna>_id\"). "
                        + "Dopiszcie @JoinColumn w: %s (strona odwrotna ma zamiast tego mappedBy).", withoutJoinColumn)
                .isEmpty();
    }

    /** R3.06 (na ocenę 4) */
    static void someEntityReferencesUserAccount(JpaModel model) {
        List<String> references = model.ownEntities().stream()
                .flatMap(c -> JpaModel.fields(c).stream())
                .filter(f -> f.isAnnotatedWith(ManyToOne.class) || f.isAnnotatedWith(OneToOne.class))
                .filter(f -> JpaModel.isUserAccount(f.getRawType()))
                .map(JpaModel::where)
                .toList();
        assertThat(references)
                .withFailMessage("Żadna własna encja nie wskazuje na konto użytkownika. Encja osoby korzystającej z aplikacji "
                        + "(klient, gość, autor, właściciel…) ma pole @OneToOne albo @ManyToOne typu UserAccount "
                        + "z @JoinColumn(name = \"user_account_id\"). Konto to logowanie, Wasza encja to dane biznesowe.")
                .isNotEmpty();
    }

    /** R3.08 (na ocenę 4): schemat jest skończony, gdy żadna własna tabela nie zostaje bez połączenia z resztą. */
    static void everyEntityIsRelated(JpaModel model) {
        Set<String> related = new HashSet<>();
        for (JavaClass entity : model.ownEntities()) {
            for (JavaField f : JpaModel.fields(entity)) {
                if (!isRelation(f)) {
                    continue;
                }
                related.add(entity.getName());
                related.add(f.getRawType().getName());
                if (f.getType() instanceof JavaParameterizedType parameterized) {
                    parameterized.getActualTypeArguments().forEach(t -> related.add(t.toErasure().getName()));
                }
            }
        }
        List<String> unrelated = model.ownEntities().stream()
                .filter(c -> !related.contains(c.getName()))
                .map(JavaClass::getSimpleName)
                .toList();
        assertThat(unrelated)
                .withFailMessage("Schemat nie jest skończony: tabele bez żadnej relacji: %s. Każda własna encja ma pole relacji "
                        + "(@ManyToOne, @OneToOne, @OneToMany) albo inna encja wskazuje na nią kluczem obcym.", unrelated)
                .isEmpty();
    }

    /** R3.07 (na ocenę 4): rozmiar skończonego schematu. Tabela łącząca @ManyToMany liczy się jak tabela. */
    static void enoughTables(JpaModel model) {
        List<String> tables = new ArrayList<>(model.ownEntities().stream().map(SchemaDiagram::tableName).toList());
        tables.addAll(SchemaDiagram.joinTables(model));
        assertThat(tables)
                .withFailMessage("Skończony schemat ma co najmniej %d własnych tabel (bez user_account, z tabelą łączącą wiele-do-wielu), "
                                + "jest %d: %s. Dopiszcie brakujące tabele z opisu pomysłu; każda encja ma co najmniej 4 kolumny poza id "
                                + "i 3 wiersze w data.sql (reguły LAB 02).",
                        MIN_TABLES, tables.size(), tables)
                .hasSizeGreaterThanOrEqualTo(MIN_TABLES);
    }

    /**
     * R3.10 (na ocenę 4): schemat jest widoczny w repozytorium jako plik SCHEMAT.md zgodny z kodem.
     * Lokalnie (IntelliJ, Maven) brakujący albo nieaktualny plik jest zapisywany na nowo; w GitHub Actions
     * ({@code ci = true}) niczego nie zapisujemy, tylko porównujemy plik z commita z modelem.
     */
    static void schemaFileIsCurrent(JpaModel model, Path file, boolean ci) {
        assertThat(model.ownEntities())
                .withFailMessage("Brak własnych encji, więc nie ma schematu do narysowania.")
                .isNotEmpty();
        String expected = SchemaDiagram.render(model);
        String actual = null;
        try {
            if (Files.exists(file)) {
                actual = Files.readString(file);
            }
            if (actual != null && normalized(actual).equals(normalized(expected))) {
                return;
            }
            if (!ci) {
                Files.writeString(file, expected);
                return;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertThat(actual)
                .withFailMessage("%s w katalogu głównym projektu %s. Uruchomcie w IntelliJ panel Maven → Lifecycle → verify: "
                                + "reguły zapiszą aktualny schemat do pliku %s. Potem Git → Commit (zaznaczcie ten plik) i Push.",
                        SchemaDiagram.FILE_NAME, actual == null ? "nie istnieje" : "nie zgadza się z encjami w kodzie",
                        SchemaDiagram.FILE_NAME)
                .isNotNull()
                .matches(a -> false);
    }

    private static String normalized(String text) {
        return text.replace("\r", "").strip();
    }

    /** R3.09 (na ocenę 4): brak zwraca opis, co dopisać. */
    static Optional<String> missingManyToMany(JpaModel model) {
        List<JavaField> manyToMany = model.ownFieldsAnnotatedWith(ManyToMany.class);
        List<String> joinEntities = model.ownEntities().stream()
                .filter(c -> JpaModel.fields(c).stream().filter(f -> f.isAnnotatedWith(ManyToOne.class)).count() >= 2)
                .map(JavaClass::getSimpleName)
                .toList();
        if (manyToMany.isEmpty() && joinEntities.isEmpty()) {
            return Optional.of("Brak relacji wiele-do-wielu. Zamodelujcie ją encją łączącą z dwiema relacjami @ManyToOne "
                    + "i własnymi atrybutami (np. ReservationExtra: rezerwacja + dodatek + ilość). "
                    + "Gdy para nie ma własnych danych, można użyć @ManyToMany z @JoinTable(name = \"…\").");
        }
        List<String> withoutJoinTable = manyToMany.stream()
                .filter(f -> f.getAnnotationOfType(ManyToMany.class).mappedBy().isBlank())
                .filter(f -> f.tryGetAnnotationOfType(JoinTable.class).map(j -> j.name().isBlank()).orElse(true))
                .map(JpaModel::where)
                .toList();
        if (!withoutJoinTable.isEmpty()) {
            return Optional.of("Pole @ManyToMany po stronie właściciela ma @JoinTable(name = \"<tabela_a>_<tabela_b>\"), "
                    + "żeby tabela łącząca miała zaplanowaną nazwę. Dopiszcie @JoinTable w: " + withoutJoinTable);
        }
        return Optional.empty();
    }

    /** R3.11 (na ocenę 5): każda relacja do jednego wiersza pobierana leniwie. */
    static void toOneRelationsAreLazy(JpaModel model) {
        List<String> eager = new ArrayList<>();
        for (JavaField f : model.ownFieldsAnnotatedWith(ManyToOne.class)) {
            if (f.getAnnotationOfType(ManyToOne.class).fetch() != FetchType.LAZY) {
                eager.add(JpaModel.where(f));
            }
        }
        for (JavaField f : model.ownFieldsAnnotatedWith(OneToOne.class)) {
            if (f.getAnnotationOfType(OneToOne.class).fetch() != FetchType.LAZY) {
                eager.add(JpaModel.where(f));
            }
        }
        assertThat(eager)
                .withFailMessage("Relacje @ManyToOne i @OneToOne domyślnie pobierają wiersz nadrzędny od razu (EAGER), także wtedy, "
                        + "gdy nikt o niego nie pyta. Dopiszcie fetch = FetchType.LAZY w: %s", eager)
                .isEmpty();
    }

    /** R3.12 (na ocenę 5): repozytorium Spring Data z zapytaniem pochodnym, które przechodzi przez relację. */
    static void repositoryWithQueryThroughRelation(JpaModel model) {
        List<JavaClass> repositories = model.classes().stream()
                .filter(JavaClass::isInterface)
                .filter(c -> c.isAssignableTo(SPRING_DATA_REPOSITORY))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
        assertThat(repositories)
                .withFailMessage("Brak repozytorium. Repozytorium to interfejs w pakiecie %s.<moduł>.repository, np. "
                        + "public interface ReservationRepository extends JpaRepository<Reservation, Long> { … }",
                        model.rootPackage())
                .isNotEmpty();

        List<String> misplaced = repositories.stream()
                .filter(c -> model.layerOf(c).filter(REPOSITORY_LAYER::equals).isEmpty())
                .map(c -> c.getSimpleName() + " (" + c.getPackageName() + ")")
                .toList();
        assertThat(misplaced)
                .withFailMessage("Repozytoria leżą w pakietach %s.<moduł>.repository, obok pakietu entity tego modułu. "
                        + "Przenieście (IntelliJ: Refactor → Move): %s", model.rootPackage(), misplaced)
                .isEmpty();

        List<String> found = new ArrayList<>();
        List<String> hints = new ArrayList<>();
        for (JavaClass repository : repositories) {
            Optional<JavaClass> entity = managedEntity(repository);
            if (entity.isEmpty()) {
                hints.add(repository.getSimpleName() + ": nie widać encji w JpaRepository<Encja, Long>");
                continue;
            }
            List<String> relations = JpaModel.fields(entity.get()).stream()
                    .filter(Lab03Checks::isRelation)
                    .map(JavaField::getName)
                    .toList();
            for (JavaMethod method : repository.getMethods()) {
                Optional<String> relation = relationUsedBy(method.getName(), relations);
                if (relation.isPresent()) {
                    found.add(repository.getSimpleName() + "." + method.getName() + " (przez " + relation.get() + ")");
                }
            }
            if (relations.isEmpty()) {
                hints.add(repository.getSimpleName() + ": encja " + entity.get().getSimpleName() + " nie ma relacji");
            } else {
                hints.add(repository.getSimpleName() + ": relacje encji " + entity.get().getSimpleName() + " to " + relations);
            }
        }
        assertThat(found)
                .withFailMessage("Żadna metoda repozytorium nie pyta przez relację. Dopiszcie zapytanie pochodne, którego nazwa zaczyna się "
                        + "od pola relacji, np. List<Reservation> findByGuest_FullName(String fullName) albo "
                        + "List<Reservation> findByTable_Number(Integer number). Spring Data buduje SQL z nazwy metody. %s", hints)
                .isNotEmpty();
    }

    /** Encja z pierwszego argumentu typowego {@code JpaRepository<Encja, Long>} (albo innego interfejsu Spring Data). */
    static Optional<JavaClass> managedEntity(JavaClass repository) {
        for (JavaType type : repository.getInterfaces()) {
            if (type instanceof JavaParameterizedType parameterized
                    && parameterized.toErasure().isAssignableTo(SPRING_DATA_REPOSITORY)
                    && !parameterized.getActualTypeArguments().isEmpty()) {
                return Optional.of(parameterized.getActualTypeArguments().get(0).toErasure());
            }
        }
        return Optional.empty();
    }

    /** Nazwa pola relacji, od którego zaczyna się warunek w nazwie metody, np. {@code findByGuest_FullName} → {@code guest}. */
    static Optional<String> relationUsedBy(String methodName, List<String> relations) {
        Matcher m = DERIVED_QUERY.matcher(methodName);
        if (!m.matches()) {
            return Optional.empty();
        }
        String condition = m.group(3);
        return relations.stream()
                .filter(r -> condition.startsWith(Character.toUpperCase(r.charAt(0)) + r.substring(1)))
                .findFirst();
    }

    private static boolean isRelation(JavaField f) {
        return f.isAnnotatedWith(ManyToOne.class) || f.isAnnotatedWith(OneToOne.class)
                || f.isAnnotatedWith(OneToMany.class) || f.isAnnotatedWith(ManyToMany.class);
    }
}
