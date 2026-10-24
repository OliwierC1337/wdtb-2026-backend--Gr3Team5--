package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.SinceLab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Autotest reguł LAB 02 (nie dotyczy projektu zespołu): dowód, że reguły przechodzą dla poprawnego modelu
 * (przykład kawiarni w {@code pl.wsb.rules.examples.cafe}) i wskazują błędy w celowo złym modelu
 * ({@code pl.wsb.rules.examples.broken}). Uruchamia się razem z regułami LAB 02.
 */
@SinceLab(2)
@DisplayName("Autotest reguł LAB 02 · przykład kawiarni")
class Lab02RulesSelfTest {

    private static final String CAFE = "pl.wsb.rules.examples.cafe";
    private static final String BROKEN = "pl.wsb.rules.examples.broken";
    private static final String NOTHING = "pl.wsb.rules.examples.nothing";

    private static JpaModel model(String rootPackage) {
        return JpaModel.of(new ClassFileImporter().importPackages(rootPackage), rootPackage);
    }

    @Test
    @DisplayName("Przykład kawiarni spełnia R2.01–R2.02, R2.04–R2.06")
    void cafeSatisfiesStaticRules() {
        JpaModel cafe = model(CAFE);
        assertThatCode(() -> {
            Lab02Checks.atLeastThreeOwnEntities(cafe);
            Lab02Checks.entitiesLiveInModuleEntityPackages(cafe);
            Lab02Checks.enumsStoredAsString(cafe);
            Lab02Checks.everyOwnEntityHasEnoughColumns(cafe);
            Lab02Checks.everyOwnEntityHasRequiredColumn(cafe);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Kolumny klasy @Embeddable liczą się do tabeli encji, relacje @OneToMany nie")
    void columnsCountEmbeddedAndSkipInverseSide() {
        JpaModel cafe = model(CAFE);
        assertThat(Lab02Checks.columns(cafe.entities().stream().filter(c -> c.getSimpleName().equals("Guest")).findFirst().orElseThrow()))
                .contains("fullName", "address.street", "address.city", "address.postalCode")
                .doesNotContain("id", "address");
        assertThat(Lab02Checks.columns(cafe.entities().stream().filter(c -> c.getSimpleName().equals("CafeTable")).findFirst().orElseThrow()))
                .containsExactlyInAnyOrder("active", "number", "room", "seats");
    }

    @Test
    @DisplayName("Na ocenę 5: przykład kawiarni ma enum i @Embedded, błędny model nie ma @Embedded")
    void gradeFiveConditions() {
        JpaModel cafe = model(CAFE);
        assertThat(Lab02Checks.missingEnumColumn(cafe)).isEmpty();
        assertThat(Lab02Checks.missingEmbedded(cafe)).isEmpty();
        JpaModel broken = model(BROKEN);
        assertThat(Lab02Checks.missingEnumColumn(broken)).isEmpty();
        assertThat(Lab02Checks.missingEmbedded(broken)).hasValueSatisfying(m -> assertThat(m).contains("@Embeddable"));
        assertThat(Lab02Checks.missingEnumColumn(model(NOTHING))).hasValueSatisfying(m -> assertThat(m).contains("enum"));
    }

    @Test
    @DisplayName("Celowo błędny model jest wskazywany z czytelnym komunikatem")
    void brokenModelIsRejected() {
        JpaModel broken = model(BROKEN);
        assertThatThrownBy(() -> Lab02Checks.atLeastThreeOwnEntities(model(NOTHING)))
                .hasMessageContaining("co najmniej 3 własnych encji");
        assertThatThrownBy(() -> Lab02Checks.entitiesLiveInModuleEntityPackages(broken))
                .hasMessageContaining("<moduł>.entity").hasMessageContaining("broken.shop");
        assertThatThrownBy(() -> Lab02Checks.enumsStoredAsString(broken))
                .hasMessageContaining("EnumType.STRING").hasMessageContaining("Order.status");
        assertThatThrownBy(() -> Lab02Checks.everyOwnEntityHasEnoughColumns(broken))
                .hasMessageContaining("co najmniej 4 kolumny").hasMessageContaining("Customer [discount]")
                .hasMessageContaining("OrderLine [quantity]").hasMessageContaining("Order [customer, status, total]");
        assertThatThrownBy(() -> Lab02Checks.everyOwnEntityHasRequiredColumn(broken))
                .hasMessageContaining("nullable = false").hasMessageContaining("Customer").hasMessageContaining("OrderLine");
    }
}
