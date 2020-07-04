/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.metadata.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;

/**
 * Check for circular dependencies within the data model for complex type. A circular dependency is found
 * when there's a path from an complex type back to itself. A recursive dependency may be a cycle if it's
 * mandatory (this would lead to "chicken-egg" situations otherwise).
 */
public class CircularComplexTypeValidationRule implements ValidationRule {

    private static final Logger LOGGER = LogManager.getLogger(CircularComplexTypeValidationRule.class);

    private final MetadataRepository repository;

    public CircularComplexTypeValidationRule(MetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        List<ComplexTypeMetadata> types = new ArrayList<ComplexTypeMetadata>(repository.getNonInstantiableTypes());

        final List<String> lineComplexNameList = new ArrayList<>();
        for (ComplexTypeMetadata currentType : types) {
            final List<ComplexTypeMetadata> cycleList = new ArrayList<>();
            lineComplexNameList.clear();
            Boolean isCircularDependency = currentType.accept(new DefaultMetadataVisitor<Boolean>() {

                @Override
                public Boolean visit(ContainedComplexTypeMetadata containedType) {
                    return isCircularComplexType(containedType);
                }

                @Override
                public Boolean visit(ComplexTypeMetadata complexType) {
                    return isCircularComplexType(complexType);
                }

                private Boolean isCircularComplexType(ComplexTypeMetadata complexType) {
                    if (lineComplexNameList.contains(complexType.getName())) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Complex Type [" + complexType.getName() + "] have a circular dependency.");
                        }
                        return true;
                    }
                    lineComplexNameList.add(complexType.getName());
                    List<FieldMetadata> fields = new ArrayList<FieldMetadata>(complexType.getFields());
                    for (FieldMetadata field : fields) {
                        Boolean isCircularFlag = field.accept(this);
                        if (isCircularFlag == null) {
                            continue;
                        } else if (isCircularFlag) {
                            cycleList.add(complexType);
                            return true;
                        }
                        lineComplexNameList.remove(field.getType().getName());
                    }
                    return false;
                }
            });

            if (isCircularDependency) {
                for (ComplexTypeMetadata cycle : cycleList) {
                    handler.error(cycle, "Type '" + cycle.getName() + "' is included in a circular dependency.",
                            cycle.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                            cycle.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                            cycle.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                            ValidationError.CIRCULAR_DEPENDENCY);
                };
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
