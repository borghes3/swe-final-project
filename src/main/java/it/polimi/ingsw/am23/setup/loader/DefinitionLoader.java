package it.polimi.ingsw.am23.setup.loader;

import java.util.List;

public interface DefinitionLoader<T> {
    List<T> loadAll(String resourcePath);
}