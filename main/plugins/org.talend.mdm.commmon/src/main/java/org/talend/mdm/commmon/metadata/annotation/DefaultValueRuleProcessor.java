/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.annotation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDAnnotation;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.w3c.dom.Element;

public class DefaultValueRuleProcessor implements XmlSchemaAnnotationProcessor {

    @Override
    public void process(MetadataRepository repository, ComplexTypeMetadata type, XSDAnnotation annotation,
            XmlSchemaAnnotationProcessorState state) {
        if (annotation != null) {
            EList<Element> annotations = annotation.getApplicationInformation();
            for (Element appInfo : annotations) {
                String source = appInfo.getAttribute("source"); //$NON-NLS-1$

                if ("X_Default_Value_Rule".equals(source)) { //$NON-NLS-1$
                    if (isValue(appInfo.getTextContent().trim())) {
                        state.setDefaultValueRule(appInfo.getTextContent());
                    }
                }
            }
        }
    }

    private boolean isValue(String text) {
        boolean isValue = false;

        if (StringUtils.isNotBlank(text)) {
            if (text.matches("('.*?'|\".*?\")")) { //$NON-NLS-1$
                isValue = true;
            } else if (NumberUtils.isNumber(text)) {
                isValue = true;
            } else if (StringUtils.equalsIgnoreCase(text, MetadataRepository.FN_FALSE)
                    || StringUtils.equalsIgnoreCase(text, MetadataRepository.FN_TRUE)) {
                isValue = true;
            }
        }
        return isValue;
    }
}
