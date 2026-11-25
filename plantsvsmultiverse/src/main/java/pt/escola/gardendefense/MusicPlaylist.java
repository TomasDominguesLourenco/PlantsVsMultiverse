package pt.escola.gardendefense;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Plays a shuffled round of the bundled tracks without repeating the previous round's last track. */
final class MusicPlaylist {
    private static final String DIRECTORY = "assets/music/";
    private static final String DEFEAT_TRACK = DIRECTORY + "Inthepool.wav";
    private static final Logger LOG = Logger.getLogger(MusicPlaylist.class.getName());
    private final List<String> tracks;
    private final ArrayDeque<String> queue = new ArrayDeque<>();
    private final ObservableDoubleValue volume;
    private final Stage stage;
    private final boolean pauseWhenMinimized;
    private String previous;
    private MediaPlayer player;
    private boolean defeat;

    MusicPlaylist(Stage stage, ObservableDoubleValue volume, boolean pauseWhenMinimized) {
        this.stage = stage;
        this.volume = volume;
        this.pauseWhenMinimized = pauseWhenMinimized;
        this.tracks = new ArrayList<>(discoverTracks());
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> disposePlayer());
        if (pauseWhenMinimized) {
            stage.iconifiedProperty().addListener((_, _, minimized) -> {
                if (player == null) return;
                if (minimized) player.pause();
                else player.play();
            });
        }
    }

    void playNext() {
        if (defeat) return;
        disposePlayer();
        while (!tracks.isEmpty()) {
            String track = nextTrack();
            if (startTrack(track, this::playNext)) {
                previous = track;
                return;
            }
        }
    }

    void playDefeat() {
        defeat = true;
        disposePlayer();
        startTrack(DEFEAT_TRACK, this::disposePlayer);
    }

    void resumePlaylist() {
        if (!defeat) return;
        defeat = false;
        playNext();
    }

    private boolean startTrack(String track, Runnable onEnd) {
        URL resource = MusicPlaylist.class.getResource("/" + track);
        if (resource == null) {
            skip(track, new IOException("Recurso não encontrado"));
            return false;
        }
        try {
            var media = new Media(resource.toExternalForm());
            var next = new MediaPlayer(media);
            player = next;
            next.volumeProperty().bind(volume);
            next.setOnEndOfMedia(() -> {
                if (player == next) onEnd.run();
            });
            next.setOnError(() -> onError(track, next, next.getError()));
            media.setOnError(() -> onError(track, next, media.getError()));
            var error = next.getError() != null ? next.getError() : media.getError();
            if (error != null) throw error;
            if (!pauseWhenMinimized || !stage.isIconified()) next.play();
            return true;
        } catch (MediaException | IllegalArgumentException error) {
            skip(track, error);
            disposePlayer();
            return false;
        }
    }

    private String nextTrack() {
        if (queue.isEmpty()) {
            var round = new ArrayList<>(tracks);
            Collections.shuffle(round);
            if (round.size() > 1 && Objects.equals(round.getFirst(), previous)) {
                Collections.swap(round, 0, 1);
            }
            queue.addAll(round);
        }
        return queue.removeFirst();
    }

    private void onError(String track, MediaPlayer failedPlayer, Throwable error) {
        if (player != failedPlayer) return;
        skip(track, error);
        if (defeat) disposePlayer();
        else playNext();
    }

    private void skip(String track, Throwable error) {
        LOG.log(Level.WARNING, "Não foi possível tocar " + track, error);
        tracks.remove(track);
        queue.remove(track);
    }

    private void disposePlayer() {
        if (player == null) return;
        var old = player;
        player = null;
        old.volumeProperty().unbind();
        old.dispose();
    }

    private static List<String> discoverTracks() {
        try {
            Path source = Path.of(MusicPlaylist.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(source)) {
                Path folder = source.resolve(DIRECTORY);
                if (!Files.isDirectory(folder)) return List.of();
                try (var entries = Files.list(folder)) {
                    return entries.filter(Files::isRegularFile)
                            .map(file -> DIRECTORY + file.getFileName())
                            .filter(MusicPlaylist::isTrack).toList();
                }
            }
            try (var jar = new JarFile(source.toFile())) {
                return jar.stream().filter(entry -> !entry.isDirectory()).map(entry -> entry.getName())
                        .filter(name -> name.startsWith(DIRECTORY))
                        .filter(MusicPlaylist::isTrack).toList();
            }
        } catch (IOException | URISyntaxException error) {
            LOG.log(Level.WARNING, "Não foi possível ler a pasta de música.", error);
            return List.of();
        }
    }

    private static boolean isTrack(String name) {
        if (DEFEAT_TRACK.equalsIgnoreCase(name)) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".wav") || lower.endsWith(".mp3");
    }
}
