package com.il.FileSplitter.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetaFileInitializer {

    @Value("${input.dir}")
    private String inputDir;

    @Value("${output.dir}")
    private String outputDir;

    @Value("${meta.file.name:meta.json}")
    private String metaFileName;

    private final ObjectMapper objectMapper;

    public void initializeMetaFile() throws IOException {
        File inputDirectory = new File(inputDir);
        File outputDirectory = new File(outputDir);

        if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
            throw new IOException("Входная директория не существует или не является директорией: " + inputDir);
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new IOException("Не удалось создать выходную директорию: " + outputDir);
        }

        File[] inputFiles = inputDirectory.listFiles(File::isFile);
        if (inputFiles == null || inputFiles.length == 0) {
            log.info("Входная директория пуста: {}", inputDir);
            return;
        }

        File unifiedMetaFile = new File(outputDirectory, metaFileName);
        Map<String, Map<String, Object>> metaData = new HashMap<>();

        for (File file : inputFiles) {
            Map<String, Object> fileMetaData = new HashMap<>();
            fileMetaData.put("fileName", file.getName());
            fileMetaData.put("isProcessed", false);
            fileMetaData.put("currentMiniFile", file.getName().replace(".log", "") + "-0001.log");
            fileMetaData.put("currentRecordPosition", 0);
            fileMetaData.put("currentOffset", 0L);

            metaData.put(file.getName(), fileMetaData);
        }

        try (FileWriter writer = new FileWriter(unifiedMetaFile)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, metaData);
        }

        log.info("Метафайл успешно инициализирован: {}", unifiedMetaFile.getAbsolutePath());
    }
}
