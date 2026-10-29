package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * Хранит историю цен: на каждую точку — отдельный маленький текстовый файл. Файлы одного тикера
 * лежат в своей подпапке, имя файла — время точки в миллисекундах:
 * <pre>
 * directory/SBER/1000.txt   содержимое: SBER;1970-01-01T00:00:01Z;250.00
 * directory/SBER/2000.txt   содержимое: SBER;1970-01-01T00:00:02Z;251.50
 * </pre>
 * Если файл с таким именем уже есть (две точки в одну миллисекунду), к имени добавляется
 * номер: {@code 2000_1.txt}, {@code 2000_2.txt} — старый файл не перезаписывается.
 */
public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    private static final String SEPARATOR = ";";
    private static final String EXTENSION = ".txt";

    @Override
    public Path append(Path directory, PricePoint point) {
        Path tickerDirectory = directory.resolve(point.ticker());
        String content = point.ticker() + SEPARATOR
                + point.timestamp() + SEPARATOR
                + point.price().toPlainString();
        try {
            Files.createDirectories(tickerDirectory);
            Path file = findFreeFileName(tickerDirectory, point.timestamp());
            Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать точку истории для " + point.ticker(), e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        List<PricePoint> points = new ArrayList<>();
        Path tickerDirectory = directory.resolve(ticker);
        if (!Files.isDirectory(tickerDirectory)) {
            return points;
        }
        try (Stream<Path> files = Files.list(tickerDirectory)) {
            List<Path> fileList = files
                    .filter(file -> file.getFileName().toString().endsWith(EXTENSION))
                    .toList();
            for (Path file : fileList) {
                points.add(readPoint(file));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю для " + ticker, e);
        }
        points.sort(Comparator.comparing(PricePoint::timestamp));
        return points;
    }

    private Path findFreeFileName(Path tickerDirectory, Instant timestamp) {
        String baseName = String.valueOf(timestamp.toEpochMilli());
        Path file = tickerDirectory.resolve(baseName + EXTENSION);
        int counter = 1;
        while (Files.exists(file)) {
            file = tickerDirectory.resolve(baseName + "_" + counter + EXTENSION);
            counter++;
        }
        return file;
    }

    private PricePoint readPoint(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8).trim();
        String[] parts = content.split(SEPARATOR);
        if (parts.length != 3) {
            throw new IllegalStateException("Неправильный файл истории: " + file);
        }
        String ticker = parts[0];
        Instant timestamp = Instant.parse(parts[1]);
        BigDecimal price = new BigDecimal(parts[2]);
        return new PricePoint(ticker, timestamp, price);
    }
}
