package com.kameya.taisenapp.twitter.batch;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.batch.api.BatchProperty;
import javax.batch.api.Batchlet;
import javax.batch.runtime.BatchStatus;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Named
@Dependent
public class ImageDownloader implements Batchlet {

    @Inject
    @BatchProperty
    protected String imageOutputPath;

    @Inject
    @BatchProperty
    protected String word;

    @Inject
    @BatchProperty
    protected String since;

    @Inject
    @BatchProperty
    protected String until;

    @Override
    public String process() throws Exception {

        Query query = new Query(word);
        query.setSince(since);
        query.setUntil(until);

        Twitter twitter = new TwitterFactory().getInstance();
        QueryResult result;
        do {

            result = twitter.search(query);
            List<Status> tweets = result.getTweets();

            for (Status tweet : tweets) {

                String tweetDir = imageOutputPath + "\\" + tweet.getId();

                if (Files.exists(Paths.get(tweetDir))) {
                    continue;
                } else {
                    Files.createDirectories(Paths.get(tweetDir));
                }

                for (MediaEntity entity : tweet.getMediaEntities()) {

                    if ("photo".equals(entity.getType())) {

                        URL mediaUrl = new URL(entity.getMediaURL());

                        String imageName = Paths.get(mediaUrl.getPath()).getFileName().toString();
                        String outputPath = tweetDir + "\\" + imageName;

                        // https://www.journaldev.com/924/java-download-file-url
                        try (ReadableByteChannel rbc = Channels.newChannel(mediaUrl.openStream());
                                FileOutputStream fos = new FileOutputStream(outputPath)) {
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        }
                    }
                }
            }

        } while ((query = result.nextQuery()) != null);

        return BatchStatus.COMPLETED.name();
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
