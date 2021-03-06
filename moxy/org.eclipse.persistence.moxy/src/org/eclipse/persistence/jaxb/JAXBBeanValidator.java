/*******************************************************************************
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Marcel Valovy - 2.6 - initial implementation
 ******************************************************************************/
package org.eclipse.persistence.jaxb;

import org.eclipse.persistence.exceptions.BeanValidationException;
import org.eclipse.persistence.jaxb.xmlmodel.XmlBindings;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * JAXB Bean Validator. Serves three purposes:
 *  1. Determines if the validation callback should take place on the (un)marshal call.
 *  2. Processes the validation.
 *  3. Stores the constraintViolations from the last validation call.
 *
 * @author Marcel Valovy - marcel.valovy@oracle.com
 * @since 2.6
 */
class JAXBBeanValidator {

    public static final Class<?>[] DEFAULT_GROUP_ARRAY = new Class<?>[] { Default.class };
    private final String prefix;
    private Set<? extends ConstraintViolation<?>> constraintViolations = Collections.emptySet();
    private boolean shouldValidate;
    private Validator validator;
    private boolean stopSearchingForValidator;
    private BeanValidationMode beanValidationMode = BeanValidationMode.NONE; // Initial value NONE will save resources by not triggering internalStateChange() when validation is off.
    private ValidatorFactory preferredValidatorFactory;

    private JAXBBeanValidator(String prefix) {
        this.prefix = prefix;
    }

    static JAXBBeanValidator getMarshallingBeanValidator(){
        return new JAXBBeanValidator("");
    }

    static JAXBBeanValidator getUnmarshallingBeanValidator(){
        return new JAXBBeanValidator("un");
    }

    /**
     * PUBLIC:
     *
     * First, determines whether the validation should proceed based on the provided parameters.
     *
     * Second, depending on Bean Validation Mode, either returns false or tries to initialize Validator:
     *  - AUTO tries to initialize Validator:
     *          returns true if succeeds, else false.
     *  - CALLBACK tries to initialize Validator:
     *          returns true if succeeds, else throws {@link BeanValidationException#providerNotFound}.
     *  - NONE returns false;
     *
     * BeanValidationMode is propagated from (un)marshaller upon each call.
     * If change of mode is detected, the internal state of the JAXBBeanValidator will be switched.
     *
     *
     * @param beanValidationMode Bean validation mode - allowed values AUTO, CALLBACK, NONE.
     * @param value Some objects should not be validated, e.g. XmlBindings.
     * @param preferredValidatorFactory May be null. Will use this factory as the preferred provider, if null, will use javax defaults.
     * @return True if should proceed with validation, else false.
     * @throws BeanValidationException Either {@link BeanValidationException#illegalValidationMode} or {@link BeanValidationException#providerNotFound}.
     * @since 2.6
     */
    boolean shouldValidate(Object value, BeanValidationMode beanValidationMode, ValidatorFactory preferredValidatorFactory) throws BeanValidationException {
        if (value instanceof XmlBindings) return false; // Do not validate XmlBindings.

        if (this.beanValidationMode != beanValidationMode) { // The Bean Validation mode was changed (or it's the first time this method is called on current instance).
            this.beanValidationMode = beanValidationMode;
            this.preferredValidatorFactory = preferredValidatorFactory;
            changeInternalState();
        }
        return shouldValidate;
    }

    /**
     * INTERNAL:
     *
     * Validates the value, as per BV spec.
     * Stores the constraintViolations in instance field. The field's value is
     * not preserved through calls and gets replaced by new constraintViolations.
     *
     * @param value Object to be validated.
     * @param groups Target groups as per BV spec. Must not be null, may be empty.
     * @throws BeanValidationException {@link BeanValidationException#constraintViolation}
     */
    void validate(Object value, Class<?>... groups) throws BeanValidationException {
        constraintViolations = validator.validate(value, groups);
        if (!constraintViolations.isEmpty())
            throw buildConstraintViolationException();
    }

    /**
     * @return constraintViolations from the last {@link #validate} call.
     */
    Set<? extends ConstraintViolation<?>> getConstraintViolations() {
        return constraintViolations;
    }

    /**
     * INTERNAL:
     *
     * Puts variables to states which conform to the internal state machine.
     *
     * Internal states:
     *  Mode/Field Value          | NONE        | AUTO         | CALLBACK
     *  --------------------------|-------------|--------------|--------------
     *  shouldValidate            | false       | true/false   | true/false
     *  stopSearchingForValidator | false       | true/false   | false
     *  constraintViolations      | EmptySet<>  | n/a          | n/a
     *
     *  n/a ... not altered.
     *
     * @throws BeanValidationException illegalValidationMode or providerNotFound
     */
    private void changeInternalState() throws BeanValidationException {
        stopSearchingForValidator = false; // Reset the switch.
        switch (beanValidationMode) {
        case NONE:
            shouldValidate = false;
            constraintViolations = Collections.emptySet(); // Clear the reference from previous (un)marshal calls.
            break;
        case CALLBACK:
        case AUTO:
            shouldValidate = initValidator();
            break;
        default:
            throw BeanValidationException.illegalValidationMode(prefix, beanValidationMode.toString());
        }
    }

