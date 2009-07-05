/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ejb3.examples.ch04.firstejb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * MultiViewCalculatorIntegrationTestCase
 * 
 * Integration tests for the CalculatorEJB, testing many views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MultiViewCalculatorIntegrationTestCase
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MultiViewCalculatorIntegrationTestCase.class);

   /**
    * The JNDI Naming Context
    */
   private static Context namingContext;

   /**
    * The EJB 3.x remote business view of the CalculatorEJB
    */
   private static CalculatorRemoteBusiness calcRemoteBusiness;

   /**
    * The EJB 2.x remote component view of the CalculatorEJB 
    */
   private static CalculatorRemote calcRemote;

   /**
    * Delegate for ensuring that the obtained Calculators are working as expected
    */
   private static CalculatorAssertionDelegate assertionDelegate;

   /**
    * JNDI Name of the Remote Business Reference
    */
   //TODO Use Global JNDI Syntax (not yet supported in JBoss EJB3)
   private static final String JNDI_NAME_CALC_REMOTE_BUSINESS = ManyViewCalculatorBean.class.getSimpleName()
         + "/remote";

   /**
    * JNDI Name of the Remote Home Reference
    */
   //TODO Use Global JNDI Syntax (not yet supported in JBoss EJB3)
   private static final String JNDI_NAME_CALC_REMOTE_HOME = ManyViewCalculatorBean.class.getSimpleName() + "/home";

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Run once before any tests
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create the naming context, using jndi.properties on the CP
      namingContext = new InitialContext();

      // Obtain EJB 3.x Business Reference
      calcRemoteBusiness = (CalculatorRemoteBusiness) namingContext.lookup(JNDI_NAME_CALC_REMOTE_BUSINESS);

      // Create Assertion Delegate
      assertionDelegate = new CalculatorAssertionDelegate();

      // Obtain EJB 2.x Component Reference via Home
      final Object calcRemoteHomeReference = namingContext.lookup(JNDI_NAME_CALC_REMOTE_HOME);
      final CalculatorRemoteHome calcRemoteHome = (CalculatorRemoteHome) PortableRemoteObject.narrow(
            calcRemoteHomeReference, CalculatorRemoteHome.class);
      calcRemote = calcRemoteHome.create();
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ensures that the CalculatorEJB adds as expected,
    * using the EJB 3.x business view
    */
   @Test
   public void testAdditionUsingBusinessReference() throws Throwable
   {
      // Test 
      log.info("Testing remote business reference...");
      assertionDelegate.assertAdditionSucceeds(calcRemoteBusiness);
   }

   /**
    * Ensures that the CalculatorEJB adds as expected,
    * using the EJB 2.x component view
    */
   @Test
   public void testAdditionUsingComponentReference() throws Throwable
   {
      // Test
      log.info("Testing remote component reference...");
      assertionDelegate.assertAdditionSucceeds(calcRemote);
   }

}
