/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.exception;

public class XmlBeanDefinitionException extends RuntimeException {

    private static final long serialVersionUID = -4320366028561946251L;

    public XmlBeanDefinitionException() {
        super();
    }

    public XmlBeanDefinitionException(String message) {
        super(message);
    }

    public XmlBeanDefinitionException(String message, Throwable e) {
        super(message, e);
    }
}
