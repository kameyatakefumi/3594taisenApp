package com.kameya.taisenapp.twitter.batch;

import javax.batch.runtime.BatchStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ImageDownloaderTest {

    @Test
    public void process() throws Exception {

        ImageDownloader downloader = new ImageDownloader();
        downloader.imageOutputPath = "src\\main\\webapp\\resources\\img\\twitter";
        downloader.word = "#三国志大戦登用 -rt -bot";
        downloader.since = "2017-11-25";
        downloader.until = "2017-11-26";
        String result = downloader.process();

        assertThat(result, is(BatchStatus.COMPLETED.name()));
    }

}
