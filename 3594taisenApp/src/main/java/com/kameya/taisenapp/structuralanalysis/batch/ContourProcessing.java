package com.kameya.taisenapp.structuralanalysis.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.batch.api.BatchProperty;
import javax.batch.api.Batchlet;
import javax.batch.runtime.BatchStatus;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.FilenameUtils;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CHAIN_APPROX_TC89_L1;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_OTSU;
import static org.bytedeco.javacpp.opencv_imgproc.RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.approxPolyDP;
import static org.bytedeco.javacpp.opencv_imgproc.arcLength;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

@Named
@Dependent
public class ContourProcessing implements Batchlet {
    
    private static final DecimalFormat FORMAT = new DecimalFormat("00");
    
    @Inject
    @BatchProperty
    protected String imageFilePath;
    
    @Inject
    @BatchProperty
    protected Double thresholdValue;
    
    @Inject
    @BatchProperty
    protected Double thresholdMaxValue;
    
    @Inject
    @BatchProperty
    protected Double contourAreaMinValue;
    
    @Override
    public String process() throws Exception {
        
        if (!Files.exists(Paths.get(imageFilePath))) {
            return BatchStatus.FAILED.name();
        }
        
        System.out.println("file name = " + FilenameUtils.getName(imageFilePath));

        // 画像を読込
        Mat mat = imread(imageFilePath);
        Mat clone = mat.clone();

        // グレースケール化
        Mat gray = new Mat(mat.size(), CV_8UC1);
        cvtColor(mat, gray, COLOR_BGR2GRAY);

        // しきい値処理
        threshold(gray, gray, thresholdValue, thresholdMaxValue, CV_THRESH_OTSU);

        // 輪郭検出
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        findContours(gray, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_TC89_L1);
        
        List<Mat> details = new ArrayList<>();
        int max_level = 0;
        for (int i = 0; i < contours.size(); i++) {

            // ある程度の面積が有るものだけに絞る
            double area = contourArea(contours.get(i), false);
            if (area > contourAreaMinValue) {

                //輪郭を直線近似する
                Mat approx = new Mat();
                approxPolyDP(contours.get(i), approx, 0.01 * arcLength(contours.get(i), true), true);

                // 矩形のみ取得
                if (approx.size().area() == 4) {

                    // １単位を収集
                    Rect rect = boundingRect(approx);
                    details.add(0, new Mat(mat, rect));

                    // 輪郭を塗る
                    drawContours(clone, contours, i, Scalar.RED, 2, CV_AA, hierarchy, max_level, new Point());
                }
            }
        }

        // 画像を保存
        Path parentPath = Paths.get(imageFilePath).getParent();
        String baseName = FilenameUtils.getBaseName(imageFilePath);
        String extension = FilenameUtils.getExtension(imageFilePath);
        
        Path grayPath = parentPath.resolve(baseName + "_gray." + extension);
        imwrite(grayPath.toString(), gray);
        
        Path resultPath = parentPath.resolve(baseName + "_result." + extension);
        imwrite(resultPath.toString(), clone);
        
        for (int i = 0; i < details.size(); i++) {
            Path detailPath = parentPath.resolve(baseName + "_result_" + FORMAT.format(i + 1) + "." + extension);
            imwrite(detailPath.toString(), details.get(i));
        }

        // 実行時の設定を出力
        Path configPath = parentPath.resolve(baseName + "_config.txt");
        Files.write(
                configPath,
                Arrays.asList(
                        "thresholdValue      : " + thresholdValue,
                        "thresholdMaxValue   : " + thresholdMaxValue,
                        "contourAreaMinValue : " + contourAreaMinValue),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        
        return BatchStatus.COMPLETED.name();
    }
    
    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
