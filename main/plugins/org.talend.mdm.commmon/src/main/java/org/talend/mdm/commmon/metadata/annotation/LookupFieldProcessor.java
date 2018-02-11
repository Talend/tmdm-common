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

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.util.XSDParser;
import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LookupFieldProcessor implements XmlSchemaAnnotationProcessor {

    @Override
    public void process(MetadataRepository repository, ComplexTypeMetadata type, XSDAnnotation annotation, XmlSchemaAnnotationProcessorState state) {
        if (annotation != null) {
            EList<Element> annotations = annotation.getApplicationInformation();
            List<FieldMetadata> lookupFields = new LinkedList<FieldMetadata>();
            for (Element appInfo : annotations) {
                if ("X_Lookup_Field".equals(appInfo.getAttribute("source"))) { //$NON-NLS-1$
                    lookupFields.add(handleLookupField(repository, state, appInfo));
                }
            }
            state.setLookupFields(lookupFields);
        }
    }

    private FieldMetadata handleLookupField(MetadataRepository repository, XmlSchemaAnnotationProcessorState state,
            Element appInfo) {
        state.markAsReference();
        String path = appInfo.getTextContent();
        FieldMetadata fieldMetadata = getFieldMetadata(repository, state, appInfo, path);
        if (fieldMetadata == null) {
            throw new IllegalArgumentException("Path '" + path + "' is not supported.");
        }
        return fieldMetadata;
    }

    // TODO Common code with ForeignKeyProcessor!
    private static FieldMetadata getFieldMetadata(MetadataRepository repository, XmlSchemaAnnotationProcessorState state,
            Element appInfo, String path) {
        String typeName = StringUtils.substringBefore(path, "/").trim(); //$NON-NLS-1$
        String fieldPath = StringUtils.substringAfter(path, "/").trim(); //$NON-NLS-1$
        String userNamespace = repository.getUserNamespace();
        SoftTypeRef referencedType = new SoftTypeRef(repository, userNamespace, typeName, true);
        state.setFieldType(referencedType); // TODO Wrong!!!!
        state.setReferencedType(referencedType); // Only reference instantiable types.
        FieldMetadata fieldMetadata = new SoftFieldRef(repository, fieldPath, typeName);
        fieldMetadata.setData(MetadataRepository.XSD_LINE_NUMBER, XSDParser.getStartLine(appInfo));
        fieldMetadata.setData(MetadataRepository.XSD_COLUMN_NUMBER, XSDParser.getStartColumn(appInfo));
        fieldMetadata.setData(MetadataRepository.XSD_DOM_ELEMENT, appInfo);
        return fieldMetadata;
    }
}
