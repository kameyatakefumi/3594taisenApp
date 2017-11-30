package com.kameya.taisenapp.structuralanalysis.batch;

import javax.batch.runtime.BatchStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TemplateMatchingTest {

    @Test
    public void process() throws Exception {

        TemplateMatching matching = new TemplateMatching();
        matching.imagesInputPath = "src\\main\\webapp\\resources\\img\\twitter\\934217089518264321";
        matching.templatesFolderPath = "src\\main\\webapp\\resources\\img\\template";
        matching.thresholdValue = 0.95f;
        matching.heightRatio = 0.72f;
        matching.widthRatio = 0.625f;
        String result = matching.process();

        assertThat(result, is(BatchStatus.COMPLETED.name()));
    }

}
