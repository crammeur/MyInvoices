package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by Marc-Antoine on 2017-10-31.
 */

public class XMLResourceBundle extends ResourceBundle {

    private Properties props;

    public static class Control extends ResourceBundle.Control {
        private static final String XML = "xml";
        private static final List<String> SINGLETON_LIST = Collections.singletonList(XML);

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {

            if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
                throw new IllegalArgumentException("baseName, locale, format and loader cannot be null");
            }
            if (!format.equals(XML)) {
                throw new IllegalArgumentException("format must be xml");
            }

            final String bundleName = toBundleName(baseName, locale);
            final String resourceName = toResourceName(bundleName, format);
            final URL url = loader.getResource(resourceName);
            if (url == null) {
                return null;
            }

            final URLConnection urlconnection = url.openConnection();
            if (urlconnection == null) {
                return null;
            }

            if (reload) {
                urlconnection.setUseCaches(false);
            }

            try (final InputStream stream = urlconnection.getInputStream();
                 final BufferedInputStream bis = new BufferedInputStream(stream);
            ) {
                return new XMLResourceBundle(bis);
            }
        }

        @Override
        public List<String> getFormats(String baseName) {
            return SINGLETON_LIST;
        }
    }

    public XMLResourceBundle(InputStream stream) throws IOException {
        props = new Properties();
        props.loadFromXML(stream);
    }

    @Override
    protected Object handleGetObject(@NotNull String key) {
        return props.getProperty(key);
    }

    @NotNull
    @Override
    public Enumeration<String> getKeys() {
        Set<String> handleKeys = props.stringPropertyNames();
        return Collections.enumeration(handleKeys);
    }

}
