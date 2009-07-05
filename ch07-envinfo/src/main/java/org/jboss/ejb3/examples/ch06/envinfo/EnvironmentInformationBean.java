package org.jboss.ejb3.examples.ch06.envinfo;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.logging.Logger;

/**
 * EnvironmentInformationBean
 * 
 * Singleton EJB, to be eagerly instanciated upon application deployment,
 * exposing information about the runtime environment.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Singleton
@Startup
@Remote(EnvironmentInformationCommonBusiness.class)
public class EnvironmentInformationBean implements EnvironmentInformationCommonBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EnvironmentInformationBean.class);

   /**
    * System property denoting the Java Version
    */
   private static final String SYS_PROP_JAVA_VERSION = "java.version";

   /**
    * System property denoting the Operating System Name
    */
   private static final String SYS_PROP_OS_NAME = "os.name";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Holds the JDK Version
    */
   private String jdkVersion;

   /**
    * Holds the operating system name
    */
   private String osName;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.envinfo.EnvironmentInformationCommonBusiness#getJdkVersion()
    */
   @Override
   public String getJdkVersion()
   {
      return jdkVersion;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.envinfo.EnvironmentInformationCommonBusiness#getOsName()
    */
   @Override
   public String getOsName()
   {
      return osName;
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Initializes all properties such that we're not querying them directly
    * during invocation
    */
   @PostConstruct
   public void initialize()
   {
      // Get system properties in a secure fashion
      final Properties props = AccessController.doPrivileged(new PrivilegedAction<Properties>()
      {
         @Override
         public Properties run()
         {
            return System.getProperties();
         }
      });

      // Eagerly initialize all information
      jdkVersion = props.getProperty(SYS_PROP_JAVA_VERSION);
      osName = props.getProperty(SYS_PROP_OS_NAME);

      // Log
      log.info("Initialized: " + this);
   }
}
