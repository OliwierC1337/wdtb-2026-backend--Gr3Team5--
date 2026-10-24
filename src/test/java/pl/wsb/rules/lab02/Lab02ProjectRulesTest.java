package pl.wsb.rules.lab02;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import pl.wsb.rules.core.JpaModel;
import pl.wsb.rules.core.ProjectApp;
import pl.wsb.rules.core.SinceLab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reguły LAB 02: pierwsze encje projektu (encje JPA, pakiety, typy kolumn, dane startowe).
 * Sprawdzenia R2.01–R2.02 i R2.04–R2.05 czytają pliki .class (ArchUnit), R2.03 uruchamia aplikację na czystej bazie H2.
 * Enum i klasa wbudowana są warunkami na ocenę 5: {@link Lab02GradeFiveRulesTest}.
 */
@SinceLab(2)
@DisplayName("LAB 02 · Encje i dane startowe")
class Lab02ProjectRulesTest {

    @Test
    @DisplayName("R2.01 Co najmniej 3 własne encje (@Entity) poza UserAccount")
    void atLeastThreeOwnEntities() {
        Lab02Checks.atLeastThreeOwnEntities(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.02 Encje i @Embeddable leżą w pakietach pl.wsb.<projekt>.<moduł>.entity")
    void entitiesLiveInModuleEntityPackages() {
        Lab02Checks.entitiesLiveInModuleEntityPackages(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.03 Dane startowe: aplikacja startuje, a każda własna tabela ma co najmniej 3 wiersze z data.sql")
    void everyOwnTableHasSeedData() {
        List<String> empty = new ArrayList<>();
        List<String> seeded = new ArrayList<>();
        try (ConfigurableApplicationContext context = ProjectApp.startWithHibernateSchema();
             EntityManager em = ProjectApp.entityManagerFactory(context).createEntityManager()) {
            List<EntityType<?>> entities = em.getMetamodel().getEntities().stream()
                    .filter(e -> !JpaModel.USER_ACCOUNT.equals(e.getJavaType().getSimpleName()))
                    .sorted(Comparator.comparing(EntityType::getName))
                    .toList();
            for (EntityType<?> entity : entities) {
                long rows = count(em, entity);
                (rows >= Lab02Checks.MIN_ROWS ? seeded : empty).add(tableName(entity) + " (" + entity.getName() + ", wierszy: " + rows + ")");
            }
        }
        assertThat(empty)
                .withFailMessage("Za mało danych startowych w tabelach: %s. Każda własna tabela potrzebuje co najmniej %d wierszy: "
                                + "dopiszcie insert do src/main/resources/data.sql. Tabele z wystarczającymi danymi: %s",
                        empty, Lab02Checks.MIN_ROWS, seeded)
                .isEmpty();
    }

    @Test
    @DisplayName("R2.04 Każda własna tabela ma co najmniej 4 kolumny poza id")
    void everyOwnEntityHasEnoughColumns() {
        Lab02Checks.everyOwnEntityHasEnoughColumns(JpaModel.ofProject());
    }

    @Test
    @DisplayName("R2.05 Każda własna tabela ma co najmniej jedną kolumnę wymaganą (nullable = false)")
    void everyOwnEntityHasRequiredColumn() {
        Lab02Checks.everyOwnEntityHasRequiredColumn(JpaModel.ofProject());
    }

    private static long count(EntityManager em, EntityType<?> entity) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        query.select(cb.count(query.from(entity)));
        return em.createQuery(query).getSingleResult();
    }

    /** Nazwa tabeli z @Table albo domyślna Spring Boota (snake_case nazwy klasy). */
    private static String tableName(EntityType<?> entity) {
        Table table = entity.getJavaType().getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return entity.getJavaType().getSimpleName()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase();
    }
}
