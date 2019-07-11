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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

public class MetadataUtils {

    private static final Logger LOGGER = Logger.getLogger(MetadataUtils.class);

    private MetadataUtils() {
    }

    private static final double ENTITY_RANK_ADJUST = 0.9;

    /**
     * <p>
     * Computes "entity rank": entity rank score is based on a modified version of Google's Page Rank algorithm (it's
     * the inverse operation of Page Rank).
     * </p>
     * <p>
     * Entity rank is computed with this algorithm:
     * ER(E) = N + d (ER(E1)/C(E1) + ... + ER(En)/C(En))
     * where:
     * <ul>
     * <li>ER(E) is the entity rank of E.</li>
     * <li>N the number of entities in <code>repository</code></li>
     * <li>d an adjustment factor (between 0 and 1)</li>
     * <li>ER(Ei) is the entity rank for entity Ei that E references via a reference field</li>
     * <li>C(Ei) the number of entities in <code>repository</code> that reference Ei in the repository.</li>
     * </ul>
     * </p>
     * <p>
     * Code is expected to run in linear time (O(n+p) where n is the number of entities and p the number of references).
     * Used memory is O(n^2) (due to a dependency ordering).
     * </p>
     *
     * @param repository A {@link MetadataRepository} instance that contains entity types.
     * @return A {@link Map} that maps a entity to its entity rank value.
     */
    public static Map<ComplexTypeMetadata, Long> computeEntityRank(MetadataRepository repository) {
        List<ComplexTypeMetadata> sortedTypes = sortTypes(repository, SortType.LENIENT);
        int totalNumber = sortedTypes.size();

        Map<ComplexTypeMetadata, Long> entityRank = new HashMap<>();
        for (ComplexTypeMetadata currentType : sortedTypes) {
            if (currentType.isInstantiable()) {
                double rank = totalNumber;
                for (FieldMetadata currentField : currentType.getFields()) {
                    if (currentField instanceof ReferenceFieldMetadata) {
                        ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) currentField).getReferencedType();
                        if (referencedType != currentType) {
                            Long referencedEntityRank = entityRank.get(referencedType);
                            if (referencedEntityRank != null) {
                                double inboundReferencesCount = getInboundReferencesCount(repository, referencedType);
                                rank += ENTITY_RANK_ADJUST * (referencedEntityRank / inboundReferencesCount);
                            }
                        }
                    }
                }
                entityRank.put(currentType, Math.round(rank));
            }
        }
        return entityRank;
    }

    private static double getInboundReferencesCount(MetadataRepository repository, ComplexTypeMetadata referencedType) {
        return repository.accept(new InboundReferences(referencedType)).size();
    }

    /**
     * Returns the top level type for <code>type</code> parameter: this method returns the type before <i>anyType</i>
     * in type hierarchy. This does not apply to types declared in {@link XMLConstants#W3C_XML_SCHEMA_NS_URI}.
     * <ul>
     * <li>In an MDM entity B inherits from A, getSuperConcreteType(B) returns A.</li>
     * <li>If a simple type LimitedString extends xsd:string, getSuperConcreteType(LimitedString) returns xsd:string.</li>
     * <li>getSuperConcreteType(xsd:long) returns xsd:long (even if xsd:long extends xsd:decimal).</li>
     * <li>If the type does not have any super type, this method returns the <code>type</code> parameter.</li>
     * </ul>
     *
     * @param type A non null type that may have super types.
     * @return The higher type in inheritance tree before <i>anyType</i>.
     */
    public static TypeMetadata getSuperConcreteType(TypeMetadata type) {
        if (type == null) {
            return null;
        }
        // Move up the inheritance tree to find the "most generic" type (used when simple types inherits from XSD types,
        // in this case, the XSD type is interesting, not the custom one).
        while (!XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace()) && !type.getSuperTypes().isEmpty()) {
            type = type.getSuperTypes().iterator().next();
        }
        return type;
    }

    /**
     * @param javaClassName A java class name.
     * @return The XSD type that can be used to store a value typed as <code>javaClassName</code>.
     * @throws UnsupportedOperationException If there's no known mapping from this java class to a XSD primitive type.
     */
    public static String getType(String javaClassName) {
        if ("java.lang.String".equals(javaClassName)) { //$NON-NLS-1$
            return Types.STRING;
        } else if ("java.lang.Integer".equals(javaClassName) //$NON-NLS-1$
                || "java.math.BigInteger".equals(javaClassName)) { //$NON-NLS-1$
            return Types.INT;
        } else if ("java.lang.Boolean".equals(javaClassName)) { //$NON-NLS-1$
            return Types.BOOLEAN;
        } else if ("java.math.BigDecimal".equals(javaClassName)) { //$NON-NLS-1$
            return Types.DECIMAL;
        } else if ("java.sql.Timestamp".equals(javaClassName)) { //$NON-NLS-1$
            return Types.DATETIME;
        } else if ("java.lang.Short".equals(javaClassName)) { //$NON-NLS-1$
            return Types.SHORT;
        } else if ("java.lang.Long".equals(javaClassName)) { //$NON-NLS-1$
            return Types.LONG;
        } else if ("java.lang.Float".equals(javaClassName)) { //$NON-NLS-1$
            return Types.FLOAT;
        } else if ("java.lang.Byte".equals(javaClassName)) { //$NON-NLS-1$
            return Types.BYTE;
        } else if ("java.lang.Double".equals(javaClassName)) { //$NON-NLS-1$
            return Types.DOUBLE;
        } else {
            throw new UnsupportedOperationException("No support for java class '" + javaClassName + "'");
        }
    }

    /**
     * Allow configuration of {@link #sortTypes(MetadataRepository) sort} for types.
     */
    public static enum SortType {
        /**
         * Follow only <b>mandatory</b> FK for the dependency sort (all optional FK or FK contained in optional elements are
         * ignored).
         * @see #LENIENT
         */
        STRICT,
        /**
         * Follow both mandatory <b>and</b> optional. This leads to a more accurate dependency order, but may lead to
         * cycles issues. In this case, sort is "best effort", meaning sort will decide of an order for remaining cycles
         * (cycle resolutions might not be always the same from one sort to another).
         * @see #STRICT
         */
        LENIENT
    }

    /**
     * <p>
     * Sorts type in inverse order of dependency (topological sort) using
     * {@link org.talend.mdm.commmon.metadata.MetadataUtils.SortType#STRICT strict} order. A dependency to <i>type</i>
     * might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing.
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository The repository that contains entity types to sort.
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency
     * on any other type of the list.
     * @throws org.talend.mdm.commmon.metadata.CircularDependencyException If repository contains types that creates a
     * cyclic dependency. Error message contains information on where the cycle is.
     */
    public static List<ComplexTypeMetadata> sortTypes(MetadataRepository repository) {
        return sortTypes(repository, SortType.STRICT);
    }

    /**
     * <p>
     * Sorts type in inverse order of dependency (topological sort) using <code>sortType</code> order. A dependency to
     * <i>type</i> might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing.
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository The repository that contains entity types to sort.
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency
     * on any other type of the list.
     * @throws org.talend.mdm.commmon.metadata.CircularDependencyException If repository contains types that creates a
     * cyclic dependency. Error message contains information on where the cycle is.
     */
    public static List<ComplexTypeMetadata> sortTypes(MetadataRepository repository, SortType sortType) {
        List<ComplexTypeMetadata> types = new ArrayList<>(repository.getUserComplexTypes());
        return _sortTypes(repository, types, sortType);
    }

    /**
     * <p>
     * Sorts types (usually a sub set of types in a {@link MetadataRepository}) in inverse order of dependency
     * (topological sort). A dependency to <i>type</i> might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing.
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository This is used to display information in case of cycle.
     * @param types The list of types to be sorted. About the list:
     * <ul>
     * <li>
     * This list should provide a transitive closure of types (all references to other types must be satisfied in this
     * list), if it isn't the unresolved FK will be ignored.</li>
     * <li>
     * If one of the type is a {@link org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata contained type},
     * please note sort will consider its containing (top-level) entity type.</li>
     * </ul>
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency
     * on any other type of the list.
     * @throws org.talend.mdm.commmon.metadata.CircularDependencyException If repository contains types that creates a
     * cyclic dependency. Error message contains information on where the cycle is.
     */
    public static List<ComplexTypeMetadata> sortTypes(MetadataRepository repository, List<ComplexTypeMetadata> types) {
        return _sortTypes(repository, types, SortType.STRICT);
    }

    /**
     * <p>
     * Sorts types (usually a sub set of types in a {@link MetadataRepository}) in inverse order of dependency
     * (topological sort). A dependency to <i>type</i> might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing.
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository This is used to display information in case of cycle.
     * @param types The list of types to be sorted. About the list:
     * <ul>
     * <li>
     * This list should provide a transitive closure of types (all references to other types must be satisfied in this
     * list), if it isn't the unresolved FK will be ignored.</li>
     * <li>
     * If one of the type is a {@link org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata contained type},
     * please note sort will consider its containing (top-level) entity type.</li>
     * </ul>
     * @param sortType The type how to sort
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency
     * on any other type of the list.
     * @throws org.talend.mdm.commmon.metadata.CircularDependencyException If repository contains types that creates a
     * cyclic dependency. Error message contains information on where the cycle is.
     */
    public static List<ComplexTypeMetadata> sortTypes(MetadataRepository repository, List<ComplexTypeMetadata> types, SortType sortType) {
        return _sortTypes(repository, types, sortType);
    }

    // Internal method for type sort
    private static List<ComplexTypeMetadata> _sortTypes(MetadataRepository repository, List<ComplexTypeMetadata> typesSubSet,
            final SortType sortType) {
        /*
         * Compute additional data for topological sorting
         */
        // Ensure to get only top level types (TMDM-7235)
        final List<ComplexTypeMetadata> types = new ArrayList<>();
        for (ComplexTypeMetadata currentType : typesSubSet) {
            if (currentType instanceof ContainedComplexTypeMetadata) {
                types.add(currentType.getEntity());
            } else if (!types.contains(currentType)) {
                types.add(currentType);
            }
        }
        // Create the dependency matrix
        final int typeNumber = types.size();
        byte[][] dependencyGraph = new byte[typeNumber][typeNumber];
        for (final ComplexTypeMetadata type : types) {
            dependencyGraph[getId(type, types)] = type.accept(new DefaultMetadataVisitor<byte[]>() {

                final Set<TypeMetadata> processedTypes = new HashSet<>();

                final Set<TypeMetadata> processedReferences = new HashSet<>();

                final byte[] lineContent = new byte[typeNumber]; // Stores dependencies of current type

                @Override
                public byte[] visit(ComplexTypeMetadata complexType) {
                    // if the complexType is not one entrty, it also to be checked
                    super.visit(complexType);
                    if (processedTypes.contains(complexType)) {
                        return lineContent;
                    } else {
                        processedTypes.add(complexType);
                    }
                    if (complexType.isInstantiable()) {
                        Collection<TypeMetadata> superTypes = complexType.getSuperTypes();
                        for (TypeMetadata superType : superTypes) {
                            if (superType instanceof ComplexTypeMetadata) {
                                int id = types.indexOf(superType);
                                if (id >= 0) {
                                    lineContent[id]++;
                                }
                            }
                        }
                    }
                    if (complexType.isInstantiable()) {
                        processedTypes.clear();
                    }
                    return lineContent;
                }

                @Override
                public byte[] visit(ContainedTypeFieldMetadata containedField) {
                    ComplexTypeMetadata containedType = containedField.getContainedType();
                    if (processedTypes.contains(containedType)) {
                        return lineContent;
                    } else {
                        processedTypes.add(containedType);
                    }
                    containedType.accept(this);
                    for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                        if (processedTypes.contains(subType)) {
                            return lineContent;
                        } else {
                            processedTypes.add(subType);
                            subType.accept(this);
                        }
                    }
                    return lineContent;
                }

                @Override
                public byte[] visit(ReferenceFieldMetadata referenceField) {
                    boolean isInherited = !referenceField.getDeclaringType().equals(referenceField.getContainingType());
                    // Only handle FK declared IN the type (inherited FKs are already processed).
                    if (isInherited) {
                        return lineContent;
                    }
                    // Within entity count only once references to other type
                    ComplexTypeMetadata referencedType = referenceField.getReferencedType();
                    if (!processedReferences.add(referencedType)) {
                        return lineContent;
                    }
                    // Only takes into account mandatory and FK integrity-enabled FKs.
                    if (include(referenceField) && referenceField.isFKIntegrity()) {
                        if (referencedType.isInstantiable()) {
                            if (types.contains(referencedType) && (!processedTypes.contains(referencedType) || isReferencedBySelf(referenceField))) {
                                lineContent[getId(referencedType, types)]++;
                                if (sortType == SortType.LENIENT) {
                                    // Implicitly include reference to sub types of referenced type for LENIENT sort (STRICT should
                                    // take in account sub types to exclude false cyclic dependencies).
                                    for (ComplexTypeMetadata subType : referencedType.getSubTypes()) {
                                        lineContent[getId(subType, types)]++;
                                    }
                                }
                            }
                        }
                    }
                    return lineContent;
                }

                private boolean include(FieldMetadata field) {
                    if (field == null) {
                        return false;
                    }
                    ComplexTypeMetadata containingType = field.getContainingType();
                    FieldMetadata containerField = containingType.getContainer();
                    switch (sortType) {
                    case STRICT:
                        if (containerField != null) {
                            return include(containerField) && field.isMandatory();
                        } else {
                            return field.isMandatory();
                        }
                    case LENIENT:
                        return true;
                    default:
                        throw new NotImplementedException("Sort '" + sortType + "' is not implemented.");
                    }
                }

                private boolean isReferencedBySelf(ReferenceFieldMetadata referenceField) {
                    ComplexTypeMetadata containingType = referenceField.getContainingType();
                    if (containingType instanceof ContainedComplexTypeMetadata) {
                        containingType = containingType.getContainer().getContainingType();
                    }
                    return containingType.equals(referenceField.getReferencedType());
                }
            });
        }
        // Log dependency matrix (before sort)
        if (LOGGER.isTraceEnabled()) {
            StringBuilder builder = logDependencyMatrix(dependencyGraph);
            LOGGER.trace("Before sort: " + builder.toString());
        }
        /*
         * TOPOLOGICAL SORTING See "Kahn, A. B. (1962), "Topological sorting of large
         * networks", Communications of the ACM"
         */
        List<ComplexTypeMetadata> sortedTypes = new LinkedList<>();
        List<ComplexTypeMetadata> noIncomingEdges = new LinkedList<>();
        int lineNumber = 0;
        for (byte[] line : dependencyGraph) {
            if (!hasIncomingEdges(line)) {
                noIncomingEdges.add(getType(types, lineNumber));
            }
            lineNumber++;
        }
        while (!noIncomingEdges.isEmpty()) {
            Iterator<ComplexTypeMetadata> iterator = noIncomingEdges.iterator();
            ComplexTypeMetadata type = iterator.next();
            iterator.remove();
            sortedTypes.add(type);
            int columnNumber = getId(type, types);
            for (int i = 0; i < typeNumber; i++) {
                int edge = dependencyGraph[i][columnNumber];
                if (edge > 0) {
                    dependencyGraph[i][columnNumber] -= edge;
                    if (!hasIncomingEdges(dependencyGraph[i])) {
                        noIncomingEdges.add(getType(types, i));
                    }
                }
            }
        }
        // Log dependency matrix (after sort)
        if (LOGGER.isTraceEnabled()) {
            StringBuilder builder = logDependencyMatrix(dependencyGraph);
            LOGGER.trace("After sort: "+ builder.toString());
        }
        // Check for cycles
        if (sortedTypes.size() < dependencyGraph.length) {
            List<List<ComplexTypeMetadata>> cycles = new LinkedList<>();
            Map<List<ComplexTypeMetadata>, ComplexTypeMetadata> map = new HashMap<>();
            byte[][] previousGraph = deepCopyArray(dependencyGraph);
            // use dependency graph matrix to get cyclic dependencies (if any).
            /**
             * if the type contains below reference chain
             * A->A1->B->C
             * A->A2->E->F
             * A->A3->H->G
             *
             * it should cycle all the referneces(A1, A2, A3) to check mutual dependency,
             */
            do {
                lineNumber = 0;
                for (byte[] line : dependencyGraph) {
                    if (hasIncomingEdges(line)) { // unresolved dependency (means this is a cycle start).
                        List<ComplexTypeMetadata> dependencyPath = new LinkedList<>();
                        int currentLineNumber = lineNumber;
                        do {
                            ComplexTypeMetadata type = getType(types, currentLineNumber);
                            if (dependencyPath.contains(type)) {
                                break;
                            }
                            dependencyPath.add(type);
                            InboundReferences incomingReferences = new InboundReferences(type);
                            Set<ReferenceFieldMetadata> incomingFields = repository.accept(incomingReferences);
                            boolean hasMetDependency = false;
                            for (ReferenceFieldMetadata incomingField : incomingFields) {
                                ComplexTypeMetadata containingType = repository.getComplexType(incomingField.getEntityTypeName());
                                // Containing type might be null if incoming reference is in the reusable type definition
                                // (but we only care about the entity relations, so use of the reusable types
                                // in entities).
                                if (containingType != null && !containingType.equals(type)) {
                                    int currentDependency = getId(containingType, types);
                                    List<ComplexTypeMetadata> list = new LinkedList<>(dependencyPath);
                                    list.add(containingType);
                                    if (hasIncomingEdges(dependencyGraph[currentDependency]) && !map.containsKey(list)) {
                                        if (dependencyGraph[currentDependency][currentLineNumber] > 0) {
                                            dependencyGraph[currentDependency][currentLineNumber]--;
                                        }
                                        currentLineNumber = currentDependency;
                                        hasMetDependency = true;
                                        map.put(list, containingType);
                                        break;
                                    }
                                }
                            }
                            if (!hasMetDependency) {
                                break;
                            }
                        } while (currentLineNumber != lineNumber);
                        addDepencyToCycleList(sortType, types, lineNumber, cycles, dependencyPath);
                    }
                    lineNumber++;
                }
                if (!equalsTwoArray(previousGraph, dependencyGraph)) {
                    previousGraph = deepCopyArray(dependencyGraph);
                } else {
                    //remove dependency by itself
                    for (int i = 0; i < dependencyGraph.length; i++) {
                        for (int j = 0; j < dependencyGraph[i].length; j++) {
                            if (i == j && dependencyGraph[i][j] > 0) {
                                dependencyGraph[i][j] = 0;
                            }
                        }
                    }
                    lineNumber = 0;
                    /**
                     * Dependency matrix
                     *   0 1 2
                     * 0 0 0 0
                     * 1 0 0 1
                     * 2 0 0 0
                     *
                     * if dependencyGraph[1][2] is > 0, need add the [2, 1] into the dependency list
                     */
                    for (int j = 0; j < dependencyGraph.length; j++) {
                        byte[] line = dependencyGraph[j];
                        if (hasIncomingEdges(line)) {
                            for (int i = 0; i < line.length; i++) {
                                List<ComplexTypeMetadata> dependencyPath = new LinkedList<>();
                                if (line[i] > 0) {
                                    dependencyPath.add(types.get(i));
                                    dependencyPath.add(types.get(j));
                                    dependencyGraph[j][i]--;
                                }
                                addDepencyToCycleList(sortType, types, lineNumber, cycles, dependencyPath);
                            }
                        }
                    }
                }
            } while (hasIncomingEdgesExceptSelf(dependencyGraph));
            // Depending on sort type, report as exception or switch to a "best effort" sort
            switch (sortType) {
            case STRICT:
                if (!cycles.isEmpty()) { // Found cycle(s): report it/them as exception
                    Iterator<List<ComplexTypeMetadata>> cyclesIterator = cycles.iterator();
                    Map<ComplexTypeMetadata, List<FieldMetadata>> cycleHints = new HashMap<>();
                    while (cyclesIterator.hasNext()) {
                        Iterator<ComplexTypeMetadata> dependencyPathIterator = cyclesIterator.next().iterator();
                        ComplexTypeMetadata previous = null;
                        while (dependencyPathIterator.hasNext()) {
                            ComplexTypeMetadata currentType = dependencyPathIterator.next();
                            ArrayList<FieldMetadata> fields = new ArrayList<>();
                            cycleHints.put(currentType, fields);
                            if (previous != null) {
                                Set<ReferenceFieldMetadata> inboundReferences = repository
                                        .accept(new InboundReferences(currentType));
                                for (ReferenceFieldMetadata inboundReference : inboundReferences) {
                                    ComplexTypeMetadata entity = repository.getComplexType(inboundReference.getEntityTypeName());
                                    if (entity != null) {
                                        fields.add(inboundReference);
                                    }
                                }
                            }
                            previous = currentType;
                        }
                    }
                    throw new CircularDependencyException(cycleHints);
                }
            case LENIENT:
                Collections.sort(cycles, new Comparator<List<ComplexTypeMetadata>>() {

                    @Override public int compare(List<ComplexTypeMetadata> o1, List<ComplexTypeMetadata> o2) {
                        return Integer.compare(o2.size(), o1.size());
                    }
                });

                //check the correct sort
                /**
                 * if the cycles contains below chain:
                 *   [S]->[G]->[EP]->[EA]->[C]
                 *   [S]->[I]->[EP]->[EA]->[C]
                 *   [C]->[S]->[U]->[EA]
                 *   [G]->[EP]->[EA]->[C]
                 *
                 *  for [EA] and [C], it have 3 times for EA is before C, and 1 times for EA is after C,
                 *  so it will be set the EA is before C
                 */
                Set<ComplexTypeMetadata> nonSortedTypes = new HashSet<>();
                for (List<ComplexTypeMetadata> cycle : cycles) {
                    nonSortedTypes.addAll(cycle);
                }
                List<ComplexTypeMetadata> complexTypeMetadataList = new ArrayList<>(nonSortedTypes);
                byte[][] graph = new byte[complexTypeMetadataList.size()][complexTypeMetadataList.size()];
                for (int i = 0; i < complexTypeMetadataList.size(); i++) {
                    ComplexTypeMetadata complexTypeMetadata = complexTypeMetadataList.get(i);
                    for (int j = 0; j < complexTypeMetadataList.size(); j++) {
                        ComplexTypeMetadata nextComplexTypeMetadata = complexTypeMetadataList.get(j);
                        if (complexTypeMetadata == nextComplexTypeMetadata) {
                            continue;
                        }
                        int count = 0;
                        for (List<ComplexTypeMetadata> cycle : cycles) {
                            int mainIndex = cycle.indexOf(complexTypeMetadata);
                            int nextIndex = cycle.indexOf(nextComplexTypeMetadata);
                            if (mainIndex >= 0 && nextIndex >= 0) {
                                if (mainIndex < nextIndex) {
                                    graph[i][j] += 1;
                                } else {
                                    count++;
                                }
                            }
                        }
                        if (count > 0 && graph[i][j] > count) {
                            for (List<ComplexTypeMetadata> cycle : cycles) {
                                int mainIndex = cycle.indexOf(complexTypeMetadata);
                                int nextIndex = cycle.indexOf(nextComplexTypeMetadata);
                                if (mainIndex >= 0 && nextIndex >= 0) {
                                    if (mainIndex > nextIndex) {
                                        cycle.remove(complexTypeMetadata);
                                        cycle.add(nextIndex, complexTypeMetadata);
                                    }
                                }
                            }
                        }
                    }
                }
                //Re-sort
                /**
                 * for below chain,
                 *  [SE]->[G]->[EP]->[EA]->[C]
                 *  [SC]->[I]->[EP]->[EA]->[C]
                 * it need to merge to one chain and keep origin order:
                 *  [SE]->[G]->[SC]->[I]->[EP]->[EA]->[C]
                 */
                LinkedList<ComplexTypeMetadata> ordersList = new LinkedList<>();
                if (cycles.size() >= 1) {
                    ordersList.addAll(cycles.get(0));
                }
                for (int i = 1; i < cycles.size(); i++) {
                    addResultList(ordersList, cycles.get(i));
                }
                sortedTypes.addAll(ordersList);
                break;
            default:
                throw new NotImplementedException("Sort '" + sortType + "' is not implemented.");
            }
        }
        return sortedTypes;
    }

    private static void addDepencyToCycleList(SortType sortType, List<ComplexTypeMetadata> types, int lineNumber,
            List<List<ComplexTypeMetadata>> cycles, List<ComplexTypeMetadata> dependencyPath) {
        if (dependencyPath.size() >= 1) {
            if (sortType == SortType.STRICT) {
                dependencyPath.add(getType(types, lineNumber)); // Include cycle start to get a better exception  message.
            }
            if (!listExistsInAnotherList(cycles, dependencyPath)) {
                cycles.add(dependencyPath);
            }
        }
    }

    private static boolean equalsTwoArray(byte[][] byte1, byte[][] byte2) {
        if (byte1 == null && byte2 == null) {
            return true;
        }
        if ((byte1 == null && byte2 != null) || (byte1 != null && byte2 == null)) {
            return false;
        }
        if (byte1.length != byte2.length) {
            return false;
        }
        for (int i = 0; i < byte1.length; i++) {
            if (byte1[i].length != byte2[i].length) {
                return false;
            }
            for (int j = 0; j < byte1[i].length; j++) {
                if (byte1[i][j] != byte2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static byte[][] deepCopyArray(byte[][] source) {
        byte[][] target = new byte[source.length][];
        for (int i = 0; i < source.length; i++) {
            target[i] = ArrayUtils.clone(source[i]);
        }
        return target;
    }

    private static boolean listExistsInAnotherList(List<List<ComplexTypeMetadata>> sourceList,
            List<ComplexTypeMetadata> checkList) {
        if (checkList == null || checkList.isEmpty()) {
            return false;
        }
        if (sourceList == null || sourceList.isEmpty()) {
            return false;
        }
        for (List<ComplexTypeMetadata> source : sourceList) {
            if (source.equals(checkList)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasIncomingEdgesExceptSelf(byte[][] graph) {
        for (int i = 0; i < graph.length;i++){
            for (int j = 0; j < graph[i].length;j++){
                byte column = graph[i][j];
                if(column > 0 && i != j){
                    return true;
                }
            }
        }
        return false;
    }

    private static void addResultList(LinkedList<ComplexTypeMetadata> ordersList, List<ComplexTypeMetadata> cycleList) {
        int start = 0;
        int end = 0;
        boolean exists = false;
        boolean hasNoContains = false;
        List<ComplexTypeMetadata> copyCycleList = new LinkedList<>(cycleList);
        ComplexTypeMetadata complexTypeMetadata = null;
        for (int j = 0; j < cycleList.size(); j++) {
            complexTypeMetadata = cycleList.get(j);
            if (!hasNoContains) {
                start = j;
                hasNoContains = !ordersList.contains(complexTypeMetadata);
            }
            exists = ordersList.contains(complexTypeMetadata);
            if (exists && hasNoContains) {
                end = cycleList.indexOf(complexTypeMetadata);
                copyCycleList.remove(complexTypeMetadata);
                break;
            } else if (exists) {
                copyCycleList.remove(complexTypeMetadata);
            }
        }
        if (exists && hasNoContains) {
            ordersList.addAll(ordersList.indexOf(complexTypeMetadata), cycleList.subList(start, end));
            copyCycleList.removeAll(cycleList.subList(start, end));
        }
        if (!exists && hasNoContains) {
            ordersList.add(complexTypeMetadata);
            copyCycleList.remove(complexTypeMetadata);
        }
        if (!copyCycleList.isEmpty()) {
            addResultList(ordersList, copyCycleList);
        }
    }

    private static StringBuilder logDependencyMatrix(byte[][] dependencyGraph) {
        StringBuilder builder = new StringBuilder();
        builder.append("Dependency matrix").append('\n');
        int maxSpace = getNumberLength(dependencyGraph.length);
        for (int i = 0; i <= maxSpace; i++) {
            builder.append(' ');
        }
        for (int i = 0; i < dependencyGraph.length; i++) {
            builder.append(i);
            for (int j = 0; j <= maxSpace - getNumberLength(i); j++) {
                builder.append(' ');
            }
        }
        builder.append('\n');
        int line = 0;
        for (byte[] lineContent : dependencyGraph) {
            builder.append(line);
            for (int j = 0; j <= maxSpace - getNumberLength(line); j++) {
                builder.append(' ');
            }
            for (byte b : lineContent) {
                builder.append(b);
                for (int j = 0; j <= maxSpace - getNumberLength(b); j++) {
                    builder.append(' ');
                }
            }
            builder.append('\n');
            line++;
        }
        return builder;
    }

    private static int getNumberLength(int number) {
        int length = 1;
        while (number >= 10) {
            number %= 10;
            length++;
        }
        return length;
    }

    // internal method for sortTypes
    private static ComplexTypeMetadata getType(List<ComplexTypeMetadata> types, int lineNumber) {
        return types.get(lineNumber);
    }

    // internal method for sortTypes
    private static boolean hasIncomingEdges(byte[] line) {
        for (byte column : line) {
            if (column > 0) {
                return true;
            }
        }
        return false;
    }

    // internal method for sortTypes
    private static int getId(ComplexTypeMetadata type, List<ComplexTypeMetadata> types) {
        if (type instanceof ContainedComplexTypeMetadata) {
            type = type.getEntity();
        }
        if (!types.contains(type)) {
            types.add(type);
        }
        return types.indexOf(type);
    }

    /**
     * Counts how many times a type (usually a reusable type) is used throughout the data model.
     *
     * @param type A data model type.
     * @return For entity types, this method returns 0. For reusable types, return a number greater or equals to 0.
     */
    public static int countEntityUsageCount(ComplexTypeMetadata type) {
        int usageCount = 0;
        for (ComplexTypeMetadata usage : type.getUsages()) {
            FieldMetadata container = usage.getContainer();
            if (container != null) {
                ComplexTypeMetadata containingType = container.getContainingType();
                ComplexTypeMetadata entity = containingType.getEntity();
                if (entity.isInstantiable()) {
                    usageCount++;
                } else if (!type.equals(entity)) {
                    // In case the non instance type is used in an entity.
                    usageCount += countEntityUsageCount(entity);
                }
            } else {
                usageCount++;
            }
        }
        return usageCount;
    }

    /**
     * Checks whether provided <code>field</code> is using a primitive type (i.e. a XSD datatype) or not.
     * @param field A {@link org.talend.mdm.commmon.metadata.FieldMetadata field}.
     * @return <code>true</code> if field's type is a XSD datatype, <code>false</code> otherwise.
     */
    public static boolean isPrimitiveTypeField(FieldMetadata field) {
        TypeMetadata fieldType = getSuperConcreteType(field.getType());
        return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(fieldType.getNamespace());
    }

    /**
     * Check where provided <code>type</code> is an Anonymous type or not
     * @param type
     * @return
     */
    public static boolean isAnonymousType(ComplexTypeMetadata type) {
        return type.getName().startsWith("X_ANONYMOUS"); //$NON-NLS-1$
    }
}

