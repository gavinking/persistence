/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * Applied to a persistent field or property of an entity 
 * class or mapped superclass to denote a composite primary 
 * key that is an embeddable class. The embeddable class 
 * must be annotated as {@link Embeddable}. 
 *
 * <p> There must be only one {@code EmbeddedId} annotation and
 * no {@code Id} annotation when the {@code EmbeddedId} annotation is used.
 *
 * <p> The {@link AttributeOverride} annotation may be used to override
 * the column mappings declared within the embeddable class.
 * 
 * <p> The {@link MapsId} annotation may be used in conjunction
 * with the {@code EmbeddedId} annotation to specify a derived
 * primary key.
 *
 * <p> If the entity has a derived primary key, the
 * {@code AttributeOverride} annotation may only be used to
 * override those attributes of the embedded id that do not correspond
 * to the relationship to the parent entity.
 *
 * <p> Relationship mappings defined within an embedded id class are not supported.
 *
 * <pre>
 *    Example 1:
 *
 *    &#064;EmbeddedId
 *    protected EmployeePK empPK;
 *
 *
 *    Example 2:
 *
 *    &#064;Embeddable
 *    public class DependentId {
 *       String name;
 *       EmployeeId empPK;   // corresponds to primary key type of Employee
 *    }
 *
 *    &#064;Entity
 *    public class Dependent {
 *       // default column name for "name" attribute is overridden
 *       &#064;AttributeOverride(name="name", &#064;Column(name="dep_name"))
 *       &#064;EmbeddedId DependentId id;
 *       ...
 *       &#064;MapsId("empPK")
 *       &#064;ManyToOne Employee emp;
 *    }
 * </pre>
 *
 * @see Embeddable
 * @see MapsId
 *
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface EmbeddedId {}
