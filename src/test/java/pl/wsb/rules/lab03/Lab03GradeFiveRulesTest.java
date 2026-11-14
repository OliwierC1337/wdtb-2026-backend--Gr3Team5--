package pl.wsb.rules.lab03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

/**
 * Reguły LAB 03 na ocenę 5. W GitHub Actions działają w osobnym etapie „Ocena 5”, uruchamianym dopiero wtedy,
 * gdy etap „Ocena 4” jest zielony. Lokalnie {@code mvn verify} uruchamia wszystkie zestawy.
 */
@SinceLab(3)
@Tag("ocena5")
@DisplayName("LAB 03 · na ocenę 5")
class Lab03GradeFiveRulesTest {

    @Test
    @DisplayName("R3.11 Każda relacja @ManyToOne i @OneToOne ma fetch = FetchType.LAZY")
    void toOneRelationsAreLazy() {
        Lab03Checks.toOneRelationsAreLazy(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R3.12 Repozytorium w pakiecie <moduł>.repository z zapytaniem pochodnym przez relację")
    void repositoryWithQueryThroughRelation() {
        Lab03Checks.repositoryWithQueryThroughRelation(JpaModel.ofProject());
    }
}
