/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.util.core.CommonUtil;

public class HibernateStorageImpactAnalyzer implements ImpactAnalyzer {

    protected static final String STRING_DEFAULT_LENGTH = "255"; //$NON-NLS-1$

    public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
        Map<Impact, List<Change>> impactSort = new EnumMap<Impact, List<Change>>(Impact.class);
        for (Impact impact : Impact.values()) {
            impactSort.put(impact, new LinkedList<Change>());
        }
        // Add actions
        analyzeAddChange(diffResult, impactSort);
        // Remove actions
        analyzeRemoveChange(diffResult, impactSort);
        // Modify actions
        analyzeModifyChange(diffResult, impactSort);
        return impactSort;
    }

    protected void analyzeAddChange(Compare.DiffResults diffResult, Map<Impact, List<Change>> impactSort) {
        for (AddChange addAction : diffResult.getAddChanges()) {
            MetadataVisitable element = addAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                impactSort.get(Impact.LOW).add(addAction);
            } else if (element instanceof FieldMetadata) {
                if (element instanceof ContainedTypeFieldMetadata) { // Newly added complex field
                    if (((FieldMetadata) element).isMandatory()) {
                        if (hasOptionalAncestor(diffResult.getAddChanges(), (FieldMetadata) element)) {
                            impactSort.get(Impact.LOW).add(addAction);
                        } else {// With mandatory ancestor
                            impactSort.get(Impact.HIGH).add(addAction);
                        }
                    } else {
                        impactSort.get(Impact.LOW).add(addAction);
                    }
                } else { // Newly added simple field
                    String defaultValue = ((FieldMetadata) element).getData(MetadataRepository.DEFAULT_VALUE);
                    if (((FieldMetadata) element).isMandatory() && StringUtils.isBlank(defaultValue)) {
                        if (hasOptionalAncestor(diffResult.getAddChanges(), (FieldMetadata) element)) {
                            impactSort.get(Impact.LOW).add(addAction);
                        } else {// With mandatory ancestor
                            impactSort.get(Impact.HIGH).add(addAction);
                        }
                    } else {
                        impactSort.get(Impact.LOW).add(addAction);
                    }
                }
            }
        }
    }

    protected void analyzeRemoveChange(Compare.DiffResults diffResult, Map<Impact, List<Change>> impactSort) {
        for (RemoveChange removeAction : diffResult.getRemoveChanges()) {
            MetadataVisitable element = removeAction.getElement();
            if (element instanceof ComplexTypeMetadata) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else if (element instanceof SimpleTypeFieldMetadata
                    && !(((FieldMetadata) element).getContainingType() instanceof ContainedComplexTypeMetadata)) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else if (element instanceof SimpleTypeFieldMetadata
                    && ((FieldMetadata) element).getContainingType().getContainer() != null
                    && ((FieldMetadata) element).getContainingType().getContainer() instanceof ContainedTypeFieldMetadata) {
                impactSort.get(Impact.HIGH).add(removeAction);
            } else if (element instanceof ContainedTypeFieldMetadata) {
                impactSort.get(Impact.HIGH).add(removeAction);
            } else if (element instanceof ReferenceFieldMetadata) {
                if (removeAction.isContainsData()) {
                    impactSort.get(Impact.HIGH).add(removeAction);
                } else {
                    impactSort.get(Impact.MEDIUM).add(removeAction);
                }
            } else if (element instanceof FieldMetadata) {
                impactSort.get(Impact.MEDIUM).add(removeAction);
            } else {
                throw new NotImplementedException();
            }
        }
    }

    protected void analyzeModifyChange(Compare.DiffResults diffResult, Map<Impact, List<Change>> impactSort) {
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
                int newLength = Integer.parseInt((currentLength == null ? STRING_DEFAULT_LENGTH : (String) currentLength));
                int oldLength = Integer.parseInt((previousLength == null ? STRING_DEFAULT_LENGTH : (String) previousLength));

                String fieldType = MetadataUtils.getSuperConcreteType(((FieldMetadata) element).getType()).getName();
                if (element instanceof SimpleTypeFieldMetadata && fieldType.equals("string") && newLength > oldLength) { //$NON-NLS-1$
                    if (MapUtils.getBooleanValue(modifyAction.getData(), Change.CHANGE_TO_CLOB)) {
                        impactSort.get(Impact.HIGH).add(modifyAction);
                    } else {
                        impactSort.get(Impact.LOW).add(modifyAction);
                    }
                } else if (!ObjectUtils.equals(previousLength, currentLength)) {
                    // Won't be able to change constraint for max length
                    if (MapUtils.getBooleanValue(modifyAction.getData(), Change.TEXT_TO_TEXT)) {
                        impactSort.get(Impact.LOW).add(modifyAction);
                    } else {
                        impactSort.get(Impact.HIGH).add(modifyAction);
                    }

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
                            String defaultValue = ((FieldMetadata) current).getData(MetadataRepository.DEFAULT_VALUE);
                            boolean isHasNullValue = MapUtils.getBooleanValue(modifyAction.getData(), Change.HAS_NULL_VALUE);
                            if (!isHasNullValue) {
                                impactSort.get(Impact.LOW).add(modifyAction);
                            } else if (isHasNullValue && StringUtils.isBlank(defaultValue)) {
                                impactSort.get(Impact.HIGH).add(modifyAction);
                            } else if (isHasNullValue && StringUtils.isNotBlank(defaultValue)) {
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

                if (previous instanceof ReferenceFieldMetadata) {
                    if (current instanceof ReferenceFieldMetadata) {
                        ReferenceFieldMetadata previousFieldMetadata = (ReferenceFieldMetadata) previous;
                        ReferenceFieldMetadata currentFieldMetadata = (ReferenceFieldMetadata) current;
                        if (!previousFieldMetadata.getReferencedType().getName()
                                .equals(currentFieldMetadata.getReferencedType().getName())) {
                            impactSort.get(Impact.HIGH).add(modifyAction);
                        }
                    } else {
                        impactSort.get(Impact.HIGH).add(modifyAction);
                    }
                }
            }
        }
    }

    /**
     * Check if an element's root parent is optional or not.
     *
     *<pre>
     * Entity
     *   |__A_optionanl_compplexType
     *          |___A1_mandatory_simpleField
     *          |___A2_optionnal_simpleField
     *          |___A3_mandatory_compplexType
     *                  |__B1_mandatory_simpleField
     *</pre>
     * 
     * As above, add A_optional_complexType to Entity, all the changes for A_optional_complexType's child elements
     * should be LOW priority. So for A1_mandatory_simpleField, A2_optionnal_simpleField, A3_mandatory_complexType,
     * B1_mandatory_simpleField will all return TRUE
     * 
     * @param addChanges
     * @param element
     * @return
     */
    private boolean hasOptionalAncestor(List<AddChange> addChanges, FieldMetadata element) {
        if (((FieldMetadata) element).getContainingType() instanceof ContainedComplexTypeMetadata) {
            ContainedComplexTypeMetadata contained = (ContainedComplexTypeMetadata) ((FieldMetadata) element).getContainingType();
            for (AddChange addChange : addChanges) {
                if (addChange.getElement().equals(contained) && !contained.getContainer().isMandatory()) {
                    return true;
                }
            }
            return hasOptionalAncestor(addChanges, contained.getContainer());
        }
        return false;
    }
}
