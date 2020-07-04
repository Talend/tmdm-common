/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;

/**
 * Default implementation for a MDM entity type (i.e. "complex" type).
 */
public class ComplexTypeMetadataImpl extends MetadataExtensions implements ComplexTypeMetadata {

    private static final Logger LOGGER = LogManager.getLogger(ComplexTypeMetadataImpl.class);

    private final String nameSpace;

    private final List<String> allowWrite;

    private final Map<String, FieldMetadata> fieldMetadata = new LinkedHashMap<String, FieldMetadata>();

    private final Map<String, FieldMetadata> keyFields = new LinkedHashMap<String, FieldMetadata>();

    private final Collection<TypeMetadata> superTypes = new HashSet<TypeMetadata>();

    private final List<String> denyCreate;

    private final List<String> hideUsers;

    private final List<String> logicalDelete;

    private final String schematron;

    private final List<String> physicalDelete;

    private final Collection<ComplexTypeMetadata> subTypes = new HashSet<ComplexTypeMetadata>();

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    private List<FieldMetadata> lookupFields;

    private boolean isInstantiable;

    private boolean isAbstract;

    private List<FieldMetadata> primaryKeyInfo;

    private String name;

    private boolean isFrozen;

    private final List<String> workflowAccessRights;

    private FieldMetadata containingField;

    private final Set<ComplexTypeMetadata> usages = new HashSet<ComplexTypeMetadata>();

    private final List<Category> categories;

    public ComplexTypeMetadataImpl(String nameSpace, String name, boolean isInstantiable) {
        this(nameSpace, name, isInstantiable, false);
    }

    public ComplexTypeMetadataImpl(String nameSpace, String name, boolean isInstantiable, boolean isAbstract) {
        this(nameSpace, name, Collections.<String> emptyList(), Collections.<String> emptyList(),
                Collections.<String> emptyList(), Collections.<String> emptyList(), Collections.<String> emptyList(),
                StringUtils.EMPTY, Collections.<FieldMetadata> emptyList(), Collections.<FieldMetadata> emptyList(),
                isInstantiable, isAbstract, Collections.<String> emptyList());
    }

    public ComplexTypeMetadataImpl(String nameSpace, String name, List<String> allowWrite, List<String> denyCreate,
            List<String> hideUsers, List<String> physicalDelete, List<String> logicalDelete, String schematron,
            List<FieldMetadata> primaryKeyInfo, List<FieldMetadata> lookupFields, boolean isInstantiable,
            List<String> workflowAccessRights) {
        this(nameSpace, name, allowWrite, denyCreate, hideUsers, physicalDelete, logicalDelete, schematron, primaryKeyInfo,
                lookupFields, isInstantiable, false, workflowAccessRights);
    }

    public ComplexTypeMetadataImpl(String nameSpace, String name, List<String> allowWrite, List<String> denyCreate,
            List<String> hideUsers, List<String> physicalDelete, List<String> logicalDelete, String schematron,
            List<FieldMetadata> primaryKeyInfo, List<FieldMetadata> lookupFields, boolean isInstantiable, boolean isAbstract,
            List<String> workflowAccessRights) {
        this(nameSpace, name, allowWrite, denyCreate, hideUsers, physicalDelete, logicalDelete, schematron, primaryKeyInfo,
                lookupFields, isInstantiable, false, workflowAccessRights, Collections.<Category> emptyList());
    }

