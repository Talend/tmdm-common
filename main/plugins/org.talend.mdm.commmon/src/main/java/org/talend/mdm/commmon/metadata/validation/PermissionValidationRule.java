// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.commmon.metadata.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.util.XSDParser;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.SoftFieldRef;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.PermissionConstants;
import org.w3c.dom.Element;

/**
 * @author sbliu
 *
 */
public class PermissionValidationRule implements ValidationRule {

    private static final String ELEMENT_TYPE_ENTITY = "entity"; //$NON-NLS-1$
    private static final String ELEMENT_TYPE_FIELD = "field"; //$NON-NLS-1$

    private FieldMetadata field;
    private ComplexTypeMetadata complexTypeMetadata;

    public PermissionValidationRule(FieldMetadata field) {
        this.field = field;
    }

    public PermissionValidationRule(ComplexTypeMetadata complexTypeMetadata) {
        this.complexTypeMetadata = complexTypeMetadata;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        boolean validationResult = true;

        if (complexTypeMetadata != null) {
            validationResult = validateComplexTypePermission(complexTypeMetadata, handler);
        } else if (field != null) {
            validationResult = validateFieldRefPermission(field, handler);
        }

        return validationResult;
    }

    private boolean validateFieldRefPermission(FieldMetadata fieldMetadata, ValidationHandler handler) {
        fieldMetadata.setData(MetadataRepository.VALIDATION_PERMISSION_MARKER, true);
        String name = fieldMetadata.getName();
        XSDElementDeclaration element = fieldMetadata.getData(MetadataRepository.XSD_ELEMENT);

        if (element == null) {
            return true;
        }

        boolean valid = true;

        XSDAnnotation annotation = element.getAnnotation();
        if (annotation != null) {
            EList<Element> appInfoElements = annotation.getApplicationInformation();

            List<FieldMetadata> writeUsers = new ArrayList<FieldMetadata>();
            List<FieldMetadata> hideUsers = new ArrayList<FieldMetadata>();
            List<FieldMetadata> denyCreate = new ArrayList<FieldMetadata>();          
            for (Element appInfo : appInfoElements) {
                String source = appInfo.getAttribute("source"); //$NON-NLS-1$
                String permissionRole = appInfo.getTextContent();
                if ("X_Write".equals(source)) { //$NON-NLS-1$
                    writeUsers.add(getFieldMetadata(appInfo, permissionRole));
                } else if ("X_Hide".equals(source)) { //$NON-NLS-1$
                    hideUsers.add(getFieldMetadata(appInfo, permissionRole));
                } else if ("X_Deny_Create".equals(source)) { //$NON-NLS-1$ )
                    denyCreate.add(getFieldMetadata(appInfo, permissionRole));
                } 
            }

            valid = doValidation(handler, ELEMENT_TYPE_FIELD, name, PermissionConstants.PERMISSIONTYPE_WRITE, writeUsers);
            valid &= doValidation(handler, ELEMENT_TYPE_FIELD, name, PermissionConstants.PERMISSIONTYPE_HIDE, hideUsers);
            valid &= doValidation(handler, ELEMENT_TYPE_FIELD, name, PermissionConstants.PERMISSIONTYPE_DENY_CREATE, denyCreate); 
        }


        if(fieldMetadata instanceof ContainedTypeFieldMetadata) {
            ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) fieldMetadata;
            ComplexTypeMetadata cTypeMetadata = containedField.getContainedType();
            Collection<FieldMetadata>  fieldMetadatas = cTypeMetadata.getFields();
            for(FieldMetadata fMetadata: fieldMetadatas) {
                boolean validateMarked = BooleanUtils
                        .isTrue(fMetadata.<Boolean> getData(MetadataRepository.VALIDATION_PERMISSION_MARKER));
                if(!validateMarked) {
                    valid &= validateFieldRefPermission(fMetadata, handler);
                }
            }
        }

