package com.il.FileSplitter.api;

import com.il.FileSplitter.managers.ProcessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProcessController {

    private final ProcessManager processManager;


    @GetMapping("/start")
    public String startProcess() {
        try {
            processManager.executeProcess();
            return "Процесс успешно выполнен.";
        } catch (Exception e) {
            return "Ошибка при выполнении процесса: " + e.getMessage();
        }
    }
}
