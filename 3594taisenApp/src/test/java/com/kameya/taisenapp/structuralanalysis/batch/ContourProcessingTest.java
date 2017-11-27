package com.kameya.taisenapp.structuralanalysis.batch;

import javax.batch.runtime.BatchStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ContourProcessingTest {

    @Test
    public void process() throws Exception {

        ContourProcessing processing = new ContourProcessing();
        processing.imagePath = "src\\main\\webapp\\resources\\img\\twitter\\930966539393748997\\DOt1H2fWkAATAEN.jpg";
        processing.thresholdValue = 0D;
        processing.thresholdMaxValue = 255D;
        processing.contourAreaMinValue = 15000D;
        String result = processing.process();

        assertThat(result, is(BatchStatus.COMPLETED.name()));
    }

}
