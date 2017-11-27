package com.kameya.taisenapp.structuralanalysis.batch;

import javax.batch.runtime.BatchStatus;
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

}
