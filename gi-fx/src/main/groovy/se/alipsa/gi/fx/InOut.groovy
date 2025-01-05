package se.alipsa.gi.fx

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
import javafx.scene.layout.FlowPane
import javafx.scene.web.WebView
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Window
import se.alipsa.gi.*
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.charts.Plot
import se.alipsa.matrix.core.Grid
import se.alipsa.matrix.core.Matrix
import se.alipsa.ymp.YearMonthPicker

import javax.swing.JComponent
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.FutureTask

class InOut extends AbstractInOut {

    Window ownerWindow = null
    ObservableList<String> styleSheetUrls = null

    InOut() {
        new JFXPanel()
    }

    InOut(Window owner) {
        this()
        ownerWindow = owner
    }

    InOut(Window owner, ObservableList<String> styleSheets) {
        this(owner)
        setStyleSheetUrls(styleSheets)
    }

    @Override
    File chooseFile(String title, File initialDirectory, String description, String... extensions) {
        FutureTask<File> task = new FutureTask<>(() -> {
            FileChooser chooser = new FileChooser()
            chooser.setTitle(title == null ? "Select file" : title)
            if (initialDirectory != null) {
                chooser.setInitialDirectory(initialDirectory)
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
        Platform.runLater(task)
        return task.get()
    }

    @Override
    File chooseFile(String title, String initialDirectory, String description, String... extensions) {
        return chooseFile(title, new File(initialDirectory), description, extensions)
    }

    @Override
    File chooseDir(String title, File initialDirectory) {
        FutureTask<File> task = new FutureTask<>(() -> {
            DirectoryChooser chooser = new DirectoryChooser()
            chooser.setInitialDirectory(initialDirectory)
            chooser.setTitle(title)
            return chooser.showDialog(ownerWindow)
        });
        Platform.runLater(task);
        return task.get()
    }

    @Override
    File chooseDir(String title, String initialDirectory) {
        return chooseDir(title, new File(initialDirectory))
    }

    @Override
    YearMonth promptYearMonth(String message) {
        FutureTask<YearMonth> task = new FutureTask<>(() -> {
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
        Platform.runLater(task)
        return task.get()
    }

    @Override
    YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
        FutureTask<YearMonth> task = new FutureTask<>(() -> {
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
        Platform.runLater(task)
        return task.get()
    }

    @Override
    LocalDate promptDate(String title, String message, LocalDate defaultValue) {
        FutureTask<LocalDate> task = new FutureTask<>(() -> {
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
        Platform.runLater(task)
        return task.get()
    }


    @Override
    Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
        List opt = options as List
        int defaultIndex = opt.indexOf(defaultValue)
        if (defaultIndex == -1) {
            defaultIndex = 0 // if we cannot find a match, choose the first item as the default
        }

        final int index = defaultIndex;
        FutureTask<Object> task = new FutureTask<>(() -> {
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
        Platform.runLater(task)
        return task.get()
    }

    @Override
    String promptPassword(String title, String message) {
        FutureTask<String> task = new FutureTask<>(() -> {
            PasswordDialog dialog = new PasswordDialog(title, message)
            dialog.setResizable(true)
            dialog.getDialogPane().getScene().getWindow().sizeToScene()
            if (styleSheetUrls != null) {
                dialog.getDialogPane().getStylesheets().addAll(styleSheetUrls)
            }
            return dialog.showAndWait().orElse(null)
        })
        Platform.runLater(task)
        return task.get()
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
        FutureTask<String> task = new FutureTask<>(() -> {
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
        Platform.runLater(task)
        return task.get()
    }

    @Override
    void view(File file, String... title) {
        if (file == null) {
            System.err.println("view file: File argument cannot be null")
            return
        }
        Platform.runLater(() -> {
            try {
                Viewer.viewHtml(file.getAbsolutePath(), title)
            } catch (Throwable e) {
                System.err.println("Failed to view html: $e")
            }
        })
    }

    @Override
    void view(String html, String... title) {
        Platform.runLater {
            try {
                Viewer.viewHtml(html, title)
            } catch (Throwable e) {
                System.err.println("Failed to view html: $e")
            }
        }
    }

    @Override
    void display(String fileName, String... title) {
        URL url = FileUtils.getResourceUrl(fileName);
        if (url == null) {
            System.err.println("Cannot display image, Failed to find $fileName")
            return
        }
        File file = new File(fileName);
        if (file.exists()) {
            try {
                String contentType = getContentType(file)
                if ("image/svg+xml" == contentType) {
                    Platform.runLater(() -> {
                        final WebView browser = new WebView()
                        browser.getEngine().load(url.toExternalForm())
                        display(browser, title)
                    });
                    return
                }
            } catch (IOException e) {
                System.err.println("Failed to detect image content type: $e")
            }
        }
        Image img = new Image(url.toExternalForm())
        display(img, title)
    }

    void display(Image img, String... title) {
        ImageView node = new ImageView(img)
        display(node, title)
    }

    @Override
    void display(File file, String... title) {
        if (file == null || !file.exists()) {
            System.err.println("Cannot display image, Failed to find $file")
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

    void display(Node node, String... title) {
        show(node, title.length > 0 ? title[0] : '')
    }

    @Override
    void display(Chart chart, String... titleOpt) {
        String title = titleOpt.length > 0 ? titleOpt[0] : chart.class.simpleName
        display(Plot.jfx(chart), title)
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION)
            alert.setHeaderText(null)
            alert.setContentText(null)
            alert.setTitle(title)
            alert.getDialogPane().setContent(node)
            alert.initModality(Modality.NONE)
            alert.showAndWait()
        }
    }

    void setStyleSheetUrls(ObservableList<String> styleSheetUrls) {
        this.styleSheetUrls = styleSheetUrls
    }
}
