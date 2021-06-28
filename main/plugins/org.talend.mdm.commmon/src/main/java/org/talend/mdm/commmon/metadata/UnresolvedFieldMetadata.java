/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;

public class UnresolvedFieldMetadata implements FieldMetadata {

    private final String fieldName;

    private final TypeMetadata declaringType;

    private final Map<String, Object> additionalData = new HashMap<String, Object>();

    private boolean isKey;

    private ComplexTypeMetadata containingType;

    public UnresolvedFieldMetadata(String fieldName, boolean key, ComplexTypeMetadata containingType) {
        this.fieldName = fieldName;
        this.isKey = key;
        this.containingType = containingType;
        this.declaringType = containingType;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public boolean isKey() {
        return isKey;
    }

    @Override
    public TypeMetadata getType() {
        return new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.ANY_TYPE);
    }

    @Override
    public ComplexTypeMetadata getContainingType() {
        return containingType;
    }

    @Override
    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        this.containingType = typeMetadata;
    }

    @Override
    public TypeMetadata getDeclaringType() {
        return declaringType;
    }

    @Override
    public List<String> getHideUsers() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getWriteUsers() {
        return null;
    }

    @Override
    public boolean isMany() {
        return false;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public void adopt(ComplexTypeMetadata metadata) {
    }

    @Override
    public FieldMetadata copy() {
        return this;
    }

    @Override
    public FieldMetadata freeze() {
        return this;
    }

    @Override
    public void promoteToKey() {
        isKey = true;
    }

    @Override
    public void validate(ValidationHandler handler) {
        ValidationFactory.getRule(this).perform(handler);
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public String getPath() {
        FieldMetadata containingField = containingType.getContainer();
        if (containingField != null) {
            return containingField.getPath() + '/' + fieldName;
        } else {
            return fieldName;
        }
    }

    @Override
    public String getEntityTypeName() {
        return containingType.getEntity().getName();
    }

    @Override
    public void registerName(Locale locale, String name) {
    }

    @Override
    public String getName(Locale locale) {
        return getName();
    }

    @Override
    public String getVisibilityRule() {
        return StringUtils.EMPTY;
    }

    @Override
    public void setData(String key, Object data) {
        additionalData.put(key, data);
    }

    @Override
    public <X> X getData(String key) {
        return (X) additionalData.get(key);
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void registerDescription(Locale locale, String description) {
    }

    @Override
    public String getDescription(Locale locale) {
        return StringUtils.EMPTY;
    }

    @Override
    public List<String> getNoAddRoles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getNoRemoveRoles() {
        throw new UnsupportedOperationException();
    }
}
