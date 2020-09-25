/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for all Xml Schema simple types supported in MDM.
 *
 * @see #NUMBERS
 * @see #DATES
 */
public interface Types {
    /**
     * Convenience constants for all number types.
     */
    String[] NUMBERS = new String[] { Types.INT,
            Types.UNSIGNED_INT,
            Types.INTEGER,
            Types.NEGATIVE_INTEGER,
            Types.POSITIVE_INTEGER,
            Types.NON_NEGATIVE_INTEGER,
            Types.NON_POSITIVE_INTEGER,
            Types.DECIMAL,
            Types.DOUBLE,
            Types.UNSIGNED_DOUBLE,
            Types.BYTE,
            Types.UNSIGNED_BYTE,
            Types.LONG,
            Types.UNSIGNED_LONG,
            Types.SHORT,
            Types.UNSIGNED_SHORT,
            Types.FLOAT };

    /**
     * Convenience constants for all integers type.
     */
    Set<String> INTEGERS = new HashSet<String>(Arrays.asList(
        Types.INT,
        Types.UNSIGNED_INT,
        Types.INTEGER,
        Types.NEGATIVE_INTEGER,
        Types.POSITIVE_INTEGER,
        Types.NON_NEGATIVE_INTEGER,
        Types.NON_POSITIVE_INTEGER));

    /**
     * Convenience constants for all long type.
     */
    Set<String> LONGS = new HashSet<String>(Arrays.asList(
        Types.LONG,
        Types.UNSIGNED_LONG));

    /**
     * Convenience constants for all short type.
     */
    Set<String> SHORTS = new HashSet<String>(Arrays.asList(
        Types.SHORT,
        Types.UNSIGNED_SHORT));

    /**
     * Convenience constants for all double type.
     */
    Set<String> DOUBLES = new HashSet<String>(Arrays.asList(
        Types.DECIMAL,
        Types.DOUBLE,
        Types.UNSIGNED_DOUBLE));

    /**
     * Convenience constants for all byte type.
     */
    Set<String> BYTES = new HashSet<String>(Arrays.asList(
        Types.BYTE,
        Types.UNSIGNED_BYTE));

    /**
     * Convenience constant for all date types.
     */
    String[] DATES = new String[] { Types.DATE,
            Types.DATETIME,
            Types.TIME };

    String LONG                 = "long"; //$NON-NLS-1$
    String DECIMAL              = "decimal"; //$NON-NLS-1$
    String BOOLEAN              = "boolean"; //$NON-NLS-1$
    String BYTE                 = "byte"; //$NON-NLS-1$
    String DATE                 = "date"; //$NON-NLS-1$
    String DATETIME             = "dateTime"; //$NON-NLS-1$
    String DOUBLE               = "double"; //$NON-NLS-1$
    String FLOAT                = "float"; //$NON-NLS-1$
    String INTEGER              = "integer"; //$NON-NLS-1$
    String INT                  = "int"; //$NON-NLS-1$
    String SHORT                = "short"; //$NON-NLS-1$
    String STRING               = "string"; //$NON-NLS-1$
    String POSITIVE_INTEGER     = "positiveInteger"; //$NON-NLS-1$
    String NEGATIVE_INTEGER     = "negativeInteger"; //$NON-NLS-1$
    String NON_POSITIVE_INTEGER = "nonPositiveInteger"; //$NON-NLS-1$
    String NON_NEGATIVE_INTEGER = "nonNegativeInteger"; //$NON-NLS-1$
    String UNSIGNED_INT         = "unsignedInt"; //$NON-NLS-1$
    String HEX_BINARY           = "hexBinary"; //$NON-NLS-1$
    String BASE64_BINARY        = "base64Binary"; //$NON-NLS-1$
    String ANY_URI              = "anyURI"; //$NON-NLS-1$
    String QNAME                = "QName"; //$NON-NLS-1$
    String TIME                 = "time"; //$NON-NLS-1$
    String DURATION             = "duration"; //$NON-NLS-1$
    String UNSIGNED_SHORT       = "unsignedShort"; //$NON-NLS-1$
    String UNSIGNED_BYTE        = "unsignedByte"; //$NON-NLS-1$
    String UNSIGNED_LONG        = "unsignedLong"; //$NON-NLS-1$
    String UNSIGNED_DOUBLE      = "unsignedDouble"; //$NON-NLS-1$
    String MULTI_LINGUAL        = "MULTI_LINGUAL"; //$NON-NLS-1$
    String UUID                 = "UUID"; //$NON-NLS-1$
    String G_YEAR_MONTH         = "gYearMonth"; //$NON-NLS-1$
    String G_YEAR               = "gYear"; //$NON-NLS-1$
    String G_MONTH_DAY          = "gMonthDay"; //$NON-NLS-1$
    String G_DAY                = "gDay"; //$NON-NLS-1$
    String G_MONTH              = "gMonth"; //$NON-NLS-1$
    String ANY_TYPE             = "anyType"; //$NON-NLS-1$
    String ANY_SIMPLE_TYPE      = "anySimpleType"; //$NON-NLS-1$
    String TOKEN                = "token"; //$NON-NLS-1$
    /**
     * Length of a field to contain a {@link #UUID} value.
     */
    int UUID_LENGTH = java.util.UUID.randomUUID().toString().length() + 10;
}
