package org.talend.mdm.commmon.metadata.annotation;

import org.apache.commons.lang.StringUtils;
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
                    boolean isValue = isValue(appInfo.getTextContent());
                    if (isValue) {
                        state.setDefaultValueRule(appInfo.getTextContent());
                    }
                }
            }
        }
    }

    private boolean isValue(String text) {
        boolean isValue = false;
        
        if (StringUtils.isEmpty(text)) {
            return isValue;
        }
        
        if (StringUtils.startsWith(text, "\"") && StringUtils.endsWith(text, "\"")) {
            isValue = true;
        } else if (StringUtils.isNumeric(text)) {
            isValue = true;
        } else if (StringUtils.equals(text, "fn:false()") || StringUtils.equals(text, "fn:true()")) {
            isValue = true;
        }
        return isValue;
    }

}
