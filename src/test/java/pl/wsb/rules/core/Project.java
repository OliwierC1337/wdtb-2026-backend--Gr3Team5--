package pl.wsb.rules.core;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.fail;

/**
 * Wspólny punkt wejścia reguł projektu.
 *
 * <p>Reguły są generyczne: nie znają domeny Waszego projektu. Wszystko wyznaczają z konwencji:
 * <pre>
 * pl.wsb.&lt;projekt&gt;                      ← klasa z @SpringBootApplication
 * pl.wsb.&lt;projekt&gt;.&lt;moduł&gt;.entity        ← encje, @Embeddable, enumy
 * pl.wsb.&lt;projekt&gt;.&lt;moduł&gt;.application   ← serwisy, przypadki użycia     (LAB 03)
 * pl.wsb.&lt;projekt&gt;.&lt;moduł&gt;.api           ← publiczny kontrakt modułu     (LAB 03)
 * pl.wsb.&lt;projekt&gt;.&lt;moduł&gt;.web           ← kontrolery REST, DTO          (LAB 04)
 * pl.wsb.&lt;projekt&gt;.&lt;moduł&gt;.infrastructure← integracje z otoczeniem      (LAB 05)
 * pl.wsb.&lt;projekt&gt;.shared                 ← kod współdzielony przez moduły
 * </pre>
 */
public final class Project {

    public static final String BASE_PACKAGE = "pl.wsb";
    public static final Set<String> SHARED_MODULES = Set.of("shared", "common", "config");

    private static JavaClasses productionClasses;
    private static JavaClasses testClasses;
    private static Class<?> applicationClass;

    private Project() {
    }

    /**
     * Numer laboratorium. W Mavenie i GitHub Actions z parametru {@code -Dlab} (z tagu lab-N albo z pom.xml).
     * Gdy testy uruchamia IntelliJ (zielona strzałka przy klasie testowej), zawsze z {@code <lab>} w pom.xml na dysku:
     * IntelliJ przekazuje {@code -Dlab} z ostatniego przeładowania Mavena, więc po zmianie pom.xml bywa nieaktualny.
     * Domyślnie 1.
     */
    public static int currentLab() {
        if (runByIntelliJ()) {
            java.util.Optional<Integer> fromPom = labFromPom();
            if (fromPom.isPresent()) {
                return fromPom.get();
            }
        }
        String fromProperty = System.getProperty("lab");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return Integer.parseInt(fromProperty.trim());
        }
        return labFromPom().orElse(1);
    }

    private static boolean runByIntelliJ() {
        return System.getProperty("sun.java.command", "").contains("com.intellij.rt.");
    }

    private static java.util.Optional<Integer> labFromPom() {
        Path pom = projectDir().resolve("pom.xml");
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("<lab>\\s*(\\d+)\\s*</lab>")
                    .matcher(java.nio.file.Files.readString(pom));
            return m.find() ? java.util.Optional.of(Integer.parseInt(m.group(1))) : java.util.Optional.empty();
        } catch (java.io.IOException | RuntimeException e) {
            return java.util.Optional.empty();
        }
    }

    public static Path projectDir() {
        return Path.of(System.getProperty("project.dir", ".")).toAbsolutePath().normalize();
    }

    public static synchronized JavaClasses productionClasses() {
        if (productionClasses == null) {
            productionClasses = new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                    .importPackages(BASE_PACKAGE);
        }
        return productionClasses;
    }

    /** Testy napisane przez zespół (bez reguł projektu). */
    public static synchronized JavaClasses teamTestClasses() {
        if (testClasses == null) {
            testClasses = new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                    .withImportOption(location -> !location.contains("/pl/wsb/rules/"))
                    .importPackages(rootPackage());
        }
        return testClasses;
    }

    /**
     * Klasa z {@code @SpringBootApplication}, wyszukana skanerem Springa (bez ArchUnit, żeby LAB 01
     * działał tylko na Spring Boot).
     */
    public static synchronized Class<?> applicationClass() {
        if (applicationClass == null) {
            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
            List<String> candidates = scanner.findCandidateComponents(BASE_PACKAGE).stream()
                    .map(BeanDefinition::getBeanClassName)
                    .filter(name -> !name.startsWith(BASE_PACKAGE + ".rules."))
                    .distinct()
                    .toList();
            if (candidates.size() != 1) {
                fail("Oczekiwano dokładnie jednej klasy z @SpringBootApplication w pakiecie pl.wsb, znaleziono: " + candidates);
            }
            try {
                applicationClass = Class.forName(candidates.get(0));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return applicationClass;
    }

    /** Np. {@code pl.wsb.kawiarnia}. */
    public static String rootPackage() {
        return applicationClass().getPackageName();
    }

    /** Nazwa modułu klasy (pierwszy segment pod pakietem głównym) albo pusty, gdy klasa leży w pakiecie głównym. */
    public static Optional<String> moduleOf(JavaClass javaClass) {
        return moduleOf(javaClass, rootPackage());
    }

    /** Jak {@link #moduleOf(JavaClass)}, ale względem podanego pakietu głównego (np. dla przykładów w testach reguł). */
    public static Optional<String> moduleOf(JavaClass javaClass, String rootPackage) {
        String root = rootPackage + ".";
        String pkg = javaClass.getPackageName();
        if (!pkg.startsWith(root)) {
            return Optional.empty();
        }
        String rest = pkg.substring(root.length());
        int dot = rest.indexOf('.');
        return Optional.of(dot < 0 ? rest : rest.substring(0, dot));
    }

    /** Warstwa klasy w module (domain/application/api/web/infrastructure) albo pusty. */
    public static Optional<String> layerOf(JavaClass javaClass) {
        return layerOf(javaClass, rootPackage());
    }

    /** Jak {@link #layerOf(JavaClass)}, ale względem podanego pakietu głównego. */
    public static Optional<String> layerOf(JavaClass javaClass, String rootPackage) {
        String root = rootPackage + ".";
        String pkg = javaClass.getPackageName();
        if (!pkg.startsWith(root)) {
            return Optional.empty();
        }
        String[] parts = pkg.substring(root.length()).split("\\.");
        return parts.length >= 2 ? Optional.of(parts[1]) : Optional.empty();
    }

    public static Set<String> modules() {
        Set<String> modules = new TreeSet<>();
        productionClasses().forEach(c -> moduleOf(c).ifPresent(modules::add));
        return modules;
    }

    public static Path file(String relative) {
        return projectDir().resolve(relative);
    }

    public static String read(String relative) {
        Path path = file(relative);
        if (!Files.exists(path)) {
            fail("Brak pliku " + relative);
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Properties applicationProperties() {
        Properties properties = new Properties();
        Path path = file("src/main/resources/application.properties");
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return properties;
    }
}
