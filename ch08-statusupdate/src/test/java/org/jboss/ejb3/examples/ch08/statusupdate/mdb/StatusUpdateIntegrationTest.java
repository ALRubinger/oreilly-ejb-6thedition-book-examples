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
package org.jboss.ejb3.examples.ch08.statusupdate.mdb;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdateConstants;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.api.server.JBossASEmbeddedServerFactory;
import org.jboss.embedded.api.server.JBossHomeClassLoader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import twitter4j.Twitter;

/**
 * Integration tests for the StatusUpdateEJBs.  Ensures that
 * the MDBs are working as expected when running inside
 * an EJB Container.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatusUpdateIntegrationTest extends StatusUpdateTestBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(StatusUpdateIntegrationTest.class.getName());

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
   private static final String NAME_MDB_ARCHIVE = "statusUpdateEjb.jar";

   /**
    * Name of the ClassLoader resource for the deployment descriptor making a new StatusUpdate JMS Topic
    */
   private static final String NAME_RESOURCE_TOPIC_DEPLOYMENT = "statusupdate-topic-service.xml";

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

   /**
    * Name of the Queue Connection Factory in JNDI
    */
   private static final String JNDI_NAME_CONNECTION_FACTORY = "ConnectionFactory";

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
      final URL source = StatusUpdateIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation();
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

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that the {@link TwitterUpdateMdb} updates Twitter when
    * it receives a new status from the JMS Topic upon which its listening
    */
   @Test
   public void testTwitterUpdateMdb() throws Exception
   {
      // Get a Twitter Client and enforce the environment
      final Twitter twitterClient;
      try
      {
         twitterClient = EnvironmentSpecificTwitterClientUtil.getTwitterClient();
      }
      catch (final IllegalStateException ise)
      {
         log.warning(ise.getMessage() + "; skipping...");
         return;
      }

      // Package up the test MDB, all required classes, and a Topic descriptor
      final JavaArchive archive = ShrinkWrap.create(NAME_MDB_ARCHIVE, JavaArchive.class).addClasses(StatusUpdate.class,
            StatusUpdateConstants.class, LoggingStatusUpdateMdb.class, StatusUpdateBeanBase.class,
            TwitterUpdateBlockingTestMdb.class, SecurityActions.class, TwitterUpdateMdb.class).addResource(
            NAME_RESOURCE_TOPIC_DEPLOYMENT);

      // Deploy the archive
      log.info("Deploying archive: " + archive.toString(true));
      server.deploy(archive);

      // Create a new status
      final StatusUpdate newStatus = this.getUniqueStatusUpdate();

      // Publish the update to a JMS Topic (where it should be consumed by the MDB subscriber)
      this.publishStatusUpdateToTopic(newStatus);

      // Wait for the MDB to process, as it's doing so in another Thread.
      // This is *only* possible when we test MDBs in the same JVM as the test.
      try
      {
         log.info("Waiting on the MDB...");
         TwitterUpdateBlockingTestMdb.BARRIER.await(10, TimeUnit.SECONDS);
      }
      catch (final InterruptedException e)
      {
         // Clear the flag and rethrow; some error in setup is in play
         Thread.interrupted();
         throw new RuntimeException(
               "Thread was interrupted while waiting for MDB processing; should not happen in this test");
      }
      catch (final BrokenBarrierException bbe)
      {
         TestCase.fail("The MDB did not process the status update in the allotted time.");
      }

      log.info("MDB signaled it's done processing, so we can resume");

      // Test
      this.assertLastUpdateSentToTwitter(twitterClient, newStatus);

      // Undeploy
      server.undeploy(archive);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sends a JMS {@link ObjectMessage} containing the specified status to the 
    * queue of the specified name  
    * 
    * @param status
    * @param topicName
    * @throws Exception
    * @throws IllegalArgumentException If either argument is not provided
    */
   private void publishStatusUpdateToTopic(final StatusUpdate status) throws Exception, IllegalArgumentException
   {
      // Precondition check
      if (status == null)
      {
         throw new IllegalArgumentException("status must be provided");
      }

      // Get the queue from JNDI
      final Topic topic = (Topic) NAMING_CONTEXT.lookup(StatusUpdateConstants.JNDI_NAME_TOPIC_STATUSUPDATE);

      // Get the ConnectionFactory from JNDI
      final TopicConnectionFactory factory = (TopicConnectionFactory) NAMING_CONTEXT
            .lookup(JNDI_NAME_CONNECTION_FACTORY);

      // Make a Connection
      final TopicConnection connection = factory.createTopicConnection();
      final TopicSession sendSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
      final TopicPublisher publisher = sendSession.createPublisher(topic);

      // Make the message
      final Message message = sendSession.createObjectMessage(status);

      // Publish the message
      publisher.publish(message);
      log.info("Published message " + message + " with contents: " + status);

      // Clean up
      sendSession.close();
      connection.close();
   }

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
