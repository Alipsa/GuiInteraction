package se.alipsa.gi

import groovy.transform.CompileStatic
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix

import javax.swing.JComponent
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.ExecutionException;

@CompileStatic
interface GuiInteraction {

  File projectFile(String path)

  File chooseFile(String title, File initialDirectory, String description, String... extensions);
  File chooseFile(String title, String initialDirectory, String description, String... extensions);

  File chooseDir(String title, File initialDirectory);
  File chooseDir(String title, String initialDirectory);

  boolean urlExists(String urlString, int timeout)

  String getContentType(String fileName) throws IOException
  String getContentType(File file) throws IOException

  /**@
   * Find a resource using available class loaders.
   * It will also load resources/files from the
   * absolute path of the file system (not only the classpath's).
   *
   * @param resource the path to the resource
   * @return the URL representation of the resource
   */
  URL getResourceUrl(String resource)

  /**@
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
  String prompt(Map<String, Object> namedParams)

  String prompt(String message);

  String prompt(String title, String message);

  String prompt(String title, String headerText, String message);

  String prompt(String title, String headerText, String message, String defaultValue);

  YearMonth promptYearMonth(String message);

  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial);

  LocalDate promptDate(String title, String message, LocalDate defaultValue);

  Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue);

  Object promptSelect(String message, Collection<Object> options);

  String promptPassword(String title, String message);

  /**@
   * View file content in a browser
   *
   * @param file
   * @param title
   */
  void view(File file, String... title);
  void view(String html, String... title);
  void view(Matrix tableMatrix, String... title);
  void view(List<List<?>> matrix, String... title);
  void view(Integer o, String... title);

  void viewMarkdown(String markdown, String... title);

  void display(String fileName, String... title);
  void display(File file, String... title);
  void display(JComponent swingComponent, String... title);
  void display(Chart chart, String... titleOpt);

  void saveToClipboard(String string) ;
  void saveToClipboard(File file);
  String getFromClipboard() throws Exception;
  File getFileFromClipboard() throws Exception;

}
