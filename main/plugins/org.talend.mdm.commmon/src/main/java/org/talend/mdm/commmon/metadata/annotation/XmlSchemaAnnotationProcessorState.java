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

package org.talend.mdm.commmon.metadata.annotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

/**
 * A simple bean that keeps track of information parsed by {@link XmlSchemaAnnotationProcessor} implementations.
 */
public class XmlSchemaAnnotationProcessorState {

    private final List<String> hide = new LinkedList<String>();

    private final List<String> allowWrite = new LinkedList<String>();

    private final List<String> denyCreate = new LinkedList<String>();

    private final List<String> denyPhysicalDelete = new LinkedList<String>();

    private final List<String> denyLogicalDelete = new LinkedList<String>();

    private final List<FieldMetadata> foreignKeyInfo = new LinkedList<FieldMetadata>();

    private String foreignKeyInfoFormat;

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    private boolean fkIntegrity = true; // Default is to enforce FK integrity

    private boolean fkIntegrityOverride = false; // Default is to disable FK integrity check

    private TypeMetadata fieldType;

    private boolean isReference;

    private TypeMetadata referencedType;

    private FieldMetadata referencedField;

    private String schematron = StringUtils.EMPTY;

    private List<FieldMetadata> primaryKeyInfo = Collections.emptyList();

    private List<FieldMetadata> lookupFields = Collections.emptyList();

    private final List<String> workflowAccessRights = new LinkedList<String>();

    private String visibilityRule;

    private String foreignKeyFilter = StringUtils.EMPTY;
    
    private String defaultValueRule;

    public void setFkIntegrity(boolean fkIntegrity) {
        this.fkIntegrity = fkIntegrity;
    }

    public void setFkIntegrityOverride(boolean fkIntegrityOverride) {
        this.fkIntegrityOverride = fkIntegrityOverride;
    }

    public void setForeignKeyInfo(FieldMetadata foreignKeyInfo) {
        this.foreignKeyInfo.add(foreignKeyInfo);
    }

    public void setForeignKeyInfoFormat(String foreignKeyInfoFormat) {
        this.foreignKeyInfoFormat = foreignKeyInfoFormat;
    }

    public void setFieldType(TypeMetadata fieldType) {
        this.fieldType = fieldType;
    }

    public void markAsReference() {
        isReference = true;
    }

    public void setReferencedType(TypeMetadata referencedType) {
        this.referencedType = referencedType;
    }

    public void setReferencedField(FieldMetadata referencedField) {
        this.referencedField = referencedField;
    }

    public boolean isFkIntegrity() {
        return fkIntegrity;
    }

    public boolean isFkIntegrityOverride() {
        return fkIntegrityOverride;
    }

    public List<FieldMetadata> getForeignKeyInfo() {
        return foreignKeyInfo;
    }

    public String getForeignKeyInfoFormat() {
        return foreignKeyInfoFormat;
    }

    public TypeMetadata getFieldType() {
        return fieldType;
    }

    public boolean isReference() {
        return isReference;
    }

    public TypeMetadata getReferencedType() {
        return referencedType;
    }

    public FieldMetadata getReferencedField() {
        return referencedField;
    }

    public List<String> getHide() {
        return hide;
    }

    public List<String> getAllowWrite() {
        return allowWrite;
    }

    public List<String> getDenyCreate() {
        return denyCreate;
    }

    public List<String> getDenyPhysicalDelete() {
        return denyPhysicalDelete;
    }

    public List<String> getDenyLogicalDelete() {
        return denyLogicalDelete;
    }

    public void setSchematron(String schematron) {
        this.schematron = schematron;
    }

    public String getSchematron() {
        return schematron;
    }

    public void setPrimaryKeyInfo(List<FieldMetadata> primaryKeyInfo) {
        this.primaryKeyInfo = primaryKeyInfo;
    }

    public List<FieldMetadata> getPrimaryKeyInfo() {
        return primaryKeyInfo;
    }

    public void setLookupFields(List<FieldMetadata> lookupFields) {
        this.lookupFields = lookupFields;
    }

    public List<FieldMetadata> getLookupFields() {
        return lookupFields;
    }

    public List<String> getWorkflowAccessRights() {
        return this.workflowAccessRights;
    }

    public void registerTypeName(Locale locale, String label) {
        localeToLabel.put(locale, label);
    }

    public Map<Locale, String> getLocaleToLabel() {
        return localeToLabel;
    }

    public void registerTypeDescription(Locale locale, String description) {
        localeToDescription.put(locale, description);
    }

    public Map<Locale, String> getLocaleToDescription() {
        return localeToDescription;
    }

    public void setForeignKeyFilter(String foreignKeyFilter) {
        this.foreignKeyFilter = foreignKeyFilter;
    }

    public String getForeignKeyFilter() {
        return foreignKeyFilter;
    }

    public void setVisibilityRule(String visibilityRule) {
        this.visibilityRule = visibilityRule;
    }

    public String getVisibilityRule() {
        return visibilityRule;
    }

    public String getDefaultValueRule() {
        return defaultValueRule;
    }

    public void setDefaultValueRule(String defaultValueRule) {
        this.defaultValueRule = defaultValueRule;
    }
}
