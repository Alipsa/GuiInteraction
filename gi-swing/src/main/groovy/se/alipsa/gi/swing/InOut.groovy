package se.alipsa.gi.swing

import com.github.lgooddatepicker.components.DatePicker
import se.alipsa.gi.AbstractInOut
import se.alipsa.groovy.charts.Chart
import se.alipsa.groovy.matrix.Matrix
import se.alipsa.symp.YearMonthPicker
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Figure

import javax.swing.table.DefaultTableCellRenderer
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.time.LocalDate
import java.time.YearMonth
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class InOut extends AbstractInOut {

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
      extensions.each {
        if (it.startsWith('*.')) {
          ext.add(it.substring(2))
        } else if (it.startsWith('.')) {
          ext.add(it.substring(1))
        } else {
          ext.add(it)
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
  void display(String fileName, String... title) {
    File file = new File(fileName)
    if (file.exists()) {
      try {
        String contentType = getContentType(file)
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

}
