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

import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PostCreate;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PreClose;

@EntityListener
public class PersistenceUnitLifecycleListener {

    public PersistenceUnitLifecycleListener() {
    }

    @PostLoad
    public void postLoad(FactoryListenerBook book) {
    }

    @PostCreate
    public void entityManagerFactoryPostCreate(EntityManagerFactory factory) {
        factory.getName();
        PersistenceUnitLifecycleEventLog.record("factory-post-create");
    }

    @PreClose
    public void entityManagerFactoryPreClose(EntityManagerFactory factory) {
        factory.getName();
        PersistenceUnitLifecycleEventLog.record("factory-pre-close");
    }

    @PostCreate
    public void entityManagerPostCreate(EntityManager manager) {
        manager.isOpen();
        PersistenceUnitLifecycleEventLog.record("manager-post-create");
    }

    @PreClose
    public void entityManagerPreClose(EntityManager manager) {
        manager.isOpen();
        PersistenceUnitLifecycleEventLog.record("manager-pre-close");
    }

    @PostCreate
    public void entityAgentPostCreate(EntityAgent agent) {
        agent.isOpen();
        PersistenceUnitLifecycleEventLog.record("agent-post-create");
    }

    @PreClose
    public void entityAgentPreClose(EntityAgent agent) {
        agent.isOpen();
        PersistenceUnitLifecycleEventLog.record("agent-pre-close");
    }
}
