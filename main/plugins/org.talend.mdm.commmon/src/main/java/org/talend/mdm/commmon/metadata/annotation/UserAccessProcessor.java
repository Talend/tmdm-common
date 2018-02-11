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

import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDAnnotation;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.w3c.dom.Element;

public class UserAccessProcessor implements XmlSchemaAnnotationProcessor {

    @Override
    public void process(MetadataRepository repository, ComplexTypeMetadata type, XSDAnnotation annotation, XmlSchemaAnnotationProcessorState state) {
        if (annotation != null) {
            EList<Element> appInfoElements = annotation.getApplicationInformation();
            for (Element appInfo : appInfoElements) {
                String source = appInfo.getAttribute("source"); //$NON-NLS-1$
                String textContent = appInfo.getTextContent();
                if ("X_Hide".equals(source)) { //$NON-NLS-1$
                    state.getHide().add(textContent);
                } else if ("X_Write".equals(source)) { //$NON-NLS-1$
                    state.getAllowWrite().add(textContent);
                } else if ("X_Deny_Create".equals(source)) { //$NON-NLS-1$
                    state.getDenyCreate().add(textContent);
                } else if ("X_Deny_LogicalDelete".equals(source)) { //$NON-NLS-1$
                    state.getDenyLogicalDelete().add(textContent);
                } else if ("X_Deny_PhysicalDelete".equals(source)) { //$NON-NLS-1$
                    state.getDenyPhysicalDelete().add(textContent);
                } else if ("X_Workflow".equals(source)) {  //$NON-NLS-1$
                    // including Writable, Read-only and Hidden
                    state.getWorkflowAccessRights().add(textContent);
                } else if ("X_Visible_Rule".equals(source)) { //$NON-NLS-1$
                    state.setVisibilityRule(textContent);
                }
            }
        }
    }
}
