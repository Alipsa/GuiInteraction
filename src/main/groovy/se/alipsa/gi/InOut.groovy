package se.alipsa.gi;

import org.apache.tika.Tika
import se.alipsa.groovy.matrix.Matrix
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Figure

import java.nio.file.Paths
import java.time.LocalDate
import java.time.YearMonth
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class InOut implements GuiInteraction {

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
  Object promptSelect(String title, String headerText, String message, List<Object> options, Object defaultValue) {
    return null
  }

  @Override
  String promptPassword(String title, String message) {
    return null
  }

  @Override
  String prompt(Map<String, Object> namedParams) {
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
  void viewMarkdown(String markdown, String... title) {

  }

  @Override
  void view(String html, String... title) {

  }

  @Override
  void view(Table table, String... title) {

  }

  @Override
  void view(Matrix tableMatrix, String... title) {

  }

  @Override
  void view(List<List<?>> matrix, String... title) {

  }

  @Override
  void view(Integer o, String... title) {

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
  void display(Figure figure, String... titleOpt) {

  }

  @Override
  void display(se.alipsa.groovy.charts.Chart chart, String... titleOpt) {

  }

  @Override
  void display(tech.tablesaw.chart.Chart chart, String... titleOpt) {

  }
}
