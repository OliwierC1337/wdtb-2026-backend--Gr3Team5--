package pl.wsb.rules.lab03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.Project;
import pl.wsb.rules.core.SinceLab;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły LAB 03 na ocenę 4: trzy rodzaje relacji i skończony schemat bazy. W GitHub Actions działają w etapie „Ocena 4”,
 * uruchamianym po zielonym etapie „Ocena 3”. Lokalnie {@code mvn verify} uruchamia wszystkie zestawy.
 */
@SinceLab(3)
@Tag("ocena4")
@DisplayName("LAB 03 · trzy rodzaje relacji i skończony schemat (ocena 4)")
class Lab03GradeFourRulesTest {

    @Test
    @DisplayName("R3.03 Co najmniej 2 relacje @ManyToOne we własnych encjach")
    void twoManyToOne() {
        Lab03Checks.manyToOneWithJoinColumn(JpaModel.ofProject(), Lab03Checks.MIN_MANY_TO_ONE_GRADE_FOUR);
    }

    @Test
    @DisplayName("R3.04 Co najmniej jedna relacja @OneToMany, każda z mappedBy albo @JoinColumn")
    void oneToManyIsInverseSide() {
        Lab03Checks.oneToManyIsInverseSide(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.05 Co najmniej jedna relacja @OneToOne, po stronie klucza obcego z @JoinColumn(name = \"…_id\")")
    void oneToOneWithJoinColumn() {
        Lab03Checks.oneToOneWithJoinColumn(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.06 Co najmniej jedna własna encja wskazuje na konto użytkownika (UserAccount)")
    void someEntityReferencesUserAccount() {
        Lab03Checks.someEntityReferencesUserAccount(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.07 Co najmniej 5 własnych tabel (z tabelą łączącą wiele-do-wielu)")
    void enoughTables() {
        Lab03Checks.enoughTables(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.08 Każda własna tabela jest połączona relacją z inną tabelą")
    void everyEntityIsRelated() {
        Lab03Checks.everyEntityIsRelated(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.09 Relacja wiele-do-wielu: encja łącząca z dwiema @ManyToOne albo @ManyToMany z @JoinTable")
    void manyToManyRelation() {
        Optional<String> missing = Lab03Checks.missingManyToMany(JpaModel.ofProject());
        assertThat(missing).withFailMessage(missing.orElse("")).isEmpty();
    }

    @Test
    @DisplayName("R3.10 Schemat widoczny w repozytorium: plik SCHEMAT.md zgodny z encjami")
    void schemaFileIsCurrent() {
        Lab03Checks.schemaFileIsCurrent(JpaModel.ofProject(), Project.file(SchemaDiagram.FILE_NAME),
                "true".equals(System.getenv("GITHUB_ACTIONS")));
    }
}
