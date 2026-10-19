package com.chatapp.storage;

import com.chatapp.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Saves profile pictures and banners to disk and hands back a public URL path such as
 * /uploads/3f2c...e1.gif. The file type is decided from the file's own bytes, never from
 * the name or Content-Type the client sends.
 */
@Service
public class FileStorageService {

    public static final String URL_PREFIX = "/uploads/";

    public enum Kind {
        AVATAR(2L * 1024 * 1024, "2 MB"),
        BANNER(5L * 1024 * 1024, "5 MB");

        final long maxBytes;
        final String label;

        Kind(long maxBytes, String label) {
            this.maxBytes = maxBytes;
            this.label = label;
        }
    }

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload directory " + root, e);
        }
    }

    public Path root() {
        return root;
    }

    /** Validates and stores the upload, returns its URL path. */
    public String save(MultipartFile file, Kind kind) {
        if (file == null || file.isEmpty()) {
            throw bad("No file uploaded");
        }
        if (file.getSize() > kind.maxBytes) {
            throw bad("File is too big (max " + kind.label + ")");
        }

        String ext;
        try (InputStream in = file.getInputStream()) {
            ext = detectExtension(in.readNBytes(12));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (ext == null) {
            throw bad("Only PNG, JPG, WEBP or GIF images are allowed");
        }

        String name = UUID.randomUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return URL_PREFIX + name;
    }

    /** Removes a previously stored file. Ignores anything that isn't one of ours. */
    public void delete(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        Path target = root.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!root.equals(target.getParent())) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // an orphaned file is harmless
        }
    }

    private static String detectExtension(byte[] h) {
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return "png";
        }
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') {
            return "gif";
        }
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private static ApiException bad(String message) {
        return ApiException.badRequest(message);
    }
}