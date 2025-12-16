package se.alipsa.gi

import groovy.transform.CompileStatic
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix

import javax.swing.JComponent
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.ExecutionException;

/**
 * Core interface for GUI interaction capabilities.
 * <p>
 * This interface provides a unified API for user interaction across different UI implementations:
 * <ul>
 *   <li><b>gi-fx</b> - JavaFX-based implementation (requires JavaFX-enabled JVM)</li>
 *   <li><b>gi-swing</b> - Swing-based implementation (works with any JDK)</li>
 *   <li><b>gi-console</b> - Console-based implementation for headless environments</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> Implementations may not be thread-safe. UI operations should typically
 * be performed on the appropriate UI thread (EDT for Swing, FX Application Thread for JavaFX).
 * <p>
 * <b>Null Handling Policy:</b>
 * <ul>
 *   <li><b>User cancellation:</b> Methods that involve user interaction (file choosers, prompts,
 *       dialogs) return {@code null} when the user cancels or dismisses the dialog. This is
 *       expected behaviour and callers should check return values.</li>
 *   <li><b>Resource not found:</b> {@link #getResourceUrl(String)} returns {@code null} if the
 *       resource cannot be located. This allows callers to handle missing resources gracefully.</li>
 *   <li><b>Invalid input:</b> Methods throw exceptions (typically {@link IllegalArgumentException})
 *       for invalid arguments such as null or empty collections passed to {@link #promptSelect}.</li>
 *   <li><b>I/O errors:</b> Methods involving file operations throw {@link java.io.IOException}
 *       or its subclasses (e.g., {@link java.io.FileNotFoundException}) for file system errors.</li>
 *   <li><b>Environment constraints:</b> gi-fx and gi-swing throw {@link UnsupportedOperationException}
 *       when instantiated in headless environments.</li>
 * </ul>
 * <p>
 * <b>Example Usage:</b>
 * <pre>{@code
 * def io = new se.alipsa.gi.swing.InOut()
 * def file = io.chooseFile("Select File", ".", "Choose a data file", "csv", "txt")
 * if (file != null) {
 *     println("Selected: ${file.absolutePath}")
 * }
 * }</pre>
 *
 * @see se.alipsa.gi.AbstractInOut
 */
@CompileStatic
interface GuiInteraction {

  /**
   * Creates a File reference relative to the project directory (user.dir).
   *
   * @param path the relative path from the project root
   * @return a File object representing the path relative to user.dir
   */
  File projectFile(String path)

  /**
   * Opens a file chooser dialog for the user to select a file.
   *
   * @param title the dialog title
   * @param initialDirectory the starting directory for the file chooser
   * @param description description text shown in the dialog
   * @param extensions optional file extensions to filter (e.g., "txt", "csv")
   * @return the selected File, or {@code null} if the user cancels
   */
  File chooseFile(String title, File initialDirectory, String description, String... extensions);

  /**
   * Opens a file chooser dialog for the user to select a file.
   *
   * @param title the dialog title
   * @param initialDirectory the starting directory path as a String
   * @param description description text shown in the dialog
   * @param extensions optional file extensions to filter (e.g., "txt", "csv")
   * @return the selected File, or {@code null} if the user cancels
   */
  File chooseFile(String title, String initialDirectory, String description, String... extensions);

  /**
   * Opens a directory chooser dialog for the user to select a directory.
   *
   * @param title the dialog title
   * @param initialDirectory the starting directory for the chooser
   * @return the selected directory, or {@code null} if the user cancels
   */
  File chooseDir(String title, File initialDirectory);

  /**
   * Opens a directory chooser dialog for the user to select a directory.
   *
   * @param title the dialog title
   * @param initialDirectory the starting directory path as a String
   * @return the selected directory, or {@code null} if the user cancels
   */
  File chooseDir(String title, String initialDirectory);

  /**
   * Checks if a URL exists and is accessible.
   * <p>
   * Performs an HTTP HEAD request to verify the URL is reachable.
   *
   * @param urlString the URL to check
   * @param timeout connection timeout in milliseconds
   * @return {@code true} if the URL returns HTTP 200, {@code false} otherwise
   */
  boolean urlExists(String urlString, int timeout)

  /**
   * Detects the MIME content type of a file using Apache Tika.
   *
   * @param fileName the path to the file
   * @return the detected MIME type (e.g., "image/png", "text/plain")
   * @throws IOException if the file cannot be read
   * @throws FileNotFoundException if the file does not exist
   */
  String getContentType(String fileName) throws IOException

  /**
   * Detects the MIME content type of a file using Apache Tika.
   *
   * @param file the file to analyze
   * @return the detected MIME type (e.g., "image/png", "text/plain")
   * @throws IOException if the file cannot be read
   * @throws FileNotFoundException if the file does not exist
   */
  String getContentType(File file) throws IOException

  /**
   * Find a resource using available class loaders.
   * It will also load resources/files from the
   * absolute path of the file system (not only the classpath's).
   *
   * @param resource the path to the resource
   * @return the URL representation of the resource
   */
  URL getResourceUrl(String resource)

  /**
   * A prompt method with support for named parameters in Groovy.
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

  /**
   * Prompts the user for text input with a simple message.
   *
   * @param message the prompt message to display
   * @return the user's input, or {@code null} if cancelled
   */
  String prompt(String message);

