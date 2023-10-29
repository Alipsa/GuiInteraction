package se.alipsa.gi

import com.github.lgooddatepicker.components.DatePicker
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.apache.tika.Tika
import se.alipsa.groovy.charts.Chart
import se.alipsa.groovy.charts.Plot
import se.alipsa.groovy.matrix.Matrix
import se.alipsa.symp.YearMonthPicker
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Page

import javax.swing.table.DefaultTableCellRenderer
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.nio.file.Paths
import java.time.LocalDate
import java.time.YearMonth
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class InOut implements GuiInteraction {

  private MutableDataSet flexmarkOptions;
  private Parser markdownParser;
  private HtmlRenderer htmlRenderer;

  File projectFile(String path) {
    return new File(System.getProperty("user.dir", "."), path)
  }

  File chooseFile(String title, File initialDirectory, String description, String... extensions) {
    JFileChooser fc = new JFileChooser(initialDirectory)
    fc.setDialogTitle(title + " - " + description)
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
    if (extensions.length > 0) {
      fc.setAcceptAllFileFilterUsed(false)
      FileNameExtensionFilter filter = new FileNameExtensionFilter(String.join(",", extensions) +" files", extensions)
      fc.addChoosableFileFilter(filter)
    }
    int resultVal = fc.showOpenDialog(null)
    if (resultVal == JFileChooser.APPROVE_OPTION) {
      return fc.getSelectedFile()
    }
    return null
  }

  File chooseFile(String title, String initialDirectory, String description, String... extensions) {
    return chooseFile(title, new File(initialDirectory), description, extensions)
  }

  @Override
  File chooseDir(String title, File initialDirectory) {
    JFileChooser fc = new JFileChooser(initialDirectory)
    fc.setDialogTitle(title)
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    int resultVal = fc.showOpenDialog(null)
    if (resultVal == JFileChooser.APPROVE_OPTION) {
      return fc.getSelectedFile()
    }
    return null;
  }

  @Override
  File chooseDir(String title, String initialDirectory) {
    return chooseDir(title, new File(initialDirectory))
  }

  @Override
  boolean urlExists(String urlString, int timeout) {
      try {
        URL url = new URL(urlString)
        HttpURLConnection con = (HttpURLConnection) url.openConnection()
        HttpURLConnection.setFollowRedirects(false)
        con.setRequestMethod("HEAD")
        con.setConnectTimeout(timeout)
        int responseCode = con.getResponseCode()
        return responseCode == 200
      } catch (RuntimeException | IOException e) {
        return false;
      }
  }

  @Override
  String getContentType(String fileName) throws IOException {
    return getContentType(new File(fileName));
  }

  @Override
  String getContentType(File file) throws IOException {
    String fileName = file.getAbsolutePath();
    if (!file.exists()) {
      URL url = getResourceUrl(fileName);
      if (url != null) {
        try {
          file = Paths.get(url.toURI()).toFile();
        } catch (URISyntaxException e) {
          // Ignore, the URI comes from the classloader so cannot have a syntax issue
        }
      }
    }
    if (!file.exists()) {
      throw new FileNotFoundException("contentType: " + fileName + " does not exist!");
    }
    Tika tika = new Tika();
    return tika.detect(file);
  }

  /**
   * Find a resource using available class loaders.
   * It will also load resources/files from the
   * absolute path of the file system (not only the classpath's).
   *
   * @param resource the path to the resource
   * @return the URL representation of the resource
   */
  @Override
  URL getResourceUrl(String resource) {
    final List<ClassLoader> classLoaders = new ArrayList<>()
    classLoaders.add(Thread.currentThread().getContextClassLoader())
    classLoaders.add(InOut.class.getClassLoader())

    for (ClassLoader classLoader : classLoaders) {
      final URL url = getResourceWith(classLoader, resource)
      if (url != null) {
        return url
      }
    }

    final URL systemResource = ClassLoader.getSystemResource(resource)
    if (systemResource != null) {
      return systemResource
    } else {
      try {
        return new File(resource).toURI().toURL()
      } catch (MalformedURLException e) {
        return null
      }
    }
  }

  private URL getResourceWith(ClassLoader classLoader, String resource) {
    if (classLoader != null) {
      return classLoader.getResource(resource)
    }
    return null
  }

  @Override
  YearMonth promptYearMonth(String message) {
    YearMonthPicker ymp = new YearMonthPicker()
    JOptionPane.showConfirmDialog(null, ymp, message, JOptionPane.PLAIN_MESSAGE)
    return ymp.getValue()
  }

  @Override
  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
    JPanel content = new JPanel()
    content.add(new JLabel(message))
    YearMonthPicker ymp = new YearMonthPicker(from, to, initial)
    content.add(ymp)
    JOptionPane.showConfirmDialog(null, content, title, JOptionPane.PLAIN_MESSAGE)
    return ymp.getValue()
  }

  @Override
  LocalDate promptDate(String title, String message, LocalDate defaultValue) {
    JPanel content = new JPanel()
    content.add(new JLabel(message))
    DatePicker datePicker = new DatePicker()
    datePicker.setDate(defaultValue)
    content.add(datePicker)
    JOptionPane.showConfirmDialog(null, content, title, JOptionPane.PLAIN_MESSAGE)
    return datePicker.getDate()
  }

  @Override
  Object promptSelect(String title, String headerText, String message, List<Object> options, Object defaultValue) {
    JPanel content = new JPanel(new BorderLayout())
    content.add(new JLabel(headerText), BorderLayout.NORTH)
    JPanel messagePanel = new JPanel(new FlowLayout())
    content.add(messagePanel)
    JComboBox<Object> combo = new JComboBox<>(options as Vector)
    combo.setSelectedItem(defaultValue)
    messagePanel.add(new JLabel(message))
    messagePanel.add(combo)
    JOptionPane.showConfirmDialog(null, content, title, JOptionPane.PLAIN_MESSAGE)
    return combo.getSelectedItem()
  }

  @Override
  String promptPassword(String title, String message) {
    JPanel content = new JPanel(new FlowLayout())
    JPasswordField pwd = new JPasswordField(12)
    content.add(new JLabel(message))
    content.add(pwd)
    JOptionPane.showConfirmDialog(null, content, title, JOptionPane.PLAIN_MESSAGE)
    return pwd.getPassword() as String
  }

  @Override
  String prompt(Map<String, Object> namedParams) {
    return prompt(String.valueOf(namedParams.getOrDefault("title", "")),
        String.valueOf(namedParams.getOrDefault("headerText", "")),
        String.valueOf(namedParams.getOrDefault("message", "")),
        String.valueOf(namedParams.getOrDefault("defaultValue", "")))
  }

  @Override
  String prompt(String message) {
    return JOptionPane.showInputDialog(message)
  }

  @Override
  String prompt(String title, String message) {
    return JOptionPane.showInputDialog(null, message, title)
  }

  @Override
  String prompt(String title, String headerText, String message) {
    return prompt(title, headerText, message, "")
  }

  @Override
  String prompt(String title, String headerText, String message, String defaultValue) {
    JPanel content = new JPanel(new BorderLayout())
    content.add(new JLabel(headerText), BorderLayout.NORTH)
    JPanel messagePanel = new JPanel(new FlowLayout())
    content.add(messagePanel)
    JTextField inputField = new JTextField(12)
    inputField.setText(defaultValue)
    messagePanel.add(new JLabel(message))
    messagePanel.add(inputField)
    JOptionPane.showConfirmDialog(null, content, title, JOptionPane.PLAIN_MESSAGE)
    return inputField.getText()
  }

  @Override
  void view(File file, String... title) {
    JEditorPane jep = new JEditorPane()
    jep.setPage(file.toURI().toURL())
    JScrollPane scrollPane = new JScrollPane(jep);
    JFrame f = new JFrame(title.length > 0 ? title[0] : file.toString())
    f.getContentPane().add(scrollPane)
    f.setSize(800, 600)
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    f.setVisible(true)
  }

  @Override
  void viewMarkdown(String markdown, String... title) {
    view(getHtmlRenderer().render(getMarkdownParser().parse(markdown)), title)
  }

  @Override
  void view(String html, String... title) {
    JEditorPane jep = new JEditorPane()
    jep.setContentType("text/html")
    jep.setText(html)
    JScrollPane scrollPane = new JScrollPane(jep);
    JFrame f = new JFrame(title.length > 0 ? title[0] : "")
    f.getContentPane().add(scrollPane)
    f.setSize(800, 600)
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    f.setVisible(true)
  }

  @Override
  void view(Table table, String... title) {
    Vector rows = new Vector(table.rowCount())
    int columnCount = table.columnCount()
    def rightAlign = []
    boolean firstRow = true
    table.each {
      Vector row = new Vector()
      for (int i = 0; i<columnCount; i++) {
        Object val = it.getObject(i)
        row.add(String.valueOf(val))
        if (firstRow) {
          rightAlign << (val instanceof Number)
        }
      }
      rows.add(row)
      firstRow = false
    }

    JTable jTable = new JTable(rows, table.columnNames() as Vector)
    def name = title.length > 0 ? title[0] : table.name()
    viewTable(jTable, rightAlign, name)
  }

  @Override
  void view(Matrix tableMatrix, String... title) {
    Vector rows = new Vector(tableMatrix.rowCount())
    def rightAlign = []
    boolean firstRow = true
    tableMatrix.each { r ->
      Vector row = new Vector()
      r.each { val ->
        row.add(String.valueOf(val))
        if (firstRow) {
          rightAlign << (val instanceof Number)
        }
      }
      rows.add(row)
      firstRow = false
    }

    JTable jTable = new JTable(rows, tableMatrix.columnNames() as Vector)
    def name = title.length > 0 ? title[0] : tableMatrix.name
    viewTable(jTable, rightAlign, name)
  }

  private viewTable(JTable jTable, List<Boolean> rightAlign, String title) {
    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer()
    rightRenderer.setHorizontalAlignment(JLabel.RIGHT)
    def model = jTable.getColumnModel()
    rightAlign.eachWithIndex { boolean ra, int i ->
      if (ra) {
        model.getColumn(i).setCellRenderer(rightRenderer);
      }
    }
    JScrollPane scrollPane = new JScrollPane(jTable)
    JFrame f = new JFrame(title)
    f.getContentPane().add(scrollPane)
    f.pack()
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    f.setVisible(true)
  }
  @Override
  void view(List<List<?>> matrix, String... title) {
    Vector rows = new Vector(matrix.size())
    def rightAlign = []
    boolean firstRow = true
    int nCol = 0
    matrix.each { r ->
      Vector row = new Vector()
      r.each { val ->
        row.add(String.valueOf(val))
        if (firstRow) {
          rightAlign << (val instanceof Number)
          nCol++
        }
      }
      rows.add(row)
      firstRow = false
    }

    JTable jTable = new JTable(rows, (1..nCol).collect({ "c$it" }) as Vector)
    def name = title.length > 0 ? title[0] : ""
    viewTable(jTable, rightAlign, name)
  }

  @Override
  void view(Integer o, String... title) {
    println("${title.length > 0 ? title[0] + ': ' : ""}Update count: $o updated rows")
  }

  @Override
  void display(String fileName, String... title) {
    File file = new File(fileName)
    Tika tika = new Tika()
    if (file.exists()) {
      try {
        String contentType = tika.detect(file);
        if ("image/svg+xml".equals(contentType)) {
          System.err.println("Sorry, no support for viewing SVG files (yet)")
          return
        }
      } catch (IOException e) {
        e.printStackTrace()
        return
      }
    }
    ImageIcon img = new ImageIcon(fileName)
    JLabel label = new JLabel(img)
    display(label, title);
  }

  @Override
  void display(File file, String... title) {
    if (file == null || !file.exists()) {
      System.err.println("Cannot display image: Failed to find " + file);
      return;
    }
    if (title.length == 0) {
      display(file.getAbsolutePath(), file.getName());
    } else {
      display(file.getAbsolutePath(), title);
    }
  }

  @Override
  void display(JComponent swingComponent, String... title) {
    JFrame f = new JFrame(title.length > 0 ? title[0]: "")
    f.getContentPane().add(swingComponent)
    f.pack()
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    f.setVisible(true)
  }

  @Override
  void display(Figure figure, String... titleOpt) {
    tech.tablesaw.plotly.Plot.show(figure, "target")
  }

  @Override
  void display(Chart chart, String... titleOpt) {
    System.err.println("Sorry displaying charts is not yet implemented")
  }

  @Override
  void display(tech.tablesaw.chart.Chart chart, String... titleOpt) {
    display(tech.tablesaw.chart.Plot.jsPlot(chart), titleOpt)
  }

  private Parser getMarkdownParser() {
    if (markdownParser == null) {
      markdownParser = Parser.builder(getFlexmarkOptions()).build();
    }
    return markdownParser;
  }

  private HtmlRenderer getHtmlRenderer() {
    if (htmlRenderer == null) {
      htmlRenderer = HtmlRenderer.builder(getFlexmarkOptions()).build();
    }
    return htmlRenderer;
  }

  MutableDataSet getFlexmarkOptions() {
    if (flexmarkOptions == null) flexmarkOptions = new MutableDataSet();
    flexmarkOptions.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
    return flexmarkOptions;
  }


}
