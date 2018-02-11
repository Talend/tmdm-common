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

/**
 * DOC hbhong class global comment. Detailled comment
 */
public abstract class AbstractObjectPropHashValueCalculator implements IHashValueCalculator {

    protected CommonHashValueCalculatorProvider provider = CommonHashValueCalculatorProvider.getInstance();

    public abstract Object[] getPropertys(Object obj);

    public abstract IHashValueCalculator[] getCalculators();

    public long calculateHash(Object obj) {
        Object[] props = getPropertys(obj);
        IHashValueCalculator[] calculators = getCalculators();
        if (props.length != calculators.length)
            throw new IllegalArgumentException();

        long hashCode = 1;
        for (int i = 0; i < props.length; i++) {
            Object prop = props[i];
            IHashValueCalculator caculator = calculators[i];
            hashCode = 31 * hashCode + (prop == null ? 0 : caculator.calculateHash(prop));
            System.out.println(prop + "\tH=" + hashCode);
        }

        return hashCode;
    }
}
