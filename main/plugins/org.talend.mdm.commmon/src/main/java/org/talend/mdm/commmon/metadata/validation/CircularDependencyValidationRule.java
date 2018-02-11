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

import java.util.List;
import java.util.Map;

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * Check for circular dependencies within the data model. A circular dependency is found when when there's a path from
 * an entity back to itself following <b>mandatory</b> foreign keys. A recursive dependency may be a cycle if it's
 * mandatory (this would lead to "chicken-egg" situations otherwise).
 * 
 * @see org.talend.mdm.commmon.metadata.MetadataUtils#sortTypes(org.talend.mdm.commmon.metadata.MetadataRepository)
 */
class CircularDependencyValidationRule implements ValidationRule {

    private final MetadataRepository repository;

    public CircularDependencyValidationRule(MetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        try {
            // Attempts to sort all types in dependency order (throw exception if can't be done).
            MetadataUtils.sortTypes(repository, MetadataUtils.SortType.STRICT);
            return true;
        } catch (CircularDependencyException e) {
            Map<ComplexTypeMetadata, List<FieldMetadata>> cycleHints = e.getCycleHints();
            for (Map.Entry<ComplexTypeMetadata, List<FieldMetadata>> cycle : cycleHints.entrySet()) {
                handler.error(cycle.getKey(), "Type '" + cycle.getKey().getName() + "' is included in a circular dependency.",
                        cycle.getKey().<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        cycle.getKey().<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        cycle.getKey().<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.CIRCULAR_DEPENDENCY);
            }
        }
        return false;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
