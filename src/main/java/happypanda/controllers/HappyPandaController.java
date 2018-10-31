package happypanda.controllers;

import happypanda.services.GalleryDownloadTask;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
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

    private Task runningTask;

    public void handleDownload() {
        disableControls();
        runningTask = new GalleryDownloadTask(galleryUrlField.getText(),
                ipbMemberIdField.getText(),
                ipbPassHashField.getText(),
                Paths.get(outputFolderField.getText()));

        runningTask.setOnSucceeded(event -> enableControls());
        runningTask.setOnFailed(event -> {
            enableControls();
            handleException(((WorkerStateEvent) event).getSource().getException());
        });
        runningTask.setOnCancelled(event -> enableControls());

        new Thread(runningTask).start();
    }

    public void handleCancel() {
        runningTask.cancel();
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
