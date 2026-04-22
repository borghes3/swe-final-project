package it.polimi.ingsw.am23.jsonParsing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResourceLoader {

    public static <T> List<T> loadListFromResource(Gson gson, String resourcePath, Class<T> clazz) throws IOException {
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return loadFromResource(gson, resourcePath, type);
    }

    public static <T> T loadFromResource(Gson gson, String resourcePath, Type type) throws IOException {

        InputStream resourceStream = ResourceLoader.class.getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            throw new FileNotFoundException("Classpath resource not found: " + resourcePath);
        }

        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
            return gson.fromJson(jsonReader, type);
        }
    }
}