/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0


package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the mapping for the key column of a map whose map key
 * is a basic type. If the {@link #name} element is not specified,
 * it defaults to the concatenation of the following: the name of
 * the referencing relationship field or property; "{@code _}";
 * "{@code KEY}".
 *
 * <p>Example:
 * <pre>
 *    &#064;Entity
 *    public class Item {
 *       &#064;Id int id;
 *       ...
 *       &#064;ElementCollection
 *       &#064;MapKeyColumn(name="IMAGE_NAME")
 *       &#064;Column(name="IMAGE_FILENAME")
 *       &#064;CollectionTable(name="IMAGE_MAPPING")
 *       Map&#060;String, String&#062; images;  // map from image name to filename
 *       ...
 *    } 
 * </pre>
 * @since 2.0
 */
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapKeyColumn {

    /**
     * (Optional) The name of the map key column. The table in which it is found
     * depends upon the context. If the map key is for an element collection,
     * the map key column is in the collection table for the map value. If the
     * map key is for a ManyToMany entity relationship or for a OneToMany entity
     * relationship using a join table, the map key column is in a join table.
     * If the map key is for a OneToMany entity relationship using a foreign key
     * mapping strategy, the map key column is in the table of the entity that
     * is the value of the map.
     * <p> Defaults to the concatenation of the following: the name of
     * the referencing relationship field or property; "_"; "{@code KEY}".
     */
    String name() default "";

    /**
     * (Optional) Whether the column is a unique key. This is a
     * shortcut for the {@code UniqueConstraint} annotation
     * at the table level and is useful for when the unique key
     * constraint corresponds to only a single column. This
     * constraint applies in addition to any constraint entailed
     * by primary key mapping and to constraints specified at the
     * table level.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the database column is nullable.
     */
    boolean nullable() default false;

    /**
     * (Optional) Whether the column is included in SQL INSERT
     * statements generated by the persistence provider.
     */
    boolean insertable() default true;

    /**
     * (Optional) Whether the column is included in SQL UPDATE
     * statements generated by the persistence provider.
     */
    boolean updatable() default true;

    /**
     * (Optional) The SQL fragment that is used when generating
     * the DDL for the column.
     * <p> Defaults to the generated SQL to create a column of
     * the inferred type.
     */
    String columnDefinition() default "";

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * which declares this column. May not be used in
     * conjunction with {@link #columnDefinition()}.
     *
     * @since 3.2
     */
    String options() default "";

    /** (Optional) The name of the table that contains the column. 
     * <p> Defaults: If the map key is for an element collection,
     * the name of the collection table for the map value. If the
     * map key is for a {@link OneToMany} or {@link ManyToMany}
     * entity relationship using a join table, the name of the
     * join table for the map. If the map key is for a
     * {@link OneToMany} entity relationship using a foreign key
     * mapping strategy, the name of the primary table of the entity
     * that is the value of the map.
     */
    String table() default "";

    /**
     * (Optional) The column length.
     * (Applies only if a string-valued column is used.)
     */
    int length() default 255;

    /**
     * (Optional) The precision for a decimal (exact numeric) column.
     * (Applies only if a decimal column is used.)
     *
     *<p> Default: 0. (The value must be set by the developer.)
     */
    int precision() default 0; // decimal precision

    /**
     * (Optional) The scale for a decimal (exact numeric) column.
     * (Applies only if a decimal column is used.)
     */
    int scale() default 0; // decimal scale
}
