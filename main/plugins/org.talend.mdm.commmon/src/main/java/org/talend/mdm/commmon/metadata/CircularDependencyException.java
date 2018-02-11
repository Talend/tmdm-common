/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.*;

/**
 * Exception thrown when a circular dependency in data model is found.
 * @see #getCycleHints()
 */
public class CircularDependencyException extends RuntimeException {

    private final Map<ComplexTypeMetadata, List<FieldMetadata>> cycleHints;

    public CircularDependencyException(Map<ComplexTypeMetadata, List<FieldMetadata>> cycleHints) {
        this.cycleHints = cycleHints;
    }

    /**
     * @return A {@link java.util.Map map} that contains as <em>key</em> a possible cycle start and as <em>value</em>
     * possible fields that created the cycle. This map is provided as 'hints': it gives a list of fields that could be
     * modified to break the cycle, but not as a list of required actions.
     */
    public Map<ComplexTypeMetadata, List<FieldMetadata>> getCycleHints() {
        return cycleHints;
    }

    @Override
    public String getMessage() {
        StringBuilder cyclesAsString = new StringBuilder();
        int i = 1;
        for (Map.Entry<ComplexTypeMetadata, List<FieldMetadata>> cycle : cycleHints.entrySet()) {
            cyclesAsString.append(i++).append(") "); //$NON-NLS-1$
            cyclesAsString.append(cycle.getKey().getName()).append(" -> "); //$NON-NLS-1$
            cyclesAsString.append(" (possible fields: ");
            Iterator<FieldMetadata> iterator = cycle.getValue().iterator();
            while (iterator.hasNext()) {
                cyclesAsString.append(iterator.next().getPath());
                if (iterator.hasNext()) {
                    cyclesAsString.append(", "); //$NON-NLS-1$
                } else {
                    cyclesAsString.append(')');
                }
            }
            cyclesAsString.append('\n');
        }
        if (cycleHints.isEmpty()) {
            return ("Data model has circular dependencies.");
        } else {
            return ("Data model has circular dependencies:\n" + cyclesAsString);
        }
    }
}
