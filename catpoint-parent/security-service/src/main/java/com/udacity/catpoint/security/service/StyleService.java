package com.udacity.catpoint.security.service;

import java.awt.*;

public final class StyleService {

    // Made field final and unmodifiable
    public static final Font HEADING_FONT = new Font("Sans Serif", Font.BOLD, 24);

    // Private constructor to prevent instantiation
    private StyleService() {
        throw new AssertionError("This is a utility class and cannot be instantiated");
    }
}