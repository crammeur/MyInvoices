package ca.qc.bergeron.marcantoine.crammeur.librairy.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-08.
 */

public class EntityConstraint implements ConstraintValidator<Entity, Data> {
    @Override
    public void initialize(Entity constraintAnnotation) {

    }

    @Override
    public boolean isValid(Data value, ConstraintValidatorContext context) {
        return value.getClass().getConstructors().length <= 1 && value.getClass().getConstructors()[0].getParameterTypes().length < 1;
    }
}
