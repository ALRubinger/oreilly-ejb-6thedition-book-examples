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

import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the CalculatorEJB, testing many views
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@RunWith(Arquillian.class)
public class MultiViewCalculatorIntegrationTestCase
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MultiViewCalculatorIntegrationTestCase.class.getName());

   /**
    * The EJB 3.x local business view of the CalculatorEJB
    */
   @EJB(mappedName="java:module/ManyViewCalculatorBean!org.jboss.ejb3.examples.ch04.firstejb.CalculatorLocalBusiness")
   private CalculatorLocalBusiness calcLocalBusiness;

   /**
    * The EJB 3.x remote business view of the CalculatorEJB 
    */
   @EJB(mappedName="java:module/ManyViewCalculatorBean!org.jboss.ejb3.examples.ch04.firstejb.CalculatorRemoteBusiness")
   private CalculatorRemoteBusiness calcRemoteBusiness;

   /**
    * Delegate for ensuring that the obtained Calculators are working as expected
    */
   private CalculatorAssertionDelegate assertionDelegate;

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment() throws MalformedURLException
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "firstejb.jar").addPackage(
            CalculatorBeanBase.class.getPackage());
      log.info(archive.toString(true));
      return archive;
   }

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Run once before any tests
    */
   @Before
   public void beforeClass() throws Throwable
   {
      // Create Assertion Delegate
      assertionDelegate = new CalculatorAssertionDelegate();
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ensures that the CalculatorEJB adds as expected,
    * using the EJB 3.x business view
    */
   @Test
   public void testAdditionUsingLocalBusinessReference() throws Throwable
   {
      // Test 
      log.info("Testing remote business reference...");
      assertionDelegate.assertAdditionSucceeds(calcLocalBusiness);
   }

   /**
    * Ensures that the CalculatorEJB adds as expected,
    * using the EJB 2.x component view
    */
   @Test
   public void testAdditionUsingRemoteBusinessReference() throws Throwable
   {
      // Test
      log.info("Testing remote component reference...");
      assertionDelegate.assertAdditionSucceeds(calcRemoteBusiness);
   }

   //TODO Add tests for EJB 2.x Views when AS7 supports it

}
