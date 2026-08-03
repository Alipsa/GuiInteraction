package se.alipsa.gi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.*

class FileUtilsTest {

  @TempDir
  File tempDir

  @Test
  void testBaseNameWithSimpleFilename() {
    assertEquals("file.txt", FileUtils.baseName("file.txt"))
  }

  @Test
  void testBaseNameWithUnixPath() {
    assertEquals("file.txt", FileUtils.baseName("/path/to/file.txt"))
  }

  @Test
  void testBaseNameWithWindowsPath() {
    assertEquals("file.txt", FileUtils.baseName("C:\\path\\to\\file.txt"))
  }

  @Test
  void testBaseNameWithUrl() {
    assertEquals("file.txt", FileUtils.baseName("http://example.com/path/file.txt"))
  }

  @Test
  void testBaseNameWithQueryString() {
    assertEquals("file.txt", FileUtils.baseName("http://example.com/path/file.txt?param=value"))
  }

  @Test
  void testBaseNameWithQueryStringAndNoPath() {
    assertEquals("file.txt", FileUtils.baseName("file.txt?param=value"))
  }

  @Test
  void testBaseNamePreservesFragmentCharactersInLocalPaths() {
    assertEquals("report#2.pdf", FileUtils.baseName("/tmp/report#2.pdf"))
  }

  @Test
  void testBaseNameStripsUrlFragments() {
    assertEquals("file.txt", FileUtils.baseName("https://example.com/file.txt#section"))
  }

  @Test
  void testSvgResourceDetectionUsesTheFinalPathSegment() {
    assertTrue(FileUtils.isSvgResource(URI.create('file:/tmp/report.svg').toURL()))
    assertFalse(FileUtils.isSvgResource(URI.create('file:/tmp/report.svg.png').toURL()))
    assertFalse(FileUtils.isSvgResource(URI.create('file:/tmp/svgs.svg/logo.png').toURL()))
  }

  @Test
  void testBaseNameWithNull() {
    assertNull(FileUtils.baseName(null))
  }

  @Test
  void testBaseNameWithEmptyString() {
    assertEquals("", FileUtils.baseName(""))
  }

  @Test
  void testBaseNameWithNoPath() {
    assertEquals("filename", FileUtils.baseName("filename"))
  }

  @Test
  void testBaseNameWithTrailingSlash() {
    // When basename is empty, baseName returns the original url
    assertEquals("/path/to/dir/", FileUtils.baseName("/path/to/dir/"))
  }

  @Test
  void testGetResourceUrlWithClasspathResource() {
    URL url = FileUtils.getResourceUrl("areachart2.png")
    assertNotNull(url, "Should find classpath resource areachart2.png")
    assertTrue(url.toString().endsWith("areachart2.png"))
  }

  @Test
  void testGetResourceUrlWithAbsoluteFilePath() {
    // Create a temp file
    File tempFile = new File(tempDir, "testfile.txt")
    tempFile.text = "test content"

    URL url = FileUtils.getResourceUrl(tempFile.absolutePath)
    assertNotNull(url, "Should find file by absolute path")
    assertEquals("file", url.protocol)
  }

  @Test
  void testGetResourceUrlWithNonExistentResource() {
    URL url = FileUtils.getResourceUrl("/nonexistent/path/file.txt")
    assertNull(url, "Non-existent resources should not resolve")
  }

  @Test
  void testGetResourceUrlWithNullOrEmptyResource() {
    assertNull(FileUtils.getResourceUrl(null))
    assertNull(FileUtils.getResourceUrl(""))
  }

  @Test
  void testGetResourceUrlWithSvgFile() {
    URL url = FileUtils.getResourceUrl("svgplot.svg")
    assertNotNull(url, "Should find classpath resource svgplot.svg")
    assertTrue(url.toString().endsWith("svgplot.svg"))
  }
}
