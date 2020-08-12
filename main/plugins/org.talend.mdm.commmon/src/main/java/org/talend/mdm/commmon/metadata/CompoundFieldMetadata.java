/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;

public class CompoundFieldMetadata extends MetadataExtensions implements FieldMetadata {

    private final FieldMetadata[] fields;

    private boolean isFrozen;

    private int cachedHashCode;

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    public CompoundFieldMetadata(FieldMetadata... fields) {
        this.fields = fields;
    }

    public FieldMetadata[] getFields() {
        return fields;
    }

    public String getName() {
        String name = StringUtils.EMPTY;
        for (FieldMetadata field : fields) {
            name += "[" + field.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;
    }

    public boolean isKey() {
        boolean isKey = true;
        for (FieldMetadata field : fields) {
            isKey &= field.isKey();
        }
        return isKey;
    }

    public TypeMetadata getType() {
        /*
         * Compound / Composite keys are always represented as strings the [id0][id1] format.
         * So this method can return "string" as type even if fields are not all string.
         */
        return new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING); //$NON-NLS-1$
    }

    public ComplexTypeMetadata getContainingType() {
        return fields[0].getContainingType();
    }

    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        throw new UnsupportedOperationException();
    }

    public FieldMetadata freeze() {
        if (isFrozen) {
            return this;
        }
        isFrozen = true;
        int i = 0;
        for (FieldMetadata field : fields) {
            fields[i++] = field.freeze();
        }
        return this;
    }

    public void promoteToKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(ValidationHandler handler) {
        for (FieldMetadata field : fields) {
            field.validate(handler);
        }
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public String getPath() {
        FieldMetadata containingField = getContainingType().getContainer();
        if (containingField != null) {
            return containingField.getPath() + '/' + getName();
        } else {
            return getName();
        }
    }

    @Override
    public String getEntityTypeName() {
        return getContainingType().getEntity().getName();
    }

    @Override
    public void registerName(Locale locale, String name) {
        localeToLabel.put(locale, name);
    }

    @Override
    public String getName(Locale locale) {
        String localizedName = localeToLabel.get(locale);
        if (localizedName == null) {
            return getName();
        }
        return localizedName;
    }

    @Override
    public String getVisibilityRule() {
        throw new UnsupportedOperationException();
    }

    public TypeMetadata getDeclaringType() {
        return fields[0].getDeclaringType();
    }

    public void adopt(ComplexTypeMetadata metadata) {
        throw new UnsupportedOperationException();
    }

    public FieldMetadata copy() {
        FieldMetadata[] fieldsCopy = new FieldMetadata[fields.length];
        int i = 0;
        for (FieldMetadata field : fields) {
            fieldsCopy[i++] = field.copy();
        }
        return new CompoundFieldMetadata(fieldsCopy);
    }

    @Override
    public String toString() {
        return "Compound {" + "# of fields=" + fields.length + '}'; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public List<String> getHideUsers() {
        throw new UnsupportedOperationException();
    }

    public List<String> getWriteUsers() {
        throw new UnsupportedOperationException();
    }

    public boolean isMany() {
        return false;
    }

    public boolean isMandatory() {
        throw new UnsupportedOperationException();
    }

    public <T> T accept(MetadataVisitor<T> visitor) {
        T returnedValue = null;
        for (FieldMetadata field : fields) {
            returnedValue = field.accept(visitor);
        }
        return returnedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompoundFieldMetadata)) {
            return false;
        }

        CompoundFieldMetadata that = (CompoundFieldMetadata) o;

        return Arrays.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        if (isFrozen && cachedHashCode != 0) {
            return cachedHashCode;
        }
        int result = fields != null ? Arrays.hashCode(fields) : 0;
        cachedHashCode = result;
        return result;
    }

    public List<String> getWorkflowAccessRights() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDescription(Locale locale, String description) {
        localeToDescription.put(locale, description);
    }

    @Override
    public String getDescription(Locale locale) {
        String localizedDescription = localeToDescription.get(locale);
        if (localizedDescription == null) {
            return StringUtils.EMPTY;
        }
        return localizedDescription;
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
