package org.jboss.ejb3.examples.ch07.envinfo;

import junit.framework.Assert;

import org.jboss.ejb3.examples.ch06.envinfo.EnvironmentInformationCommonBusiness;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * EnvironmentInformationTestCaseBase
 * 
 * Base tests for the enviromment information @Singleton 
 * test classes, may be extended either from unit or 
 * integration tests.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class EnvironmentInformationTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EnvironmentInformationTestCaseBase.class);

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures the JDK Version has been initialized and is available
    */
   @Test
   public void testJdkVersion() throws Exception
   {
      // Log
      log.info("testJdkVersion");

      // Get the environment information bean
      final EnvironmentInformationCommonBusiness envInfo = this.getEnvInfoBean();

      // Get the JDK Version
      final String jdkVersion = envInfo.getJdkVersion();
      log.info("Got JDK Version: " + jdkVersion);

      // Ensure it's been specified/initialized
      Assert.assertNotNull("JDK Version was either not initialized or is returning null", jdkVersion);
   }

   /**
    * Ensures the OS Name has been initialized and is available
    */
   @Test
   public void testOperatingSystemName() throws Exception
   {
      // Log
      log.info("testOperatingSystemName");

      // Get the environment information bean
      final EnvironmentInformationCommonBusiness envInfo = this.getEnvInfoBean();

      // Get the OS Name
      final String osName = envInfo.getOsName();
      log.info("Got OS Name: " + osName);

      // Ensure it's been specified/initialized
      Assert.assertNotNull("OS Name was either not initialized or is returning null", osName);
   }

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the environment information bean to be used for this test
    */
   protected abstract EnvironmentInformationCommonBusiness getEnvInfoBean();

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

}
