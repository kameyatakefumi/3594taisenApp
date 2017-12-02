package com.kameya.taisenapp.structuralanalysis.batch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.batch.api.BatchProperty;
import javax.batch.api.Batchlet;
import javax.batch.runtime.BatchStatus;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.INTER_AREA;
import static org.bytedeco.javacpp.opencv_imgproc.TM_CCORR_NORMED;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.matchTemplate;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

@Named
@Dependent
public class TemplateMatching implements Batchlet {

    @Inject
    @BatchProperty
    protected String imagesInputPath;

    @Inject
    @BatchProperty
    protected String templatesFolderPath;

    @Inject
    @BatchProperty
    protected Float thresholdValue;

    @Inject
    @BatchProperty
    protected Float heightRatio;

    @Inject
    @BatchProperty
    protected Float widthRatio;

    @Override
    public String process() throws Exception {

        if (!Files.exists(Paths.get(imagesInputPath))) {
            return BatchStatus.FAILED.name();
        }

        for (File template : new File(templatesFolderPath).listFiles()) {

            for (File image : new File(imagesInputPath).listFiles()) {

                String imageName = image.getName();

                if (imageName.matches(".*_result_[0-9]{2}.*") && !imageName.matches(".*_result_[0-9]{2}_match_.*")) {

                    Mat imageMat = imread(image.toPath().toString());
                    Mat greyMat = new Mat(imageMat.size(), CV_8UC1);
                    cvtColor(imageMat, greyMat, COLOR_BGR2GRAY);

                    Mat templateMat = imread(template.toPath().toString(), CV_LOAD_IMAGE_GRAYSCALE);

                    // サイズ調整
                    Size cardSize = calcCardSize(greyMat.size().height());
                    resize(templateMat, templateMat, cardSize, 0, 0, INTER_AREA);

                    Size resultSize = new Size(greyMat.cols() - templateMat.cols() + 1, greyMat.rows() - templateMat.rows() + 1);
                    Mat resultMat = new Mat(resultSize, CV_32FC1);
                    matchTemplate(greyMat, templateMat, resultMat, TM_CCORR_NORMED);

                    List<Point> points = getPointsFromMatAboveThreshold(resultMat, thresholdValue);
                    if (!points.isEmpty()) {

                        Point point = points.get(0);
                        rectangle(imageMat, new Rect(point.x(), point.y(), templateMat.cols(), templateMat.rows()), Scalar.RED, 2, 0, 0);

                        // 画像を保存
                        String templateName = FilenameUtils.getBaseName(template.getName());
                        String baseName = FilenameUtils.getBaseName(imageName);
                        String extension = FilenameUtils.getExtension(imageName);
                        String writePath = imagesInputPath + "\\" + baseName + "_match_" + templateName + "." + extension;
                        imwrite(writePath, imageMat);
                    }

                }
            }
        }

        return BatchStatus.COMPLETED.name();
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Size calcCardSize(int height) {

        int reHeight = (int) (height * heightRatio);
        int reWidth = (int) (reHeight * widthRatio);

        return new Size(reWidth, reHeight);
    }

    public List<Point> getPointsFromMatAboveThreshold(Mat m, float t) {
        List<Point> matches = new ArrayList<>();
        FloatIndexer indexer = m.createIndexer();
        for (int y = 0; y < m.rows(); y++) {
            for (int x = 0; x < m.cols(); x++) {
                if (indexer.get(y, x) > t) {
                    System.out.println("(" + x + "," + y + ") = " + indexer.get(y, x));
                    matches.add(new Point(x, y));
                }
            }
        }
        return matches;
    }

}
