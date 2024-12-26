package com.il.FileSplitter.processors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MetaFileChecker {

    @Value("${output.dir}")
    private String outputDir;

    @Value("${meta.file.name:meta.json}")
    private String metaFileName;

    public boolean isMetaFilePresent() {
        File metaFile = new File(outputDir, metaFileName);
        return metaFile.exists() && metaFile.isFile();
    }
}
