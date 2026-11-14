package pl.wsb.rules.lab03;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import pl.wsb.rules.core.JpaModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Schemat bazy danych jako diagram ER w składni Mermaid (GitHub rysuje go w plikach .md i w Summary przebiegu).
 * Powstaje z adnotacji JPA odczytanych z plików .class, bez uruchamiania aplikacji, i jest deterministyczny:
 * ten sam kod zawsze daje ten sam tekst, więc plik {@code SCHEMAT.md} można porównać z modelem (R3.10).
 */
final class SchemaDiagram {

    static final String FILE_NAME = "SCHEMAT.md";

    /** Najpierw klucz główny, potem pozostałe kolumny alfabetycznie. */
    private static final Comparator<ColumnInfo> COLUMN_ORDER =
            Comparator.comparing((ColumnInfo c) -> !"PK".equals(c.key())).thenComparing(ColumnInfo::name);

    private SchemaDiagram() {
    }

    /** Jedna kolumna tabeli: typ Javy, nazwa kolumny i znacznik klucza (PK, FK albo pusty). */
    record ColumnInfo(String type, String name, String key) {
    }

    static String render(JpaModel model) {
        Map<String, JavaClass> tables = new TreeMap<>();
        model.entities().forEach(e -> tables.put(tableName(e), e));

        Map<String, List<ColumnInfo>> columns = new TreeMap<>();
        tables.forEach((table, entity) -> columns.put(table, columns(entity)));
        TreeSet<String> relations = new TreeSet<>();
        TreeSet<String> joinTables = new TreeSet<>();
        int manyToOne = 0, oneToMany = 0, oneToOne = 0, manyToMany = 0;
        for (JavaClass entity : tables.values()) {
            String table = tableName(entity);
            for (JavaField f : JpaModel.fields(entity)) {
                if (f.isAnnotatedWith(ManyToOne.class)) {
                    manyToOne++;
                    boolean required = !f.getAnnotationOfType(ManyToOne.class).optional() || !joinColumnNullable(f);
                    relations.add("    %s %s--o{ %s : \"%s\"".formatted(tableName(f.getRawType()), required ? "||" : "|o", table, joinColumn(f)));
                } else if (f.isAnnotatedWith(OneToOne.class)) {
                    oneToOne++;
                    if (f.getAnnotationOfType(OneToOne.class).mappedBy().isBlank()) {
                        boolean required = !f.getAnnotationOfType(OneToOne.class).optional() || !joinColumnNullable(f);
                        relations.add("    %s %s--o| %s : \"%s\"".formatted(tableName(f.getRawType()), required ? "||" : "|o", table, joinColumn(f)));
                    }
                } else if (f.isAnnotatedWith(OneToMany.class)) {
                    oneToMany++;
                    if (f.getAnnotationOfType(OneToMany.class).mappedBy().isBlank() && f.isAnnotatedWith(JoinColumn.class)) {
                        // relacja jednokierunkowa: klucz obcy leży w tabeli podrzędnej, choć pole jest w encji nadrzędnej
                        String child = targetTable(f);
                        String column = joinColumn(f);
                        List<ColumnInfo> childColumns = columns.get(child);
                        if (childColumns != null && childColumns.stream().noneMatch(c -> c.name().equals(column))) {
                            childColumns.add(new ColumnInfo("Long", column, "FK"));
                            childColumns.sort(COLUMN_ORDER);
                        }
                        relations.add("    %s %s--o{ %s : \"%s\"".formatted(table, joinColumnNullable(f) ? "|o" : "||", child, column));
                    }
                } else if (f.isAnnotatedWith(ManyToMany.class)) {
                    manyToMany++;
                    if (f.getAnnotationOfType(ManyToMany.class).mappedBy().isBlank()) {
                        String joinTable = joinTable(f, table);
                        joinTables.add(joinTable);
                        relations.add("    %s }o--o{ %s : \"%s\"".formatted(table, targetTable(f), joinTable));
                    }
                }
            }
        }

        int columnCount = columns.values().stream().mapToInt(List::size).sum();
        StringBuilder out = new StringBuilder();
        out.append("# Schemat bazy danych\n\n");
        out.append("Plik generują reguły projektu (panel Maven → Lifecycle → verify). Nie edytujcie go ręcznie.\n\n");
        out.append("Tabele: %d".formatted(tables.size() + joinTables.size()));
        if (!joinTables.isEmpty()) {
            out.append(" (w tym tabele łączące @ManyToMany: %s)".formatted(String.join(", ", joinTables)));
        }
        out.append(" · kolumny: %d · relacje: %d × @ManyToOne, %d × @OneToMany, %d × @OneToOne, %d × @ManyToMany\n\n"
                .formatted(columnCount, manyToOne, oneToMany, oneToOne, manyToMany));
        out.append("| Tabela | Encja | Kolumny | Klucze obce |\n|---|---|---|---|\n");
        tables.forEach((table, entity) -> {
            List<ColumnInfo> cols = columns.get(table);
            List<String> foreignKeys = cols.stream().filter(c -> "FK".equals(c.key())).map(ColumnInfo::name).toList();
            out.append("| %s | %s | %d | %s |\n".formatted(table, entity.getSimpleName(), cols.size(),
                    foreignKeys.isEmpty() ? "—" : String.join(", ", foreignKeys)));
        });
        out.append("\n```mermaid\nerDiagram\n");
        columns.forEach((table, cols) -> {
            out.append("    ").append(table).append(" {\n");
            cols.forEach(c -> out.append("        %s %s%s\n".formatted(c.type(), c.name(), c.key().isEmpty() ? "" : " " + c.key())));
            out.append("    }\n");
        });
        relations.forEach(r -> out.append(r).append('\n'));
        out.append("```\n");
        return out.toString();
    }

