package se.alipsa.gi

import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import se.alipsa.matrix.charts.Chart
import se.alipsa.matrix.core.Matrix

import javax.swing.JComponent
import java.time.LocalDate
import java.time.YearMonth

import static org.junit.jupiter.api.Assertions.*

class AbstractInOutTest {

    @TempDir
    File tempDir

    TestableInOut inOut

    @BeforeEach
    void setUp() {
        inOut = new TestableInOut()
    }

    @Test
    void testProjectFile() {
        File result = inOut.projectFile("test.txt")
        assertNotNull(result)
        assertEquals("test.txt", result.name)
        assertTrue(result.absolutePath.contains(System.getProperty("user.dir")))
    }

    @Test
    void testProjectFileWithSubdirectory() {
        File result = inOut.projectFile("sub/dir/test.txt")
        assertNotNull(result)
        assertEquals("test.txt", result.name)
    }

    @Test
    void testGetContentTypeForPngFile() {
        // Using test resource
        URL pngUrl = FileUtils.getResourceUrl("areachart2.png")
        assertNotNull(pngUrl, "Test resource should exist")

        File pngFile = new File(pngUrl.toURI())
        String contentType = inOut.getContentType(pngFile)
        assertEquals("image/png", contentType)
    }

    @Test
    void testGetContentTypeForSvgFile() {
        URL svgUrl = FileUtils.getResourceUrl("svgplot.svg")
        assertNotNull(svgUrl, "Test resource should exist")

        File svgFile = new File(svgUrl.toURI())
        String contentType = inOut.getContentType(svgFile)
        assertTrue(contentType.contains("svg") || contentType.contains("xml"),
                "SVG content type should contain 'svg' or 'xml', got: " + contentType)
    }

    @Test
    void testGetContentTypeForTextFile() {
        File textFile = new File(tempDir, "test.txt")
        textFile.text = "Hello, World!"

        String contentType = inOut.getContentType(textFile)
        assertTrue(contentType.contains("text"),
                "Text file content type should contain 'text', got: " + contentType)
    }

    @Test
    void testGetContentTypeWithStringPath() {
        File textFile = new File(tempDir, "test2.txt")
        textFile.text = "Test content"

        String contentType = inOut.getContentType(textFile.absolutePath)
        assertTrue(contentType.contains("text"),
                "Content type should contain 'text', got: " + contentType)
    }

    @Test
    void testGetContentTypeForNonExistentFileThrows() {
        assertThrows(FileNotFoundException.class) {
            inOut.getContentType("/nonexistent/file.txt")
        }
    }

    @Test
    void testGetResourceUrlWithClasspathResource() {
        URL url = inOut.getResourceUrl("areachart2.png")
        assertNotNull(url)
        assertTrue(url.toString().endsWith("areachart2.png"))
    }

    @Test
    void testGetResourceUrlWithAbsolutePath() {
        File tempFile = new File(tempDir, "resource.txt")
        tempFile.text = "resource content"

        URL url = inOut.getResourceUrl(tempFile.absolutePath)
        assertNotNull(url)
    }

    @Test
    void testPromptWithNamedParams() {
        Map<String, Object> params = [
                title: "Test Title",
                headerText: "Test Header",
                message: "Test Message",
                defaultValue: "Default"
        ]
        // This calls the abstract prompt method, so it will return what our test impl provides
        String result = inOut.prompt(params)
        assertEquals("mocked", result)
    }

    @Test
    void testPromptSelectWithEmptyCollectionThrows() {
        // This tests the known bug in promptSelect with empty collections
        Collection<Object> emptyOptions = []
        assertThrows(NoSuchElementException.class) {
            inOut.promptSelect("Select one", emptyOptions)
        }
    }

    @Test
    void testViewInteger() {
        // Just ensure it doesn't throw - call directly
        inOut.view(42, "Title")
        // If we got here, no exception was thrown
        assertTrue(true)
    }

    @Test
    void testViewIntegerWithoutTitle() {
        // Just ensure it doesn't throw - call directly
        inOut.view(100)
        // If we got here, no exception was thrown
        assertTrue(true)
    }

    /**
     * Concrete implementation of AbstractInOut for testing purposes.
     * Provides minimal stub implementations for abstract methods.
     */
    @CompileStatic
    static class TestableInOut extends AbstractInOut {

        @Override
        File chooseFile(String title, File initialDirectory, String description, String... extensions) {
            return null
        }

        @Override
        File chooseFile(String title, String initialDirectory, String description, String... extensions) {
            return null
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
        String prompt(String message) {
            return "mocked"
        }

        @Override
        String prompt(String title, String message) {
            return "mocked"
        }

        @Override
        String prompt(String title, String headerText, String message) {
            return "mocked"
        }

        @Override
        String prompt(String title, String headerText, String message, String defaultValue) {
            return "mocked"
        }

        @Override
        YearMonth promptYearMonth(String message) {
            return YearMonth.now()
        }

        @Override
        YearMonth promptYearMonth(String title, String message, YearMonth from, YearMonth to, YearMonth initial) {
            return initial
        }

        @Override
        LocalDate promptDate(String title, String message, LocalDate defaultValue) {
            return defaultValue
        }

        @Override
        Object promptSelect(String title, String headerText, String message, Collection<Object> options, Object defaultValue) {
            return defaultValue
        }

        @Override
        String promptPassword(String title, String message) {
            return "password"
        }

        @Override
        void view(File file, String... title) {
            // no-op for testing
        }

        @Override
        void view(String html, String... title) {
            // no-op for testing
        }

        @Override
        void view(Matrix tableMatrix, String... title) {
            // no-op for testing
        }

        @Override
        void view(List<List<?>> matrix, String... title) {
            // no-op for testing
        }

        @Override
        void display(String fileName, String... title) {
            // no-op for testing
        }

        @Override
        void display(File file, String... title) {
            // no-op for testing
        }

        @Override
        void display(JComponent swingComponent, String... title) {
            // no-op for testing
        }

        @Override
        void display(Chart chart, String... titleOpt) {
            // no-op for testing
        }
    }
}
