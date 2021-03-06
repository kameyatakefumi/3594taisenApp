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
    protected String imagesOutputFolderPath;

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

        System.out.println(word);

        Query query = new Query(word);
        query.setSince(since);
        query.setUntil(until);

        int tweetsExistsCount = 0;
        int tweetsNotExistsCount = 0;

        Twitter twitter = new TwitterFactory().getInstance();
        QueryResult result;
        do {

            result = twitter.search(query);
            List<Status> tweets = result.getTweets();

            for (Status tweet : tweets) {

                String tweetDir = imagesOutputFolderPath + "\\" + tweet.getId();

                if (Files.exists(Paths.get(tweetDir))) {
                    System.out.println("exists = " + tweet.getId());
                    tweetsExistsCount++;
                    continue;
                } else {
                    System.out.println("not exists = " + tweet.getId());
                    tweetsNotExistsCount++;
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

                        System.out.println("download = " + imageName);
                    }
                }
            }

        } while ((query = result.nextQuery()) != null);

        System.out.println("tweetsExistsCount = " + tweetsExistsCount);
        System.out.println("tweetsNotExistsCount = " + tweetsNotExistsCount);
        return BatchStatus.COMPLETED.name();
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
