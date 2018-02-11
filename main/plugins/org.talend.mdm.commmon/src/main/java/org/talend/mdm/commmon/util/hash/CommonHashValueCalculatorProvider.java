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
public class CommonHashValueCalculatorProvider {

    private static CommonHashValueCalculatorProvider provider = new CommonHashValueCalculatorProvider();

    public static CommonHashValueCalculatorProvider getInstance() {
        return provider;
    }

    private CommonHashValueCalculatorProvider() {
    }

    IHashValueCalculator booleanCalculator = new IHashValueCalculator() {

        public long calculateHash(Object obj) {
            if (!(obj instanceof Boolean))
                throw new IllegalArgumentException();
            return ((Boolean) obj) ? 1231 : 1237;

        }
    };

    IHashValueCalculator stringCalculator = new IHashValueCalculator() {

        public long calculateHash(Object obj) {
            if (!(obj instanceof String))
                throw new IllegalArgumentException();
            return ((String) obj).hashCode();
        }
    };

    public IHashValueCalculator getStringCalculator() {
        return this.stringCalculator;
    }

    IHashValueCalculator intCalculator = new IHashValueCalculator() {

        public long calculateHash(Object obj) {
            if (!(obj instanceof Integer))
                throw new IllegalArgumentException();
            return ((Integer) obj).longValue();
        }
    };


    public IHashValueCalculator getIntCalculator() {
        return this.intCalculator;
    }


    public IHashValueCalculator getLongCalculator() {
        return this.longCalculator;
    }

    IHashValueCalculator longCalculator = new IHashValueCalculator() {

        public long calculateHash(Object obj) {
            if (!(obj instanceof Long))
                throw new IllegalArgumentException();
            return ((Long) obj).longValue();
        }
    };

    public IHashValueCalculator getBooleanCalculator() {
        return booleanCalculator;
    }
}
