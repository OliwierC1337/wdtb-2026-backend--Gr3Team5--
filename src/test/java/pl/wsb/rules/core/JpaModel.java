package pl.wsb.rules.core;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Widok reguł na model danych projektu: encje, ich pola i relacje, odczytane z plików .class
 * przez ArchUnit (bez uruchamiania aplikacji).
 *
 * <p>Model jest generyczny: nie zna domeny zespołu. Jedyna klasa, którą rozpoznaje po nazwie,
 * to gotowe konto użytkownika {@code UserAccount} z szablonu.
 */
public final class JpaModel {

    /** Nazwa gotowej encji z szablonu; jej nie liczymy jako własnej encji zespołu. */
    public static final String USER_ACCOUNT = "UserAccount";

    private final JavaClasses classes;
    private final String rootPackage;

    private JpaModel(JavaClasses classes, String rootPackage) {
        this.classes = classes;
        this.rootPackage = rootPackage;
    }

    /** Model produkcyjnego kodu zespołu ({@code pl.wsb.<projekt>}). */
    public static JpaModel ofProject() {
        return new JpaModel(Project.productionClasses(), Project.rootPackage());
    }

    /** Model dowolnego zestawu klas, np. przykładu w testach reguł. */
    public static JpaModel of(JavaClasses classes, String rootPackage) {
        return new JpaModel(classes, rootPackage);
    }

    public String rootPackage() {
        return rootPackage;
    }

    /** Wszystkie klasy z {@code @Entity}, posortowane po nazwie. */
    public List<JavaClass> entities() {
        return annotated(Entity.class);
    }

    /** Klasy z {@code @Embeddable}. */
    public List<JavaClass> embeddables() {
        return annotated(Embeddable.class);
    }

    /** Encje zespołu: wszystkie poza gotowym {@code UserAccount}. */
    public List<JavaClass> ownEntities() {
        return entities().stream().filter(c -> !isUserAccount(c)).toList();
    }

    /** Encje i klasy wbudowane: wszystko, co opisuje kolumny tabel. */
    public List<JavaClass> mappedClasses() {
        return java.util.stream.Stream.concat(entities().stream(), embeddables().stream()).toList();
    }

    public static boolean isUserAccount(JavaClass javaClass) {
        return USER_ACCOUNT.equals(javaClass.getSimpleName());
    }

    public Optional<String> moduleOf(JavaClass javaClass) {
        return Project.moduleOf(javaClass, rootPackage);
    }

    public Optional<String> layerOf(JavaClass javaClass) {
        return Project.layerOf(javaClass, rootPackage);
    }

    /** Pola instancji klasy (bez statycznych), posortowane po nazwie. */
    public static List<JavaField> fields(JavaClass javaClass) {
        return javaClass.getFields().stream()
                .filter(f -> !f.getModifiers().contains(JavaModifier.STATIC))
                .sorted(Comparator.comparing(JavaField::getName))
                .toList();
    }

    /** Pola własnych encji z daną adnotacją, np. wszystkie {@code @ManyToOne}. */
    public List<JavaField> ownFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
        return ownEntities().stream()
                .flatMap(c -> fields(c).stream())
                .filter(f -> f.isAnnotatedWith(annotation))
                .toList();
    }

    /** Czytelne miejsce w kodzie, np. {@code Reservation.table}. */
    public static String where(JavaField field) {
        return field.getOwner().getSimpleName() + "." + field.getName();
    }

    public static List<String> names(List<JavaClass> classes) {
        return classes.stream().map(JavaClass::getSimpleName).toList();
    }

    private List<JavaClass> annotated(Class<? extends Annotation> annotation) {
        return classes.stream()
                .filter(c -> c.isAnnotatedWith(annotation))
                .sorted(Comparator.comparing(JavaClass::getName))
                .toList();
    }
}
