/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.examples.testsupport.dbquery;

import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;

/**
 * Contract of a test EJB which exposes generic database
 * query operations directly via the {@link EntityManager}.
 * Used in validating pre- and postconditions during testing.
 * All methods will be executed in an existing Transaction, which
 * is {@link TransactionAttributeType#MANDATORY}. 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface DbQueryLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Delegates to {@link EntityManager#find(Class, Object)}
    * 
    * @return
    * @throws IllegalArgumentException If either argument was not specified
    */
   <T> T find(Class<T> type, Object id) throws IllegalArgumentException;
}
