package com.il.FileSplitter.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.il.FileSplitter.processors.JsonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MetaFileProcessor {

    @Value("${output.dir}")
    private String outputDir;

    @Value("${meta.file.name:meta.json}")
    private String metaFileName;

    @Value("${records.per.file:}")
    private int recordsPerFile;

    private final ObjectMapper objectMapper;
    private final JsonBuilder jsonBuilder;

    public void processMetaFile() throws IOException {
        File metaFile = new File(outputDir, metaFileName);
        if (!metaFile.exists()) {
            throw new IOException("Метафайл не найден: " + metaFile.getAbsolutePath());
        }

        Map<String, Map<String, Object>> metaData = objectMapper.readValue(metaFile, Map.class);

        for (Map.Entry<String, Map<String, Object>> entry : metaData.entrySet()) {
            String parentFileName = entry.getKey();
            Map<String, Object> fileMeta = entry.getValue();

            boolean isProcessed = (boolean) fileMeta.get("isProcessed");
            if (isProcessed) {
                continue;
            }

            String currentMiniFile = (String) fileMeta.get("currentMiniFile");
            long currentOffset = ((Number) fileMeta.get("currentOffset")).longValue();

            File outputSubDir = new File(outputDir, parentFileName.replace(".log", ""));
            if (!outputSubDir.exists() && !outputSubDir.mkdirs()) {
                throw new IOException("Не удалось создать директорию для мини-файлов: " + outputSubDir.getAbsolutePath());
            }

            File miniFile = new File(outputSubDir, currentMiniFile);

            File parentFile = new File(outputDir, parentFileName.replace(".log", "") + File.separator + parentFileName);
            jsonBuilder.setParentFile(parentFile, currentOffset);

            System.out.println("Начинаем обработку файла: " + parentFileName);

            while (true) {
                int recordCount = 0;

                try (FileWriter writer = new FileWriter(miniFile, true)) {
                    JsonNode record;

                    while ((record = jsonBuilder.getNextRecord()) != null) {
                        writer.write(record.toString());
                        writer.write(System.lineSeparator());
                        recordCount++;

                        if (recordCount >= recordsPerFile) {
                            System.out.println("Минифайл создан: " + miniFile.getName());
                            break;
                        }
                    }

                    if (record == null) {
                        fileMeta.put("isProcessed", true);
                        System.out.println("Файл обработан: " + parentFileName);
                        break;
                    }
                }

                if (!((boolean) fileMeta.get("isProcessed"))) {
                    String nextMiniFileName = generateNextMiniFileName(miniFile.getName());
                    fileMeta.put("currentMiniFile", nextMiniFileName);
                    miniFile = new File(outputSubDir, nextMiniFileName);
                    System.out.println(nextMiniFileName);

                    fileMeta.put("currentOffset", jsonBuilder.getCurrentOffset());
                }
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(metaFile, metaData);
        }
    }

    private String generateNextMiniFileName(String currentMiniFileName) {
        int lastIndex = currentMiniFileName.lastIndexOf('-');

        String baseName = currentMiniFileName.substring(0, lastIndex);

        int fileNumber = Integer.parseInt(currentMiniFileName.substring(lastIndex + 1, currentMiniFileName.lastIndexOf('.')));

        return String.format("%s-%04d.log", baseName, fileNumber + 1);
    }
}
