package happypanda.controllers;

import happypanda.services.GalleryDownloadTask;
import happypanda.services.HappyPandaException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("NullableProblems")
public class HappyPandaController {

    private static final Logger logger = LoggerFactory.getLogger(HappyPandaController.class);

    private Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML @NotNull
    private TextField galleryUrlField;
    @FXML @NotNull
    private TextField ipbMemberIdField;
    @FXML @NotNull
    private TextField ipbPassHashField;
    @FXML @NotNull
    private TextField outputFolderField;

    @FXML @NotNull
    private Button downloadButton;
    @FXML @NotNull
    private Button cancelButton;
    @FXML @NotNull
    private Button browseButton;

    @FXML @NotNull
    private Text errorMessage;
    private Task<Void> errorMessageHider;

    private Task<Void> runningTask;

    public void handleDownload() {
        logger.debug("Download button action received");
        if (!validateInput()) {
            return;
        }
        preDownload();

        runningTask = new GalleryDownloadTask(galleryUrlField.getText(),
                ipbMemberIdField.getText(),
                ipbPassHashField.getText(),
                Paths.get(outputFolderField.getText()));

        runningTask.setOnSucceeded(event -> postDownload());
        runningTask.setOnFailed(event -> {
            postDownload();
            handleException(event.getSource().getException());
        });
        runningTask.setOnCancelled(event -> postDownload());

        new Thread(runningTask).start();
    }

    public void handleCancel() {
        logger.debug("Cancel button action received");
        runningTask.cancel();
        runningTask = null;
    }

    public void handleBrowse() {
        logger.debug("Browse button action received");
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose output folder...");
        Optional.ofNullable(chooser.showDialog(stage))
                .map(File::getAbsolutePath)
                .ifPresent(outputFolderField::setText);
    }

    private void handleException(Throwable e) {
        String errorMessage = e instanceof HappyPandaException ?
                ((HappyPandaException) e).prettyMessage() :
                "An exception occurred. See log for details.";
        displayError(errorMessage, Duration.of(5, SECONDS), e);
    }

    private boolean validateInput() {
        logger.debug("Validating input");
        return validateGalleryUrlField() &&
                validateIpbMemberIdField() &&
                validateIpbPassHashField() &&
                validateOutputFolderField();
    }

    private boolean validateGalleryUrlField() {
        if (galleryUrlField.getText().isEmpty()) {
            displayError("Please fill the gallery URL field.", Duration.of(5, SECONDS));
            return false;
        }

        try {
            new URL(galleryUrlField.getText());
        } catch (MalformedURLException e) {
            displayError("Invalid gallery URL!", Duration.of(5, SECONDS), e);
            return false;
        }
        logger.debug("Gallery URL validated: " + galleryUrlField.getText());
        return true;
    }

    private boolean validateIpbMemberIdField() {
        if (ipbMemberIdField.getText().isEmpty()) {
            displayError("Please fill the ipb_member_id cookie field.", Duration.of(5, SECONDS));
            return false;
        }
        logger.debug("ipb_member_id validated: " + ipbMemberIdField.getText());
        return true;
    }

    private boolean validateIpbPassHashField() {
        if (ipbPassHashField.getText().isEmpty()) {
            displayError("Please fill the ipb_pass_hash cookie field.", Duration.of(5, SECONDS));
            return false;
        }
        logger.debug("ipb_pass_hash validated: " + ipbPassHashField.getText());
        return true;
    }

    private boolean validateOutputFolderField() {
        if (outputFolderField.getText().isEmpty()) {
            displayError("Please fill the output folder field.", Duration.of(5, SECONDS));
            return false;
        }

        Path outputFolder;
        try {
            outputFolder = Paths.get(outputFolderField.getText());
        } catch (InvalidPathException e) {
            displayError("Invalid output folder path!", Duration.of(5, SECONDS), e);
            return false;
        }

        if (!Files.exists(outputFolder)) {
            displayError("The output folder does not exist!", Duration.of(5, SECONDS));
            return false;
        }

        if (!Files.isDirectory(outputFolder)) {
            displayError("The provided \"output folder\" is not a folder!", Duration.of(5, SECONDS));
            return false;
        }
        logger.debug("Output folder validated: " + outputFolderField.getText());
        return true;
    }

    private void displayError(String message, Duration sleepDuration) {
        displayError(message, sleepDuration, null);
    }

    private void displayError(String message, Duration sleepDuration, Throwable e) {
        if (e == null) {
            logger.error(message);
        } else {
            logger.error(message, e);
        }

        errorMessage.setText(message);
        errorMessage.setVisible(true);

        if (errorMessageHider != null) {
            errorMessageHider.cancel();
        }
        errorMessageHider = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(sleepDuration.toMillis());
                } catch (InterruptedException ignored) {
                }
                return null;
            }
            @Override
            protected void succeeded() {
                errorMessage.setVisible(false);
            }
        };
        new Thread(errorMessageHider).start();
    }

    private void preDownload() {
        logger.debug("Disabling controls");
        for (Node node : controlNodes()) {
            node.setDisable(true);
        }

        downloadButton.setVisible(false);
        cancelButton.setVisible(true);
    }

    private void postDownload() {
        logger.debug("Enabling controls");
        for (Node node : controlNodes()) {
            node.setDisable(false);
        }

        cancelButton.setVisible(false);
        downloadButton.setVisible(true);
    }

    private Node[] controlNodes() {
        return new Node[]{ galleryUrlField, ipbMemberIdField, ipbPassHashField, outputFolderField, browseButton };
    }

}
