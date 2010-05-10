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
package org.jboss.ejb3.examples.chxx.transactions;

import java.math.BigDecimal;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunMode;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.entity.User;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing dev only 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@RunMode(RunModeType.LOCAL)
public class SimpleDeploymentIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(SimpleDeploymentIntegrationTest.class.getName());

   /**
    * Naming Context
    */
   private static Context jndiContext;

   /**
    * The Deployment into the EJB Container
    */
   @Deployment
   public static JavaArchive getDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create("test.jar", JavaArchive.class).addPackages(true,
            BankLocalBusiness.class.getPackage(), User.class.getPackage()).addManifestResource("persistence.xml")
            .addClass(DbInitializerBean.class);
      log.info(archive.toString(true));
      return archive;
   }

   /**
    * Test-only DB initializer to sanitize and prepopulate the DB with each test run
    */
   // TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
   private static DbInitializerLocalBusiness dbInitializer;

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /*
    * TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
    */

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Performs suite-wide initialization
    */
   @BeforeClass
   public static void init() throws Exception
   {
      // After the server is up, we don't need to pass any explicit properties
      jndiContext = new InitialContext();

      // Lookup the Singleton initializer and store a reference
      dbInitializer = (DbInitializerLocalBusiness) jndiContext.lookup(DbInitializerLocalBusiness.JNDI_NAME);
   }

   /**
    * Clears and repopulates the database with test data 
    * after each run
    * @throws Exception
    */
   @After
   public void refreshWithDefaultData() throws Exception
   {
      dbInitializer.refreshWithDefaultData();
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Test
   public void test() throws Exception
   {

      final BankLocalBusiness ejb = (BankLocalBusiness) jndiContext.lookup(BankLocalBusiness.JNDI_NAME);

      ejb.transfer(DbInitializerBean.ACCOUNT_ALRUBINGER_PERSONAL_ID, DbInitializerBean.ACCOUNT_ALRUBINGER_POKER_ID,
            new BigDecimal(100));

   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
}
