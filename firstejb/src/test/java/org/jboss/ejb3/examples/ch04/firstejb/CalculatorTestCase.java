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

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CalculatorTestCase
 * 
 * Tests to ensure that the business methods of the CalculatorEJB
 * are working as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CalculatorTestCase
{
   // ---------------------------------------------------------------------------||
   // Required Implementations --------------------------------------------------||
   // ---------------------------------------------------------------------------||

   private static SimpleCalculatorBean calc;

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass()
   {
      calc = new SimpleCalculatorBean();
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ensures that the CalculatorEJB adds as expected
    */
   @Test
   public void testAddition()
   {
      // Initialize
      int expectedSum = 10;

      // Add
      int actualSum = calc.add(2, 3, 5);

      // Test
      TestCase.assertEquals("Addition did not return the expected result", expectedSum, actualSum);
   }
}
