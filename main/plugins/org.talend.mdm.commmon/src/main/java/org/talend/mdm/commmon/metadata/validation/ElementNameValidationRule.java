package org.talend.mdm.commmon.metadata.validation;

import java.util.regex.Pattern;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadataImpl;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;


public class ElementNameValidationRule implements ValidationRule {

    private ComplexTypeMetadataImpl ctype;

    private SimpleTypeFieldMetadata stype;

    private ContainedTypeFieldMetadata containerField;

    ElementNameValidationRule(ComplexTypeMetadataImpl type) {
        this.ctype = type;
    }

    ElementNameValidationRule(SimpleTypeFieldMetadata field) {
        this.stype = field;
    }

    ElementNameValidationRule(ContainedTypeFieldMetadata field) {
        this.containerField  = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if (ctype != null) {
            boolean isEntity = ctype.isInstantiable(); 
            if(isEntity && !isValid(ctype.getName())) {
                handler.error(ctype, "Name of entity '" + ctype.getName() + "' contains invalid character",
                        ctype.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        ctype.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        ctype.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.NAME_CONTAINS_INVALID_CHARACTER);

                return false;
            } else if(!isEntity && !isValid(ctype.getName())) {
                FieldMetadata container = ctype.getContainer();
                if (container == null) {// 'null' means it's top level complex type
                    handler.error(ctype, "Name of complex type '" + ctype.getName() + "' contains invalid character",
                            ctype.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                            ctype.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                            ctype.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                            ValidationError.NAME_CONTAINS_INVALID_CHARACTER);

                    return false;
                }
            }
        }

        if (stype != null) {
             if(!isValid(stype.getName())) {
                handler.error(stype, "Name of simple type field '" + stype.getName() + "' contains invalid character",
                         stype.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                         stype.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                         stype.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                         ValidationError.NAME_CONTAINS_INVALID_CHARACTER);

                 return false;
             }
        }
        
        if(containerField != null) {
            if (!isValid(containerField.getName()) && !containerField.isFieldReferenceToEntity()) {
                handler.error(containerField,
                        "Name of complex type field '" + containerField.getName() + "' contains invalid character",
                        containerField.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        containerField.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        containerField.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.NAME_CONTAINS_INVALID_CHARACTER);

                return false;
            }
        }
        
        return true;
    }

    private boolean isValid(String newText) {
        boolean result = false;
        if (newText != null) {
            Pattern pattern = Pattern.compile("([a-zA-Z][-|\\.|\\w]*\\w)|[a-zA-Z]"); //$NON-NLS-1$
            result = pattern.matcher(newText).matches();
        }

        return result;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }

}
