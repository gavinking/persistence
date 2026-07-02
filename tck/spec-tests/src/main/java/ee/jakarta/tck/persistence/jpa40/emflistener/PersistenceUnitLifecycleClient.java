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

package ee.jakarta.tck.persistence.jpa40.emflistener;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistenceUnitLifecycleClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = PersistenceUnitLifecycleClient.class.getPackageName();
        String[] classes = {
                packageName + ".FactoryListenerBook",
                packageName + ".PersistenceUnitLifecycleEventLog",
                packageName + ".PersistenceUnitLifecycleListener"};
        return createDeploymentJar("jpa_jpa40_persistenceunitlifecycle.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        getEntityManager();
        removeTestData();
        PersistenceUnitLifecycleEventLog.reset();
    }

    /**
     * Tests Jakarta Persistence 4.0 persistence unit lifecycle callbacks for
     * an {@link EntityManagerFactory}. The test verifies that
     * {@code @PostCreate} is invoked when a factory is created and
     * {@code @PreClose} is invoked when it is closed.
     */
    @Test
    public void entityManagerFactoryLifecycleCallbacksTest() {
        Map<String, Object> properties = new HashMap<>();
        getPersistenceUnitProperties().forEach((key, value) -> properties.put((String) key, value));
        properties.put(JAKARTA_SCHEMAGEN_DATABASE_ACTION, "none");

        PersistenceUnitLifecycleEventLog.reset();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
        try {
            List<String> events = PersistenceUnitLifecycleEventLog.events();
            assertEventRecorded(events, "factory-post-create");
            assertEventNotRecorded(events, "factory-pre-close");
        } finally {
            if (emf.isOpen()) {
                emf.close();
            }
        }

        List<String> events = PersistenceUnitLifecycleEventLog.events();
        assertEventRecorded(events, "factory-pre-close");
        assertEventOrder(events, "factory-post-create", "factory-pre-close");
    }

    /**
     * Tests Jakarta Persistence 4.0 persistence unit lifecycle callbacks for
     * an application-managed {@link EntityManager}. The test verifies that
     * {@code @PostCreate} is invoked when a manager is created and
     * {@code @PreClose} is invoked when it is closed.
     */
    @Test
    public void entityManagerLifecycleCallbacksTest() {
        EntityManager manager = getEntityManagerFactory().createEntityManager();
        try {
            List<String> events = PersistenceUnitLifecycleEventLog.events();
            assertEventRecorded(events, "manager-post-create");
            assertEventNotRecorded(events, "manager-pre-close");
        } finally {
            if (manager.isOpen()) {
                manager.close();
            }
        }

        List<String> events = PersistenceUnitLifecycleEventLog.events();
        assertEventRecorded(events, "manager-pre-close");
        assertEventOrder(events, "manager-post-create", "manager-pre-close");
    }

    /**
     * Tests Jakarta Persistence 4.0 persistence unit lifecycle callbacks for
     * an application-managed {@link EntityAgent}. The test verifies that
     * {@code @PostCreate} is invoked when an agent is created and
     * {@code @PreClose} is invoked when it is closed.
     */
    @Test
    public void entityAgentLifecycleCallbacksTest() {
        EntityAgent agent = getEntityManagerFactory().createEntityAgent();
        try {
            List<String> events = PersistenceUnitLifecycleEventLog.events();
            assertEventRecorded(events, "agent-post-create");
            assertEventNotRecorded(events, "agent-pre-close");
        } finally {
            if (agent.isOpen()) {
                agent.close();
            }
        }

        List<String> events = PersistenceUnitLifecycleEventLog.events();
        assertEventRecorded(events, "agent-pre-close");
        assertEventOrder(events, "agent-post-create", "agent-pre-close");
    }

    private void assertEventOrder(List<String> events, String first, String second) {
        assertEventRecorded(events, first);
        assertEventRecorded(events, second);
        assertTrue(events.indexOf(first) < events.indexOf(second),
                () -> "Expected " + first + " before " + second + " in " + events);
    }

    private void assertEventRecorded(List<String> events, String event) {
        assertTrue(events.contains(event), () -> "Expected event " + event + " in " + events);
    }

    private void assertEventNotRecorded(List<String> events, String event) {
        assertFalse(events.contains(event), () -> "Unexpected event " + event + " in " + events);
    }
}
