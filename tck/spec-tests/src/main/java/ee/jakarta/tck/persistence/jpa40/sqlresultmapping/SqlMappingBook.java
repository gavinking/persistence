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

package ee.jakarta.tck.persistence.jpa40.sqlresultmapping;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;

/**
 * Named SQL result set mapping carried by this entity class for use in
 * {@link ee.jakarta.tck.persistence.jpa40.sqlresultmapping.Jpa40ResultSetMappingMetadataClient}.
 */
@Entity(name = "Jpa40SqlMappingBook")
@Table(name = "JPA40_SQL_BOOK")
@SqlResultSetMapping(
        name = SqlMappingBook.MAPPING_NAME,
        classes = @ConstructorResult(
                targetClass = SqlMappingDto.class,
                columns = {
                        @ColumnResult(name = "BOOK_ID", type = Integer.class),
                        @ColumnResult(name = "BOOK_TITLE", type = String.class)
                }))
public class SqlMappingBook {

    public static final String MAPPING_NAME = "Jpa40SqlMappingBook.dto";

    @Id
    private Integer id;

    private String title;

    public SqlMappingBook() {
    }

    public SqlMappingBook(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
