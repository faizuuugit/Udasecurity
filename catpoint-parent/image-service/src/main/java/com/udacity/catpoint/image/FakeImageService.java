package com.udacity.catpoint.image;

import java.awt.image.BufferedImage;
import java.util.Random;

public class FakeImageService implements ImageService {

    private final Random rng;
    private boolean isDeterministic = false;
    private boolean fixedResult = false;
    private float thresholdUsedLast = 0.5f;

    public FakeImageService() {
        this.rng = new Random();
    }

    public FakeImageService(long seed) {
        this.rng = new Random(seed);
    }

    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceLevel) {
        this.thresholdUsedLast = confidenceLevel;

        if (isDeterministic) {
            return fixedResult;
        }
        return rng.nextBoolean();
    }

    public FakeImageService enableFixedResult(boolean returnCat) {
        this.isDeterministic = true;
        this.fixedResult = returnCat;
        return this;
    }

    public FakeImageService useRandomBehavior() {
        this.isDeterministic = false;
        return this;
    }

    public float getThresholdUsedLast() {
        return thresholdUsedLast;
    }
}
