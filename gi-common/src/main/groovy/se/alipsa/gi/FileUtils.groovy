package se.alipsa.gi

class FileUtils {

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
     * Find a resource using available class loaders.
     * It will also load resources/files from the
     * absolute path of the file system (not only the classpath's).
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
