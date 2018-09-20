/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.compare;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.util.core.CommonUtil;

public class HibernateStorageImpactAnalyzer implements ImpactAnalyzer {

    protected final String STRING_DEFAULT_LENGTH = "255"; //$NON-NLS-1$

    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        Map<Impact, List<Change>> impactSort = new EnumMap<Impact, List<Change>>(Impact.class);
        for (Impact impact : Impact.values()) {
            impactSort.put(impact, new LinkedList<Change>());
        }
        // Add actions
        for (AddChange addAction : diffResult.getAddChanges()) {
            MetadataVisitable element = addAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                impactSort.get(Impact.LOW).add(addAction);
            } else if (element instanceof FieldMetadata) {
                if (element instanceof ContainedTypeFieldMetadata) {
                    // Contained field may change mapping strategy
                    impactSort.get(Impact.HIGH).add(addAction);
                } else {
                    String defaultValueRule = ((FieldMetadata) element).getData(MetadataRepository.DEFAULT_VALUE_RULE);
                    
                    // TMDM-7895: Newly added element and mandatory should be considered as "high" change
                    if (((FieldMetadata) element).isMandatory() && StringUtils.isBlank(defaultValueRule)) {
                        impactSort.get(Impact.HIGH).add(addAction);
                    } else {
                        impactSort.get(Impact.LOW).add(addAction);
                    }
                }
            }
        }
        // Remove actions
        for (RemoveChange removeAction : diffResult.getRemoveChanges()) {
            MetadataVisitable element = removeAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else if (element instanceof SimpleTypeFieldMetadata && !(((FieldMetadata)element).getContainingType() instanceof ContainedComplexTypeMetadata)) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else if (element instanceof SimpleTypeFieldMetadata && ((FieldMetadata)element).getContainingType().getContainer() != null && 
                    ((FieldMetadata)element).getContainingType().getContainer() instanceof ContainedTypeFieldMetadata) {
                impactSort.get(Impact.HIGH).add(removeAction);
            } else if (element instanceof ContainedTypeFieldMetadata) {
                impactSort.get(Impact.HIGH).add(removeAction);
            } else if (element instanceof FieldMetadata) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else {
                throw new NotImplementedException();
            }
        }
        // Modify actions
        for (ModifyChange modifyAction : diffResult.getModifyChanges()) {
            MetadataVisitable element = modifyAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                // Type modifications may include many things (inheritance changes for instance).
                impactSort.get(Impact.HIGH).add(modifyAction);
            } else if (element instanceof FieldMetadata) {
                FieldMetadata previous = (FieldMetadata) modifyAction.getPrevious();
                FieldMetadata current = (FieldMetadata) modifyAction.getCurrent();
                // TMDM-9909: Increase the length of a string element should be low impact
                Object previousLength = CommonUtil.getSuperTypeMaxLength(previous.getType(), previous.getType());
                Object currentLength = CommonUtil.getSuperTypeMaxLength(current.getType(), current.getType());

                // TMDM-8022: issues about custom decimal type totalDigits/fractionDigits.
                Object previousTotalDigits = previous.getType().getData(MetadataRepository.DATA_TOTAL_DIGITS);
                Object currentTotalDigits = current.getType().getData(MetadataRepository.DATA_TOTAL_DIGITS);

                Object previousFractionDigits = previous.getType().getData(MetadataRepository.DATA_FRACTION_DIGITS);
                Object currentFractionDigits = current.getType().getData(MetadataRepository.DATA_FRACTION_DIGITS);
                /*
                 * HIGH IMPACT CHANGES
                 */
                if (element instanceof SimpleTypeFieldMetadata
                        && MetadataUtils.getSuperConcreteType(((FieldMetadata) element).getType()).getName().equals("string")
                        && Integer.valueOf((String) (currentLength == null ? STRING_DEFAULT_LENGTH : currentLength)).compareTo(
                                Integer.valueOf((String) (previousLength == null ? STRING_DEFAULT_LENGTH : previousLength))) > 0) {
                    impactSort.get(Impact.LOW).add(modifyAction);
                } else if (!ObjectUtils.equals(previousLength, currentLength)) {
                    // Won't be able to change constraint for max length
                    impactSort.get(Impact.HIGH).add(modifyAction);
                } else if (!ObjectUtils.equals(previousTotalDigits, currentTotalDigits)) {
                    // TMDM-8022: issues about custom decimal type totalDigits/fractionDigits.
                    impactSort.get(Impact.HIGH).add(modifyAction);
                } else if (!ObjectUtils.equals(previousFractionDigits, currentFractionDigits)) {
                    // TMDM-8022: issues about custom decimal type totalDigits/fractionDigits.
                    impactSort.get(Impact.HIGH).add(modifyAction);
                } else if (!previous.getType().equals(current.getType())) {
                    TypeMetadata superPreviousType = MetadataUtils.getSuperConcreteType(previous.getType());
                    TypeMetadata superCurrentType = MetadataUtils.getSuperConcreteType(current.getType());
                    if (superPreviousType == null) {
                        throw new IllegalStateException("Unable to find super type of '" + previous.getType().getName() + "'.");
                    }
                    if (superPreviousType.equals(superCurrentType)) {
                        // TMDM-7748: Type modification is ok as long as the super type remains the same.
                        impactSort.get(Impact.LOW).add(modifyAction);
                    } else {
                        // Type modification has high impact (values might not be following correct format).
                        impactSort.get(Impact.HIGH).add(modifyAction);
                    }
                } else if (previous.isMany() != current.isMany()) {
                    // Collection mapping undo (or creation) has a high impact on schema
                    impactSort.get(Impact.HIGH).add(modifyAction);
                } else if (previous.isKey() != current.isKey()) {
                    // Key creation might have high impact (in case of duplicated values).
                    impactSort.get(Impact.HIGH).add(modifyAction);
                } else if (previous.isMandatory() != current.isMandatory()) {
                    if (element instanceof SimpleTypeFieldMetadata) {
                        if (!previous.isMandatory() && current.isMandatory()) {
                            // Won't be able to change constraint
                            String defaultValueRule = ((FieldMetadata) current).getData(MetadataRepository.DEFAULT_VALUE_RULE);
                            if (!modifyAction.isHasNullValue()) {
                                impactSort.get(Impact.LOW).add(modifyAction);
                            } else if (modifyAction.isHasNullValue() && StringUtils.isBlank(defaultValueRule)) {
                                impactSort.get(Impact.HIGH).add(modifyAction);
                            } else if (modifyAction.isHasNullValue() && StringUtils.isNotBlank(defaultValueRule)) {
                                impactSort.get(Impact.MEDIUM).add(modifyAction);
                            }
                        } else if (previous.isMandatory() && !current.isMandatory()) {
                            impactSort.get(Impact.LOW).add(modifyAction);
                        }
                    } else if (element instanceof ContainedTypeFieldMetadata) {
                        if (!previous.isMany() && !previous.isMandatory()) {
                            impactSort.get(Impact.HIGH).add(modifyAction);
                        } else if (previous.isMandatory() && !previous.isMany()) {
                            if (!current.isMandatory() && !current.isMany()) {
                                impactSort.get(Impact.LOW).add(modifyAction);
                            } else if (current.isMany()) {
                                impactSort.get(Impact.LOW).add(modifyAction);
                            }
                        } else if (previous.isMany()) {
                            if (!current.isMany()) {
                                impactSort.get(Impact.HIGH).add(modifyAction);
                            } else {
                                impactSort.get(Impact.LOW).add(modifyAction);
                            }
                        }
                    }
                }
                
                if(previous instanceof ReferenceFieldMetadata){
                    if(current instanceof ReferenceFieldMetadata){
                        ReferenceFieldMetadata previousFieldMetadata = (ReferenceFieldMetadata)previous;
                        ReferenceFieldMetadata currentFieldMetadata = (ReferenceFieldMetadata)current;
                        if(!previousFieldMetadata.getReferencedType().getName().equals(currentFieldMetadata.getReferencedType().getName())){
                            impactSort.get(Impact.HIGH).add(modifyAction);
                        }
                    } else {
                        impactSort.get(Impact.HIGH).add(modifyAction);
                    }
                }
            }
        }
        return impactSort;
    }
}
