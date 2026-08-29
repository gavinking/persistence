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

package ee.jakarta.tck.persistence.jpa40.emflistener;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityListenerRegistration;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import jakarta.persistence.PostDelete;
import jakarta.persistence.PostInsert;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreDelete;
import jakarta.persistence.PreInsert;
import jakarta.persistence.Table;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Jpa40EntityManagerFactoryListenerClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40EntityManagerFactoryListenerClient.class.getPackageName();
        String[] classes = {packageName + ".FactoryListenerBook"};
        return createDeploymentJar("jpa_jpa40_emflistener.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        getEntityManager();
        removeTestData();
    }

    /**
     * Tests Jakarta Persistence 4.0 entity manager factory listener
     * registration. The test verifies {@code addListener()} receives a
     * lifecycle event for the requested entity and callback type and verifies
     * the returned {@link EntityListenerRegistration} can cancel the listener.
     */
    @Test
    public void entityManagerFactoryAddListenerTest() {
        List<Integer> observed = new ArrayList<>();
        EntityListenerRegistration registration = getEntityManagerFactory()
                .addListener(FactoryListenerBook.class, PostPersist.class,
                        book -> observed.add(book.getId()));

        persistBook(1, "Alpha");
        assertEquals(List.of(1), observed);

        registration.cancel();
        persistBook(2, "Beta");
        assertEquals(List.of(1), observed);
    }

    /**
     * Tests that {@link jakarta.persistence.EntityManagerFactory#addListener} with
     * new Jakarta Persistence 4.0 callback types ({@link PreInsert} and
     * {@link PostInsert}) receives events when an entity is inserted via
     * {@link EntityAgent}.
     */
    @Test
    public void addListenerForAgentInsertCallbacksTest() {
        List<String> observed = new ArrayList<>();
        EntityListenerRegistration preReg = getEntityManagerFactory()
                .addListener(FactoryListenerBook.class, PreInsert.class,
                        book -> observed.add("pre-insert:" + book.getId()));
        EntityListenerRegistration postReg = getEntityManagerFactory()
                .addListener(FactoryListenerBook.class, PostInsert.class,
                        book -> observed.add("post-insert:" + book.getId()));

        getEntityManagerFactory().runInTransaction(EntityAgent.class,
                agent -> agent.insert(new FactoryListenerBook(10, "Inserted")));

        preReg.cancel();
        postReg.cancel();

        assertTrue(observed.contains("pre-insert:10"), "PreInsert listener must be called");
        assertTrue(observed.contains("post-insert:10"), "PostInsert listener must be called");
        assertTrue(observed.indexOf("pre-insert:10") < observed.indexOf("post-insert:10"),
                "PreInsert must fire before PostInsert");
    }

    /**
     * Tests that {@link jakarta.persistence.EntityManagerFactory#addListener} with
     * {@link PreDelete} and {@link PostDelete} callbacks fires during an
     * {@link EntityAgent#delete} operation.
     */
    @Test
    public void addListenerForAgentDeleteCallbacksTest() {
        getEntityManagerFactory().runInTransaction(EntityAgent.class,
                agent -> agent.insert(new FactoryListenerBook(20, "ToDelete")));

        List<String> observed = new ArrayList<>();
        EntityListenerRegistration preReg = getEntityManagerFactory()
                .addListener(FactoryListenerBook.class, PreDelete.class,
                        book -> observed.add("pre-delete:" + book.getId()));
        EntityListenerRegistration postReg = getEntityManagerFactory()
                .addListener(FactoryListenerBook.class, PostDelete.class,
                        book -> observed.add("post-delete:" + book.getId()));

        getEntityManagerFactory().runInTransaction(EntityAgent.class, agent -> {
            FactoryListenerBook book = agent.get(FactoryListenerBook.class, 20);
            agent.delete(book);
        });

        preReg.cancel();
        postReg.cancel();

        assertTrue(observed.contains("pre-delete:20"), "PreDelete listener must be called");
        assertTrue(observed.contains("post-delete:20"), "PostDelete listener must be called");
    }

    /**
     * Tests that an {@link jakarta.persistence.EntityManagerFactory#addListener}
     * registration on {@code Object.class} receives events for any entity type.
     */
    @Test
    public void addListenerOnObjectSupertypeReceivesAllEntityEventsTest() {
        List<Object> observed = new ArrayList<>();
        EntityListenerRegistration registration = getEntityManagerFactory()
                .addListener(Object.class, PostPersist.class, observed::add);

        persistBook(30, "ForObject");
        persistBook(31, "AlsoForObject");

        registration.cancel();

        assertEquals(2, observed.size(),
                "Object.class supertype listener must receive PostPersist from all entity types");
    }

    /**
     * Verifies that passing a non-callback annotation class as {@code callbackType}
     * throws {@link IllegalArgumentException}.
     */
    @Test
    public void addListenerForUnrecognizedCallbackTypeThrowsIAETest() {
        assertThrows(IllegalArgumentException.class, () ->
                getEntityManagerFactory()
                        .addListener(FactoryListenerBook.class, Entity.class, book -> {}));
    }

    private void persistBook(Integer id, String title) {
        EntityTransaction transaction = getEntityTransaction();
        transaction.begin();
        getEntityManager().persist(new FactoryListenerBook(id, title));
        transaction.commit();
        getEntityManager().clear();
    }
}
