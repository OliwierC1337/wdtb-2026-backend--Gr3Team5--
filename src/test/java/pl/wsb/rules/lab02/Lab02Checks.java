package pl.wsb.rules.lab02;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import pl.wsb.rules.core.JpaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sprawdzenia reguł LAB 02 (encje, pakiety, typy kolumn) na modelu {@link JpaModel}. Oddzielone od klasy testowej,
 * żeby ten sam kod działał na projekcie zespołu i na przykładach w {@code pl.wsb.rules.examples}.
 */
final class Lab02Checks {

    static final int MIN_OWN_ENTITIES = 3;
    static final int MIN_COLUMNS = 4;
    static final int MIN_ROWS = 3;

    private Lab02Checks() {
    }

    /** R2.01 */
    static void atLeastThreeOwnEntities(JpaModel model) {
        List<JavaClass> own = model.ownEntities();
        assertThat(own)
                .withFailMessage("Potrzeba co najmniej %d własnych encji (@Entity) poza UserAccount, jest %d: %s. "
                                + "Każda tabela to klasa z @Entity w pakiecie %s.<moduł>.entity.",
                        MIN_OWN_ENTITIES, own.size(), JpaModel.names(own), model.rootPackage())
                .hasSizeGreaterThanOrEqualTo(MIN_OWN_ENTITIES);
    }

    static final String ENTITY_LAYER = "entity";

    /** R2.02: własne encje i klasy wbudowane w pakietach {@code <root>.<moduł>.entity} (gotowe UserAccount zostaje na swoim miejscu). */
    static void entitiesLiveInModuleEntityPackages(JpaModel model) {
        List<String> misplaced = new ArrayList<>();
        for (JavaClass c : model.mappedClasses()) {
            if (JpaModel.isUserAccount(c)) {
                continue;
            }
            boolean ok = model.moduleOf(c).isPresent() && model.layerOf(c).filter(ENTITY_LAYER::equals).isPresent();
            if (!ok) {
                misplaced.add(c.getSimpleName() + " (" + c.getPackageName() + ")");
            }
        }
        assertThat(misplaced)
                .withFailMessage("Encje, klasy @Embeddable i enumy leżą w pakietach %s.<moduł>.entity, np. %s.booking.entity. "
                                + "Przenieście (IntelliJ: Refactor → Move): %s",
                        model.rootPackage(), model.rootPackage(), misplaced)
                .isEmpty();
    }

    /** R2.06 (na ocenę 5), część 2: każde pole enum zapisane jako tekst. */
    static void enumsStoredAsString(JpaModel model) {
        List<String> offenders = new ArrayList<>();
        for (JavaClass c : model.mappedClasses()) {
            if (JpaModel.isUserAccount(c)) {
                continue;
            }
            for (JavaField f : JpaModel.fields(c)) {
                if (!f.getRawType().isEnum()) {
                    continue;
                }
                boolean asString = f.tryGetAnnotationOfType(Enumerated.class)
                        .map(e -> e.value() == EnumType.STRING)
                        .orElse(false);
                if (!asString) {
                    offenders.add(JpaModel.where(f));
                }
            }
        }
        assertThat(offenders)
                .withFailMessage("Enumy w bazie zapisujemy jako tekst, nie numer pozycji (po zmianie kolejności stałych dane "
                        + "przestałyby się zgadzać). Dopiszcie @Enumerated(EnumType.STRING) nad polami: %s", offenders)
                .isEmpty();
    }


    /** R2.04 */
    static void everyOwnEntityHasEnoughColumns(JpaModel model) {
        List<String> tooSmall = new ArrayList<>();
        for (JavaClass c : model.ownEntities()) {
            List<String> columns = columns(c);
            if (columns.size() < MIN_COLUMNS) {
                tooSmall.add(c.getSimpleName() + " " + columns);
            }
        }
        assertThat(tooSmall)
                .withFailMessage("Każda własna tabela ma co najmniej %d kolumny poza id. Za mało kolumn mają: %s. "
                                + "Dopiszcie pola z @Column, np. nazwę, opis, datę, kwotę albo status (typy z części B2 instrukcji).",
                        MIN_COLUMNS, tooSmall)
                .isEmpty();
    }

    /** Kolumny tabeli encji poza id: pola trwałe, a pole @Embedded liczy się jako wszystkie kolumny klasy wbudowanej. */
    static List<String> columns(JavaClass entity) {
        List<String> columns = new ArrayList<>();
        for (JavaField f : JpaModel.fields(entity)) {
            if (f.isAnnotatedWith(Id.class) || f.isAnnotatedWith(Transient.class)
                    || f.getModifiers().contains(JavaModifier.TRANSIENT)
                    || f.isAnnotatedWith(OneToMany.class) || f.isAnnotatedWith(ManyToMany.class)) {
                continue;
            }
            if (f.isAnnotatedWith(Embedded.class)) {
                JpaModel.fields(f.getRawType()).forEach(e -> columns.add(f.getName() + "." + e.getName()));
            } else {
                columns.add(f.getName());
            }
        }
        return columns;
    }

    /** R2.05 */
    static void everyOwnEntityHasRequiredColumn(JpaModel model) {
        List<String> withoutRequired = model.ownEntities().stream()
                .filter(c -> JpaModel.fields(c).stream().noneMatch(Lab02Checks::isRequired))
                .map(JavaClass::getSimpleName)
                .toList();
        assertThat(withoutRequired)
                .withFailMessage("Każda własna tabela ma co najmniej jedną kolumnę wymaganą: @Column(name = \"…\", nullable = false), "
                        + "np. nazwę albo datę, bez której wiersz nie ma sensu. Brak kolumny wymaganej w: %s", withoutRequired)
                .isEmpty();
    }

    private static boolean isRequired(JavaField f) {
        if (f.isAnnotatedWith(Id.class)) {
            return false;
        }
        return f.tryGetAnnotationOfType(Column.class).map(c -> !c.nullable()).orElse(false)
                || f.tryGetAnnotationOfType(JoinColumn.class).map(j -> !j.nullable()).orElse(false);
    }

    /** R2.06 (na ocenę 5), część 1: brak zwraca opis, co dopisać. */
    static Optional<String> missingEnumColumn(JpaModel model) {
        boolean present = model.ownEntities().stream()
                .flatMap(c -> JpaModel.fields(c).stream())
                .anyMatch(f -> f.getRawType().isEnum());
        return present ? Optional.empty() : Optional.of(
                "Na ocenę 5: żadna własna tabela nie ma kolumny typu enum. Dodajcie np. status albo kategorię jako enum "
                        + "z @Enumerated(EnumType.STRING) i stałymi pisanymi wielkimi literami.");
    }

    /** R2.07 (na ocenę 5): brak zwraca opis, co dopisać. */
    static Optional<String> missingEmbedded(JpaModel model) {
        boolean present = model.ownEntities().stream()
                .flatMap(c -> JpaModel.fields(c).stream())
                .anyMatch(f -> f.isAnnotatedWith(Embedded.class) && f.getRawType().isAnnotatedWith(jakarta.persistence.Embeddable.class));
        return present ? Optional.empty() : Optional.of(
                "Na ocenę 5: żadna własna tabela nie używa klasy wbudowanej. Utwórzcie klasę z @Embeddable (np. adres, okres od–do, "
                        + "dane kontaktowe) w pakiecie <moduł>.entity i dodajcie ją do encji jako pole z @Embedded.");
    }
}
