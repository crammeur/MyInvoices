package ca.qc.bergeron.marcantoine.crammeur.librairy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Marc-Antoine on 2017-09-07.
 */
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Data {
}
