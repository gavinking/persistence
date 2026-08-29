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

package ee.jakarta.tck.persistence.jpa40.options;

import ee.jakarta.tck.persistence.common.PMClientBase;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Jakarta Persistence 4.0 {@link EntityManager#addOption} and
 * {@link EntityManager#getOptions} API.
 */
public class Jpa40EntityManagerOptionsClient extends PMClientBase {

    public JavaArchive createDeployment() throws Exception {
        String packageName = Jpa40EntityManagerOptionsClient.class.getPackageName();
        String[] classes = {packageName + ".OptionsBook"};
        return createDeploymentJar("jpa_jpa40_options.jar", packageName, classes);
    }

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        createDeployment();
        getEntityManager();
    }

    /**
     * Verifies {@link EntityManager#addOption} with cache modes: the option is
     * reflected in {@link EntityManager#getOptions}, and adding a new option of
     * the same type overwrites the previous value (same-type overwrite rule).
     */
    @Test
    public void entityManagerAddOptionTest() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.addOption(CacheRetrieveMode.BYPASS);
            em.addOption(CacheStoreMode.BYPASS);

            Set<EntityManager.Option> options = em.getOptions();
            assertTrue(options.contains(CacheRetrieveMode.BYPASS));
            assertTrue(options.contains(CacheStoreMode.BYPASS));

            // same-type overwrite: adding USE replaces BYPASS for retrieve mode
            em.addOption(CacheRetrieveMode.USE);
            assertTrue(em.getOptions().contains(CacheRetrieveMode.USE));
            assertFalse(em.getOptions().contains(CacheRetrieveMode.BYPASS));
            // store mode unchanged
            assertTrue(em.getOptions().contains(CacheStoreMode.BYPASS));
        } finally {
            em.close();
        }
    }

    /**
     * Verifies that the set returned by {@link EntityManager#getOptions} is a
     * defensive copy: mutating it does not affect the options held by the entity
     * manager.
     */
    @Test
    public void entityManagerOptionsSetIsDefensiveCopyTest() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.addOption(CacheRetrieveMode.BYPASS);
            em.addOption(CacheStoreMode.BYPASS);

            Set<EntityManager.Option> snapshot = em.getOptions();
            snapshot.clear();

            // entity manager options must be unchanged after clearing the copy
            Set<EntityManager.Option> fresh = em.getOptions();
            assertTrue(fresh.contains(CacheRetrieveMode.BYPASS));
            assertTrue(fresh.contains(CacheStoreMode.BYPASS));
        } finally {
            em.close();
        }
    }

    /**
     * Verifies that {@link FlushModeType} acts as an {@link EntityManager.Option}:
     * setting it via {@link EntityManager#addOption} is reflected by both
     * {@link EntityManager#getFlushMode()} and {@link EntityManager#getOptions()}.
     */
    @Test
    public void entityManagerFlushModeAsOptionTest() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.addOption(FlushModeType.COMMIT);

            assertEquals(FlushModeType.COMMIT, em.getFlushMode());
            assertTrue(em.getOptions().contains(FlushModeType.COMMIT));
        } finally {
            em.close();
        }
    }

    /**
     * Verifies that cache modes set via the dedicated setters
     * ({@link EntityManager#setCacheRetrieveMode} and
     * {@link EntityManager#setCacheStoreMode}) also appear in
     * {@link EntityManager#getOptions()}.
     */
    @Test
    public void entityManagerCacheModesViaSetterAppearsInOptionsTest() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.setCacheRetrieveMode(CacheRetrieveMode.BYPASS);
            em.setCacheStoreMode(CacheStoreMode.REFRESH);

            assertTrue(em.getOptions().contains(CacheRetrieveMode.BYPASS));
            assertTrue(em.getOptions().contains(CacheStoreMode.REFRESH));

            assertEquals(CacheRetrieveMode.BYPASS, em.getCacheRetrieveMode());
            assertEquals(CacheStoreMode.REFRESH, em.getCacheStoreMode());
        } finally {
            em.close();
        }
    }
}
