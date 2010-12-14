// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.commmon.util.datamodel.synchronization;

import java.io.Serializable;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DMUpdateEvent implements Serializable {

    private static final long serialVersionUID = -2210287846483785474L;

    public static final String EVENT_TYPE_INIT = "INIT";

    public static final String EVENT_TYPE_UPDATE = "UPDATE";

    public static final String EVENT_TYPE_DELETE = "DELETE";

    private static final String DATA_MODEL_DEFAULT_VERSION = null;

    private String dataModelPK;

    private String dataModelVersion;

    private String eventType;

    private long updateTime;

    /**
     * DOC HSHU DMUpdateEvent constructor comment.
     */
    public DMUpdateEvent() {

    }

    public DMUpdateEvent(String dataModelPK) {
        this(dataModelPK, DATA_MODEL_DEFAULT_VERSION, EVENT_TYPE_UPDATE);
    }

    public DMUpdateEvent(String dataModelPK, String dataModelVersion) {
        this(dataModelPK, dataModelVersion, EVENT_TYPE_UPDATE);
    }

    public DMUpdateEvent(String dataModelPK, String dataModelVersion, String eventType) {
        super();
        this.dataModelPK = dataModelPK;
        this.dataModelVersion = dataModelVersion;
        this.eventType = eventType;
        this.updateTime = System.currentTimeMillis();
    }

    public String getDataModelPK() {
        return dataModelPK;
    }

    public void setDataModelPK(String dataModelPK) {
        this.dataModelPK = dataModelPK;
    }

    public String getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(String dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "DMUpdateEvent [dataModelPK=" + dataModelPK + ", dataModelVersion=" + dataModelVersion + ", eventType="
                + eventType + ", updateTime=" + updateTime + "]";
    }

}
