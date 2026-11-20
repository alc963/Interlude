package com.naugroup3.interlude;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MigrationOrderTest {
    private static final Pattern MIGRATION_NAME = Pattern.compile("V(\\d+)__\\w+\\.sql");

    private static List<String> file_names;

    @BeforeAll
    static void load_migrations() throws IOException, URISyntaxException {
        URL directory = MigrationOrderTest.class.getClassLoader().getResource("db/migration");
        assertThat(directory).as("db/migration on the classpath").isNotNull();
        try(Stream<Path> files = Files.list(Path.of(directory.toURI()))) {
            file_names = files.map(path -> path.getFileName().toString()).sorted().toList();
        }
    }

    private static int version_of(String file_name) {
        Matcher matcher = MIGRATION_NAME.matcher(file_name);
        matcher.matches();
        return Integer.parseInt(matcher.group(1));
    }

    @Test
    void has_migrations() {
        assertThat(file_names).isNotEmpty();
    }

    @Test
    void every_file_follows_naming_convention() {
        assertThat(file_names)
            .as("migrations must be named V<number>__<description>.sql")
            .allMatch(name -> MIGRATION_NAME.matcher(name).matches());
    }

    @Test
    void versions_are_unique() {
        Map<Integer, List<String>> duplicates = file_names.stream()
            .filter(name -> MIGRATION_NAME.matcher(name).matches())
            .collect(Collectors.groupingBy(MigrationOrderTest::version_of))
            .entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(duplicates).as("migrations sharing a version").isEmpty();
    }

    @Test
    void versions_start_at_one_without_gaps() {
        List<Integer> versions = file_names.stream()
            .filter(name -> MIGRATION_NAME.matcher(name).matches())
            .map(MigrationOrderTest::version_of)
            .distinct()
            .sorted()
            .toList();
        List<Integer> expected = IntStream.rangeClosed(1, versions.size()).boxed().toList();
        assertThat(versions).as("migration versions").isEqualTo(expected);
    }
}
