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
package org.jboss.ejb3.examples.ch08.messagedestinationlink;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.api.MessageDestinationLinkConstants;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.mdb.MessageDestinationLinkMdb;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb.MessageSendingBean;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb.MessageSendingBusiness;
import org.jboss.embedded.api.DeploymentException;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.api.server.JBossASEmbeddedServerFactory;
import org.jboss.embedded.api.server.JBossHomeClassLoader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Ensures that a SLSB wired to an MDB by way of a logical mapping
 * (message-destination-link) is working as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MessageDestinationLinkIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MessageDestinationLinkIntegrationTest.class.getName());

   /**
    * The server instance
    */
   private static JBossASEmbeddedServer server;

   /**
    * The CL of the test as originally loaded
    */
   private static ClassLoader originalClassLoader;

   /**
    * Name of the archive we'll deploy into the server for testing
    */
   private static final String NAME_MDB_ARCHIVE = "messageDestinationLink.jar";

   /**
    * Name of the system property for JBOSS_HOME
    */
   private static final String NAME_SYSPROP_JBOSS_HOME = "jboss.home";

   /**
    * Location of the EJB deployment descriptor
    */
   private static final String NAME_EJB_JAR = "META-INF/ejb-jar.xml";

   /**
    * The JNDI Context
    */
   private static Context NAMING_CONTEXT;

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Archive containing our EJBs
    */
   private JavaArchive testArchive;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates and starts a new JBossAS Server Embedded within this JVM, setting a JNDI context as well
    */
   @BeforeClass
   public static void createAndStartJBossASAndSetNamingContext() throws Exception
   {
      // Get JBOSS_HOME
      final URL jbossHome = getJBossHome();

      // Get additional binaries which need CL visibility (ie. jboss-embedded-core,
      // which is placed under "target/deps" by the build).  These
      // binaries are not presently available under $JBOSS_HOME
      final Set<URL> additionalUrls = new HashSet<URL>();
      final URL source = MessageDestinationLinkIntegrationTest.class.getProtectionDomain().getCodeSource()
            .getLocation();
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
      Thread.currentThread().setContextClassLoader(jbossHomeClassLoader);

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
    * Creates and deploys the test archive
    * @throws DeploymentException
    */
   @Before
   public void createAndDeployArchive() throws DeploymentException
   {
      // Package up the EJBs and a Deployment Descriptor
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, NAME_MDB_ARCHIVE).addClasses(
            MessageDestinationLinkConstants.class, MessageDestinationLinkMdb.class, MessageSendingBusiness.class,
            MessageSendingBean.class).addResource(NAME_EJB_JAR);

      // Deploy the archive
      log.info("Deploying archive: " + archive.toString(true));
      server.deploy(archive);
      testArchive = archive;
   }

   /**
    * Undeploys the test archive
    */
   @After
   public void undeployArchive() throws DeploymentException
   {
      if (testArchive != null)
      {
         server.undeploy(testArchive);
      }
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that invoking an SLSB which sends messages to
    * a logical name (message-destination-link) works correctly
    * where the target MDB receives the message in question
    */
   @Test
   public void testMessageLinkingFromSlsbToMdb() throws Exception
   {
      // Obtain the EJB
      final MessageSendingBusiness bean = (MessageSendingBusiness) NAMING_CONTEXT
            .lookup(MessageSendingBusiness.NAME_JNDI);

      // Send a message
      final String message = "Testing Message Linking";
      bean.sendMessage(message);

      // Wait for the MDB to process, as it's doing so in another Thread.
      // This is *only* possible when we test MDBs in the same JVM as the test.
      final boolean processed;
      try
      {
         log.info("Waiting on the MDB...");
         processed = MessageDestinationLinkMdb.LATCH.await(10, TimeUnit.SECONDS);
      }
      catch (final InterruptedException e)
      {
         // Clear the flag and rethrow; some error in setup is in play
         Thread.interrupted();
         throw new RuntimeException(
               "Thread was interrupted while waiting for MDB processing; should not happen in this test");
      }

      // Ensure the MDB processed the message
      if (!processed)
      {
         TestCase.fail("The MDB did not process the message in the allotted time.");
      }
      log.info("MDB signaled it's done processing, so we can resume");

      // Ensure the contents are as expected
      final String roundtrip = MessageDestinationLinkMdb.LAST_MESSAGE;
      TestCase.assertEquals("Last message sent was not as expected", message, roundtrip);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains $JBOSS_HOME from the system property
    * 
    * @return
    */
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
