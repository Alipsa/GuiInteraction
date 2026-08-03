package se.alipsa.gi

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.apache.tika.Tika

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.nio.file.Paths
import java.util.concurrent.ExecutionException;

@CompileStatic
abstract class AbstractInOut implements GuiInteraction {

  private static final int MAX_REDIRECTS = 5

  private Parser markdownParser
  private HtmlRenderer htmlRenderer
  protected def clipboard

  @Override
  File projectFile(String path) {
    return new File(System.getProperty("user.dir", "."), path)
  }

  @Override
  boolean urlExists(String urlString, int timeout) {
    try {
      URL url = new URL(urlString)
      for (int redirect = 0; redirect <= MAX_REDIRECTS; redirect++) {
        HttpURLConnection con = null
        try {
          con = (HttpURLConnection) url.openConnection()
          con.setInstanceFollowRedirects(false)
          con.setRequestMethod("HEAD")
          con.setConnectTimeout(timeout)
          con.setReadTimeout(timeout)
          int responseCode = con.getResponseCode()
          if (responseCode == HttpURLConnection.HTTP_BAD_METHOD ||
              responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
            con.disconnect()
            con = (HttpURLConnection) url.openConnection()
            con.setInstanceFollowRedirects(false)
            con.setRequestMethod("GET")
            con.setRequestProperty("Range", "bytes=0-0")
            con.setConnectTimeout(timeout)
            con.setReadTimeout(timeout)
            responseCode = con.getResponseCode()
            if (responseCode == 416) {
              con.disconnect()
              con = (HttpURLConnection) url.openConnection()
              con.setInstanceFollowRedirects(false)
              con.setRequestMethod("GET")
              con.setConnectTimeout(timeout)
              con.setReadTimeout(timeout)
              responseCode = con.getResponseCode()
            }
            closeResponseBody(con, responseCode)
          }
          if (responseCode >= 300 && responseCode < 400) {
            String location = con.getHeaderField("Location")
            if (location == null || redirect == MAX_REDIRECTS) {
              return location == null
            }
            url = new URL(url, location)
            continue
          }
          return responseCode >= 200 && responseCode < 300
        } finally {
          if (con != null) {
            con.disconnect()
          }
        }
      }
    } catch (RuntimeException | IOException ignored) {
      return false
    }
    return false
  }

  private static void closeResponseBody(HttpURLConnection con, int responseCode) {
    try {
      InputStream response = responseCode >= 200 && responseCode < 400 ?
          con.getInputStream() : con.getErrorStream()
      if (response != null) {
        response.close()
      }
    } catch (IOException ignored) {
      // The response code is authoritative for this existence check.
    }
  }

  @Override
  String getContentType(String fileName) throws IOException {
    return getContentType(new File(fileName))
  }

  @Override
  String getContentType(File file) throws IOException {
    String originalPath = file.getPath()
    String fileName = file.getAbsolutePath()
    if (!file.exists()) {
      URL url = getResourceUrl(originalPath)
      if (url == null && fileName != originalPath) {
        url = getResourceUrl(fileName)
      }
      if (url != null) {
        if ("file".equalsIgnoreCase(url.getProtocol())) {
          try {
            file = Paths.get(url.toURI()).toFile();
          } catch (URISyntaxException ignored) {
            // Ignore, the URI comes from the classloader so cannot have a syntax issue
          }
        } else {
          Tika tika = new Tika()
          return tika.detect(url)
        }
      }
    }
    if (!file.exists()) {
      throw new FileNotFoundException("contentType: " + fileName + " does not exist!");
    }
    Tika tika = new Tika();
    return tika.detect(file);
  }

  @Override
  URL getResourceUrl(String resource) {
    return FileUtils.getResourceUrl(resource)
  }

  @Override
  String prompt(Map<String, Object> namedParams) {
    if (namedParams == null) {
      throw new IllegalArgumentException("namedParams cannot be null")
    }
    return prompt(asPromptValue(namedParams, "title"),
        asPromptValue(namedParams, "headerText"),
        asPromptValue(namedParams, "message"),
        asPromptValue(namedParams, "defaultValue"))
  }

  private static String asPromptValue(Map<String, Object> namedParams, String key) {
    Object value = namedParams.get(key)
    return value == null ? "" : String.valueOf(value)
  }

  @Override
  void viewMarkdown(String markdown, String... title) {
    view(getHtmlRenderer().render(getMarkdownParser().parse(markdown)), title)
  }

  private Parser getMarkdownParser() {
    if (markdownParser == null) {
      markdownParser = Parser.builder().build()
    }
    return markdownParser
  }

  private HtmlRenderer getHtmlRenderer() {
    if (htmlRenderer == null) {
      htmlRenderer = HtmlRenderer.builder()
          .softbreak("<br />\n")
          .extensions(List.of(TablesExtension.create()))
          .build()
    }
    return htmlRenderer
  }

  @Override
  void view(Integer o, String... title) {
    println("${title.length > 0 ? title[0] + ': ' : ""}Update count: $o updated rows")
  }

  @Override
  Object promptSelect(String message, Collection<Object> options) {
    if (options == null || options.isEmpty()) {
      throw new IllegalArgumentException("Options collection cannot be null or empty")
    }
    return promptSelect("Select", "", message, options, options.iterator().next())
  }

  @CompileDynamic
  @Override
  void saveToClipboard(String string) {
    getClipboard().setContents(new StringSelection(string), null)
  }

  @CompileDynamic
  @Override
  void saveToClipboard(File file) {
    List listOfFiles = new ArrayList();
    listOfFiles.add(file);

    FileTransferable ft = new FileTransferable(listOfFiles);

    getClipboard().setContents(ft, new ClipboardOwner() {
          @Override
          void lostOwnership(Clipboard clipboard, Transferable contents) {
            System.out.println("Lost ownership")
          }
        })
  }

  @CompileDynamic
  @Override
  String getFromClipboard() throws ExecutionException, InterruptedException {
    getClipboard().getData(DataFlavor.stringFlavor)
  }

  @CompileDynamic
  @Override
  File getFileFromClipboard() throws ExecutionException, InterruptedException {
    List<File> files = getClipboard().getData(DataFlavor.javaFileListFlavor) as List<File>
    files?.getFirst()
  }

  @CompileDynamic
  protected def getClipboard() {
    if (clipboard == null) {
      clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    }
    return clipboard
  }
}
