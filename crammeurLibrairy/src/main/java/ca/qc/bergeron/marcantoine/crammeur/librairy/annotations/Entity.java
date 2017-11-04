/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.librairy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.constraints.NotNull;

/**
 * Created by Marc-Antoine on 2016-05-15.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {

    @NotNull
    String dbName() default "";

    @Inherited
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Id {
        @org.intellij.lang.annotations.Pattern(value = "\\w+")
        String name() default "Id";
    }

    @Inherited
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Cascade {
        boolean onUpdateCascade() default false;

        boolean onDeleteCascade() default false;
    }

    @Inherited
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Column {
        @org.intellij.lang.annotations.Pattern(value = "\\w+")
        String name();
    }
}