        return valid;
    }

    private boolean validateComplexTypePermission(ComplexTypeMetadata cTypeMetadata, ValidationHandler handler) {// entity
        String name = cTypeMetadata.getName();
        XSDElementDeclaration element = cTypeMetadata.getData(MetadataRepository.XSD_ELEMENT);
        if (element == null || element.getAnnotation() == null) {
            return true;
        }

        XSDAnnotation annotation = element.getAnnotation();
        EList<Element> appInfoElements = annotation.getApplicationInformation();

        List<FieldMetadata> writeUsers = new ArrayList<FieldMetadata>();
        List<FieldMetadata> hideUsers = new ArrayList<FieldMetadata>();
        List<FieldMetadata> denyCreate = new ArrayList<FieldMetadata>();
        List<FieldMetadata> denyDeleteLogical = new ArrayList<FieldMetadata>();
        List<FieldMetadata> denyDeletePhysical = new ArrayList<FieldMetadata>();
        for (Element appInfo : appInfoElements) {
            String source = appInfo.getAttribute("source"); //$NON-NLS-1$
            String permissionRole = appInfo.getTextContent();
            if ("X_Write".equals(source)) { //$NON-NLS-1$
                writeUsers.add(getFieldMetadata(appInfo, permissionRole));
            } else if ("X_Hide".equals(source)) { //$NON-NLS-1$
                hideUsers.add(getFieldMetadata(appInfo, permissionRole));
            } else if ("X_Deny_Create".equals(source)) { //$NON-NLS-1$
                denyCreate.add(getFieldMetadata(appInfo, permissionRole));
            } else if ("X_Deny_LogicalDelete".equals(source)) { //$NON-NLS-1$
                denyDeleteLogical.add(getFieldMetadata(appInfo, permissionRole));
            } else if ("X_Deny_PhysicalDelete".equals(source)) { //$NON-NLS-1$
                denyDeletePhysical.add(getFieldMetadata(appInfo, permissionRole));
            } 
        }

        boolean valid = doValidation(handler, ELEMENT_TYPE_ENTITY, name, PermissionConstants.PERMISSIONTYPE_WRITE, writeUsers);
        valid &= doValidation(handler, ELEMENT_TYPE_ENTITY, name, PermissionConstants.PERMISSIONTYPE_HIDE, hideUsers);
        valid &= doValidation(handler, ELEMENT_TYPE_ENTITY, name, PermissionConstants.PERMISSIONTYPE_DENY_CREATE, denyCreate);
        valid &= doValidation(handler, ELEMENT_TYPE_ENTITY, name, PermissionConstants.PERMISSIONTYPE_DENY_DELETE_PHYSICAL, denyDeletePhysical);
        valid &= doValidation(handler, ELEMENT_TYPE_ENTITY, name, PermissionConstants.PERMISSIONTYPE_DENY_DELETE_LOGICAL, denyDeleteLogical);

        return valid;
    }

    private boolean doValidation(ValidationHandler handler, String elementType, String elementName, String permissionType, List<FieldMetadata> roles) {
        boolean valid = true;
        for (FieldMetadata roleMetadata : roles) {
            String lowerCaseRoleName = roleMetadata.getName().toLowerCase();
            if (lowerCaseRoleName.startsWith(ICoreConstants.SYSTEM_ROLE_PREFIX.toLowerCase()) || lowerCaseRoleName.equals(ICoreConstants.ADMIN_PERMISSION)) {
                String message = "System role \"" + roleMetadata.getName() + "\" shouldn't be used to set \"" + permissionType //$NON-NLS-1$ //$NON-NLS-2$
                        + "\" permission on " + elementType + " \"" + elementName + "\" ."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                Element data = roleMetadata.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT);
                Integer lineNum = roleMetadata.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER);
                Integer colNum = roleMetadata.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER);

                if(complexTypeMetadata != null) {
                    handler.error(complexTypeMetadata, message, data,lineNum, colNum, ValidationError.PERMISSION_SYSTEM_ROLE_NOT_SETTABLE);
                } else {
                    handler.error(field, message, data,lineNum, colNum, ValidationError.PERMISSION_SYSTEM_ROLE_NOT_SETTABLE);
                }
                valid &= false;
            }
        }

        return valid;
    }

    private FieldMetadata getFieldMetadata(Element appInfo, String fieldName) {

        FieldMetadata fieldMetadata = new SoftFieldRef(null, fieldName, "");
        fieldMetadata.setData(MetadataRepository.XSD_LINE_NUMBER, XSDParser.getStartLine(appInfo));
        fieldMetadata.setData(MetadataRepository.XSD_COLUMN_NUMBER, XSDParser.getStartColumn(appInfo));
        fieldMetadata.setData(MetadataRepository.XSD_DOM_ELEMENT, appInfo);
        return fieldMetadata;
    }
}