    /**
     * PUBLIC:
     *
     * Initializes validator if not already initialized.
     * If mode is BeanValidationMode.AUTO, then after an unsuccessful try to
     * initialize a Validator, property {@code stopSearchingForValidator} will be set to true.
     *
     * NOTE: Property {@code stopSearchingForValidator} can be reset only by triggering
     * {@link #changeInternalState}.
     *
     * @return {@code true} if validator initialization succeeded, otherwise {@code false}.
     * @throws BeanValidationException
     */
    private boolean initValidator() throws BeanValidationException {
        if (validator == null && !stopSearchingForValidator){
            try {
                ValidatorFactory factory = getValidatorFactory();
                validator = factory.getValidator();
            } catch (ValidationException ve) {
                if (beanValidationMode == BeanValidationMode.CALLBACK){
                    /* The following line ensures that changeInternalState() will be the
                     triggered on next (un)marshalling trials if mode is still CALLBACK.
                      That will ensure searching for Validator implementation again. */
                    beanValidationMode = BeanValidationMode.AUTO;
                    throw BeanValidationException.providerNotFound(prefix, ve);
                } else { // mode AUTO
                    stopSearchingForValidator = true; // will not try to initialize validator on next tries.
                }
            }
        }
        return validator != null;
    }

    /**
     * INTERNAL:
     *
     * @return Preferred ValidatorFactory if set, else {@link Validation#buildDefaultValidatorFactory()}.
     */
    private ValidatorFactory getValidatorFactory() {
        return preferredValidatorFactory != null ? preferredValidatorFactory : Validation.buildDefaultValidatorFactory();
    }

    /**
     * INTERNAL:
     *
     * Builds ConstraintViolationException with constraintViolations, but no message.
     * Builds BeanValidationException with fully descriptive message, containing
     * the ConstraintViolationException.
     *
     * @return BeanValidationException, containing ConstraintViolationException.
     */
    private BeanValidationException buildConstraintViolationException() throws BeanValidationException {
        ConstraintViolationException cve = new ConstraintViolationException((Set<ConstraintViolation<?>>) /* do NOT remove the cast */ constraintViolations);
        return BeanValidationException.constraintViolation(createConstraintViolationExceptionArgs(), cve);
    }

    /**
     * INTERNAL:
     * Builds an Object array containing args for ConstraintViolationException constructor.
     *
     * @return  [0] - prefix,
     *          [1] - rootBean (on what object the validation failed),
     *          [2] - linkedList of violatedConstraints, with overriden toString() for better formatting.
     */
    private Object[] createConstraintViolationExceptionArgs() {
        Object[] args = new Object[3];
        Iterator<? extends ConstraintViolation<?>> iterator = constraintViolations.iterator();
        ConstraintViolation<?> cv = iterator.next();
        Collection<ConstraintViolationInfo> violatedConstraints = new LinkedList<ConstraintViolationInfo>(){
            public String toString() {
                Iterator<ConstraintViolationInfo> it = iterator();
                StringBuilder sb = new StringBuilder();
                while (it.hasNext())
                    sb.append("\n-->").append(it.next().toString());
                return sb.toString();
            }
        };
        args[0] = prefix;
        Object bean = cv.getRootBean();
        args[1] = bean.getClass().toString().substring("class ".length()) + "@" + Integer.toHexString(System.identityHashCode(bean)); // Can't use toString because of security reasons; identityHashCode can't throw NPE.
        args[2] = violatedConstraints;
        for (;;) {
            violatedConstraints.add(new ConstraintViolationInfo(cv.getMessage(), cv.getPropertyPath()));
            if (iterator.hasNext()) cv = iterator.next();
            else break;
        }
        return args;
    }


    /**
     * INTERNAL:
     *
     * Value Object class that provides adequate toString() method which describes
     * on which field a Validation Constraint was violated and includes it's violationDescription.
     */
    private static class ConstraintViolationInfo {
        private final String violationDescription;
        private final Path propertyPath;

        private ConstraintViolationInfo(String message, Path propertyPath){
            this.violationDescription = message;
            this.propertyPath = propertyPath;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("Violated constraint on property ").append(propertyPath)
                    .append(": \"").append(violationDescription).append("\".")
                    .toString();
        }
    }

}
