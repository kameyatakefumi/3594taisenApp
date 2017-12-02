package com.kameya.taisenapp.structuralanalysis.batch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.batch.runtime.BatchStatus;
import org.apache.commons.io.FilenameUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ContourProcessingTest {

    @Test
    public void process() throws Exception {

        ContourProcessing processing = new ContourProcessing();
        processing.imageFilePath = "src\\main\\webapp\\resources\\img\\twitter\\934217089518264321\\DPcBexMUIAAiUYi.jpg";
        processing.thresholdValue = 0D;
        processing.thresholdMaxValue = 255D;
        processing.contourAreaMinValue = 15000D;
        String result = processing.process();

        assertThat(result, is(BatchStatus.COMPLETED.name()));
    }

    @Test
    public void process_multi() throws Exception {

        ContourProcessing processing = new ContourProcessing();
        processing.thresholdValue = 0D;
        processing.thresholdMaxValue = 255D;
        processing.contourAreaMinValue = 15000D;

        String imageFoldersPath = "src\\main\\webapp\\resources\\img\\twitter";

        for (File folder : new File(imageFoldersPath).listFiles()) {

            if (!folder.isDirectory()) {
                continue;
            }
            
            System.out.println("folder name = " + folder.getName());

            for (File file : folder.listFiles()) {

                String fileName = file.getName();

                if (fileName.matches(".*_config.*") || fileName.matches(".*_gray.*") || fileName.matches(".*_result.*")) {
                    continue;
                }

                String baseName = FilenameUtils.getBaseName(fileName);
                String configPath = folder + "\\" + baseName + "_config.txt";

                if (Files.exists(Paths.get(configPath))) {
                    continue;
                }

                processing.imageFilePath = file.toString();
                String result = processing.process();

                assert result.equals(BatchStatus.COMPLETED.name());
            }
        }
    }

}
