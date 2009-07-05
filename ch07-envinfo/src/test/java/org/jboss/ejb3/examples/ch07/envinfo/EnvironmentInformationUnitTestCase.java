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
package org.jboss.ejb3.examples.ch07.envinfo;

import org.jboss.ejb3.examples.ch06.envinfo.EnvironmentInformationBean;
import org.jboss.ejb3.examples.ch06.envinfo.EnvironmentInformationCommonBusiness;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * EnvironmentInformationUnitTestCase
 * 
 * Unit Tests for the EnvironmentInformationEJB classes, 
 * used as a POJO outside the context of the EJB container
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EnvironmentInformationUnitTestCase extends EnvironmentInformationTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EnvironmentInformationUnitTestCase.class);

   /**
    * The bean (POJO) instance to test, mocking a @Singleton EJB
    */
   private static EnvironmentInformationCommonBusiness bean;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a POJO instance to mock the real Container EJB @Singleton
    * before any tests are run
    */
   @BeforeClass
   public static void createPojo()
   {
      // Instanciate and set
      final EnvironmentInformationBean bean = new EnvironmentInformationBean();
      EnvironmentInformationUnitTestCase.bean = bean;
      log.info("Created POJO instance: " + bean);

      // Mock container initialization
      bean.initialize();
   }

   /**
    * Resets the POJO instance to null after all tests are run
    */
   @AfterClass
   public static void clearPojo()
   {
      // Set to null so we don't ever leak instances between test runs
      bean = null;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.envinfo.EnvironmentInformationTestCaseBase#getEnvInfoBean()
    */
   @Override
   protected EnvironmentInformationCommonBusiness getEnvInfoBean()
   {
      return bean;
   }

}
