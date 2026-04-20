package it.polimi.ingsw.am23.setup.loader;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JsonDefinitionLoader<T> implements DefinitionLoader<T> {

    private final ObjectMapper objectMapper;
    private final Class<T[]> arrayType;

    public JsonDefinitionLoader(ObjectMapper objectMapper, Class<T[]> arrayType) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.arrayType = Objects.requireNonNull(arrayType, "arrayType cannot be null");
    }

    @Override
    public List<T> loadAll(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath cannot be null");

        try (InputStream inputStream = openResourceStream(resourcePath)) {
            T[] definitions = objectMapper.readValue(inputStream, arrayType);
            return List.copyOf(Arrays.asList(definitions));

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load JSON resource: " + resourcePath, e);
        }
    }

    // fix del loader risorse, aggiunti path di fallback
    private InputStream openResourceStream(String resourcePath) throws IOException {
        InputStream classpathStream = JsonDefinitionLoader.class.getResourceAsStream(resourcePath);
        if (classpathStream != null) {
            return classpathStream;
        }

        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        Path[] fallbackPaths = new Path[] {
                Path.of("src", "main", "resources", normalized),
                Path.of("src", "resources", normalized)
        };

        for (Path fallbackPath : fallbackPaths) {
            if (Files.exists(fallbackPath)) {
                return Files.newInputStream(fallbackPath);
            }
        }

        throw new IllegalArgumentException("Resource not found in classpath or fallback paths: " + resourcePath);
    }
}
