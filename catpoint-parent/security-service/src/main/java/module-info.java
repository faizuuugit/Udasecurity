module com.udacity.catpoint.security {
    requires com.udacity.catpoint.image;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires com.google.common;

    opens com.udacity.catpoint.security.data to com.google.gson;
}