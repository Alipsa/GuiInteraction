package se.alipsa.gi.fx

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.SingleSelectionModel
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.Tooltip
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseButton
import javafx.scene.text.Text
import javafx.scene.web.WebEngine
import javafx.scene.web.WebHistory
import javafx.scene.web.WebView
import javafx.stage.Modality
import org.w3c.dom.Document
import se.alipsa.gi.FileUtils
import se.alipsa.matrix.core.Grid
import se.alipsa.matrix.core.Matrix

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.text.NumberFormat

class Viewer {

    static final KeyCodeCombination KEY_CODE_COPY =
            System.getProperty("os.name").toLowerCase().contains("mac") ?
                    new KeyCodeCombination(KeyCode.C, KeyCombination.META_ANY)
                    : new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY)

    private static final List<String> NUMERIC_TYPES = [
            "NUMBER", "BYTE", "SHORT", "INTEGER", "INT", "LONG", "BIGINTEGER",
            "FLOAT", "DOUBLE", "BIGDECIMAL"]

    static void viewHtml(String url, String... title) {
        if (url == null) {
            System.err.println("url is null, nothing to view")
            return
        }

        TabPane viewPane = new TabPane()
        url = url.trim()
        Tab tab = new Tab()
        if (title.length > 0) {
            tab.setText(title[0]);
        } else {
            tab.setText(FileUtils.baseName(url))
        }
        tab.setTooltip(new Tooltip(url))
        viewPane.getTabs().add(tab)
        WebView browser = new WebView()
        browser.setContextMenuEnabled(false)
        WebEngine webEngine = browser.getEngine()
        if (url.startsWith("http") || url.startsWith("file:")) {
            webEngine.load(url);
            createContextMenu(browser, url, true)
        } else {
            try {
                if (Paths.get(url).toFile().exists()) {
                    String path = Paths.get(url).toUri().toURL().toExternalForm();
                    webEngine.load(path);
                    createContextMenu(browser, path, true);
                } else {
                    //println("url is not a http url nor a local path, assuming it is content...");
                    webEngine.loadContent(url);
                    createContextMenu(browser, url);
                }
            } catch (MalformedURLException | InvalidPathException e) {
                println("url is not a http url nor a local path, assuming it is content...");
                webEngine.loadContent(url);
                createContextMenu(browser, url)
            }
        }
        tab.setContent(browser)

        Alert alert = new Alert(Alert.AlertType.INFORMATION)
        alert.setHeaderText(null)
        alert.setContentText(null)
        alert.getDialogPane().setContent(viewPane)
        alert.initModality(Modality.NONE)
        alert.showAndWait()
    }

    private static void createContextMenu(WebView browser, String content, boolean... useLoadOpt) {
        boolean useLoad = useLoadOpt.length > 0 && useLoadOpt[0]
        ContextMenu contextMenu = new ContextMenu()
        WebEngine webEngine = browser.getEngine()

        MenuItem reloadMI = new MenuItem("Reload")
        reloadMI.setOnAction(e -> webEngine.reload())

        MenuItem originalPageMI = new MenuItem("Original page")
        // history only updates for external urls, so we add original back as a fallback
        // e.g when going from a local file to an external link
        originalPageMI.setOnAction(e -> {
            if (useLoad) {
                webEngine.load(content);
            } else {
                webEngine.loadContent(content);
            }
        });

        MenuItem goBackMI = new MenuItem("Go back")
        goBackMI.setOnAction(e -> goBack(webEngine))

        MenuItem goForwardMI = new MenuItem("Go forward")
        goForwardMI.setOnAction(a -> goForward(webEngine))

        MenuItem viewSourceMI = new MenuItem("View source")

        viewSourceMI.setOnAction(a -> viewSource(webEngine))

        contextMenu.getItems().addAll(reloadMI, originalPageMI, goBackMI, goForwardMI, viewSourceMI);
        browser.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(browser, e.getScreenX(), e.getScreenY())
            } else {
                contextMenu.hide()
            }
        });
    }

    private static void goBack(WebEngine webEngine) {
        final WebHistory history = webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();
        int backOffset= entryList.size() > 1 && currentIndex > 0 ? -1 : 0;
        history.go(backOffset);
    }

    private static void goForward(WebEngine webEngine) {
        final WebHistory history = webEngine.getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();
        history.go(entryList.size() > 1 && currentIndex < entryList.size() - 1 ? 1 : 0);
    }

    private static void viewSource(WebEngine webEngine) {
        Document doc = webEngine.getDocument();
        try (StringWriter writer = new StringWriter()){
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(webEngine.getTitle());
            alert.setHeaderText(null);
            TextArea xmlTextArea = new TextArea();
            xmlTextArea.setText(writer.toString())
            alert.getDialogPane().setContent(xmlTextArea);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(800, 600);
            alert.showAndWait();
            //Alerts.info(webEngine.getTitle(), writer.toString());
        } catch (TransformerException | IOException e) {
            System.err.println("Failed to read DOM: $e");
        }
    }

    static void viewTable(Matrix tableMatrix, String... title) {
        String tit = title.length > 0 ? title[0] : tableMatrix.name
        viewTable(tableMatrix.columnNames(), tableMatrix.rowList(), tableMatrix.columnTypeNames(), tit)
    }

    static void viewTable(Grid grid, String... title) {
        // assume uniform format
        String type = "STRING"
        if (grid.getAt(0,0) instanceof Number) {
            type = "NUMBER"
        }
        List<String> typeList = new ArrayList<>()
        List<String> headerList = new ArrayList<>()
        for (int i = 0; i < grid[0].size(); i++) {
            typeList.add(type)
            headerList.add("c${i+1}")
        }
        viewTable(headerList, grid.getRowList(), typeList, title)
    }

    static void viewTable(List<String> headerList, List<List<Object>> rowList, List<String> columnTypes, String... title) {
        try {

            NumberFormat numberFormatter = NumberFormat.getInstance()
            numberFormatter.setGroupingUsed(false)

            TableView<List<String>> tableView = new TableView<>()
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE)
            tableView.setOnKeyPressed(event -> {
                if (KEY_CODE_COPY.match(event)) {
                    // Include header if all rows are selected
                    boolean includeHeader = tableView.getSelectionModel().getSelectedCells().size() == rowList.size()
                    if (includeHeader) {
                        copySelectionToClipboard(tableView, headerList)
                    } else {
                        copySelectionToClipboard(tableView, null)
                    }
                }
            });

            tableView.setRowFactory(tv -> {
                final TableRow<List<String>> row = new TableRow<>()
                final ContextMenu contextMenu = new ContextMenu()
                final MenuItem copyMenuItem = new MenuItem("copy")
                copyMenuItem.setOnAction(event -> copySelectionToClipboard(tv, null))
                final MenuItem copyWithHeaderMenuItem = new MenuItem("copy with header")
                copyWithHeaderMenuItem.setOnAction(event -> copySelectionToClipboard(tv, headerList))

                contextMenu.getItems().addAll(copyMenuItem, copyWithHeaderMenuItem)
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                )
                return row
            })

            for (int i = 0; i < headerList.size(); i++) {
                final int j = i
                String colName = String.valueOf(headerList.get(i))
                TableColumn<List<String>, String> col = new TableColumn<>()
                if (shouldRightAlign(columnTypes.get(i))) {
                    col.setStyle("-fx-alignment: CENTER-RIGHT;")
                }
                Label colLabel = new Label(colName)
                colLabel.setTooltip(new Tooltip(columnTypes.get(i)))
                col.setGraphic(colLabel)
                col.setPrefWidth(new Text(colName).getLayoutBounds().getWidth() * 1.25 + 12.0)

                tableView.getColumns().add(col)
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
            }
            ObservableList<List<String>> data = FXCollections.observableArrayList();
            for (List<?> row : rowList) {
                List<String> obsRow = new ArrayList<>()
                for (Object obj : row) {
                    if (obj instanceof Number) {
                        obsRow.add(numberFormatter.format(obj))
                    } else {
                        obsRow.add(String.valueOf(obj))
                    }
                }
                data.add(obsRow)
            }
            tableView.setItems(data)
            Tab tab = new Tab()
            String tabTitle = " (" + rowList.size() + " rows)"
            if (title.length > 0) {
                tabTitle = title[0] + tabTitle
            }
            tab.setText(tabTitle)
            tab.setContent(tableView)
            TabPane viewPane = new TabPane()
            viewPane.getTabs().add(tab)
            SingleSelectionModel<Tab> selectionModel = viewPane.getSelectionModel()
            selectionModel.select(tab)
            Alert alert = new Alert(Alert.AlertType.INFORMATION)
            alert.setTitle(title.length > 0 ? title[0] : tabTitle)
            alert.setHeaderText(null)
            alert.setContentText(null)
            alert.getDialogPane().setContent(viewPane)
            alert.initModality(Modality.NONE)
            alert.showAndWait()
        } catch (RuntimeException e) {
            System.err.println("Failed to view table: $e")
        }
    }

    private static boolean shouldRightAlign(String type) {
        if (type == null) return false
        boolean isNumeric = false
        // All tablesaw numbers columns are NUMBER, we already cover that in NUMERIC_TYPES
        if (NUMERIC_TYPES.contains(type.toUpperCase())) {
            isNumeric = true
        }
        return isNumeric
    }


    @SuppressWarnings("rawtypes")
    private static void copySelectionToClipboard(final TableView<?> table, List<String> headerList) {
        final Set<Integer> rows = new TreeSet<>()
        for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow())
        }
        final StringBuilder strb = new StringBuilder()
        if (headerList != null) {
            strb.append(String.join("\t", headerList)).append("\n")
        }
        boolean firstRow = true
        for (final Integer row : rows) {
            if (!firstRow) {
                strb.append('\n')
            }
            firstRow = false
            boolean firstCol = true
            for (final TableColumn<?, ?> column : table.getColumns()) {
                if (!firstCol) {
                    strb.append('\t')
                }
                firstCol = false
                final Object cellData = column.getCellData(row);
                strb.append(cellData == null ? "" : cellData.toString())
            }
        }
        final ClipboardContent clipboardContent = new ClipboardContent()
        clipboardContent.putString(strb.toString())
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }
}
