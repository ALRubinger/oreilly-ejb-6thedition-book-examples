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
package org.jboss.ejb3.examples.testsupport.dbinit;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Base support for prepopulating the database with
 * some default data.  Also permits
 * refreshing the DB with default state via 
 * {@link DbInitializerLocalBusiness#refreshWithDefaultData()}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class DbInitializerBeanBase implements DbInitializerLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   protected static final Logger log = Logger.getLogger(DbInitializerBeanBase.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Hook to interact w/ the database via JPA
    */
   @PersistenceContext
   protected EntityManager em;

   /**
    * Because @PostConstruct runs in an unspecified
    * Tx context (as invoked by the container), we'll
    * make one via this manager.  For EJBs that use
    * TransactionManagementType.BEAN, this is the hook
    * we use to programmatically demarcate transactional
    * boundaries.
    */
   @Resource(mappedName = "java:jboss/TransactionManager")
   private TransactionManager txManager;

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public abstract void populateDefaultData() throws Exception;

   public abstract void cleanup() throws Exception;

   /**
    * Called by the container on startup; populates the database with test data.
    * Because EJB lifecycle operations are invoked outside of a 
    * transactional context, we manually demarcate the Tx boundaries
    * via the injected {@link TransactionManager}. 
    */
   @PostConstruct
   public void populateDatabase() throws Exception
   {
      // Get the current Tx (if we have one, we may have been invoked via 
      // "refreshWithDefaultData"
      final Transaction tx = txManager.getTransaction();
      final boolean startOurOwnTx = tx == null;
      // If we need to start our own Tx (ie. this was called by the container as @PostConstruct)
      if (startOurOwnTx)
      {
         // Start a Tx via the injected TransactionManager
         txManager.begin();
      }

      // Populate with default data
      try
      {
         this.populateDefaultData();
      }
      catch(final Throwable t)
      {
         txManager.setRollbackOnly();
      }
      finally
      {
         // Mark the end of the Tx if we started it; will trigger the EntityManager to flush
         // outgoing changes
         if (startOurOwnTx)
         {
            txManager.commit();
         }
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.testsupport.dbinit.DbInitializerLocalBusiness#refreshWithDefaultData()
    */
   @Override
   public void refreshWithDefaultData() throws Exception
   {
      // Start a Tx
      txManager.begin();
      try
      {
         // Cleanup
         this.cleanup();

         // Repopulate
         this.populateDatabase();

      }
      finally
      {
         txManager.commit();
      }

   }
}
