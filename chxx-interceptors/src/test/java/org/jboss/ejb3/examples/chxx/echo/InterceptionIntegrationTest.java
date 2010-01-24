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
package org.jboss.ejb3.examples.chxx.echo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.interceptor.Interceptors;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.api.server.JBossASEmbeddedServerFactory;
import org.jboss.embedded.api.server.JBossHomeClassLoader;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test ensuring that an EJB with {@link Interceptors} 
 * declared are intercepted when invoked
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InterceptionIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(InterceptionIntegrationTest.class.getName());

   /**
    * The CL of the test as originally loaded
    */
   private static ClassLoader originalClassLoader;

   /**
    * The server instance
    */
   private static JBossASEmbeddedServer server;

   /**
    * Name of the system property for JBOSS_HOME
    * @deprecated EJBBOOK-14
    */
   @Deprecated
   private static final String NAME_SYSPROP_JBOSS_HOME = "jboss.home";

   /**
    * The JNDI Context
    */
   private static Context NAMING_CONTEXT;

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private JavaArchive deployment;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates and starts a new JBossAS Server Embedded within this JVM
    */
   //TODO EJBBOOK-15
   @BeforeClass
   public static void createAndStartJBossASAndSetNamingContext() throws Exception
   {
      // Get JBOSS_HOME
      final URL jbossHome = getJBossHome();

      // Get additional binaries which need CL visibility (ie. jboss-embedded-core,
      // which is placed under "target/deps" by the build).  These
      // binaries are not presently available under $JBOSS_HOME
      final Set<URL> additionalUrls = new HashSet<URL>();
      final URL source = InterceptionIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation();
      final URL target = new URL(source, "..");
      final URL additionalDeps = new URL(target, "deps");
      final File deps = new File(additionalDeps.toURI());
      TestCase.assertTrue("Dependencies location does not exist: " + deps, deps.exists());
      TestCase.assertTrue("Dependencies location is not a directory: " + deps, deps.isDirectory());
      for (final File child : deps.listFiles())
      {
         additionalUrls.add(child.toURI().toURL());
         log.info("Booting with: " + child);
      }

      // Make the new ClassLoader
      originalClassLoader = SecurityActions.getThreadContextClassLoader();
      final ClassLoader jbossHomeClassLoader = JBossHomeClassLoader.newInstance(jbossHome, additionalUrls
            .toArray(new URL[]
            {}), originalClassLoader);

      // Make Server
      server = JBossASEmbeddedServerFactory.createServer(jbossHomeClassLoader);
      log.info("Created: " + server);

      // Start
      log.info("Starting Server: " + server);

      // Set TCCL
      SecurityActions.setThreadContextClassLoader(jbossHomeClassLoader);

      // Start the Server
      server.start();

      // Set Naming Context
      NAMING_CONTEXT = new InitialContext();
   }

   /**
    * Stops the Application Server
    */
   @AfterClass
   public static void stopJBossAS() throws Exception
   {
      if (server != null && server.getState().equals(LifecycleState.STARTED))
      {
         try
         {
            server.shutdown();
         }
         finally
         {
            // Reset the TCCL 
            Thread.currentThread().setContextClassLoader(originalClassLoader);
         }
      }
   }

   /**
    * Deploys the EJB into the server
    * @throws Exception
    */
   @Before
   public void deploy() throws Exception
   {

      // Create the archive
      deployment = Archives.create("echo.jar", JavaArchive.class).addClasses(EchoLocalBusiness.class, EchoBean.class,
            CachingAuditor.class);

      // Deploy
      server.deploy(deployment);
   }

   /**
    * Undeploys the EJB from the server
    * @throws Exception
    */
   @After
   public void undeploy() throws Exception
   {
      // If we've created the running server and deployed into it
      if (deployment != null && server != null && server.getState().equals(LifecycleState.STARTED))
      {
         // Undeploy
         server.undeploy(deployment);
      }

   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that invocation upon an EJB with {@link Interceptors} declared
    * results in the interception of targeted methods
    */
   @Test
   public void testInterception() throws NamingException
   {
      // Lookup
      final EchoLocalBusiness bean = (EchoLocalBusiness) NAMING_CONTEXT.lookup(EchoLocalBusiness.JNDI_NAME);

      // Ensure no invocations intercepted yet
      TestCase.assertEquals("No invocations should have yet been intercepted", 0, CachingAuditor.getInvocations()
            .size());

      // Invoke
      final String request = "Hey-o!";
      final String response = bean.echo(request);

      // Test the response is as expected
      TestCase.assertEquals("Did not obtain expected response", request, response);
      TestCase.assertTrue(
            "Invocation returned an object equal by value, but local invocations must be pass-by-reference",
            request == response);

      // Test the invocation was intercepted 
      TestCase.assertEquals("The invocation should have been intercepted", 1, CachingAuditor.getInvocations().size());
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains $JBOSS_HOME from the system property
    * 
    * @deprecated EJBBOOK-14
    * @return
    */
   @Deprecated
   private static URL getJBossHome()
   {
      final String sysProp = NAME_SYSPROP_JBOSS_HOME;
      final String jbossHomeString = SecurityActions.getSystemProperty(sysProp);
      if (jbossHomeString == null)
      {
         throw new IllegalStateException("System property \"" + sysProp + "\" must be present in the environment");
      }
      final File jbossHomeFile = new File(jbossHomeString);
      if (!jbossHomeFile.exists())
      {
         throw new IllegalStateException("JBOSS_HOME does not exist: " + jbossHomeFile.getAbsolutePath());
      }
      try
      {
         return jbossHomeFile.toURI().toURL();
      }
      catch (final MalformedURLException murle)
      {
         throw new RuntimeException("Could not get JBOSS_HOME", murle);
      }
   }
}
