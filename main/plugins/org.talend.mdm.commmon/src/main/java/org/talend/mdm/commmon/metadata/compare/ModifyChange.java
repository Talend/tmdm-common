/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.compare;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.talend.mdm.commmon.metadata.*;

public class ModifyChange extends Change {

    private final MetadataVisitable current;

    private ResourceBundle bundle;

    public ModifyChange(MetadataVisitable previous, MetadataVisitable current) {
        super(previous);
        this.current = current;
    }

    public MetadataVisitable getPrevious() {
        return getElement();
    }

    public MetadataVisitable getCurrent() {
        return current;
    }


    @Override
    public String getMessage(Locale locale) {
        if (bundle == null || !bundle.getLocale().equals(locale)) {
            bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, locale);
        }
        return element.accept(new DefaultMetadataVisitor<String>() {

            @Override
            public String visit(ComplexTypeMetadata complexType) {
                return MessageFormat.format(bundle.getString("modify_entity_type"), complexType.getName()); //$NON-NLS-1$
            }

            @Override
            public String visit(ReferenceFieldMetadata referenceField) {
                return MessageFormat.format(bundle.getString("modify_reference_field"), referenceField.getName()); //$NON-NLS-1$
            }

            @Override
            public String visit(ContainedTypeFieldMetadata containedField) {
                return MessageFormat.format(bundle.getString("modify_contained_field"), containedField.getName()); //$NON-NLS-1$
            }

            @Override
            public String visit(SimpleTypeFieldMetadata simpleField) {
                return MessageFormat.format(bundle.getString("modify_simple_field"), simpleField.getName()); //$NON-NLS-1$
            }

            @Override
            public String visit(EnumerationFieldMetadata enumField) {
                return MessageFormat.format(bundle.getString("modify_enum_field"), enumField.getName()); //$NON-NLS-1$
            }
        });

    }
}
