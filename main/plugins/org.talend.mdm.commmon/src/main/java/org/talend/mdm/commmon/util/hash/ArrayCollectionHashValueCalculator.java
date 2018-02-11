/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.hash;

import java.util.Collection;

/**
 * DOC hbhong class global comment. Detailled comment
 */
public class ArrayCollectionHashValueCalculator implements IHashValueCalculator {

    private final IHashValueCalculator calculator;

    public ArrayCollectionHashValueCalculator(IHashValueCalculator calculator) {
        this.calculator = calculator;
    }

    @SuppressWarnings("rawtypes")
    public long calculateHash(Object obj) {
        if (!(obj instanceof Object[]) && !(obj instanceof Collection))
            throw new IllegalArgumentException();

        Object[] objs = null;
        if (obj instanceof Object[]) {
            objs = (Object[]) obj;
        }
        if (obj instanceof Collection) {
            objs = ((Collection) obj).toArray();
        }
        long hashCode = 0;
        for (Object child : objs) {
            hashCode = 31 * hashCode + (child == null ? 0 : calculator.calculateHash(child));
        }
        return hashCode;
    }
}
