/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.hash.impl;

import org.talend.mdm.commmon.util.hash.AbstractObjectPropHashValueCalculator;
import org.talend.mdm.commmon.util.hash.IHashValueCalculator;

/**
 * DOC hbhong class global comment. Detailled comment
 */
public abstract class RoutingRuleHVCalculator extends AbstractObjectPropHashValueCalculator {

    IHashValueCalculator nameCal = provider.getStringCalculator();

    IHashValueCalculator descriptionCal = provider.getStringCalculator();

    IHashValueCalculator synchronousCal = provider.getBooleanCalculator();

    IHashValueCalculator conceptCal = provider.getStringCalculator();

    IHashValueCalculator serviceJNDICal = provider.getStringCalculator();

    IHashValueCalculator parametersCal = provider.getStringCalculator();

    IHashValueCalculator conditionCal = provider.getStringCalculator();

    IHashValueCalculator deactiveCal = provider.getBooleanCalculator();

    IHashValueCalculator expressionCal = getRoutinRuleExpressionHVCalculator();

    IHashValueCalculator[] caculators = new IHashValueCalculator[] { nameCal, descriptionCal, synchronousCal, conceptCal,

    serviceJNDICal, parametersCal, conditionCal, deactiveCal, expressionCal };

    /**
     * 1- name; 2- description; 3- synchronous; 4- concept; 5- serviceJNDI; 6- parameters; 7- condition; 8- deactive; 9-
     * WSRoutingRuleExpression
     */
    @Override
    public IHashValueCalculator[] getCalculators() {
        return caculators;
    }

    public abstract class RoutingRuleExpressionHVCalculator extends AbstractObjectPropHashValueCalculator {

        IHashValueCalculator nameCal = provider.getStringCalculator();

        IHashValueCalculator xpathCal = provider.getStringCalculator();

        IHashValueCalculator valueCal = provider.getStringCalculator();

        IHashValueCalculator operatorCal = getRoutingRuleOperatorHVCalculator();

        IHashValueCalculator[] caculators = new IHashValueCalculator[] { nameCal, xpathCal, valueCal, operatorCal };

        /**
         * 1-name 2-xpath 3-value 4-wsOperator
         */
        @Override
        public IHashValueCalculator[] getCalculators() {
            return caculators;
        }
    }

    public abstract class RoutingRuleOperatorHVCalculator extends AbstractObjectPropHashValueCalculator {

        IHashValueCalculator valueCal = provider.getStringCalculator();

        IHashValueCalculator[] calculators = new IHashValueCalculator[] { valueCal };

        /**
         * 1-value
         */
        @Override
        public IHashValueCalculator[] getCalculators() {
            return calculators;
        }
    }

    protected abstract IHashValueCalculator getRoutinRuleExpressionHVCalculator();

    protected abstract IHashValueCalculator getRoutingRuleOperatorHVCalculator();
}
