package se.alipsa.gi

/*
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ext.tables.TablesExtension
*/
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

abstract class AbstractInOut implements GuiInteraction {

  //private MutableDataSet flexmarkOptions
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
      HttpURLConnection con = (HttpURLConnection) url.openConnection()
      HttpURLConnection.setFollowRedirects(false)
      con.setRequestMethod("HEAD")
      con.setConnectTimeout(timeout)
      int responseCode = con.getResponseCode()
      return responseCode == 200
    } catch (RuntimeException | IOException ignored) {
      return false
    }
  }

  @Override
  String getContentType(String fileName) throws IOException {
    return getContentType(new File(fileName))
  }

  @Override
  String getContentType(File file) throws IOException {
    String fileName = file.getAbsolutePath();
    if (!file.exists()) {
      URL url = getResourceUrl(fileName);
      if (url != null) {
        try {
          file = Paths.get(url.toURI()).toFile();
        } catch (URISyntaxException ignored) {
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

  @Override
  URL getResourceUrl(String resource) {
    final List<ClassLoader> classLoaders = new ArrayList<>()
    classLoaders.add(Thread.currentThread().getContextClassLoader())
    classLoaders.add(GuiInteraction.class.getClassLoader())

    URL url = null
    for (ClassLoader classLoader : classLoaders) {
      if (classLoader != null) {
        url = classLoader.getResource(resource)
      }
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
      } catch (MalformedURLException ignored) {
        return null
      }
    }
  }

  @Override
  String prompt(Map<String, Object> namedParams) {
    return prompt(String.valueOf(namedParams.getOrDefault("title", "")),
            String.valueOf(namedParams.getOrDefault("headerText", "")),
            String.valueOf(namedParams.getOrDefault("message", "")),
            String.valueOf(namedParams.getOrDefault("defaultValue", "")))
  }

  @Override
  void viewMarkdown(String markdown, String... title) {
    view(getHtmlRenderer().render(getMarkdownParser().parse(markdown)), title)
  }

  private Parser getMarkdownParser() {
    if (markdownParser == null) {
      //markdownParser = Parser.builder(getFlexmarkOptions()).build()
      markdownParser = Parser.builder().build()
    }
    return markdownParser
  }

  private HtmlRenderer getHtmlRenderer() {
    if (htmlRenderer == null) {
      //htmlRenderer = HtmlRenderer.builder(getFlexmarkOptions()).build()
      htmlRenderer = HtmlRenderer.builder()
          .softbreak("<br />\n")
          .extensions(List.of(TablesExtension.create()))
          .build()
    }
    return htmlRenderer
  }

  /*
  private MutableDataSet getFlexmarkOptions() {
    if (flexmarkOptions == null) flexmarkOptions = new MutableDataSet()
    flexmarkOptions.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
    return flexmarkOptions
  }*/

  @Override
  void view(Integer o, String... title) {
    println("${title.length > 0 ? title[0] + ': ' : ""}Update count: $o updated rows")
  }

  @Override
  Object promptSelect(String message, Collection<Object> options) {
    return promptSelect(message, "", message, options, options.iterator().next())
  }

  @Override
  void saveToClipboard(String string) {
    getClipboard().setContents(new StringSelection(string), null)
  }

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

  @Override
  String getFromClipboard() throws ExecutionException, InterruptedException {
    getClipboard().getData(DataFlavor.stringFlavor)
  }

  @Override
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
