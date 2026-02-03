package se.alipsa.gi.swing

import com.github.lgooddatepicker.components.DatePicker
import groovy.transform.CompileStatic
import se.alipsa.matrix.chartexport.ChartToSwing
import se.alipsa.gi.ImageTransferable
import se.alipsa.matrix.charts.Plot

import java.awt.Image
import java.awt.Toolkit
import se.alipsa.gi.AbstractInOut
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix
import se.alipsa.symp.YearMonthPicker

import javax.swing.table.DefaultTableCellRenderer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.time.LocalDate
import java.time.YearMonth
import javax.swing.*
import se.alipsa.matrix.core.util.Logger

import java.awt.GraphicsEnvironment
import javax.swing.filechooser.FileNameExtensionFilter
import java.util.concurrent.ExecutionException

@CompileStatic
class InOut extends AbstractInOut {

    private static final Logger log = Logger.getLogger(InOut.class)

    static {
        if (GraphicsEnvironment.isHeadless()) {
            throw new UnsupportedOperationException(
                "gi-swing InOut requires a graphical environment. " +
                "Use gi-console for headless environments.")
        }
    }

  InOut() {
    // This is needed due to timing issues to ensure swing UI starts properly
    // Note: attempts with invokeLater to start the EDT did not work on MacOs
    JFrame frame = new JFrame()
    frame.setVisible(true)
    frame.setVisible(false)
    frame.dispose()
  }

  File chooseFile(String title, File initialDirectory, String description, String... extensions) {
    JFileChooser fc = new JFileChooser(initialDirectory)
    fc.setDialogTitle(title + " - " + description)
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY)
    if (extensions.length > 0) {
      fc.setAcceptAllFileFilterUsed(false)
      List<String> ext = []
      for (String extension : extensions) {
        if (extension.startsWith('*.')) {
          ext.add(extension.substring(2))
        } else if (extension.startsWith('.')) {
          ext.add(extension.substring(1))
        } else {
          ext.add(extension)
        }
      }
      FileNameExtensionFilter filter = new FileNameExtensionFilter(String.join(",", ext) +" files", ext as String[])
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
  YearMonth promptYearMonth(String message) {
    YearMonthPicker ymp = new YearMonthPicker()
    int result = JOptionPane.showConfirmDialog(null, ymp, message, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return ymp.getValue()
  }

  @Override
  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
    JPanel content = new JPanel()
    content.add(new JLabel(message))
    YearMonthPicker ymp = new YearMonthPicker(from, to, initial)
    content.add(ymp)
    int result = JOptionPane.showConfirmDialog(null, content, title, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return ymp.getValue()
  }

  @Override
  LocalDate promptDate(String title, String message, LocalDate defaultValue) {
    JPanel content = new JPanel()
    content.add(new JLabel(message))
    DatePicker datePicker = new DatePicker()
    datePicker.setDate(defaultValue)
    content.add(datePicker)
    int result = JOptionPane.showConfirmDialog(null, content, title, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return datePicker.getDate()
  }

  @Override
  Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
    JPanel content = new JPanel(new BorderLayout())
    content.add(new JLabel(headerText), BorderLayout.NORTH)
    JPanel messagePanel = new JPanel(new FlowLayout())
    content.add(messagePanel)
    JComboBox<Object> combo = new JComboBox<>(options as Vector)
    combo.setSelectedItem(defaultValue)
    messagePanel.add(new JLabel(message))
    messagePanel.add(combo)
    int result = JOptionPane.showConfirmDialog(null, content, title, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return combo.getSelectedItem()
  }

  @Override
  String promptPassword(String title, String message) {
    JPanel content = new JPanel(new FlowLayout())
    JPasswordField pwd = new JPasswordField(12)
    content.add(new JLabel(message))
    content.add(pwd)
    int result = JOptionPane.showConfirmDialog(null, content, title, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return pwd.getPassword() as String
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
    int result = JOptionPane.showConfirmDialog(null, content, title, JOptionPane.OK_CANCEL_OPTION)
    if (result != JOptionPane.OK_OPTION) {
      return null
    }
    return inputField.getText()
  }

  @Override
  void view(File file, String... title) {
    JEditorPane jep = new JEditorPane()
    jep.setPage(file.toURI().toURL())
    JScrollPane scrollPane = new JScrollPane(jep)
    JFrame f = new JFrame(title.length > 0 ? title[0] : file.toString())
    f.getContentPane().add(scrollPane)
    f.setSize(800, 600)
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    f.setVisible(true)
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
  void view(Matrix tableMatrix, String... title) {
    Vector rows = new Vector(tableMatrix.rowCount())
    List<Boolean> rightAlign = []
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
    jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS)
    def name = title.length > 0 ? title[0] : tableMatrix.matrixName
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
    if (matrix == null || matrix.isEmpty()) {
      JTable jTable = new JTable(new Vector(), new Vector())
      def name = title.length > 0 ? title[0] : ""
      viewTable(jTable, [], name)
      return
    }
    Vector rows = new Vector(matrix.size())
    List<Boolean> rightAlign = []
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
  void display(String fileName, String... title) {
    File file = new File(fileName)
    if (file.exists()) {
      try {
        String contentType = getContentType(file)
        if ("image/svg+xml" == contentType) {
          displaySvg(file, title)
          return
        }
      } catch (IOException e) {
        log.error("Error detecting content type", e)
        return
      }
    }
    ImageIcon img = new ImageIcon(fileName)
    JLabel label = new JLabel(img)
    display(label, title)
  }

  /**
   * Displays an SVG file using Apache Batik's JSVGCanvas.
   */
  private void displaySvg(File svgFile, String... title) {
    String svg
    try {
      svg = svgFile.getText("UTF-8")
    } catch (IOException e) {
      log.error("Failed to read svg file {}", svgFile, e)
      return
    }
    def svgPanel = ChartToSwing.export(svg)
    svgPanel.setPreferredSize(new Dimension(800, 600))

    JFrame frame = new JFrame(title.length > 0 ? title[0] : svgFile.getName())
    frame.getContentPane().add(new JScrollPane(svgPanel))
    frame.setSize(800, 600)
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
    frame.setVisible(true)
  }

  @Override
  void display(File file, String... title) {
    if (file == null || !file.exists()) {
      log.warn("Cannot display image: Failed to find {}", file)
      return
    }
    if (title.length == 0) {
      display(file.getAbsolutePath(), file.getName())
    } else {
      display(file.getAbsolutePath(), title)
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
  void display(Chart chart, String... titleOpt) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    Plot.png(chart, baos)
    Image image = Toolkit.getDefaultToolkit().createImage(baos.toByteArray())
    ImageIcon img = new ImageIcon(image)
    JLabel label = new JLabel(img)
    display(label, titleOpt.length > 0 ? titleOpt[0] : chart.getTitle())
  }

  void saveToClipboard(Image img) {
    getClipboard().setContents(new ImageTransferable(img), null)
  }

  Image getImageFromClipboard() throws ExecutionException, InterruptedException {
    getClipboard().getData(DataFlavor.imageFlavor) as Image
  }

  Object getFromClipboard(DataFlavor format)
      throws ExecutionException, InterruptedException {
    getClipboard().getData(format)
  }

  @Override
  Clipboard getClipboard() {
    super.getClipboard() as Clipboard
  }

}
