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

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests to ensure that the business methods of the CalculatorEJB
 * are working as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class CalculatorUnitTestCase
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CalculatorUnitTestCase.class.getName());

   /**
    * The POJO instance to test
    */
   private static CalculatorCommonBusiness calc;

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass()
   {
      // Make a POJO instance adhering to the 
      // CalculatorCommonBusiness contract
      calc = new SimpleCalculatorBean();
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ensures that the business logic behind the
    * CalculatorEJB adds as expected when used 
    * as a pure POJO
    */
   @Test
   public void testAddition()
   {
      // Initialize
      final int[] arguments = new int[]
      {3, 7, 2};
      final int expectedSum = 12;

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
