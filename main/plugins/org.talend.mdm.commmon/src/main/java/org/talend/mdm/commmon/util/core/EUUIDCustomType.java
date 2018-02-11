/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.core;

/**
 * all uuid type
 */
public enum EUUIDCustomType {
	//two custom simple type (only used for concept id)
	UUID("UUID"), //$NON-NLS-1$
	PICTURE("PICTURE"), //$NON-NLS-1$
	URL("URL"), //$NON-NLS-1$
	AUTO_INCREMENT("AUTO_INCREMENT"), //$NON-NLS-1$
	MULTI_LINGUAL("MULTI_LINGUAL"); //$NON-NLS-1$

    String name;

	EUUIDCustomType(String name){
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
}
