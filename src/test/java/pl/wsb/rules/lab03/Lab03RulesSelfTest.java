package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Autotest reguł LAB 03 (nie dotyczy projektu zespołu): reguły relacji i repozytorium przechodzą dla przykładu kawiarni
 * ({@code pl.wsb.rules.examples.cafe}) i wskazują błędy w celowo złym modelu ({@code pl.wsb.rules.examples.broken}).
 * Uruchamia się razem z regułami LAB 03.
 */
@SinceLab(3)
@DisplayName("Autotest reguł LAB 03 · przykład kawiarni")
class Lab03RulesSelfTest {

    private static final String CAFE = "pl.wsb.rules.examples.cafe";
    private static final String BROKEN = "pl.wsb.rules.examples.broken";
    private static final String NOTHING = "pl.wsb.rules.examples.nothing";

    private static JpaModel model(String rootPackage) {
        return JpaModel.of(new ClassFileImporter().importPackages(rootPackage), rootPackage);
    }

    @Test
    @DisplayName("Przykład kawiarni spełnia reguły na ocenę 3, 4 i 5 (R3.01–R3.12 bez danych i pliku schematu)")
    void cafeSatisfiesRelationRules() {
        JpaModel cafe = model(CAFE);
        assertThatCode(() -> {
            Lab03Checks.manyToOneWithJoinColumn(cafe, Lab03Checks.MIN_MANY_TO_ONE);
            Lab03Checks.manyToOneWithJoinColumn(cafe, Lab03Checks.MIN_MANY_TO_ONE_GRADE_FOUR);
            Lab03Checks.oneToManyIsInverseSide(cafe);
            Lab03Checks.oneToOneWithJoinColumn(cafe);
            Lab03Checks.someEntityReferencesUserAccount(cafe);
            Lab03Checks.enoughTables(cafe);
            Lab03Checks.everyEntityIsRelated(cafe);
            Lab03Checks.toOneRelationsAreLazy(cafe);
            Lab03Checks.repositoryWithQueryThroughRelation(cafe);
        }).doesNotThrowAnyException();
        assertThat(Lab03Checks.missingManyToMany(cafe)).isEmpty();
    }

    @Test
    @DisplayName("Nazwa zapytania pochodnego: warunek zaczyna się od pola relacji")
    void derivedQueryNames() {
        List<String> relations = List.of("guest", "table");
        assertThat(Lab03Checks.relationUsedBy("findByGuest_FullName", relations)).contains("guest");
        assertThat(Lab03Checks.relationUsedBy("findAllByTableNumberOrderByStartsAtDesc", relations)).contains("table");
        assertThat(Lab03Checks.relationUsedBy("countByGuestId", relations)).contains("guest");
        assertThat(Lab03Checks.relationUsedBy("existsByTable_Room", relations)).contains("table");
        assertThat(Lab03Checks.relationUsedBy("findByStatus", relations)).isEmpty();
        assertThat(Lab03Checks.relationUsedBy("findByStartsAtAfter", relations)).isEmpty();
        assertThat(Lab03Checks.relationUsedBy("save", relations)).isEmpty();
    }

