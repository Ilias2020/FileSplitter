package com.il.FileSplitter.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Component
@RequiredArgsConstructor
public class JsonBuilder {

    private final ObjectMapper objectMapper;
    private RandomAccessFile randomAccessFile; // Для чтения файла с произвольной позиции
    private long currentOffset = 0L;

    public void setParentFile(File parentFile, long offsetOffset) throws IOException {
        this.currentOffset = offsetOffset;

        if (randomAccessFile != null) {
            randomAccessFile.close(); // Закрываем старый файл, если он был открыт
        }

        randomAccessFile = new RandomAccessFile(parentFile, "r");
        randomAccessFile.seek(currentOffset); // Устанавливаем указатель на текущий offset
    }

    public JsonNode getNextRecord() throws IOException {
        if (randomAccessFile == null) {
            throw new IllegalStateException("Файл не установлен.");
        }

        StringBuilder jsonBlock = new StringBuilder();
        int openBracesCount = 0;

        while (true) {
            int readByte = randomAccessFile.read();
            if (readByte == -1) {
                break;
            }

            char character = (char) readByte;
            jsonBlock.append(character);

            if (character == '{') {
                openBracesCount++;
            } else if (character == '}') {
                openBracesCount--;
                if (openBracesCount == 0) {

                    currentOffset = randomAccessFile.getFilePointer(); // Обновляем текущий offset
                    return objectMapper.readTree(jsonBlock.toString());
                }
            }
        }

        /*if (!jsonBlock.isEmpty()) {
            throw new IOException("Некорректный JSON: файл завершился неожиданно.");
        }*/

        return null;
    }

    public long getCurrentOffset() {
        return currentOffset;
    }

    public void close() throws IOException {
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}
