package pl.wsb.rules.lab02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły LAB 02 na ocenę 5. W GitHub Actions działają w osobnym etapie „Ocena 5”, uruchamianym dopiero wtedy,
 * gdy etap „Ocena 4” (reguły obowiązkowe) jest zielony. Lokalnie {@code mvn verify} uruchamia oba zestawy.
 */
@SinceLab(2)
@Tag("ocena5")
@DisplayName("LAB 02 · na ocenę 5")
class Lab02GradeFiveRulesTest {

    @Test
    @DisplayName("R2.06 Kolumna typu enum zapisana jako tekst: @Enumerated(EnumType.STRING)")
    void enumColumn() {
        JpaModel model = JpaModel.ofProject();
        met(Lab02Checks.missingEnumColumn(model));
        Lab02Checks.enumsStoredAsString(model);
    }

    @Test
    @DisplayName("R2.07 Klasa wbudowana: pole @Embedded typu klasy @Embeddable")
    void embeddedClass() {
        met(Lab02Checks.missingEmbedded(JpaModel.ofProject()));
    }

    private static void met(Optional<String> missing) {
        assertThat(missing).withFailMessage(missing.orElse("")).isEmpty();
    }
}