    @Test
    @DisplayName("Celowo błędny model jest wskazywany z czytelnym komunikatem")
    void brokenModelIsRejected() {
        JpaModel broken = model(BROKEN);
        assertThatThrownBy(() -> Lab03Checks.manyToOneWithJoinColumn(broken, Lab03Checks.MIN_MANY_TO_ONE))
                .hasMessageContaining("@JoinColumn").hasMessageContaining("Order.customer");
        assertThatThrownBy(() -> Lab03Checks.manyToOneWithJoinColumn(model(NOTHING), Lab03Checks.MIN_MANY_TO_ONE))
                .hasMessageContaining("minimum relacji @ManyToOne we własnych encjach: 1");
        assertThatThrownBy(() -> Lab03Checks.manyToOneWithJoinColumn(model(NOTHING), Lab03Checks.MIN_MANY_TO_ONE_GRADE_FOUR))
                .hasMessageContaining("minimum relacji @ManyToOne we własnych encjach: 2");
        assertThatThrownBy(() -> Lab03Checks.someEntityReferencesUserAccount(broken))
                .hasMessageContaining("UserAccount");
        assertThatThrownBy(() -> Lab03Checks.oneToManyIsInverseSide(broken))
                .hasMessageContaining("mappedBy").hasMessageContaining("Order.lines")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("LoyaltyCard.rewardedLines"));
        assertThat(SchemaDiagram.render(broken))
                .contains("        Long loyalty_card_id FK")
                .contains("    loyalty_card |o--o{ order_line : \"loyalty_card_id\"");
        assertThatThrownBy(() -> Lab03Checks.oneToManyIsInverseSide(model(NOTHING)))
                .hasMessageContaining("co najmniej jednej relacji @OneToMany");
        assertThatThrownBy(() -> Lab03Checks.oneToOneWithJoinColumn(model(NOTHING)))
                .hasMessageContaining("co najmniej jednej relacji @OneToOne");
        assertThatThrownBy(() -> Lab03Checks.oneToOneWithJoinColumn(broken))
                .hasMessageContaining("@JoinColumn").hasMessageContaining("LoyaltyCard.customer");
        assertThatThrownBy(() -> Lab03Checks.enoughTables(model(NOTHING)))
                .hasMessageContaining("co najmniej 5 własnych tabel");
        assertThatThrownBy(() -> Lab03Checks.everyEntityIsRelated(broken))
                .hasMessageContaining("bez żadnej relacji").hasMessageContaining("Coupon");
        assertThat(Lab03Checks.missingManyToMany(broken)).hasValueSatisfying(m -> assertThat(m).contains("wiele-do-wielu"));
        assertThatThrownBy(() -> Lab03Checks.toOneRelationsAreLazy(broken))
                .hasMessageContaining("FetchType.LAZY").hasMessageContaining("Order.customer");
        assertThatThrownBy(() -> Lab03Checks.repositoryWithQueryThroughRelation(broken))
                .hasMessageContaining("<moduł>.repository").hasMessageContaining("OrderRepository");
        assertThatThrownBy(() -> Lab03Checks.repositoryWithQueryThroughRelation(model(NOTHING)))
                .hasMessageContaining("Brak repozytorium");
    }

    @Test
    @DisplayName("Schemat kawiarni: diagram Mermaid z tabelami, kluczami obcymi i relacjami")
    void cafeSchemaDiagram() {
        String diagram = SchemaDiagram.render(model(CAFE));
        assertThat(diagram)
                .contains("```mermaid\nerDiagram")
                .contains("    reservation {\n        Long id PK\n")
                .contains("        Long guest_id FK")
                .contains("    cafe_table ||--o{ reservation : \"table_id\"")
                .contains("    user_account |o--o| guest : \"user_account_id\"")
                .contains("| reservation | Reservation | 7 | guest_id, table_id |")
                .contains("1 × @OneToMany, 1 × @OneToOne");
        assertThat(SchemaDiagram.render(model(CAFE))).isEqualTo(diagram);
    }

    @Test
    @DisplayName("SCHEMAT.md: lokalnie zapisywany, w GitHub Actions porównywany z modelem")
    void schemaFileLifecycle(@TempDir Path dir) throws IOException {
        JpaModel cafe = model(CAFE);
        Path file = dir.resolve(SchemaDiagram.FILE_NAME);
        assertThatThrownBy(() -> Lab03Checks.schemaFileIsCurrent(cafe, file, true))
                .hasMessageContaining("SCHEMAT.md").hasMessageContaining("nie istnieje");
        assertThat(file).doesNotExist();

        Lab03Checks.schemaFileIsCurrent(cafe, file, false);
        assertThat(file).exists();
        assertThatCode(() -> Lab03Checks.schemaFileIsCurrent(cafe, file, true)).doesNotThrowAnyException();

        Files.writeString(file, Files.readString(file).replace("\n", "\r\n"));
        assertThatCode(() -> Lab03Checks.schemaFileIsCurrent(cafe, file, true)).doesNotThrowAnyException();

        Files.writeString(file, "# stary schemat\n");
        assertThatThrownBy(() -> Lab03Checks.schemaFileIsCurrent(cafe, file, true))
                .hasMessageContaining("nie zgadza się z encjami");
        assertThatThrownBy(() -> Lab03Checks.schemaFileIsCurrent(model(NOTHING), file, false))
                .hasMessageContaining("Brak własnych encji");
    }
}
