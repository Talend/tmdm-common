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
 * A default implementation to return constant validation results (i.e. always succeeding or failing).
 */
class NoOpValidationRule implements ValidationRule {

    /**
     * A rule that always <b>succeeds</b>.
     */
    public static final ValidationRule SUCCESS = new NoOpValidationRule(true);

    /**
     * A rule that always <b>fails</b>.
     */
    public static final ValidationRule FAIL = new NoOpValidationRule(false);

    private final boolean value;

    private NoOpValidationRule(boolean value) {
        this.value = value;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        // No op
        return value;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
