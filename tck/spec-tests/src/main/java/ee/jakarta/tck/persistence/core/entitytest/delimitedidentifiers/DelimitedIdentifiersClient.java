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

package ee.jakarta.tck.persistence.core.entitytest.delimitedidentifiers;

import ee.jakarta.tck.persistence.common.PMClientBase;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DelimitedIdentifiersClient extends PMClientBase {

	public JavaArchive createDeployment() throws Exception {
		String packageName = DelimitedIdentifiersClient.class.getPackageName();
		String[] classes = { DelimitedIdentifiers.class.getName() };
		String[] xmlFiles = { ORM_XML };
		// Global quoting must not affect the legacy tests containing unquoted native SQL.
		return createDeploymentJar("jpa_core_entitytest_delimitedidentifiers.jar", packageName, classes, xmlFiles);
	}

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		createDeployment();
		removeTestData();
	}

	/*
	 * @testName: delimitedIdentifiersTest
	 *
	 * @assertion_ids: PERSISTENCE:SPEC:1369.1; PERSISTENCE:SPEC:2208
	 *
	 * @test_Strategy: The delimited-identifiers element causes both table and
	 * column names to be quoted in generated SQL. Persist and reload an entity
	 * whose table and basic column names are SQL reserved words, so the generated
	 * SQL fails unless both identifiers are quoted.
	 */
	@Test
	public void delimitedIdentifiersTest() {
		DelimitedIdentifiers expected = new DelimitedIdentifiers(1, "quoted identifiers");

		getEntityTransaction().begin();
		getEntityManager().persist(expected);
		getEntityManager().flush();
		getEntityManager().clear();

		DelimitedIdentifiers actual = getEntityManager().find(DelimitedIdentifiers.class, expected.getId());
		assertNotNull(actual);
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getText(), actual.getText());
		getEntityTransaction().commit();
	}
}
