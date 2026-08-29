/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package ee.jakarta.tck.persistence.jpa40.queryflush;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Query;
import jakarta.persistence.QueryFlushMode;
import jakarta.persistence.Statement;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Jpa40QueryFlushClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40QueryFlushClient.class.getPackageName();
        String[] classes = {packageName + ".FlushBook"};
        return createDeploymentJar("jpa_jpa40_queryflush.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        getEntityManager();
        removeTestData();
        createTestData();
    }

    /**
     * Tests Jakarta Persistence 4.0 named query flush modes. The test verifies
     * the provider honors {@code flush = NO_FLUSH} by not synchronizing a
     * pending change before query execution and honors {@code flush = FLUSH} by
     * synchronizing a pending change before query execution.
     */
    @Test
    public void namedQueryFlushModeExecutionTest() {
        getEntityManager().setFlushMode(FlushModeType.EXPLICIT);

        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().find(FlushBook.class, 1).setTitle("NoFlush");
        Long unflushed = getEntityManager()
                .createNamedQuery(FlushBook.NO_FLUSH_QUERY, Long.class)
                .getSingleResult();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(0L, unflushed);

        transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().find(FlushBook.class, 1).setTitle("Flush");
        Long flushed = getEntityManager()
                .createNamedQuery(FlushBook.FLUSH_QUERY, Long.class)
                .getSingleResult();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(1L, flushed);
    }

    /**
     * Tests Jakarta Persistence 4.0 named native query flush modes. The test
     * verifies a native named query with {@code flush = FLUSH} synchronizes a
     * pending managed-entity change before the native SQL query is executed.
     */
    @Test
    public void namedNativeQueryFlushModeExecutionTest() {
        getEntityManager().setFlushMode(FlushModeType.EXPLICIT);

        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().find(FlushBook.class, 1).setTitle("NativeFlush");
        Long flushed = getEntityManager()
                .createNamedQuery(FlushBook.NATIVE_FLUSH_QUERY, Long.class)
                .getSingleResult();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(1L, flushed);
    }

    /**
     * Tests Jakarta Persistence 4.0 runtime query flush modes. The test
     * verifies {@link Query#setQueryFlushMode(QueryFlushMode)} affects whether
     * pending managed-entity changes are synchronized before query execution
     * when the entity manager itself uses {@link FlushModeType#EXPLICIT}.
     */
    @Test
    public void queryFlushModeApiTest() {
        getEntityManager().setFlushMode(FlushModeType.EXPLICIT);

        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().find(FlushBook.class, 1).setTitle("QueryNoFlush");
        Long unflushed = getEntityManager()
                .createQuery("SELECT COUNT(b) FROM Jpa40FlushBook b WHERE b.title = 'QueryNoFlush'", Long.class)
                .setQueryFlushMode(QueryFlushMode.NO_FLUSH)
                .getSingleResult();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(0L, unflushed);

        transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().find(FlushBook.class, 1).setTitle("QueryFlush");
        Long flushed = getEntityManager()
                .createQuery("SELECT COUNT(b) FROM Jpa40FlushBook b WHERE b.title = 'QueryFlush'", Long.class)
                .setQueryFlushMode(QueryFlushMode.FLUSH)
                .getSingleResult();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(1L, flushed);
    }

    /**
     * Tests that {@link jakarta.persistence.QueryFlushMode#FLUSH} on a
     * {@link jakarta.persistence.Statement} causes pending managed-entity changes
     * to be synchronised before the bulk statement is executed, while
     * {@link jakarta.persistence.QueryFlushMode#NO_FLUSH} suppresses that
     * synchronisation.
     */
    @Test
    public void statementQueryFlushModeTest() {
        // NO_FLUSH: the pending title change must not be visible to the bulk UPDATE
        getEntityManager().setFlushMode(FlushModeType.EXPLICIT);

        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        FlushBook book = getEntityManager().find(FlushBook.class, 1);
        book.setTitle("PendingNoFlush");

        // Bulk UPDATE matches only the new title — if NOT flushed, count = 0
        int countNoFlush = getEntityManager()
                .createStatement("UPDATE Jpa40FlushBook b SET b.title = 'BulkUpdated' WHERE b.title = 'PendingNoFlush'")
                .setQueryFlushMode(QueryFlushMode.NO_FLUSH)
                .execute();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(0, countNoFlush, "Statement with NO_FLUSH must not see unflushed pending changes");

        // FLUSH: the pending title change must be visible to the bulk UPDATE
        transaction = getEntityTransaction();
        transaction.begin();
        FlushBook book2 = getEntityManager().find(FlushBook.class, 1);
        book2.setTitle("PendingFlush");

        int countFlush = getEntityManager()
                .createStatement("UPDATE Jpa40FlushBook b SET b.title = 'BulkUpdated' WHERE b.title = 'PendingFlush'")
                .setQueryFlushMode(QueryFlushMode.FLUSH)
                .execute();
        transaction.rollback();
        getEntityManager().clear();

        assertEquals(1, countFlush, "Statement with FLUSH must synchronise pending changes before executing");
    }

    private void createTestData() {
        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().persist(new FlushBook(1, "Initial"));
        transaction.commit();
        getEntityManager().clear();
    }
}