  /**
   * Prompts the user for text input with a title and message.
   *
   * @param title the dialog title
   * @param message the prompt message to display
   * @return the user's input, or {@code null} if cancelled
   */
  String prompt(String title, String message);

  /**
   * Prompts the user for text input with title, header, and message.
   *
   * @param title the dialog title
   * @param headerText the header text displayed above the input
   * @param message the prompt message to display
   * @return the user's input, or {@code null} if cancelled
   */
  String prompt(String title, String headerText, String message);

  /**
   * Prompts the user for text input with a default value.
   *
   * @param title the dialog title
   * @param headerText the header text displayed above the input
   * @param message the prompt message to display
   * @param defaultValue the initial value shown in the input field
   * @return the user's input (or default if unchanged), or {@code null} if cancelled
   */
  String prompt(String title, String headerText, String message, String defaultValue);

  /**
   * Prompts the user to select a year and month.
   *
   * @param message the prompt message to display
   * @return the selected YearMonth
   * @throws java.time.format.DateTimeParseException if input cannot be parsed
   */
  YearMonth promptYearMonth(String message);

  /**
   * Prompts the user to select a year and month within a range.
   *
   * @param title the dialog title
   * @param message the prompt message to display
   * @param from the earliest selectable YearMonth
   * @param to the latest selectable YearMonth
   * @param initial the initially selected YearMonth
   * @return the selected YearMonth
   */
  YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial);

  /**
   * Prompts the user to select a date.
   *
   * @param title the dialog title
   * @param message the prompt message to display
   * @param defaultValue the initially selected date
   * @return the selected LocalDate
   */
  LocalDate promptDate(String title, String message, LocalDate defaultValue);

  /**
   * Prompts the user to select from a list of options.
   *
   * @param title the dialog title
   * @param headerText the header text displayed above the options
   * @param message the prompt message to display
   * @param options the collection of options to choose from (must not be empty)
   * @param defaultValue the initially selected option
   * @return the selected option
   * @throws IllegalArgumentException if options collection is empty
   */
  Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue);

  /**
   * Prompts the user to select from a list of options (simplified version).
   *
   * @param message the prompt message to display
   * @param options the collection of options to choose from (must not be null or empty)
   * @return the selected option
   * @throws IllegalArgumentException if options collection is null or empty
   */
  Object promptSelect(String message, Collection<Object> options);

  /**
   * Prompts the user for a password with masked input.
   * <p>
   * In console mode, this may return {@code null} if no console is available
   * (e.g., when running in an IDE or CI environment).
   *
   * @param title the dialog title
   * @param message the prompt message to display
   * @return the entered password, or {@code null} if unavailable/cancelled
   */
  String promptPassword(String title, String message);

  /**
   * Displays file content in a viewer (browser or HTML panel).
   *
   * @param file the file to view
   * @param title optional title for the viewer window
   */
  void view(File file, String... title);

  /**
   * Displays HTML content in a viewer.
   *
   * @param html the HTML content to display
   * @param title optional title for the viewer window
   */
  void view(String html, String... title);

  /**
   * Displays a Matrix as a formatted table.
   *
   * @param tableMatrix the Matrix to display
   * @param title optional title for the display
   */
  void view(Matrix tableMatrix, String... title);

  /**
   * Displays a list-of-lists as a formatted table.
   *
   * @param matrix the data as nested lists (rows of columns)
   * @param title optional title for the display
   */
  void view(List<List<?>> matrix, String... title);

  /**
   * Displays an update count result (typically from database operations).
   *
   * @param o the update count to display
   * @param title optional title prefix
   */
  void view(Integer o, String... title);

  /**
   * Renders and displays Markdown content as HTML.
   * <p>
   * Uses CommonMark parser with GitHub Flavored Markdown tables extension.
   *
   * @param markdown the Markdown text to render
   * @param title optional title for the viewer window
   */
  void viewMarkdown(String markdown, String... title);

  /**
   * Displays a file using the system's default application or internal viewer.
   * <p>
   * For images and supported formats, displays inline. For other files,
   * may open with the system's default application.
   *
   * @param fileName the path to the file to display
   * @param title optional title for the display window
   */
  void display(String fileName, String... title);

  /**
   * Displays a file using the system's default application or internal viewer.
   *
   * @param file the file to display
   * @param title optional title for the display window
   */
  void display(File file, String... title);

  /**
   * Displays a Swing component in a window.
   * <p>
   * Note: Not supported in console mode.
   *
   * @param swingComponent the Swing component to display
   * @param title optional title for the window
   */
  void display(JComponent swingComponent, String... title);

  /**
   * Displays a chart.
   * <p>
   * Note: Not supported in console mode.
   *
   * @param chart the Chart to display
   * @param titleOpt optional title for the display window
   */
  void display(Chart chart, String... titleOpt);

  /**
   * Copies a string to the system clipboard.
   *
   * @param string the text to copy to clipboard
   */
  void saveToClipboard(String string);

  /**
   * Copies a file reference to the system clipboard.
   *
   * @param file the file to copy to clipboard
   */
  void saveToClipboard(File file);

  /**
   * Retrieves text content from the system clipboard.
   *
   * @return the clipboard text content
   * @throws Exception if clipboard access fails or content is not text
   */
  String getFromClipboard() throws Exception;

  /**
   * Retrieves a file reference from the system clipboard.
   *
   * @return the first file from the clipboard, or {@code null} if none
   * @throws Exception if clipboard access fails
   */
  File getFileFromClipboard() throws Exception;

}
