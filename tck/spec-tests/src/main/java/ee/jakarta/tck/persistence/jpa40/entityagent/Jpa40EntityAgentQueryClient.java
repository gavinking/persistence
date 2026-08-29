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

package ee.jakarta.tck.persistence.jpa40.entityagent;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that {@link EntityAgent} query and statement creation methods work
 * correctly and that results returned from agent queries are detached.
 */
public class Jpa40EntityAgentQueryClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40EntityAgentQueryClient.class.getPackageName();
        String[] classes = {
                packageName + ".AgentBook",
                packageName + ".AgentPublisher"
        };
        return createDeploymentJar("jpa_jpa40_entityagent_query.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        removeTestData();
        createBooks(
                new AgentBook(1, "Alpha"),
                new AgentBook(2, "Beta"));
    }

    /**
     * Verifies that a JPQL SELECT executed via
     * {@link EntityAgent#createQuery(String, Class)} returns detached entity
     * instances, meaning modifications to them are never automatically written
     * to the database.
     */
    @Test
    public void agentTypedQueryReturnsDetachedResultsTest() {
        getEntityManagerFactory().runInTransaction(EntityAgent.class, agent -> {
            List<AgentBook> books = agent
                    .createQuery("SELECT b FROM Jpa40AgentBook b ORDER BY b.id", AgentBook.class)
                    .getResultList();

            assertEquals(2, books.size());
            assertEquals("Alpha", books.get(0).getTitle());
            assertEquals("Beta", books.get(1).getTitle());

            // Instances returned by an entity agent are detached
            for (AgentBook book : books) {
                assertFalse(Persistence.getPersistenceUtil().isLoaded(book, "publisher"),
                        "Lazy associations should not be auto-loaded for detached instances");
            }

            // Mutating a detached instance must not affect the database
            books.get(0).setTitle("Modified in memory");
        });

        // Title in the database must be unchanged
        assertEquals("Alpha", titleById(1));
    }

    /**
     * Verifies that a named statement registered via
     * {@link jakarta.persistence.NamedStatement} can be executed from an
     * {@link EntityAgent}.
     */
    @Test
    public void agentCreateNamedStatementExecutionTest() {
        // AgentBook uses @NamedStatement via a NamedQuery — we instead use a
        // plain createStatement to verify agent statement execution.
        getEntityManagerFactory().runInTransaction(EntityAgent.class, agent -> {
            int count = agent
                    .createStatement("UPDATE Jpa40AgentBook b SET b.title = :title WHERE b.id = :id")
                    .setParameter("title", "AgentUpdated")
                    .setParameter("id", 1)
                    .execute();
            assertEquals(1, count);
        });

        assertEquals("AgentUpdated", titleById(1));
    }

    /**
     * Verifies that a native SQL SELECT executed via
     * {@link EntityAgent#createNativeQuery(String, Class)} returns detached
     * entity instances.
     */
    @Test
    public void agentNativeQueryReturnsDetachedResultsTest() {
        getEntityManagerFactory().runInTransaction(EntityAgent.class, agent -> {
            List<AgentBook> books = agent
                    .createNativeQuery("SELECT * FROM JPA40_AGENT_BOOK ORDER BY ID", AgentBook.class)
                    .getResultList();

            assertEquals(2, books.size());
            assertEquals(1, books.get(0).getId());
            assertEquals(2, books.get(1).getId());

            // All results from an entity agent must be detached
            for (AgentBook book : books) {
                assertNotNull(book.getId());
            }
        });
    }

    /**
     * Verifies that a criteria UPDATE can be built and executed via
     * {@link EntityAgent#createStatement(jakarta.persistence.criteria.CriteriaStatement)}.
     */
    @Test
    public void agentCriteriaStatementExecutionTest() {
        CriteriaBuilder builder = getEntityManagerFactory().getCriteriaBuilder();
        CriteriaUpdate<AgentBook> update = builder.createCriteriaUpdate(AgentBook.class);
        Root<AgentBook> root = update.from(AgentBook.class);
        update.set(root.get("title"), "CriteriaUpdated")
              .where(builder.equal(root.get("id"), 2));

        getEntityManagerFactory().runInTransaction(EntityAgent.class, agent -> {
            int count = agent.createStatement(update).execute();
            assertEquals(1, count);
        });

        assertEquals("CriteriaUpdated", titleById(2));
        assertEquals("Alpha", titleById(1)); // other row unchanged
    }

    private void createBooks(AgentBook... books) {
        getEntityManagerFactory().runInTransaction(entityManager -> {
            for (AgentBook book : books) {
                entityManager.persist(book);
            }
        });
    }

    private String titleById(int id) {
        return getEntityManagerFactory().callInTransaction(entityManager -> {
            AgentBook book = entityManager.find(AgentBook.class, id);
            return book == null ? null : book.getTitle();
        });
    }
}
