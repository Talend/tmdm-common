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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PrimaryKeyInfoProcessor implements XmlSchemaAnnotationProcessor {

    @Override
    public void process(MetadataRepository repository, ComplexTypeMetadata type, XSDAnnotation annotation, XmlSchemaAnnotationProcessorState state) {
        if (annotation != null) {
            EList<Element> appInfoElements = annotation.getApplicationInformation();
            List<FieldMetadata> primaryKeyInfo = new LinkedList<FieldMetadata>();
            for (Element appInfo : appInfoElements) {
                if ("X_PrimaryKeyInfo".equals(appInfo.getAttribute("source"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    primaryKeyInfo.add(handlePrimaryKeyInfo(repository, appInfo));
                }
            }
            if (!primaryKeyInfo.isEmpty()) {
                state.setPrimaryKeyInfo(primaryKeyInfo);
            } else {
                state.setPrimaryKeyInfo(Collections.<FieldMetadata>emptyList());
            }
        }
    }

    private FieldMetadata handlePrimaryKeyInfo(MetadataRepository repository, Element appInfo) {
        String path = appInfo.getTextContent();
        String typeName = StringUtils.substringBefore(path, "/").trim(); //$NON-NLS-1$
        String fieldName = StringUtils.substringAfter(path, "/").trim(); //$NON-NLS-1$
        SoftFieldRef field = new SoftFieldRef(repository, fieldName, typeName);
        field.setData(MetadataRepository.XSD_LINE_NUMBER, XSDParser.getStartLine(appInfo));
        field.setData(MetadataRepository.XSD_COLUMN_NUMBER, XSDParser.getStartColumn(appInfo));
        field.setData(MetadataRepository.XSD_DOM_ELEMENT, appInfo);
        return field;
    }
}
