package happypanda.controllers;

import happypanda.services.GalleryDownloadTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;

// @SuppressWarnings("NullableProblems")
public class HappyPandaController {

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
    private Button browseButton;

    @FXML @NotNull
    private Text errorMessage;
    private Task<Void> errorMessageHider;

    private Task<Void> runningTask;

    public void handleDownload() {
        if (!validateInput()) {
            return;
        }
        disableControls();

        runningTask = new GalleryDownloadTask(galleryUrlField.getText(),
                ipbMemberIdField.getText(),
                ipbPassHashField.getText(),
                Paths.get(outputFolderField.getText()));

        runningTask.setOnSucceeded(event -> enableControls());
        runningTask.setOnFailed(event -> {
            enableControls();
            handleException(event.getSource().getException());
        });
        runningTask.setOnCancelled(event -> enableControls());

        new Thread(runningTask).start();
    }

    public void handleCancel() {
        runningTask.cancel();
        runningTask = null;
    }

    public void handleBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose output folder...");
        Optional.ofNullable(chooser.showDialog(stage))
                .map(File::getAbsolutePath)
                .ifPresent(outputFolderField::setText);
    }

    private void handleException(Throwable e) {

    }

    private boolean validateInput() {
        return validateGalleryUrlField() &&
                validateIpbMemberIdField() &&
                validateIpbPassHashField() &&
                validateOutputFolderField();
    }

    private boolean validateGalleryUrlField() {
        try {
            new URI(galleryUrlField.getText());
        } catch (URISyntaxException e) {
            displayError("Invalid gallery URL!", Duration.of(5, SECONDS));
            return false;
        }
        return true;
    }

    private boolean validateIpbMemberIdField() {
        if (ipbMemberIdField.getText().isEmpty()) {
            displayError("Please fill the ipb_member_id cookie field.", Duration.of(5, SECONDS));
            return false;
        }
        return true;
    }

    private boolean validateIpbPassHashField() {
        if (ipbPassHashField.getText().isEmpty()) {
            displayError("Please fill the ipb_pass_hash cookie field.", Duration.of(5, SECONDS));
            return false;
        }
        return true;
    }

    private boolean validateOutputFolderField() {
        Path outputFolder;
        try {
            outputFolder = Paths.get(outputFolderField.getText());
        } catch (InvalidPathException e) {
            displayError("Invalid output folder path!", Duration.of(5, SECONDS));
            return false;
        }

        if (!Files.exists(outputFolder)) {
            displayError("The output folder does not exist!", Duration.of(5, SECONDS));
            return false;
        }

        if (!Files.isDirectory(outputFolder)) {
            displayError("The provided \"output folder\" is not a folder", Duration.of(5, SECONDS));
            return false;
        }
        return true;
    }

    private void displayError(String message, Duration sleepDuration) {
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

    private void disableControls() {
        for (Node node : controlNodes()) {
            node.setDisable(true);
        }
    }

    private void enableControls() {
        for (Node node : controlNodes()) {
            node.setDisable(false);
        }
    }

    private Node[] controlNodes() {
        return new Node[]{ galleryUrlField, ipbMemberIdField, ipbPassHashField, outputFolderField, downloadButton, browseButton };
    }

}
