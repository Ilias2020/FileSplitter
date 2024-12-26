package com.il.FileSplitter.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Slf4j
public class FileCopier {

    @Value("${input.dir}")
    private String inputDir;

    @Value("${output.dir}")
    private String outputDir;

    public void copyAndCleanFiles() throws IOException {
        File inputDirectory = new File(inputDir);
        File outputDirectory = new File(outputDir);

        if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
            throw new IOException("Входная директория не существует или не является директорией: " + inputDir);
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IOException("Не удалось создать выходную директорию: " + outputDir);
        }

        File[] files = inputDirectory.listFiles();
        if (files == null || files.length == 0) {
            log.info("Входная директория пуста: {}", inputDir);
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                log.info("Обработка файла: {}", file.getName());

                String fileNameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));
                File outputSubDir = new File(outputDirectory, fileNameWithoutExtension);
                if (!outputSubDir.exists() && !outputSubDir.mkdirs()) {
                    throw new IOException("Не удалось создать папку для файла: " + outputSubDir.getAbsolutePath());
                }

                File outputFile = new File(outputSubDir, file.getName());
                cleanAndCopyFile(file, outputFile);

                log.info("Файл скопирован и очищен: {}", outputFile.getAbsolutePath());
            }
        }
    }

    private void cleanAndCopyFile(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("Records Count:")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }
}
