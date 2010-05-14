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
package org.jboss.ejb3.examples.chxx.transactions.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.LocalBinding;

/**
 * 
 * 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(DbQueryLocalBusiness.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
// We make a current Tx mandatory from the caller so that the 
// caller is sure to get back an entity instance which is still attached.  This way we can run any tests/checks
// on it without fear that we'll run into Exceptions.  We can use a TxWrappingBean to submit the test logic and
// execute it in the context of a new Transaction.  David Blevins has a writeup of this technique: 
// http://openejb.apache.org/3.0/testing-transactions-example.html
@LocalBinding(jndiBinding = DbQueryLocalBusiness.JNDI_NAME)
public class DbQueryBean implements DbQueryLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Underlying hook to persistence
    */
   @PersistenceContext
   private EntityManager em;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @see org.jboss.ejb3.examples.chxx.transactions.ejb.DbQueryLocalBusiness#find(java.lang.Class, java.lang.Object)
    */
   @Override
   public <T> T find(final Class<T> type, final Object id) throws IllegalArgumentException
   {
      // Precondition checks
      if (type == null)
      {
         throw new IllegalArgumentException("type must be specified");
      }
      if (id == null)
      {
         throw new IllegalArgumentException("id must be specified");
      }

      // Find
      return em.find(type, id);
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
}
