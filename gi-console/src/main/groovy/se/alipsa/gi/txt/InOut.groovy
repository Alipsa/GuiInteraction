package se.alipsa.gi.txt

import groovy.transform.CompileStatic
import org.jsoup.Jsoup
import se.alipsa.gi.AbstractInOut
import se.alipsa.gi.ImageTransferable
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix

import javax.swing.JComponent
import java.awt.Desktop
import java.awt.Image
import java.awt.datatransfer.Clipboard
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.awt.datatransfer.DataFlavor
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ExecutionException

@CompileStatic
class InOut extends AbstractInOut {

    private static final Logger log = LoggerFactory.getLogger(InOut.class)

    BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in))

    String read(String prompt) {
        print(prompt)
        return sysin.readLine()
    }

    @Override
    File chooseFile(String title, File initialDirectory, String description, String... extensions) {
        String filePath = read("$title: $description>")
        return new File(filePath)
    }

    @Override
    File chooseFile(String title, String initialDirectory, String description, String... extensions) {
        String filePath = read("$title: $description>")
        return new File(filePath)
    }

    @Override
    File chooseDir(String title, File initialDirectory) {
        String filePath = read("$title")
        return new File(filePath)
    }

    @Override
    File chooseDir(String title, String initialDirectory) {
        String filePath = read("$title")
        return new File(filePath)
    }

    @Override
    YearMonth promptYearMonth(String message) {
        String yearMonth = read(message)
        return YearMonth.parse(yearMonth)
    }

    @Override
    YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
        String yearMonth = read("title: $message")
        return YearMonth.parse(yearMonth)
    }

    @Override
    LocalDate promptDate(String title, String message, LocalDate defaultValue) {
        String yearMonth = read("title: $message")
        return LocalDate.parse(yearMonth)
    }

    @Override
    Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
        println title
        println headerText
        int i = 0
        for (def option : options) {
            println "${i}. $option"
        }
        println "Default value is $defaultValue"
        String input = read(message)
        if (input.isInteger()) {
            int index = input.toInteger()
            if (index >= 0 && index < options.size()) {
                return options[index]
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


    @Override
    void display(Chart chart, String... titleOpt) {
        println("Chart display is not supported in this implementation")
    }

    void saveToClipboard(Image img) {
        getClipboard().setContents(new ImageTransferable(img), null)
    }

    Image getImageFromClipboard() throws ExecutionException, InterruptedException {
        getClipboard().getData(DataFlavor.imageFlavor) as Image
    }

    @Override
    Clipboard getClipboard() {
        super.getClipboard() as Clipboard
    }
}
