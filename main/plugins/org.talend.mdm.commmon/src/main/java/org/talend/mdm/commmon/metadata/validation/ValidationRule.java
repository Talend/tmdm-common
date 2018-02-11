/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.validation;

import org.talend.mdm.commmon.metadata.ValidationHandler;

/**
 * A interface for all metadata related validation rules. Using {@link CompositeValidationRule}, you may have a single
 * validation rule that execute multiple rules.
 * 
 * @see CompositeValidationRule
 */
public interface ValidationRule {

    /**
     * @param handler The validation handler used to report errors and warnings.
     * @return <code>true</code> if the rule was a success (no error found) or <code>false</code> in case the validation
     * rule failed.
     */
    boolean perform(ValidationHandler handler);

    /**
     * @return <code>true</code> if rule is not considered as a blocking error (e.g. a warning) and if consecutive rules
     * should still be executed even if this one failed. Returns <code>false</code> if other rule execution(s) should be
     * discarded if this rule fails.
     * @see CompositeValidationRule
     */
    boolean continueOnFail();

}
