package se.alipsa.gi.console

import groovy.transform.CompileStatic
import org.jsoup.Jsoup
import se.alipsa.gi.AbstractInOut
import se.alipsa.gi.ImageTransferable
import se.alipsa.groovy.svg.Svg
import se.alipsa.matrix.core.Matrix
import se.alipsa.matrix.core.util.Logger

import javax.swing.JComponent
import java.awt.Desktop
import java.awt.Image
import java.awt.GraphicsEnvironment
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ExecutionException

@CompileStatic
class InOut extends AbstractInOut {

  private static final Logger log = Logger.getLogger(InOut.class)

  BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in))

  String read(String prompt) {
    print(prompt)
    System.out.flush()
    return sysin.readLine()
  }

  @Override
  File chooseFile(String title, File initialDirectory, String description, String... extensions) {
    String filePath = read("$title: $description>")
    if (filePath == null || filePath.trim().isEmpty()) {
      return null
    }
    return new File(filePath)
  }

  @Override
  File chooseFile(String title, String initialDirectory, String description, String... extensions) {
    String filePath = read("$title: $description>")
    if (filePath == null || filePath.trim().isEmpty()) {
      return null
    }
    return new File(filePath)
  }

  @Override
  File chooseDir(String title, File initialDirectory) {
    String filePath = read("$title>")
    if (filePath == null || filePath.trim().isEmpty()) {
      return null
    }
    return new File(filePath)
  }

  @Override
  File chooseDir(String title, String initialDirectory) {
    String filePath = read("$title>")
    if (filePath == null || filePath.trim().isEmpty()) {
      return null
    }
    return new File(filePath)
  }

  @Override
  YearMonth promptYearMonth(String message) {
    String yearMonth = read(message)
    if (yearMonth == null || yearMonth.trim().isEmpty()) {
      return null
    }
    return YearMonth.parse(yearMonth.trim())
  }

  @Override
  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
    String yearMonth = read("$title: $message")
    if (yearMonth == null) {
      return null
    }
    if (yearMonth.trim().isEmpty()) {
      return initial
    }
    YearMonth selected = YearMonth.parse(yearMonth.trim())
    if (from != null && selected.isBefore(from) || to != null && selected.isAfter(to)) {
      throw new IllegalArgumentException("Selected year-month $selected is outside the range $from to $to")
    }
    return selected
  }

  @Override
  LocalDate promptDate(String title, String message, LocalDate defaultValue) {
    String date = read("$title: $message")
    if (date == null) {
      return null
    }
    if (date.trim().isEmpty()) {
      return defaultValue
    }
    return LocalDate.parse(date.trim())
  }

  @Override
  Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
    if (options == null || options.isEmpty()) {
      throw new IllegalArgumentException("Options collection cannot be null or empty")
    }
    List<Object> optionList = new ArrayList<>(options)
    println title
    println headerText
    int i = 0
    for (Object option : optionList) {
      println "${i}. $option"
      i++
    }
    println "Default value is $defaultValue"
    String input = read(message)
    if (input != null && input.trim().isInteger()) {
      int index = input.trim().toInteger()
      if (index >= 0 && index < optionList.size()) {
        return optionList[index]
      } else {
        println("Invalid index $input. Returning default value $defaultValue")
        return defaultValue
      }
    } else {
      println("Invalid input $input. Returning default value $defaultValue")
      return defaultValue
    }
  }

  @Override
  String promptPassword(String title, String message) {
    println title
    def console = System.console()
    if (console != null) {
      char[] ch = console.readPassword("$message : ")
      return new String(ch)
    }
    // Fallback for IDEs/CI where System.console() is unavailable
    // Warning: input will be visible (not masked)
    log.warn("No console available. Password input will be visible.")
    print("$message : ")
    System.out.flush()
    return sysin.readLine()
  }

  @Override
  String prompt(String message) {
    read(message)
  }

  @Override
  String prompt(String title, String message) {
    println title
    return read(message)
  }

  @Override
  String prompt(String title, String headerText, String message) {
    println title
    println headerText
    return read(message)
  }

  @Override
  String prompt(String title, String headerText, String message, String defaultValue) {
    println title
    println headerText
    String input = read(message)
    if (input == null) {
      return null
    }
    if (input.isEmpty()) {
      return defaultValue
    } else {
      return input
    }
  }

  @Override
  void view(File file, String... title) {
    display(file, title)
  }

  @Override
  void view(String html, String... title) {
    String text = Jsoup.parse(html).text()
    if (title.length > 0) {
      println("${title[0]}: $text")
    } else {
      println(text)
    }
  }

  @Override
  void view(Matrix tableMatrix, String... title) {
    println tableMatrix.content()
  }

  @Override
  void view(List<List<?>> matrix, String... title) {
    Matrix built = Matrix.builder()
        .rows(matrix)
        .matrixName(title.length > 0 ? title[0] : "")
        .build()
    println(built.content())
  }

  @Override
  void display(String fileName, String... title) {
    display(new File(fileName), title)
  }

  @Override
  void display(File file, String... title) {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop()
      if (file.exists()) {
        desktop.open(file)
      } else {
        println("File $file does not exist")
      }
    } else {
      println("Desktop is not supported on this platform")
    }
  }

  @Override
  void display(JComponent swingComponent, String... title) {
    println ("Swing component display is not supported in this implementation")
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
    println("Chart display is not supported in this implementation")
  }

  void saveToClipboard(Image img) {
    if (clipboardUnavailable()) {
      return
    }
    Clipboard clipboard = getClipboard()
    if (clipboard == null) {
      return
    }
    clipboard.setContents(new ImageTransferable(img), null)
  }

  Image getImageFromClipboard() throws ExecutionException, InterruptedException {
    if (clipboardUnavailable()) {
      return null
    }
    Clipboard clipboard = getClipboard()
    if (clipboard == null) {
      return null
    }
    clipboard.getData(DataFlavor.imageFlavor) as Image
  }

  @Override
  Clipboard getClipboard() {
    if (clipboardUnavailable()) {
      return null
    }
    super.getClipboard() as Clipboard
  }

  @Override
  void saveToClipboard(String string) {
    if (clipboardUnavailable()) {
      return
    }
    super.saveToClipboard(string)
  }

  @Override
  void saveToClipboard(File file) {
    if (clipboardUnavailable()) {
      return
    }
    super.saveToClipboard(file)
  }

  @Override
  String getFromClipboard() throws ExecutionException, InterruptedException {
    if (clipboardUnavailable()) {
      return null
    }
    Clipboard clipboard = getClipboard()
    if (clipboard == null) {
      return null
    }
    clipboard.getData(DataFlavor.stringFlavor)
  }

  @Override
  File getFileFromClipboard() throws ExecutionException, InterruptedException {
    if (clipboardUnavailable()) {
      return null
    }
    Clipboard clipboard = getClipboard()
    if (clipboard == null) {
      return null
    }
    List<File> files = clipboard.getData(DataFlavor.javaFileListFlavor) as List<File>
    files?.getFirst()
  }

  private boolean clipboardUnavailable() {
    if (!GraphicsEnvironment.isHeadless()) {
      return false
    }
    String message = "Clipboard is not available in headless environments"
    log.error(message)
    System.err.println(message)
    return true
  }
}
