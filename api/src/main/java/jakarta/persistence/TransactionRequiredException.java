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

/**
 * Thrown by the persistence provider when a transaction is required but is
 * not active.
 * <p>
 * A {@code TransactionRequiredException} must be thrown by the persistence
 * provider when:
 * <ul>
 * <li>The {@link EntityManager#flush} operation is called on an entity
 *     manager with no active transaction context.
 * <li>The {@link EntityManager#joinTransaction} operation is called on
 *     an entity manager with no active transaction context.
 * <li>An entity lifecycle operation like {@link EntityManager#persist},
 *     {@link EntityManager#remove}, {@link EntityManager#merge}, or
 *     {@link EntityManager#refresh} is called on an entity manager with
 *     a {@linkplain PersistenceContextType#TRANSACTION transaction-scoped}
 *     persistence context with no active transaction context.
 * <li>The {@link EntityManager#lock} operation is called on an entity
 *     manager with no active transaction context.
 * <li>The {@link EntityManager#getLockMode} operation is called on an
 *     entity manager with no active transaction context, or on an entity
 *     manager that has not been joined to the current transaction.
 * <li>Any operation of {@code EntityManager} or {@code EntityAgent} which
 *     accepts a {@linkplain LockModeType lock mode} is called on a manager
 *     or agent with no active transaction context, and the given lock mode
 *     is not {@link LockModeType#NONE}.
 * <li>A JPQL bulk update or delete statement is
 *     {@linkplain Statement#execute executed} with no active transaction
 *     context.
 * <li>A JPQL query is executed with no active transaction context, and a
 *     {@linkplain LockModeType lock mode} other than {@link LockModeType#NONE}
 *     is specified via {@link TypedQuery#setLockMode setLockMode()},
 *     {@link jakarta.persistence.query.QueryOptions#lockMode}, or
 *     {@link NamedQuery#lockMode}.
 * </ul>
 * 
 * @since 1.0
 */
public class TransactionRequiredException extends PersistenceException {

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * {@code null} as its detail message.
	 */
	public TransactionRequiredException() {
		super();
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * {@code null} as its detail message.
	 */
	public TransactionRequiredException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * the specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public TransactionRequiredException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code TransactionRequiredException} exception with
	 * the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public TransactionRequiredException(String message, Exception cause) {
		super(message, cause);
	}
}
