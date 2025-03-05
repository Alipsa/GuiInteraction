package se.alipsa.gi.txt

import se.alipsa.gi.AbstractInOut
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix

import javax.swing.JComponent
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ExecutionException

class InOut extends AbstractInOut {

    Clipboard clipboard
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
        return null
    }

    @Override
    String promptPassword(String title, String message) {
        return null
    }

    @Override
    String prompt(String message) {
        return null
    }

    @Override
    String prompt(String title, String message) {
        return null
    }

    @Override
    String prompt(String title, String headerText, String message) {
        return null
    }

    @Override
    String prompt(String title, String headerText, String message, String defaultValue) {
        return null
    }

    @Override
    void view(File file, String... title) {

    }

    @Override
    void view(String html, String... title) {

    }


    @Override
    void view(Matrix tableMatrix, String... title) {

    }

    @Override
    void view(List<List<?>> matrix, String... title) {

    }

    @Override
    void display(String fileName, String... title) {

    }

    @Override
    void display(File file, String... title) {

    }

    @Override
    void display(JComponent swingComponent, String... title) {

    }


    @Override
    void display(Chart chart, String... titleOpt) {

    }


    void saveToClipboard(String string) {
        getClipboard().setContents(new StringSelection(string), null)
    }

    /* TODO: figure out a way to do this */
    void saveToClipboard(File file) {
        throw new RuntimeException("Not yet implemented!")
    }

    String getFromClipboard() throws ExecutionException, InterruptedException {
        getClipboard().getData(DataFlavor.stringFlavor)
    }

    File getFileFromClipboard() throws ExecutionException, InterruptedException {
        List<File> files = getClipboard().getData(DataFlavor.javaFileListFlavor) as List<File>
        files?.getFirst()
    }

    private Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
        }
        return clipboard
    }

}
