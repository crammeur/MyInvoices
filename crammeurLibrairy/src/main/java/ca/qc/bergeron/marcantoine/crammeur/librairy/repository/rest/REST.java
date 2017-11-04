package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.rest;

import org.intellij.lang.annotations.Pattern;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-28.
 */
public abstract class REST<T extends Data<K>, K extends Serializable> extends DataFramework<T, K> {

    /*protected final java.util.regex.Pattern mPatternUrl = Patterns.WEB_URL;*/
    protected final String mUrl;

    public REST(Class<T> pClass, Class<K> pKey, Repository pRepository, @Pattern(value = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?(?:(([a-zA-Z0-9 -\uD7FF豈-\uFDCFﷰ-\uFFEF]([a-zA-Z0-9 -\uD7FF豈-\uFDCFﷰ-\uFFEF\\-]{0,61}[a-zA-Z0-9 -\uD7FF豈-\uFDCFﷰ-\uFFEF]){0,1}\\.)+[a-zA-Z -\uD7FF豈-\uFDCFﷰ-\uFFEF]{2,63}|((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9 -\uD7FF豈-\uFDCFﷰ-\uFFEF\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)") String pUrl) {
        super(pClass, pKey, pRepository);
        mUrl = pUrl;

    }
}
