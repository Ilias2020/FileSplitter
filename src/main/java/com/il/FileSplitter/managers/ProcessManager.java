package com.il.FileSplitter.managers;

import com.il.FileSplitter.processors.FileCopier;
import com.il.FileSplitter.processors.MetaFileChecker;
import com.il.FileSplitter.processors.MetaFileInitializer;
import com.il.FileSplitter.services.MetaFileProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessManager {

    private final FileCopier fileCopier;
    private final MetaFileInitializer metaFileInitializer;
    private final MetaFileProcessor metaFileProcessor;
    private final MetaFileChecker metaFileChecker;

    public void executeProcess() {
        try {
            if (metaFileChecker.isMetaFilePresent()) {
                log.info("Метафайл найден. Пропускаем шаги 1 и 2.");
            } else {
                log.info("Шаг 1: Копирование файлов...");
                fileCopier.copyAndCleanFiles();
                log.info("Шаг 1 завершён.");

                log.info("Шаг 2: Создание метафайла...");
                metaFileInitializer.initializeMetaFile();
                log.info("Шаг 2 завершён.");
            }

            log.info("Шаг 3: Обработка данных...");
            metaFileProcessor.processMetaFile();
            log.info("Шаг 3 завершён.");
        } catch (Exception e) {
            log.error("Ошибка при выполнении процесса: {}", e.getMessage(), e);
        }
    }
}

