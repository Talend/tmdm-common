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

package org.talend.mdm.commmon.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetadataExtensions implements MetadataExtensible {

    protected Map<String, Object> dataMap;

    public synchronized void setData(String key, Object data) {
        if (dataMap == null) {
            dataMap = new HashMap<String, Object>() {

                private static final int THRESHOLD = 12;

                @Override
                public Object put(String s, Object o) {
                    if (size() > THRESHOLD) {
                        throw new IllegalStateException("Map is not aimed to contain more than " + THRESHOLD + " elements.");
                    }
                    return super.put(s, o);
                }
            };
        }
        dataMap.put(key, data);
    }

    public <X> X getData(String key) {
        if (dataMap == null) {
            return null;
        }
        return (X) dataMap.get(key);
    }

}
