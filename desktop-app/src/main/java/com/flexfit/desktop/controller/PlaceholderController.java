package com.flexfit.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class PlaceholderController {

    @FXML
    private Text moduleTitle;

    @FXML
    private Label phaseBadge;

    @FXML
    private Label descriptionLabel;

    public void setModuleInfo(String title, String phase, String description) {
        if (moduleTitle != null) moduleTitle.setText(title);
        if (phaseBadge != null) phaseBadge.setText(phase);
        if (descriptionLabel != null) descriptionLabel.setText(description);
    }
}
