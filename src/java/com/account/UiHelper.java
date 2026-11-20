package com.account;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** Small presentation helpers shared by the views. Contains no business logic. */
public final class UiHelper {
    private UiHelper() { }

    public static Label styled(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    public static void showMessage(Label label, OperationResult result) {
        showMessage(label, result.getMessage(), result.isSuccess());
    }

    public static void showMessage(Label label, String text, boolean success) {
        label.getStyleClass().removeAll("error-label", "success-label", "info-label");
        label.getStyleClass().add(success ? "success-label" : "error-label");
        label.setWrapText(true);
        label.setText(text);
    }

    public static void showInfo(Label label, String text) {
        label.getStyleClass().removeAll("error-label", "success-label", "info-label");
        label.getStyleClass().add("info-label");
        label.setWrapText(true);
        label.setText(text);
    }

    public static void clearMessage(Label label) {
        label.setText("");
    }

    /** Lets controls stretch to the width of their container. */
    public static void fillWidth(Region... regions) {
        for (Region region : regions) region.setMaxWidth(Double.MAX_VALUE);
    }
}
