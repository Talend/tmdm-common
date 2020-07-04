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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.MetadataVisitable;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.util.core.CommonUtil;

public class Compare {

    private static final Logger LOGGER = LogManager.getLogger(Compare.class);

    /**
     * Compare two {@link org.talend.mdm.commmon.metadata.MetadataRepository repositories} and return the differences
     * between them.
     *
     * @param left The original {@link org.talend.mdm.commmon.metadata.MetadataRepository repository}.
     * @param right The new {@link org.talend.mdm.commmon.metadata.MetadataRepository repository}.
     * @return The {@link org.talend.mdm.commmon.metadata.compare.Compare.DiffResults differences} between the two
     * repositories.
     * @see org.talend.mdm.commmon.metadata.compare.Compare.DiffResults
     */
    public static DiffResults compare(MetadataRepository left, MetadataRepository right) {
        Collection<ComplexTypeMetadata> leftEntityTypes = left.getUserComplexTypes();
        DiffResults diffResults = new DiffResults();
        compareEntitiesChange(left, right, diffResults);

        DumpContent dumpContent = new DumpContent();
        for (ComplexTypeMetadata leftType : leftEntityTypes) {
            ComplexTypeMetadata rightType = right.getComplexType(leftType.getName());
            if(rightType != null){
                // Read left content
                List<MetadataVisitable> leftContent = new ArrayList<MetadataVisitable>(leftType.accept(dumpContent));
                dumpContent.reset();
                // Read right content
                List<MetadataVisitable> rightContent = new ArrayList<MetadataVisitable>(rightType.accept(dumpContent));
                dumpContent.reset();
                // Compare contents
                Map<String, FieldMetadata> removedElementNames = new HashMap<String, FieldMetadata>();
                for (MetadataVisitable leftVisitable : leftContent) {
                    int index = rightContent.indexOf(leftVisitable);
                    if (index < 0) {
                        // Different (right does not exist, but might be removed or modified).
                        if (leftVisitable instanceof FieldMetadata) {
                            FieldMetadata field = (FieldMetadata) leftVisitable;
                            if (field.getContainingType() instanceof ContainedComplexTypeMetadata) {
                                removedElementNames.put(field.getContainingType().getContainer().getName() + "/"
                                        + field.getContainingType().getName() + "/" + field.getName(), field);
                            } else {
                                removedElementNames.put(field.getContainingType().getName() + "/" + field.getName(), field);
                            }
                        }
                    } else {
                        // Field exists on both sides, but checks max length
                        MetadataVisitable rightElement = rightContent.get(index);
                        if (leftVisitable instanceof FieldMetadata) {
                            TypeMetadata leftVisitableType = ((FieldMetadata) leftVisitable).getType();
                            TypeMetadata rightVisitableType = ((FieldMetadata) rightElement).getType();
                            if(leftVisitable instanceof ReferenceFieldMetadata){
                                compareReferenceFieldMetadata(diffResults.modifyChanges, (ReferenceFieldMetadata) leftVisitable, (ReferenceFieldMetadata) rightElement);
                            }
                            // TMDM-9909: Increase the length of a string element should be low impact
                            Object leftLength = CommonUtil.getSuperTypeMaxLength(leftVisitableType, leftVisitableType) ;
                            Object rightLength = CommonUtil.getSuperTypeMaxLength(rightVisitableType, rightVisitableType) ;
                            if (!ObjectUtils.equals(leftLength, rightLength)) {
                                diffResults.modifyChanges.add(new ModifyChange(leftVisitable, rightElement));
                            }
                            // TMDM-8022: issues about custom decimal type totalDigits/fractionDigits.
                            Object leftTotalDigits = leftVisitableType.getData(MetadataRepository.DATA_TOTAL_DIGITS);
                            Object rightTotalDigits = rightVisitableType.getData(MetadataRepository.DATA_TOTAL_DIGITS);
                            if (!ObjectUtils.equals(leftTotalDigits, rightTotalDigits)) {
                                diffResults.modifyChanges.add(new ModifyChange(leftVisitable, rightElement));
                            }
                            Object leftFractionDigits = leftVisitableType.getData(MetadataRepository.DATA_FRACTION_DIGITS);
                            Object rightFractionDigits = rightVisitableType.getData(MetadataRepository.DATA_FRACTION_DIGITS);
                            if (!ObjectUtils.equals(leftFractionDigits, rightFractionDigits)) {
                                diffResults.modifyChanges.add(new ModifyChange(leftVisitable, rightElement));
                            }
                        }
                        rightContent.remove(index); // Same or already marked as diff, so remove from things to compare
                    }
                }
                if (!rightContent.isEmpty()) {
                    Iterator<MetadataVisitable> addedElements = rightContent.iterator();
                    while (addedElements.hasNext()) {
                        MetadataVisitable current = addedElements.next();
                        MetadataVisitable modifiedElement = null;
                        if (current instanceof FieldMetadata) {
                            FieldMetadata field = (FieldMetadata) current;

                            if (field.getContainingType() instanceof ContainedComplexTypeMetadata) {
                                modifiedElement = removedElementNames.get(field.getContainingType().getContainer().getName()
                                        + "/" + field.getContainingType().getName() + "/" + field.getName());
                            } else {
                                modifiedElement = removedElementNames.get(field.getContainingType().getName() + "/"
                                        + field.getName());
                            }
                        }
                        if (modifiedElement != null) {
                            // Modified element (only exist in right, not in left).
                            diffResults.modifyChanges.add(new ModifyChange(modifiedElement, current));
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[MODIFIED] " + current + " was modified" + "\t was " + modifiedElement + "\t now "
                                        + current);
                            }
                            if (current instanceof FieldMetadata) {
                                FieldMetadata field = (FieldMetadata) current;

                                if (field.getContainingType() instanceof ContainedComplexTypeMetadata) {
                                    removedElementNames.remove(field.getContainingType().getContainer().getName() + "/"
                                            + field.getContainingType().getName() + "/" + field.getName());
                                } else {
                                    modifiedElement = removedElementNames.get(field.getContainingType().getName() + "/"
                                            + field.getName());
                                    removedElementNames.remove(field.getContainingType().getName() + "/" + field.getName());
                                }
                            }
                        } else {
                            // Added element (only exist in right, not in left).
                            diffResults.addChanges.add(new AddChange(current));
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[ADDED] " + current + " was added.");
                            }
                        }
                        addedElements.remove();
                    }
                }
                // Process removed elements
                for (FieldMetadata fieldMetadata : removedElementNames.values()) {
                    // Different (right does not exist).
                    diffResults.removeChanges.add(new RemoveChange(fieldMetadata));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[REMOVED] " + fieldMetadata + " no longer exist.");
                    }
                }
                if (!rightContent.isEmpty()) { // Not expected at all -> likely a bug
                    throw new IllegalStateException("Elements remain for comparison.");
                }
            }
        }

        List<ComplexTypeMetadata> instantiableTypes = left.getNonInstantiableTypes();
        compareTypesChange(left, right, diffResults);
        for (ComplexTypeMetadata leftType : instantiableTypes) {
            TypeMetadata rightType = right.getNonInstantiableType(leftType.getNamespace(), leftType.getName());
            if (rightType != null) {
                if (!leftType.getClass().equals(rightType.getClass())) {
                    // This is a very strange case (e.g. leftType was a simple type and new is a complex one...)
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[MODIFY] Type '" + leftType.getName() + "' changed (parsed object is different).");
                    }
                    diffResults.removeChanges.add(new RemoveChange(leftType));
                    diffResults.addChanges.add(new AddChange(rightType));
                }
            }
        }
        return diffResults;
    }

    private static class DumpContent extends DefaultMetadataVisitor<List<MetadataVisitable>> {

        private final Stack<MetadataVisitable> content = new Stack<MetadataVisitable>();

        public void reset() {
            content.clear();
        }

        @Override
        public List<MetadataVisitable> visit(MetadataRepository repository) {
            super.visit(repository);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(SimpleTypeMetadata simpleType) {
            content.push(simpleType);
            super.visit(simpleType);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(ComplexTypeMetadata complexType) {
            content.push(complexType);
            super.visit(complexType);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(ContainedComplexTypeMetadata containedType) {
            super.visit(containedType);
            content.push(containedType);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(ReferenceFieldMetadata referenceField) {
            super.visit(referenceField);
            content.push(referenceField);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(ContainedTypeFieldMetadata containedField) {
            if (containedField.getType() instanceof ComplexTypeMetadata
                    && MetadataRepository.isCircle((ComplexTypeMetadata) containedField.getType(), null)) {
            } else {
                super.visit(containedField);
            }
            content.push(containedField);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(FieldMetadata fieldMetadata) {
            super.visit(fieldMetadata);
            content.push(fieldMetadata);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(SimpleTypeFieldMetadata simpleField) {
            super.visit(simpleField);
            content.push(simpleField);
            return content;
        }

        @Override
        public List<MetadataVisitable> visit(EnumerationFieldMetadata enumField) {
            content.push(enumField);
            super.visit(enumField);
            return content;
        }
    }

    /**
     * Groups and sorts all differences between 2 repositories.
     *
     * @see #getAddChanges()
     * @see #getModifyChanges()
     * @see #getRemoveChanges()
     */
    public static class DiffResults {

        private final List<AddChange> addChanges = new LinkedList<AddChange>();

        private final List<RemoveChange> removeChanges = new LinkedList<RemoveChange>();

        private final List<ModifyChange> modifyChanges = new LinkedList<ModifyChange>();

        public List<AddChange> getAddChanges() {
            return Collections.unmodifiableList(addChanges);
        }

        public List<RemoveChange> getRemoveChanges() {
            return Collections.unmodifiableList(removeChanges);
        }

        public List<ModifyChange> getModifyChanges() {
            return Collections.unmodifiableList(modifyChanges);
        }

        @SuppressWarnings("unused")
        public List<Change> getActions() {
            List<Change> allChanges = new ArrayList<Change>(addChanges.size() + removeChanges.size() + modifyChanges.size());
            allChanges.addAll(addChanges);
            allChanges.addAll(removeChanges);
            allChanges.addAll(modifyChanges);
            return allChanges;
        }
    }

    @SuppressWarnings("unused")
    private static void compareEntitiesChange(MetadataRepository left, MetadataRepository right, DiffResults diffResults){
        List<ComplexTypeMetadata> unusedLeftEntityTypes = new ArrayList<ComplexTypeMetadata>();
        List<ComplexTypeMetadata> unusedRightEntityTypes = new ArrayList<ComplexTypeMetadata>();
        Set<ComplexTypeMetadata> complexTypeSet = new HashSet<ComplexTypeMetadata>();
        complexTypeSet.addAll(left.getUserComplexTypes());
        complexTypeSet.addAll(right.getUserComplexTypes());

        for(ComplexTypeMetadata ctm : complexTypeSet){
            if (left.getComplexType(ctm.getName()) != null && right.getComplexType(ctm.getName()) == null) {
                unusedLeftEntityTypes.add(ctm);
            } else if (left.getComplexType(ctm.getName()) == null && right.getComplexType(ctm.getName()) != null){
                unusedRightEntityTypes.add(ctm);
            }
        }

        if(unusedLeftEntityTypes != null && unusedLeftEntityTypes.size() > 0){
            for(ComplexTypeMetadata leftType : unusedLeftEntityTypes){
                // Right type does not exist
                diffResults.removeChanges.add(new RemoveChange(leftType));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[REMOVED] Type " + leftType + " no longer exist.");  //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }

        if(unusedRightEntityTypes != null && unusedRightEntityTypes.size() > 0){
            for(ComplexTypeMetadata rightType : unusedRightEntityTypes){
                // Added Right type element (only exist in right, not in left).
                diffResults.addChanges.add(new AddChange(rightType));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[ADDED] " + rightType + " was added.");  //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static void compareTypesChange(MetadataRepository left, MetadataRepository right, DiffResults diffResults){
        List<TypeMetadata> unusedLeftTypes = new ArrayList<TypeMetadata>();
        List<TypeMetadata> unusedRightTypes = new ArrayList<TypeMetadata>();
        Set<TypeMetadata> complexTypeSet = new HashSet<TypeMetadata>();
        complexTypeSet.addAll(left.getNonInstantiableTypes());
        complexTypeSet.addAll(right.getNonInstantiableTypes());

        for(TypeMetadata ctm : complexTypeSet){
            if (left.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) != null && right.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) == null) {
                if(left.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) instanceof ComplexTypeMetadata && MetadataUtils.countEntityUsageCount((ComplexTypeMetadata)left.getNonInstantiableType(ctm.getNamespace(), ctm.getName())) > 0){
                    unusedLeftTypes.add(ctm);
                }
            } else if (left.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) == null && right.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) != null){
                if(right.getNonInstantiableType(ctm.getNamespace(), ctm.getName()) instanceof ComplexTypeMetadata && MetadataUtils.countEntityUsageCount((ComplexTypeMetadata)right.getNonInstantiableType(ctm.getNamespace(), ctm.getName())) > 0){
                    unusedRightTypes.add(ctm);
                }
            }
        }

        if(unusedLeftTypes != null && unusedLeftTypes.size() > 0){
            for(TypeMetadata tm : unusedLeftTypes){
                // Right type does not exist
                diffResults.removeChanges.add(new RemoveChange(tm));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[REMOVED] Type " + tm + " no longer exist.");  //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }

        if(unusedRightTypes != null && unusedRightTypes.size() > 0){
            for(TypeMetadata tm : unusedRightTypes){
                // Added Right type element (only exist in right, not in left).
                diffResults.addChanges.add(new AddChange(tm));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[ADDED] " + tm + " was added.");  //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }
    }

    private static void compareReferenceFieldMetadata(List<ModifyChange> modifyChanges, ReferenceFieldMetadata leftField, ReferenceFieldMetadata rightField) {
        if(!leftField.getReferencedType().getName().equals(rightField.getReferencedType().getName())) {
            modifyChanges.add(new ModifyChange(leftField, rightField));
        }
    }

}
