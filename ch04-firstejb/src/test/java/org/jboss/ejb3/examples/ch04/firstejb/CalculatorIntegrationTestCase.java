/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CalculatorIntegrationTestCase
 * 
 * Integration tests for the CalculatorEJB
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CalculatorIntegrationTestCase
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CalculatorIntegrationTestCase.class);

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
    * JNDI Name of the Remote Business Reference
    */
   private static final String JNDI_NAME_CALC_REMOTE_BUSINESS = ManyViewCalculatorBean.class.getSimpleName()
         + "/remote";

   /**
    * JNDI Name of the Remote Home Reference
    */
   private static final String JNDI_NAME_CALC_REMOTE_HOME = ManyViewCalculatorBean.class.getSimpleName() + "/home";

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create the naming context, using jndi.properties on the CP
      namingContext = new InitialContext();

      // Obtain EJB 3.x Business Reference
      calcRemoteBusiness = (CalculatorRemoteBusiness) namingContext.lookup(JNDI_NAME_CALC_REMOTE_BUSINESS);

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
      this.assertAdditionSucceeds(calcRemoteBusiness);
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
      this.assertAdditionSucceeds(calcRemote);
   }

   // ---------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Uses the supplied Calculator instance to test the addition
    * algorithm
    */
   private void assertAdditionSucceeds(CalculatorCommonBusiness calc)
   {
      // Initialize
      final int[] arguments = new int[]
      {2, 3, 5};
      final int expectedSum = 10;

      // Add
      final int actualSum = calc.add(arguments);

      // Test
      TestCase.assertEquals("Addition did not return the expected result", expectedSum, actualSum);

      // Log
      final StringBuffer sb = new StringBuffer();
      sb.append("Obtained expected result, ");
      sb.append(actualSum);
      sb.append(", from arguments: ");
      for (final int arg : arguments)
      {
         sb.append(arg);
         sb.append(" ");
      }
      log.info(sb.toString());
   }

}
