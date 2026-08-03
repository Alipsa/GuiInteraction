package se.alipsa.gi.fx

import groovy.transform.CompileStatic
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.embed.swing.JFXPanel
import javafx.embed.swing.SwingNode
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.DatePicker
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.layout.FlowPane
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Window
import se.alipsa.gi.*
import se.alipsa.groovy.svg.Svg
import se.alipsa.matrix.chartexport.ChartToJfx
import se.alipsa.matrix.core.Grid
import se.alipsa.matrix.core.Matrix
import se.alipsa.ymp.YearMonthPicker

import se.alipsa.matrix.core.util.Logger

import javax.swing.JComponent
import java.awt.GraphicsEnvironment
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

@CompileStatic
class InOut extends AbstractInOut {

    private static final Logger log = Logger.getLogger(InOut.class)

    Window ownerWindow = null
    ObservableList<String> styleSheetUrls = null
    //Clipboard clipboard

    InOut() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new UnsupportedOperationException(
                "gi-fx InOut requires a graphical environment. " +
                "Use gi-console for headless environments.")
        }
        initializeToolkit()
    }

    InOut(Window owner) {
        this()
        ownerWindow = owner
    }

    InOut(Window owner, ObservableList<String> styleSheets) {
        this(owner)
        setStyleSheetUrls(styleSheets)
    }

    private static void initializeToolkit() {
        new JFXPanel()
    }

    @Override
    File chooseFile(String title, File initialDirectory, String description, String... extensions) {
        return runOnFxThread(() -> {
            FileChooser chooser = new FileChooser()
            chooser.setTitle(title == null ? "Select file" : title)
            File normalizedInitialDir = normalizeInitialDirectory(initialDirectory)
            if (normalizedInitialDir != null) {
                chooser.setInitialDirectory(normalizedInitialDir)
            }
            if (extensions.length > 0) {
                List<String> ext = new ArrayList<>();
                for (String e : extensions) {
                    if (e.startsWith("*.")) {
                        ext.add(e);
                    } else if (e.startsWith(".")) {
                        ext.add("*" + e);
                    } else {
                        ext.add("*." + e);
                    }
                }
                chooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter(description, ext)
                )
            }
            return chooser.showOpenDialog(ownerWindow)
        })
    }

    @Override
    File chooseFile(String title, String initialDirectory, String description, String... extensions) {
        return chooseFile(title, initialDirectory ? new File(initialDirectory) : null, description, extensions)
    }

    @Override
    File chooseDir(String title, File initialDirectory) {
        return runOnFxThread(() -> {
            DirectoryChooser chooser = new DirectoryChooser()
            File normalizedInitialDir = normalizeInitialDirectory(initialDirectory)
            if (normalizedInitialDir != null) {
                chooser.setInitialDirectory(normalizedInitialDir)
            }
            chooser.setTitle(title)
            return chooser.showDialog(ownerWindow)
        });
    }

    @Override
    File chooseDir(String title, String initialDirectory) {
        return chooseDir(title, initialDirectory ? new File(initialDirectory) : null)
    }

    @Override
    YearMonth promptYearMonth(String message) {
        return runOnFxThread(() -> {
            Dialog<YearMonth> dialog = new Dialog<>()
            dialog.setTitle("")
            FlowPane content = new FlowPane()
            content.setHgap(5)
            content.getChildren().add(new Label(message))
            YearMonthPicker picker = new YearMonthPicker()
            content.getChildren().add(picker)
            dialog.getDialogPane().setContent(content)
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL)
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return picker.getValue()
                }
                return null
            });
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        });
    }

    @Override
    YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
        return runOnFxThread(() -> {
            Dialog<YearMonth> dialog = new Dialog<>()
            dialog.setTitle(title)
            FlowPane content = new FlowPane()
            content.setHgap(5)
            content.getChildren().add(new Label(message))
            YearMonthPicker picker = new YearMonthPicker(from, to, initial, Locale.getDefault(), "yyyy-MM")
            content.getChildren().add(picker)
            dialog.getDialogPane().setContent(content)
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL)
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return picker.getValue()
                }
                return null
            })
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        })
    }

    @Override
    LocalDate promptDate(String title, String message, LocalDate defaultValue) {
        return runOnFxThread(() -> {
            Dialog<LocalDate> dialog = new Dialog<>()
            dialog.setTitle(title)
            FlowPane content = new FlowPane()
            content.setHgap(5)
            content.getChildren().add(new Label(message))
            DatePicker picker
            if (defaultValue == null) {
                picker = new DatePicker()
            } else {
                picker = new DatePicker(defaultValue)
            }
            content.getChildren().add(picker)
            dialog.getDialogPane().setContent(content)
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL)
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return picker.getValue()
                }
                return null
            });
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        });
    }


    @Override
    Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options collection cannot be null or empty")
        }
        List opt = options as List
        int defaultIndex = opt.indexOf(defaultValue)
        if (defaultIndex == -1) {
            defaultIndex = 0 // if we cannot find a match, choose the first item as the default
        }

        final int index = defaultIndex;
        return runOnFxThread(() -> {
            ChoiceDialog<Object> dialog = new ChoiceDialog<>(opt.get(index), options)
            dialog.setTitle(title)
            dialog.setHeaderText(headerText)
            dialog.setContentText(message)
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        })
    }

    @Override
    String promptPassword(String title, String message) {
        return runOnFxThread(() -> {
            PasswordDialog dialog = new PasswordDialog(title, message)
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        })
    }

    @Override
    String prompt(String message) {
        return prompt("", "", message, "")
    }

    @Override
    String prompt(String title, String message) {
        return prompt(title, "", message, "")
    }

    @Override
    String prompt(String title, String headerText, String message) {
        return prompt(title, headerText, message, "")
    }

    @Override
    String prompt(String title, String headerText, String message, String defaultValue) {
        return runOnFxThread(() -> {
            TextInputDialog dialog = new TextInputDialog(defaultValue)
            dialog.setTitle(title)
            dialog.setHeaderText(headerText)
            dialog.setContentText(message)
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        })
    }

    @Override
    void view(File file, String... title) {
        if (file == null) {
            log.warn("view file: File argument cannot be null")
            return
        }
        Platform.runLater(() -> {
            try {
                Viewer.viewHtml(file.getAbsolutePath(), title)
            } catch (Throwable e) {
                log.error("Failed to view html", e)
            }
        })
    }

    @Override
    void view(String html, String... title) {
        Platform.runLater {
            try {
                Viewer.viewHtml(html, title)
            } catch (Throwable e) {
                log.error("Failed to view html", e)
            }
        }
    }

    @Override
    void display(String fileName, String... title) {
        URL url = FileUtils.getResourceUrl(fileName);
        if (url == null) {
            log.warn("Cannot display image, Failed to find {}", fileName)
            return
        }
        File file = null
        if ('file' == url.protocol) {
            try {
                file = new File(url.toURI())
            } catch (URISyntaxException e) {
                log.warn("Cannot display image: Invalid resource URL {}", url, e)
                return
            }
        }
        if (file != null && file.exists()) {
            try {
                String contentType = getContentType(file)
                if ("image/svg+xml" == contentType) {
                    displaySvg(url, title)
                    return
                }
            } catch (IOException e) {
                log.error("Failed to detect image content type", e)
            }
        }
        if (FileUtils.isSvgResource(url)) {
            displaySvg(url, title)
            return
        }
        try {
            Image img = new Image(url.toExternalForm())
            if (img.isError()) {
                log.error("Failed to display image {}", fileName, img.getException())
                return
            }
            display(img, title)
        } catch (RuntimeException e) {
            log.error("Failed to display image {}", fileName, e)
        }
    }

    void display(Image img, String... title) {
        ImageView node = new ImageView(img)
        display(node, title)
    }

    private void displaySvg(URL url, String... title) {
        String svgContent
        try {
            svgContent = FileUtils.readXml(url)
        } catch (IOException e) {
            log.error("Failed to read SVG {}", url, e)
            return
        }
        String windowTitle = title.length > 0 ? title[0] : ''
        Platform.runLater(() -> {
            try {
                Node node = ChartToJfx.export(svgContent)
                showNow(node, windowTitle)
            } catch (RuntimeException e) {
                log.error("Failed to parse SVG {}", url, e)
            }
        })
    }

    @Override
    void display(File file, String... title) {
        if (file == null || !file.exists()) {
            log.warn("Cannot display image, Failed to find {}", file)
            return;
        }
        if (title.length == 0) {
            display(file.getAbsolutePath(), file.getName())
        } else {
            display(file.getAbsolutePath(), title)
        }
    }

    @Override
    void display(JComponent swingComponent, String... title) {
        SwingNode swingNode = new SwingNode()
        swingNode.setContent(swingComponent)
        display(swingNode, title)
    }

    /**
     * Displays an Svg image or chart.
     * <p>
     * Note: Not supported in console mode.
     *
     * @param svg the svg to display
     * @param titleOpt optional title for the display window
     */
    @Override
    void display(Svg svg, String... titleOpt) {
        String title = titleOpt.length > 0 ? titleOpt[0] : svg.title?.content
        display(ChartToJfx.export(svg), title)
    }

    void display(Node node, String... title) {
        show(node, title.length > 0 ? title[0] : '')
    }

    @Override
    void view(List<List<?>> matrix, String... title) {
        Platform.runLater {
            Viewer.viewTable(matrix as Grid, title)
        }
    }

    @Override
    void view(Matrix matrix, String... title) {
        Platform.runLater {
            Viewer.viewTable(matrix, title)
        }
    }

    private static void show(Node node, String title) {
        Platform.runLater {
            showNow(node, title)
        }
    }

    private static void showNow(Node node, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION)
        alert.setHeaderText(null)
        alert.setContentText(null)
        alert.setTitle(title)
        alert.getDialogPane().setContent(node)
        alert.initModality(Modality.NONE)
        alert.showAndWait()
    }

    void setStyleSheetUrls(ObservableList<String> styleSheetUrls) {
        this.styleSheetUrls = styleSheetUrls
    }

    void saveToClipboard(String string) {
        runOnFxThreadAsync(() -> {
            ClipboardContent content = new ClipboardContent()
            content.putString(string)
            getClipboard().setContent(content)
            return null
        });
    }

    void saveToClipboard(File file) {
        runOnFxThreadAsync(() -> {
            ClipboardContent content = new ClipboardContent()
            content.putFiles(List.of(file))
            getClipboard().setContent(content)
            return null
        });
    }

    void saveToClipboard(Image img) {
        runOnFxThreadAsync(() -> {
            ClipboardContent content = new ClipboardContent()
            content.putImage(img)
            getClipboard().setContent(content)
            return null
        });
    }

    void saveToClipboard(Object obj, DataFormat format) {
        runOnFxThreadAsync(() -> {
            ClipboardContent content = new ClipboardContent()
            content.put(format, obj)
            getClipboard().setContent(content)
            return null
        });
    }

    String getFromClipboard() throws ExecutionException, InterruptedException {
        return runOnFxThreadChecked(() -> getClipboard().getString())
    }

    File getFileFromClipboard() throws ExecutionException, InterruptedException {
        return runOnFxThreadChecked(() -> {
            List<File> files = getClipboard().getFiles()
            if (files == null || files.isEmpty()) {
                return null
            }
            return files.getFirst()
        }) as File
    }

    Image getImageFromClipboard() throws ExecutionException, InterruptedException {
        return runOnFxThreadChecked(() -> getClipboard().getImage())
    }

    Object getFromClipboard(DataFormat format)
        throws ExecutionException, InterruptedException {
        return runOnFxThreadChecked(() -> getClipboard().getContent(format))
    }

    @Override
    Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = Clipboard.getSystemClipboard();
        }
        return clipboard;
    }

    private static File normalizeInitialDirectory(File initialDirectory) {
        if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
            return initialDirectory
        }
        return null
    }

    private static <T> T runOnFxThread(Callable<T> action) {
        if (Platform.isFxApplicationThread()) {
            try {
                return action.call()
            } catch (Exception e) {
                throw new RuntimeException(e)
            }
        }
        FutureTask<T> task = new FutureTask<>(action)
        Platform.runLater(task)
        try {
            return task.get()
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
            throw new RuntimeException(e)
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause() ?: e)
        }
    }

    private static void runOnFxThreadAsync(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run()
        } else {
            Platform.runLater(action)
        }
    }

    private static <T> T runOnFxThreadChecked(Callable<T> action)
        throws ExecutionException, InterruptedException {
        if (Platform.isFxApplicationThread()) {
            try {
                return action.call()
            } catch (Exception e) {
                throw new ExecutionException(e)
            }
        }
        FutureTask<T> task = new FutureTask<>(action)
        Platform.runLater(task)
        return task.get()
    }
}
