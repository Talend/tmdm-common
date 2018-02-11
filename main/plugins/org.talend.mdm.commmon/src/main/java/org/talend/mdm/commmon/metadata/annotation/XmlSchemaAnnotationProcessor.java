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

import org.eclipse.xsd.XSDAnnotation;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

/**
 * Enrich a {@link ComplexTypeMetadata} being built with information contained in XML Schema information.
 * @see MetadataRepository#createFieldMetadata(org.eclipse.xsd.XSDElementDeclaration, org.talend.mdm.commmon.metadata.ComplexTypeMetadata, int, int)
 */
public interface XmlSchemaAnnotationProcessor {

    /**
     * Process additional type information contained in {@link XSDAnnotation}.
     *
     * @param repository The repository that contains the <code>type</code>.
     * @param type       The {@link ComplexTypeMetadata} being enriched by the <code>annotation</code>.
     * @param annotation An XML Schema annotation.
     * @param state      A {@link org.talend.mdm.commmon.metadata.annotation.XmlSchemaAnnotationProcessorState} that keeps track of information parsed by
     *                   {@link org.talend.mdm.commmon.metadata.annotation.XmlSchemaAnnotationProcessor}.
     */
    void process(MetadataRepository repository, ComplexTypeMetadata type, XSDAnnotation annotation, XmlSchemaAnnotationProcessorState state);
}
