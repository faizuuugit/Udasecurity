package com.udacity.catpoint.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class AwsImageService implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(AwsImageService.class);
    private RekognitionClient rekClient;

    public AwsImageService() {
        try {
            this.rekClient = setupRekognitionClient();
        } catch (Exception e) {
            logger.error("Unable to set up Rekognition client", e);
            rekClient = null;
        }
    }

    private RekognitionClient setupRekognitionClient() {
        Properties config = fetchAwsConfig();
        checkAwsProps(config);

        return RekognitionClient.builder()
                .region(Region.of(config.getProperty("aws.region")))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                config.getProperty("aws.id"),
                                config.getProperty("aws.secret"))))
                .build();
    }

    private Properties fetchAwsConfig() {
        Properties config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Missing config.properties file in classpath");
            }
            config.load(input);
            return config;
        } catch (IOException ex) {
            logger.error("Error loading AWS credentials", ex);
            return new Properties(); // empty fallback
        }
    }

    private void checkAwsProps(Properties config) {
        if (!config.containsKey("aws.id") || !config.containsKey("aws.secret") || !config.containsKey("aws.region")) {
            logger.error("AWS credentials incomplete in properties file");
            throw new IllegalStateException("AWS configuration is incomplete");
        }
    }

    @Override
    public boolean imageContainsCat(BufferedImage inputImage, float confidenceThreshold) {
        if (inputImage == null) {
            logger.warn("Provided image is null");
            return false;
        }

        if (rekClient == null) {
            logger.error("Rekognition client not initialized");
            return false;
        }

        try (ByteArrayOutputStream imageBytes = new ByteArrayOutputStream()) {
            ImageIO.write(inputImage, "jpg", imageBytes);

            DetectLabelsRequest labelRequest = DetectLabelsRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes.toByteArray()))
                            .build())
                    .minConfidence(confidenceThreshold)
                    .build();

            DetectLabelsResponse result = rekClient.detectLabels(labelRequest);

            return result.labels().stream()
                    .anyMatch(label -> "cat".equalsIgnoreCase(label.name()));

        } catch (IOException ioEx) {
            logger.error("Image processing failed", ioEx);
        } catch (RekognitionException rekEx) {
            logger.error("AWS Rekognition failed", rekEx);
        }

        return false;
    }
}