    /** Tabele łączące zadeklarowane przez {@code @ManyToMany} po stronie właściciela (nie mają własnej encji). */
    static List<String> joinTables(JpaModel model) {
        TreeSet<String> names = new TreeSet<>();
        for (JavaField f : model.ownFieldsAnnotatedWith(ManyToMany.class)) {
            if (f.getAnnotationOfType(ManyToMany.class).mappedBy().isBlank()) {
                names.add(joinTable(f, tableName(f.getOwner())));
            }
        }
        return List.copyOf(names);
    }

    /** Kolumny tabeli encji: najpierw klucz główny, potem pozostałe alfabetycznie. */
    static List<ColumnInfo> columns(JavaClass entity) {
        List<ColumnInfo> columns = new ArrayList<>();
        for (JavaField f : JpaModel.fields(entity)) {
            if (f.isAnnotatedWith(Transient.class) || f.getModifiers().contains(JavaModifier.TRANSIENT)
                    || f.isAnnotatedWith(OneToMany.class) || f.isAnnotatedWith(ManyToMany.class)) {
                continue;
            }
            if (f.isAnnotatedWith(Id.class)) {
                columns.add(new ColumnInfo(type(f), columnName(f), "PK"));
            } else if (f.isAnnotatedWith(ManyToOne.class) || f.isAnnotatedWith(OneToOne.class)) {
                if (f.isAnnotatedWith(OneToOne.class) && !f.getAnnotationOfType(OneToOne.class).mappedBy().isBlank()) {
                    continue;
                }
                columns.add(new ColumnInfo("Long", joinColumn(f), "FK"));
            } else if (f.isAnnotatedWith(Embedded.class)) {
                for (JavaField e : JpaModel.fields(f.getRawType())) {
                    columns.add(new ColumnInfo(type(e), columnName(e), ""));
                }
            } else {
                columns.add(new ColumnInfo(type(f), columnName(f), ""));
            }
        }
        columns.sort(COLUMN_ORDER);
        return columns;
    }

    static String tableName(JavaClass entity) {
        return entity.tryGetAnnotationOfType(Table.class).map(Table::name).filter(n -> !n.isBlank())
                .map(SchemaDiagram::identifier).orElseGet(() -> snake(entity.getSimpleName()));
    }

    private static String columnName(JavaField f) {
        return f.tryGetAnnotationOfType(Column.class).map(Column::name).filter(n -> !n.isBlank())
                .map(SchemaDiagram::identifier).orElseGet(() -> snake(f.getName()));
    }

    private static String joinColumn(JavaField f) {
        return f.tryGetAnnotationOfType(JoinColumn.class).map(JoinColumn::name).filter(n -> !n.isBlank())
                .map(SchemaDiagram::identifier).orElseGet(() -> snake(f.getName()) + "_id");
    }

    private static boolean joinColumnNullable(JavaField f) {
        return f.tryGetAnnotationOfType(JoinColumn.class).map(JoinColumn::nullable).orElse(true);
    }

    private static String joinTable(JavaField f, String ownerTable) {
        return f.tryGetAnnotationOfType(JoinTable.class).map(JoinTable::name).filter(n -> !n.isBlank())
                .map(SchemaDiagram::identifier).orElseGet(() -> ownerTable + "_" + snake(f.getName()));
    }

    private static String targetTable(JavaField collection) {
        if (collection.getType() instanceof JavaParameterizedType parameterized && !parameterized.getActualTypeArguments().isEmpty()) {
            return tableName(parameterized.getActualTypeArguments().get(0).toErasure());
        }
        return tableName(collection.getRawType());
    }

    private static String type(JavaField f) {
        String simple = f.getRawType().getSimpleName().replaceAll("[^A-Za-z0-9]", "");
        return simple.isEmpty() ? "Object" : simple;
    }

    private static String snake(String camel) {
        return identifier(camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase());
    }

    private static String identifier(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
