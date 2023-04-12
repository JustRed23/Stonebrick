package dev.JustRed23.stonebrick.data;

import dev.JustRed23.stonebrick.log.SBLogger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileStructure {

    private static final Logger LOGGER = SBLogger.getLogger(FileStructure.class);

    private static Class<?> fileStructure = DefaultFileStructure.class;
    private static boolean INITIALIZED = false;

    private static final List<Directory> mappedDirectories = new ArrayList<>();
    private static final List<File> mappedFiles = new ArrayList<>();

    private static boolean disabled;

    public static void init() {
        if (disabled)
            return;

        if (INITIALIZED)
            throw new IllegalStateException("FileStructure has already been initialized");
        INITIALIZED = true;
        LOGGER.debug("=====Initializing File Structure=====");
        LOGGER.debug("File Structure: " + fileStructure.getName());
        Arrays.stream(fileStructure.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(dev.JustRed23.stonebrick.data.annotation.Directory.class))
                .forEach(field -> {
                    if (!field.trySetAccessible())
                        return;

                    try {
                        Directory directory = new Directory(Paths.get(field.getAnnotation(dev.JustRed23.stonebrick.data.annotation.Directory.class).path()));
                        mappedDirectories.add(directory);
                        field.set(field.getType(), directory);
                        LOGGER.debug("Added directory {}", directory.getPath());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        Arrays.stream(fileStructure.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(dev.JustRed23.stonebrick.data.annotation.File.class))
                .forEach(field -> {
                    if (!field.trySetAccessible())
                        return;

                    dev.JustRed23.stonebrick.data.annotation.File annotation = field.getAnnotation(dev.JustRed23.stonebrick.data.annotation.File.class);

                    Directory dir = getDirectory(annotation.directory());
                    if (dir == null) {
                        LOGGER.warn("Could not find directory {}", annotation.directory());
                        dir = Directory.ROOT;
                    }

                    try {
                        File file = new File(annotation.name(), dir, annotation.content());
                        dir.getFiles().add(file);
                        mappedFiles.add(file);
                        field.set(field.getType(), file);
                        LOGGER.debug("Added file {}", file.getPath());
                    } catch (IOException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        LOGGER.debug("=====Initialization complete=====");
    }

    public static void discover(Class<?> fileStructure) {
        if (disabled)
            return;

        if (INITIALIZED)
            throw new IllegalStateException("FileStructure has already been initialized");

        if (fileStructure == null)
            throw new IllegalArgumentException("FileStructure cannot be null");
        if (fileStructure == DefaultFileStructure.class)
            throw new IllegalArgumentException("FileStructure cannot be " + DefaultFileStructure.class.getSimpleName());

        if (FileStructure.fileStructure == DefaultFileStructure.class) {
            if (fileStructure.isAnnotationPresent(dev.JustRed23.stonebrick.data.annotation.FileStructure.class)) {
                FileStructure.fileStructure = fileStructure;
            } else
                LOGGER.error("File structure {} is not annotated with @{}", fileStructure.getName(), dev.JustRed23.stonebrick.data.annotation.FileStructure.class.getSimpleName());
        } else throw new RuntimeException("FileStructure already discovered as " + FileStructure.fileStructure.getName());
    }

    public static void disable() {
        disabled = true;
    }

    @Nullable
    public static Directory getDirectory(String name) {
        if (name.isBlank() || name.equals("."))
            return Directory.ROOT;
        return mappedDirectories.stream()
                .filter(directory -> directory.getDirectory().getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static File getFile(String name) {
        if (name.isBlank())
            return null;
        return mappedFiles.stream()
                .filter(file -> file.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
