package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import academy.backend.market_pulse.repository.store.WatchlistStore;

/**
 * Хранит watchlist в обычном текстовом файле: один тикер на строку. Строки пишутся в том порядке,
 * в котором тикеры лежат в наборе, а при чтении складываются в {@link LinkedHashSet} — поэтому
 * порядок добавления сохраняется.
 */
public class WatchlistStoreSkeleton implements WatchlistStore {

    @Override
    public void save(Path path, Set<String> tickers) {
        List<String> lines = new ArrayList<>(tickers);
        try {
            createParentDirectories(path);
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить watchlist в файл " + path, e);
        }
    }

    @Override
    public Set<String> load(Path path) {
        Set<String> tickers = new LinkedHashSet<>();
        if (!Files.exists(path)) {
            return tickers;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String ticker = line.trim();
                if (!ticker.isEmpty()) {
                    tickers.add(ticker);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать watchlist из файла " + path, e);
        }
        return tickers;
    }

    private void createParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}