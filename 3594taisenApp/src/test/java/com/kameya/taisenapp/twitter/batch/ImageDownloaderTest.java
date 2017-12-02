package com.kameya.taisenapp.twitter.batch;

import java.util.Arrays;
import java.util.List;
import javax.batch.runtime.BatchStatus;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ImageDownloaderTest {

    @Test
    public void process() throws Exception {

        ImageDownloader downloader = new ImageDownloader();
        downloader.imagesOutputFolderPath = "src\\main\\webapp\\resources\\img\\twitter";
        downloader.word = "#三国志大戦登用 -rt -bot";
        downloader.since = "2017-11-25";
        downloader.until = "2017-12-02";
        String result = downloader.process();

        assertThat(result, is(BatchStatus.COMPLETED.name()));
    }

    @Test
    public void process_multi() throws Exception {

        List<String> words = Arrays.asList(
                "#三国志大戦登用 -rt -bot",
                "#三国志大戦解任 -rt -bot",
                "#在野登用 -rt -bot",
                "#紐切り -rt -bot",
                "#登用 -rt -bot",
                "#解任 -rt -bot");

        ImageDownloader downloader = new ImageDownloader();
        downloader.imagesOutputFolderPath = "src\\main\\webapp\\resources\\img\\twitter";
        downloader.since = "2017-11-25";
        downloader.until = "2017-12-02";

        for (String word : words) {

            downloader.word = word;
            String result = downloader.process();

            assert result.equals(BatchStatus.COMPLETED.name()) : "word = " + word;
        }
    }

}
