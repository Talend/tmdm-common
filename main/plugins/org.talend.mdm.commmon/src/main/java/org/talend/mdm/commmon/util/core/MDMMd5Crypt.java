/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.core;

import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

public final class MDMMd5Crypt {

    protected static String mergePasswordAndSalt(String password, Object salt, boolean strict) {
        if (password == null) {
            password = "";
        }

        if ((strict) && (salt != null)
                && ((salt.toString().lastIndexOf("{") != -1) || (salt.toString().lastIndexOf("}") != -1))) {
            throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
        }

        if ((salt == null) || ("".equals(salt))) {
            return password;
        }
        return password + "{" + salt.toString() + "}";
    }

    public static String encodePassword(String password, String salt) {
        String merge = mergePasswordAndSalt(password, salt, false);
        return DigestUtils.md5Hex(merge);
    }

    public static boolean isPasswordValid(String encPass, String rawPass, String salt) {
        String pass1 = "" + encPass;
        String pass2 = encodePassword(rawPass, salt);
        return Objects.deepEquals(pass1, pass2);
    }
}
