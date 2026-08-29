/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.persistence.jpa40.sqlresultmapping;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.sql.ResultSetMapping;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the Jakarta Persistence 4.0
 * {@link jakarta.persistence.EntityManagerFactory#getResultSetMappings(Class)}
 * factory method which exposes typed references to named
 * {@link jakarta.persistence.SqlResultSetMapping} declarations.
 */
public class Jpa40ResultSetMappingMetadataClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40ResultSetMappingMetadataClient.class.getPackageName();
        String[] classes = {
                packageName + ".SqlMappingBook",
                packageName + ".SqlMappingDetails",
                packageName + ".SqlMappingEmbeddedBook",
                packageName + ".SqlMappingDto"
        };
        return createDeploymentJar("jpa_jpa40_resultsetmapping_metadata.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        removeTestData();
        createTestData();
    }

    /**
     * Verifies that {@code getResultSetMappings(Class)} returns the named
     * {@link jakarta.persistence.SqlResultSetMapping} declared on
     * {@link SqlMappingBook} when queried with the exact mapped result type.
     */
    @Test
    public void getResultSetMappingsReturnsAnnotatedMappingsTest() {
        Map<String, ResultSetMapping<SqlMappingDto>> mappings =
                getEntityManagerFactory().getResultSetMappings(SqlMappingDto.class);

        assertNotNull(mappings);
        assertTrue(mappings.containsKey(SqlMappingBook.MAPPING_NAME),
                "Expected mapping '" + SqlMappingBook.MAPPING_NAME + "' in " + mappings.keySet());
    }

    /**
     * Verifies that {@code getResultSetMappings(Object.class)} returns a superset
     * that includes all mappings regardless of result type.
     */
    @Test
    public void getResultSetMappingsWithObjectClassReturnsAllTest() {
        Map<String, ResultSetMapping<Object>> allMappings =
                getEntityManagerFactory().getResultSetMappings(Object.class);
        Map<String, ResultSetMapping<SqlMappingDto>> dtoMappings =
                getEntityManagerFactory().getResultSetMappings(SqlMappingDto.class);

        assertTrue(allMappings.keySet().containsAll(dtoMappings.keySet()),
                "Object.class query must include all mappings returned by the specific type query");
    }

    /**
     * Verifies that a {@link ResultSetMapping} reference retrieved via
     * {@code getResultSetMappings} can be used directly in a native query to
     * produce correctly typed results.
     */
    @Test
    public void resultSetMappingFromFactoryUsedInNativeQueryTest() {
        Map<String, ResultSetMapping<SqlMappingDto>> mappings =
                getEntityManagerFactory().getResultSetMappings(SqlMappingDto.class);
        ResultSetMapping<SqlMappingDto> mapping = mappings.get(SqlMappingBook.MAPPING_NAME);
        assertNotNull(mapping, "Mapping must be registered in the factory");

        SqlMappingDto result = getEntityManager()
                .createNativeQuery(
                        "SELECT ID AS BOOK_ID, TITLE AS BOOK_TITLE FROM JPA40_SQL_BOOK WHERE ID = 1",
                        mapping)
                .getSingleResult();

        assertEquals(new SqlMappingDto(1, "Alpha"), result);
    }

    private void createTestData() {
        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().persist(new SqlMappingBook(1, "Alpha"));
        getEntityManager().persist(new SqlMappingBook(2, "Beta"));
        transaction.commit();
        getEntityManager().clear();
    }
}
