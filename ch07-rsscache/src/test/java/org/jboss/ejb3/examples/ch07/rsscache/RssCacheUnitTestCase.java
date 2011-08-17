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
package org.jboss.ejb3.examples.ch07.rsscache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.ejb3.examples.ch07.rsscache.impl.rome.TestRssCacheBean;
import org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Unit Tests for the RssCache classes, 
 * used as a POJO outside the context of the EJB container
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class RssCacheUnitTestCase extends RssCacheTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(RssCacheUnitTestCase.class.getName());

   /**
    * The bean (POJO) instance to test, mocking a @Singleton EJB
    */
   private static RssCacheCommonBusiness bean;

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
      final TestRssCacheBean bean = new TestRssCacheBean();
      RssCacheUnitTestCase.bean = bean;
      log.info("Created POJO instance: " + bean);

      // Set the URL of the Mock RSS File
      URL url = null;
      try
      {
         url = new URL(getBaseConnectUrl(), FILENAME_RSS_FEED);
      }
      catch (final MalformedURLException murle)
      {
         throw new RuntimeException("Error in test setup while constructing the mock RSS feed URL", murle);
      }
      bean.setUrl(url);

      // Mock container initialization upon the bean
      bean.refresh();
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

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch07.envinfo.EnvironmentInformationTestCaseBase#getEnvInfoBean()
    */
   @Override
   protected RssCacheCommonBusiness getRssCacheBean()
   {
      return bean;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the base of the code source
    */
   private static URL getBaseConnectUrl()
   {
      try
      {
         return new URL("http://localhost:" + HTTP_TEST_BIND_PORT);
      }
      catch (final MalformedURLException e)
      {
         throw new RuntimeException("Error in creating the base URL during set setup", e);
      }
   }

}
