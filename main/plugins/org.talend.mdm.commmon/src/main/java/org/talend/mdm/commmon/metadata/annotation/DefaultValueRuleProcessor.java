package org.talend.mdm.commmon.metadata.annotation;

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
            // Process X_ForeignKey annotation first to get referenced type right
            for (Element appInfo : annotations) {
                String source = appInfo.getAttribute("source"); //$NON-NLS-1$
                if ("X_Default_Value_Rule".equals(source)) { //$NON-NLS-1$
                    state.setDefaultValueRule(appInfo.getTextContent());
                }
            }
        }
    }

}
