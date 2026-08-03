package se.alipsa.gi

import groovy.transform.CompileStatic

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Utility class for file and resource operations.
 * <p>
 * Provides helper methods for extracting file names from paths/URLs
 * and locating resources from various classloaders.
 */
@CompileStatic
class FileUtils {

  private static final Pattern XML_ENCODING = Pattern.compile(
      "(?i)<\\?xml[^>]*encoding\\s*=\\s*['\"]([^'\"]+)['\"]")

  /**
   * Extracts the base filename from a path or URL string.
   * <p>
   * Handles both Unix and Windows path separators, and strips query strings from paths or URLs.
   * URL fragments are stripped only from URL-shaped input; {@code #} remains valid in local filenames.
   * <p>
   * Examples:
   * <ul>
   *   <li>{@code baseName("/path/to/file.txt")} returns {@code "file.txt"}</li>
   *   <li>{@code baseName("C:\\path\\to\\file.txt")} returns {@code "file.txt"}</li>
   *   <li>{@code baseName("http://example.com/file.txt?param=1")} returns {@code "file.txt"}</li>
   *   <li>{@code baseName("/tmp/report#2.pdf")} returns {@code "report#2.pdf"}</li>
   *   <li>{@code baseName("filename")} returns {@code "filename"}</li>
   *   <li>{@code baseName("/path/to/dir/")} returns {@code "/path/to/dir/"} (empty basename)</li>
   * </ul>
   *
   * @param url the path or URL string to extract the filename from
   * @return the base filename, or the original path if it ends with a separator,
   *         or {@code null} if the input is {@code null}
   */
  static String baseName(String url) {
    if (url == null) return null
    url = url.replace('\\', '/')
    int queryIndex = url.indexOf('?')
    boolean urlLike = url ==~ /^[a-zA-Z][a-zA-Z0-9+.-]*:\/\/.*$/ ||
        url.startsWith('file:') || url.startsWith('jar:')
    int fragmentIndex = urlLike ? url.indexOf('#') : -1
    int suffixIndex = queryIndex >= 0 && fragmentIndex >= 0 ?
        Math.min(queryIndex, fragmentIndex) : Math.max(queryIndex, fragmentIndex)
    if (suffixIndex >= 0) {
      url = url.substring(0, suffixIndex)
    }
    String basename = ""
    if (url.contains("/")) {
      String filePart = url.substring(url.lastIndexOf('/')+1)
      basename = filePart
    }
    return basename.length() > 0 ? basename : url
  }

  /**
   * Returns whether a URL identifies an SVG resource by its final path segment.
   * Query strings and URL fragments are ignored, while names such as {@code report.svg.png}
   * and {@code /svgs.svg/logo.png} are not treated as SVG resources.
   */
  static boolean isSvgResource(URL url) {
    return url != null && baseName(url.toExternalForm()).toLowerCase(Locale.ROOT).endsWith('.svg')
  }

  /**
   * Decodes XML bytes using the encoding declared in the XML prolog when present.
   * UTF-8 is used when no declaration or byte-order mark is available.
   */
  static String decodeXml(byte[] content) {
    if (content == null || content.length == 0) {
      return ''
    }
    Charset charset = StandardCharsets.UTF_8
    if (content.length >= 2 && content[0] == (byte) 0xFF && content[1] == (byte) 0xFE) {
      charset = StandardCharsets.UTF_16LE
    } else if (content.length >= 2 && content[0] == (byte) 0xFE && content[1] == (byte) 0xFF) {
      charset = StandardCharsets.UTF_16BE
    }
    String prefix = new String(content, 0, Math.min(content.length, 512), charset)
    def matcher = XML_ENCODING.matcher(prefix)
    if (matcher.find()) {
      try {
        charset = Charset.forName(matcher.group(1))
      } catch (IllegalArgumentException ignored) {
        // Keep the UTF-8/BOM-derived fallback for an unknown declaration.
      }
    }
    String decoded = new String(content, charset)
    return decoded.startsWith('\uFEFF') ? decoded.substring(1) : decoded
  }

  /**
   * Finds a resource using multiple classloader strategies.
   * <p>
   * Resolution order:
   * <ol>
   *   <li>Thread context classloader</li>
   *   <li>FileUtils class classloader</li>
   *   <li>FileUtils.class.getResource()</li>
   *   <li>System classloader</li>
   *   <li>File system (treats resource as absolute path)</li>
   * </ol>
   * <p>
   * This method allows loading resources from both the classpath and the file system.
   *
   * @param resource the resource path to locate (classpath resource or file path)
   * @return the URL of the resource, or {@code null} if it cannot be found
   */
  static URL getResourceUrl(String resource) {
    if (resource == null || resource.isEmpty()) {
      return null
    }
    final List<ClassLoader> classLoaders = new ArrayList<>()
    classLoaders.add(Thread.currentThread().getContextClassLoader())
    classLoaders.add(FileUtils.class.getClassLoader())

    for (ClassLoader classLoader : classLoaders) {
      final URL url = getResourceWith(classLoader, resource)
      if (url != null) {
        return url
      }
    }
    URL classResource = FileUtils.class.getResource(resource)
    if (classResource != null) {
      return classResource
    }

    final URL systemResource = ClassLoader.getSystemResource(resource)
    if (systemResource != null) {
      return systemResource
    } else {
      try {
        File file = new File(resource)
        return file.exists() ? file.toURI().toURL() : null
      } catch (MalformedURLException e) {
        return null
      }
    }
  }

  private static URL getResourceWith(ClassLoader classLoader, String resource) {
    if (classLoader != null) {
      return classLoader.getResource(resource)
    }
    return null;
  }
}
