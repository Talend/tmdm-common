/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.compare;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Implementations of this interface can rank diffs between 2 data models from {@link Impact#LOW} to {@link Impact#HIGH}
 * . Each storage implementation in MDM is supposed to provide an implementation of this interface.
 * </p>
 * <p>
 * This interface is located in common module to allow usage in both MDM studio and MDM server sides.
 * </p>
 */
public interface ImpactAnalyzer {

    enum Impact {
        /**
         * A change that underlying storage may not be able to properly take into account <b>AND</b> will severely
         * impact the existing schema in a way that user won't be to query or update data.
         */
        HIGH,
        /**
         * <p>
         * A change that underlying storage is not able to properly take into account <b>BUT</b> operations on storage
         * will not be altered (some useless elements may still exist in underlying storage afterwards though).
         * </p>
         * <p>
         * Please note that such changes may impact later changes on the database in a way MDM won't be able to detect
         * actual conflicts. Medium changes <b>should not be considered as 'minor'</b> and should also be addressed. A
         * medium change only indicates a change that still allows normal operation after the update (but doesn't
         * guarantee normal operation after later update(s)).
         * </p>
         */
        MEDIUM,
        /**
         * A change that underlying storage can properly take into account <b>AND</b> operations on storage will not be
         * altered (some useless elements may still exist in underlying storage).
         */
        LOW
    }

    /**
     * Analyzes impact of the <code>diffResult</code>. Analysis may differ from underlying storage (some storages may be
     * able to cope with changes that others can't).
     *
     * @param diffResult A diff computed between 2 data models, the diff contains all differences (add, remove, modify
     * between the 2 data models).
     * @return All the changes sorted by impact for the underlying storage.
     * @see org.talend.mdm.commmon.metadata.compare.Compare#compare(org.talend.mdm.commmon.metadata.MetadataRepository,
     * org.talend.mdm.commmon.metadata.MetadataRepository)
     * @see org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer.Impact
     */
    Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult);
}