    public ComplexTypeMetadataImpl(String nameSpace, String name, List<String> allowWrite, List<String> denyCreate,
            List<String> hideUsers, List<String> physicalDelete, List<String> logicalDelete, String schematron,
            List<FieldMetadata> primaryKeyInfo, List<FieldMetadata> lookupFields, boolean isInstantiable, boolean isAbstract,
            List<String> workflowAccessRights, List<Category> categories) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.allowWrite = allowWrite;
        this.denyCreate = denyCreate;
        this.hideUsers = hideUsers;
        this.physicalDelete = physicalDelete;
        this.logicalDelete = logicalDelete;
        this.schematron = schematron;
        this.primaryKeyInfo = primaryKeyInfo;
        this.lookupFields = lookupFields;
        this.isInstantiable = isInstantiable;
        this.isAbstract = isAbstract;
        this.workflowAccessRights = workflowAccessRights;
        this.categories = categories;
    }

    @Override
    public void addSuperType(TypeMetadata superType) {
        if (isFrozen) {
            throw new IllegalStateException("Type '" + name + "' is frozen and can not be modified.");
        }
        superTypes.add(superType);
    }

    @Override
    public Collection<TypeMetadata> getSuperTypes() {
        return Collections.unmodifiableCollection(superTypes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (isFrozen) {
            throw new IllegalStateException("Cannot change name after type was frozen.");
        }
        this.name = name;
    }

    @Override
    public String getNamespace() {
        return nameSpace;
    }

    @Override
    public FieldMetadata getField(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Field name can not be null nor empty.");
        }
        FieldMetadata foundField;
        if (path.indexOf('/') < 0) {
            foundField = fieldMetadata.get(path); // Shortcut for direct look up for a field (no path involved).
            if (foundField == null) {
                for (TypeMetadata superType : superTypes) {
                    foundField = ((ComplexTypeMetadata) superType).getField(path);
                    if (foundField != null) {
                        break;
                    }
                }
            }
        } else {
            foundField = _getField(this, path);
        }
        if (foundField == null) {
            throw new IllegalArgumentException("Type '" + getName() + "' does not own field '" + path + "'.");
        }
        return foundField;
    }

    private static FieldMetadata _getField(ComplexTypeMetadata type, String path) {
        String fieldName = StringUtils.substringBefore(StringUtils.substringBefore(path, "/"), "["); //$NON-NLS-1$
        String remainingPath = StringUtils.substringAfter(path, "/"); //$NON-NLS-1$
        if (type.hasField(fieldName)) {
            FieldMetadata field = type.getField(fieldName);
            if (!remainingPath.isEmpty()) {
                TypeMetadata fieldType = field.getType();
                if (fieldType instanceof ComplexTypeMetadata) {
                    return _getField((ComplexTypeMetadata) fieldType, remainingPath);
                } else {
                    return null; // Simple type field shouldn't have remaining path, this is dead end.
                }
            } else {
                return field;
            }
        } else {
            // Handle xsi:type in XPath query
            if (fieldName.contains("xsi:type")) { //$NON-NLS-1$
                String reusableTypeName = StringUtils.substringAfter(fieldName, "@xsi:type").replace('=', ' ').replace(']', ' ') //$NON-NLS-1$
                        .trim();
                if (reusableTypeName.isEmpty()) {
                    throw new IllegalArgumentException("Reusable type could not be null for fieldName '" + fieldName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (!type.getName().equals(reusableTypeName)) { // Look real type in sub types
                    boolean foundRealType = false;
                    for (TypeMetadata subType : type.getSubTypes()) {
                        if (subType instanceof ComplexTypeMetadata && subType.getName().equals(reusableTypeName)) {
                            type = (ComplexTypeMetadata) subType;
                            foundRealType = true;
                            break;
                        }
                    }
                    // xsi:type not found, assume type is default field type.
                    if (!foundRealType) {
                        LOGGER.error("Type '" + reusableTypeName + "' does not exist. Assuming '" + type.getName()
                                + "' has field type.");
                    }
                }
                return _getField(type, path);
            } else { // Or implicit assumption on a field accessible from sub types.
                for (ComplexTypeMetadata subType : type.getSubTypes()) {
                    FieldMetadata subTypeField = _getField(subType, path);
                    if (subTypeField != null) {
                        return subTypeField;
                    }
                }
            }
            return null; // Not found
        }
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isInstantiable() {
        return isInstantiable;
    }

    @Override
    public boolean isFrozen() {
        return isFrozen;
    }

    @Override
    public void validate(ValidationHandler handler) {
        ValidationFactory.getRule(this).perform(handler);
        // Validate all fields.
        for (FieldMetadata value : fieldMetadata.values()) {
            ValidationFactory.getRule(value).perform(handler);
        }
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    public void setAbstract(boolean isAbstract) {
        if (isFrozen) {
            throw new IllegalStateException("Type '" + name + "' is frozen and can not be modified.");
        }
        this.isAbstract = isAbstract;
    }

    @Override
    public void setInstantiable(boolean isInstantiable) {
        if (isFrozen) {
            throw new IllegalStateException("Type '" + name + "' is frozen and can not be modified.");
        }
        this.isInstantiable = isInstantiable;
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
    public ComplexTypeMetadata getEntity() {
        if (getContainer() == null) {
            return this;
        } else {
            return getContainer().getContainingType().getEntity();
        }
    }

    @Override
    public FieldMetadata getContainer() {
        return containingField;
    }

    @Override
    public void setContainer(FieldMetadata field) {
        this.containingField = field;
        for (ComplexTypeMetadata subType : subTypes) {
            subType.setContainer(field);
        }
    }

    @Override
    public Collection<FieldMetadata> getKeyFields() {
        return Collections.unmodifiableCollection(keyFields.values());
    }

    @Override
    public Collection<FieldMetadata> getFields() {
        return Collections.unmodifiableCollection(fieldMetadata.values());
    }

    @Override
    public boolean isAssignableFrom(TypeMetadata type) {
        // Check one level of inheritance
        Collection<TypeMetadata> superTypes = getSuperTypes();
        for (TypeMetadata superType : superTypes) {
            if (type.getName().equals(superType.getName())) {
                return true;
            }
        }
        // Checks in type inheritance hierarchy.
        for (TypeMetadata superType : superTypes) {
            if (superType.isAssignableFrom(type)) {
                return true;
            }
        }
        return getName().equals(type.getName());
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        if (nameSpace != null && nameSpace.isEmpty()) {
            return '[' + name + ']';
        }
        return '[' + nameSpace + ':' + name + ']';
    }

    @Override
    public void addField(FieldMetadata fieldMetadata) {
        if (isFrozen) {
            throw new IllegalStateException("Type '" + name + "' is frozen and can not be modified.");
        }
        if (fieldMetadata == null) {
            throw new IllegalArgumentException("Field can not be null.");
        }
        this.fieldMetadata.put(fieldMetadata.getName(), fieldMetadata);
        if (fieldMetadata.isKey()) {
            registerKey(fieldMetadata);
        }
    }

    @Override
    public void registerKey(FieldMetadata keyField) {
        if (keyField == null) {
            throw new IllegalArgumentException("Key field can not be null.");
        }
        keyFields.put(keyField.getName(), keyField);
    }

    @Override
    public ComplexTypeMetadata copy() {
        ComplexTypeMetadataImpl copy = new ComplexTypeMetadataImpl(getNamespace(), getName(), allowWrite, denyCreate, hideUsers,
                physicalDelete, logicalDelete, schematron, primaryKeyInfo, Collections.<FieldMetadata> emptyList(),
                isInstantiable, isAbstract, workflowAccessRights, categories);
        Collection<FieldMetadata> fields = getFields();
        for (FieldMetadata field : fields) {
            FieldMetadata fieldCopy = field.copy();
            fieldCopy.setContainingType(copy);
            copy.addField(fieldCopy);
        }
        for (TypeMetadata superType : superTypes) {
            copy.addSuperType(superType);
            if (superType instanceof ComplexTypeMetadata) {
                ((ComplexTypeMetadata) superType).registerSubType(copy);
            }
        }
        for (ComplexTypeMetadata subType : subTypes) {
            copy.subTypes.add((ComplexTypeMetadata) subType.copy());
        }
        // Copy key fields
        copy.keyFields.clear(); // Need to clear due to use of addField(...) during field copy.
        Collection<FieldMetadata> typeKeyFields = getKeyFields();
        for (FieldMetadata typeKeyField : typeKeyFields) {
            FieldMetadata fieldCopy = typeKeyField.copy();
            fieldCopy.setContainingType(copy);
            copy.registerKey(fieldCopy);
        }
        copy.isFrozen = false;
        copy.localeToLabel.putAll(localeToLabel);
        copy.localeToDescription.putAll(localeToDescription);
        if (dataMap != null) {
            copy.dataMap = new HashMap<String, Object>(dataMap);
        }
        copy.usages.addAll(usages);
        return copy;
    }

    @Override
    public TypeMetadata copyShallow() {
        ComplexTypeMetadataImpl copy = new ComplexTypeMetadataImpl(getNamespace(), getName(), allowWrite, denyCreate, hideUsers,
                physicalDelete, logicalDelete, schematron, primaryKeyInfo, Collections.<FieldMetadata> emptyList(),
                isInstantiable, isAbstract, workflowAccessRights, categories);
        copy.localeToLabel.putAll(localeToLabel);
        return copy;
    }

    @Override
    public List<String> getWriteUsers() {
        return Collections.unmodifiableList(allowWrite);
    }

    @Override
    public List<String> getDenyCreate() {
        return Collections.unmodifiableList(denyCreate);
    }

    @Override
    public List<String> getHideUsers() {
        return Collections.unmodifiableList(hideUsers);
    }

    @Override
    public List<String> getWorkflowAccessRights() {
        return Collections.unmodifiableList(workflowAccessRights);
    }

    @Override
    public List<String> getDenyDelete(DeleteType type) {
        switch (type) {
        case LOGICAL:
            return Collections.unmodifiableList(logicalDelete);
        case PHYSICAL:
            return Collections.unmodifiableList(physicalDelete);
        default:
            throw new NotImplementedException("Security information parsing for delete type '" + type + "'");
        }
    }

    @Override
    public String getSchematron() {
        return schematron;
    }

    @Override
    public List<FieldMetadata> getPrimaryKeyInfo() {
        return Collections.unmodifiableList(primaryKeyInfo);
    }

    @Override
    public List<FieldMetadata> getLookupFields() {
        return Collections.unmodifiableList(lookupFields);
    }

    @Override
    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @Override
    public void declareUsage(ComplexTypeMetadata usage) {
        usages.add(usage);
    }

    @Override
    public Collection<ComplexTypeMetadata> getUsages() {
        return usages;
    }

    @Override
    public void setSubTypes(List<ComplexTypeMetadata> subTypes) {
        this.subTypes.clear();
        this.subTypes.addAll(subTypes);
    }

    @Override
    public void registerName(Locale locale, String label) {
        localeToLabel.put(locale, label);
    }

    @Override
    public boolean hasField(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        if (fieldName.indexOf('/') < 0) {
            boolean contains = fieldMetadata.containsKey(fieldName);
            if (!contains) {
                for (TypeMetadata typeMetadata : getSuperTypes()) {
                    contains |= ((ComplexTypeMetadata) typeMetadata).hasField(fieldName);
                }
            }
            return contains;
        }
        StringTokenizer tokenizer = new StringTokenizer(fieldName, "/"); //$NON-NLS-1$
        ComplexTypeMetadata currentType = this;
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            if (currentType.getSubTypes() != null && currentType.getSubTypes().size() > 0) {
                for (TypeMetadata typeMetadata : currentType.getSubTypes()) {
                    if (((ComplexTypeMetadata) typeMetadata).hasField(current)) {
                        return true;
                    }
                }
            }
            if (!currentType.hasField(current)) {
                return false;
            }
            if (tokenizer.hasMoreTokens()) {
                TypeMetadata type = currentType.getField(current).getType();
                if (!(type instanceof ComplexTypeMetadata)) {
                    return false;
                }
                currentType = (ComplexTypeMetadata) type;
            }
        }
        return true;
    }

    @Override
    public Collection<ComplexTypeMetadata> getSubTypes() {
        List<ComplexTypeMetadata> subTypes = new LinkedList<ComplexTypeMetadata>();
        for (ComplexTypeMetadata subType : this.subTypes) {
            subTypes.add(subType);
            subTypes.addAll(subType.getSubTypes());
        }
        return subTypes;
    }

    @Override
    public Collection<ComplexTypeMetadata> getDirectSubTypes() {
        List<ComplexTypeMetadata> subTypes = new LinkedList<ComplexTypeMetadata>();
        for (ComplexTypeMetadata subType : this.subTypes) {
            subTypes.add(subType);
        }
        return subTypes;
    }

    @Override
    public void registerSubType(ComplexTypeMetadata type) {
        subTypes.add(type);
    }

    @Override
    public TypeMetadata freeze() {
        if (isFrozen) {
            return this;
        }
        // Gets fields from super types.
        if (!superTypes.isEmpty()) {
            Collection<FieldMetadata> thisTypeFields = new LinkedList<FieldMetadata>(fieldMetadata.values());
            fieldMetadata.clear();
            List<TypeMetadata> thisSuperTypes = new LinkedList<TypeMetadata>(superTypes);
            superTypes.clear();
            // Only accept super types with same instantiable status (if more than one).
            // TODO Type should use declareUsage iso. super type for this!
            if (thisSuperTypes.size() > 1) {
                Iterator<TypeMetadata> iterator = thisSuperTypes.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().isInstantiable() != isInstantiable) {
                        iterator.remove();
                    }
                }
            }
            for (TypeMetadata superType : thisSuperTypes) {
                if (isInstantiable() == superType.isInstantiable()) {
                    superType = superType.freeze();
                    if (superType instanceof ComplexTypeMetadata) {
                        ((ComplexTypeMetadata) superType).registerSubType(this);
                        usages.addAll(((ComplexTypeMetadata) superType).getUsages());
                    }
                    superTypes.add(superType);
                } else {
                    superType = superType.freeze();
                }
                if (superType instanceof ComplexTypeMetadata) {
                    ((ComplexTypeMetadata) superType).registerSubType(this);
                    Collection<FieldMetadata> superTypeFields = ((ComplexTypeMetadata) superType).getFields();
                    for (FieldMetadata superTypeField : superTypeFields) {
                        superTypeField.adopt(this);
                    }
                }
            }
            for (FieldMetadata thisTypeField : thisTypeFields) {
                fieldMetadata.put(thisTypeField.getName(), thisTypeField);
            }
        }
        isFrozen = true;
        // Freeze all fields.
        Collection<FieldMetadata> values = new LinkedList<FieldMetadata>(fieldMetadata.values());
        for (FieldMetadata value : values) {
            FieldMetadata frozenFieldDeclaration = value.freeze();
            fieldMetadata.put(value.getName(), frozenFieldDeclaration);
            if (keyFields.containsKey(value.getName()) && !frozenFieldDeclaration.isKey()) {
                frozenFieldDeclaration.promoteToKey();
                FieldMetadata keyField = keyFields.get(value.getName());
                if (!keyField.equals(frozenFieldDeclaration)) {
                    registerKey(frozenFieldDeclaration);
                }
            }
        }
        for (Map.Entry<String, FieldMetadata> keyField : keyFields.entrySet()) {
            keyField.setValue(keyField.getValue().freeze());
        }
        // Freeze primary info
        List<FieldMetadata> frozenPrimaryKeyInfo = new LinkedList<FieldMetadata>();
        for (FieldMetadata pkInfo : primaryKeyInfo) {
            FieldMetadata freeze = pkInfo.freeze();
            if (freeze != null) {
                frozenPrimaryKeyInfo.add(freeze);
            }
        }
        primaryKeyInfo = frozenPrimaryKeyInfo;
        // Freeze lookup fields
        List<FieldMetadata> frozenLookupFields = new LinkedList<FieldMetadata>();
        for (FieldMetadata lookupField : lookupFields) {
            FieldMetadata freeze = lookupField.freeze();
            if (freeze != null) {
                frozenLookupFields.add(freeze);
            }
        }
        lookupFields = frozenLookupFields;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComplexTypeMetadata)) {
            return false;
        }
        ComplexTypeMetadata that = (ComplexTypeMetadata) o;
        if (!name.equals(that.getName())) {
            return false;
        }
        if (nameSpace != null ? !nameSpace.equals(that.getNamespace()) : that.getNamespace() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = nameSpace != null ? nameSpace.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
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
}
