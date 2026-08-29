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

package ee.jakarta.tck.persistence.jpa40.resultcount;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Jpa40ResultCountClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40ResultCountClient.class.getPackageName();
        String[] classes = {packageName + ".CountBook"};
        return createDeploymentJar("jpa_jpa40_resultcount.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        removeTestData();
        createTestData();
    }

    /**
     * Tests Jakarta Persistence 4.0 {@link TypedQuery#getResultCount()}. The
     * test verifies that a typed query can return the number of matching rows
     * independently of the current pagination settings.
     */
    @Test
    public void typedQueryGetResultCountTest() {
        TypedQuery<CountBook> query = getEntityManager()
                .createQuery("SELECT b FROM Jpa40CountBook b WHERE b.category = :category ORDER BY b.id",
                        CountBook.class)
                .setParameter("category", "fiction")
                .setMaxResults(1);

        assertEquals(2L, query.getResultCount());
        assertEquals(1, query.getResultList().size());
    }

    /**
     * Tests {@link jakarta.persistence.TypedQuery#getResultCount()} on a native
     * SQL typed query. The test verifies the total match count is returned
     * independently of the current pagination settings.
     */
    @Test
    public void nativeTypedQueryGetResultCountTest() {
        var query = getEntityManager()
                .createNativeQuery(
                        "SELECT * FROM JPA40_COUNT_BOOK WHERE CATEGORY = 'fiction' ORDER BY ID",
                        CountBook.class)
                .setMaxResults(1);

        assertEquals(2L, query.getResultCount(),
                "getResultCount() on a native TypedQuery must return the total match count, ignoring setMaxResults");
        assertEquals(1, query.getResultList().size(),
                "getResultList() must still respect setMaxResults");
    }

    private void createTestData() {
        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().persist(new CountBook(1, "Alpha", "fiction"));
        getEntityManager().persist(new CountBook(2, "Beta", "fiction"));
        getEntityManager().persist(new CountBook(3, "Gamma", "tech"));
        transaction.commit();
        getEntityManager().clear();
    }
}
