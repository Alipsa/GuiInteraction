package se.alipsa.gi.txt

import se.alipsa.gi.AbstractInOut
import se.alipsa.groovy.matrix.Matrix
import tech.tablesaw.chart.Chart

import javax.swing.JComponent
import java.time.LocalDate
import java.time.YearMonth

class InOut extends AbstractInOut {

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
        return null
    }

    @Override
    File chooseDir(String title, String initialDirectory) {
        return null
    }

    @Override
    YearMonth promptYearMonth(String message) {
        return null
    }

    @Override
    YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
        return null
    }

    @Override
    LocalDate promptDate(String title, String message, LocalDate defaultValue) {
        return null
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
    void display(se.alipsa.groovy.charts.Chart chart, String... titleOpt) {

    }
}
