package se.alipsa.gi

import groovy.transform.CompileStatic

/**
 * Utility class for file and resource operations.
 * <p>
 * Provides helper methods for extracting file names from paths/URLs
 * and locating resources from various classloaders.
 */
@CompileStatic
class FileUtils {

    /**
     * Extracts the base filename from a path or URL string.
     * <p>
     * Handles both Unix and Windows path separators, and strips query strings from URLs.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code baseName("/path/to/file.txt")} returns {@code "file.txt"}</li>
     *   <li>{@code baseName("C:\\path\\to\\file.txt")} returns {@code "file.txt"}</li>
     *   <li>{@code baseName("http://example.com/file.txt?param=1")} returns {@code "file.txt"}</li>
     *   <li>{@code baseName("filename")} returns {@code "filename"}</li>
     *   <li>{@code baseName("/path/to/dir/")} returns {@code "/path/to/dir/"} (empty basename)</li>
     * </ul>
     *
     * @param url the path or URL string to extract the filename from
     * @return the base filename, or the original string if no path separator is found,
     *         or {@code null} if the input is {@code null}
     */
    static String baseName(String url) {
        if (url == null) return null;
        String basename = "";
        url = url.replace('\\', '/');
        if (url.contains("/")) {
            String filePart = url.substring(url.lastIndexOf('/')+1);
            if (filePart.contains("?")) {
                basename = filePart.substring(0, filePart.indexOf('?'));
            } else {
                basename = filePart;
            }
        }
        return basename.length() > 0 ? basename : url;
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
     * @return the URL of the resource, or {@code null} if not found and path is invalid
     */
    static URL getResourceUrl(String resource) {
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
                return new File(resource).toURI().toURL()
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
