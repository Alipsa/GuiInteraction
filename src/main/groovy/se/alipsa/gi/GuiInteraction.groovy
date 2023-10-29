package se.alipsa.gi;

import se.alipsa.groovy.matrix.Matrix;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Figure;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public interface GuiInteraction {

  File projectFile(String path);

  File chooseFile(String title, File initialDirectory, String description, String... extensions);
  File chooseFile(String title, String initialDirectory, String description, String... extensions);

  File chooseDir(String title, File initialDirectory);
  File chooseDir(String title, String initialDirectory);

  boolean urlExists(String urlString, int timeout);

  String getContentType(String fileName) throws IOException;

  String getContentType(File file)  throws IOException;

  URL getResourceUrl(String resource);

  YearMonth promptYearMonth(String message);

  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial);

  LocalDate promptDate(String title, String message, LocalDate defaultValue);

  Object promptSelect(String title, String headerText, String message, List<Object> options, Object defaultValue);

  public String promptPassword(String title, String message);

  /**
   * A prompt method with support for named parameters i Groovy.
   * Example usage:
   * applicationId =  io.prompt(
   *    title: "Calculated variables",
   *    headerText: "Score variables for applicationId",
   *    message: "applicationId"
   * )
   *
   * @param namedParams a key/value map with the parameter name and its value
   * @return the user input prompted for
   * @throws ExecutionException if a threading issue occurs
   * @throws InterruptedException if a threading interrupt issue occurs
   */
  String prompt(Map<String, Object> namedParams);

  String prompt(String message);

  String prompt(String title, String message);

  String prompt(String title, String headerText, String message);

  String prompt(String title, String headerText, String message, String defaultValue);

  /**
   * View file content in a browser
   *
   * @param file
   * @param title
   */
  void view(File file, String... title);

  void viewMarkdown(String markdown, String... title);

  void view(String html, String... title);

  void view(Table table, String... title);

  void view(Matrix tableMatrix, String... title);

  void view(List<List<?>> matrix, String... title);

  void view(Integer o, String... title);

  void display(String fileName, String... title);

  void display(File file, String... title);

  void display(JComponent swingComponent, String... title);

  void display(Figure figure, String... titleOpt);

  void display(se.alipsa.groovy.charts.Chart chart, String... titleOpt);

  void display(tech.tablesaw.chart.Chart chart, String... titleOpt);
}
