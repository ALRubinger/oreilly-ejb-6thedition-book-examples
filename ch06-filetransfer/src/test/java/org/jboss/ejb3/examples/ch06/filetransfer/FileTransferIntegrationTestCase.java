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
package org.jboss.ejb3.examples.ch06.filetransfer;

import java.io.File;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.NoSuchEJBException;

import junit.framework.TestCase;

import org.apache.commons.net.SocketClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases to ensure that the FileTransferEJB is working as
 * a Stateful Session Bean from the EJB Container.
 * 
 * Inherits some test support from {@link FileTransferTestCaseBase},
 * and additionally tests EJB-specific tasks upon the 
 * proxy.  Shows that sessions operate in isolation, and that removal
 * of a session means you cannot use it anymore.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@RunWith(Arquillian.class)
public class FileTransferIntegrationTestCase extends FileTransferTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferIntegrationTestCase.class.getName());

   /**
    * Name of the configuration file for the FTP server users
    */
   private static final String FTP_SERVER_USERS_CONFIG_FILENAME = "ftpusers.properties";

   /**
    * Port to which the FTP server should bind
    */
   private static final int FTP_SERVER_BIND_PORT = 12345;

   /**
    * The FTP Server
    */
   private static FtpServerPojo ftpServer;

   /**
    * The Deployment
    * @return
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "ftpclient.jar").addPackages(true,
            FileTransferBean.class.getPackage(),SocketClient.class.getPackage());
      log.info(archive.toString(true));
      return archive;
   }

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Our view of the EJB, local business interface type of the Proxy
    */
   @EJB(mappedName="java:module/FileTransferEJB!org.jboss.ejb3.examples.ch06.filetransfer.FileTransferLocalBusiness")
   private FileTransferLocalBusiness client1;

   /**
    * Another FTP Client Session
    */
   @EJB(mappedName="java:module/FileTransferEJB!org.jboss.ejb3.examples.ch06.filetransfer.FileTransferLocalBusiness")
   private FileTransferLocalBusiness client2;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates and starts the FTP Server
    */
   @BeforeClass
   public static void startFtpServer() throws Exception
   {
      // Create
      final FtpServerPojo server = new FtpServerPojo();

      // Configure
      server.setUsersConfigFileName(FTP_SERVER_USERS_CONFIG_FILENAME);
      server.setBindPort(FTP_SERVER_BIND_PORT);

      // Start and set
      server.initializeServer();
      server.startServer();
      ftpServer = server;
   }

   /**
    * Stops the FTP Server
    * @throws Exception
    */
   @AfterClass
   public static void stopFtpServer() throws Exception
   {
      ftpServer.stopServer();
   }

   /**
    * Ends the session upon the FTP Client SFSB Proxy 
    * and resets
    */
   @After
   public void endClientSessions() throws Exception
   {
      // End the session for client 1
      try
      {
         client1.endSession();
      }
      // If we've already been ended
      catch (final NoSuchEJBException nsee)
      {
         // Ignore
      }

      // End the session for client 2
      try
      {
         client2.endSession();
      }
      // If we've already been ended
      catch (final NoSuchEJBException nsee)
      {
         // Ignore
      }
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that two separate sessions will act in isolation from each other
    * 
    * @throws Exception
    */
   @Test
   public void testSessionIsolation() throws Exception
   {
      // Log
      log.info("testSessionIsolation");

      // Get the existing client as made from the test lifecycle
      final FileTransferLocalBusiness session1 = this.getClient();

      // Use another client
      final FileTransferLocalBusiness session2 = this.client2;

      // cd into a home directory for each
      final String ftpHome = getFtpHome().getAbsolutePath();
      session1.cd(ftpHome);
      session2.cd(ftpHome);

      // Now make a new directory for each session, and go into it
      final String newDirSession1 = "newDirSession1";
      final String newDirSession2 = "newDirSession2";
      session1.mkdir(newDirSession1);
      session1.cd(newDirSession1);
      session2.mkdir(newDirSession2);
      session2.cd(newDirSession2);

      // Get the current working directory for each session
      final String pwdSession1 = session1.pwd();
      final String pwdSession2 = session2.pwd();

      // Ensure each session is in the proper working directory
      TestCase.assertEquals("Session 1 is in unexpected pwd", ftpHome + File.separator + newDirSession1, pwdSession1);
      TestCase.assertEquals("Session 2 is in unexpected pwd", ftpHome + File.separator + newDirSession2, pwdSession2);

      // End the session manually for session2 (session1 will be ended by test lifecycle)
      session2.endSession();
   }

   /**
    * Tests that a call to {@link FileTransferLocalBusiness#endSession()}
    * results in the SFSB's backing instance removal, and that subsequent 
    * operations result in a {@link NoSuchEJBException}
    * 
    * @throws Exception
    */
   @Test
   public void testSfsbRemoval() throws Exception
   {
      // Log
      log.info("testSfsbRemoval");

      // Get the existing client as made from the test lifecycle
      final FileTransferLocalBusiness sfsb = this.getClient();

      // cd into the home directory
      final String ftpHome = getFtpHome().getAbsolutePath();
      sfsb.cd(ftpHome);

      // Get and test the pwd
      final String pwdBefore = sfsb.pwd();
      TestCase.assertEquals("Session should be in the FTP Home directory", ftpHome, pwdBefore);

      // End the session, resulting in an underlying instance
      // removal due to the annotation with @Remove upon 
      // the bean implementation class
      sfsb.endSession();

      // Now try some other operation, and ensure that we get a NoSuchEJBException
      boolean gotExpectedException = false;
      try
      {
         // This should not succeed, because we've called a method marked as @Remove
         sfsb.pwd();
      }
      catch (final NoSuchEJBException nsee)
      {
         gotExpectedException = true;
      }
      TestCase.assertTrue("Call to end the session did not result in underlying removal of the SFSB bean instance",
            gotExpectedException);
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferTestCaseBase#getClient()
    */
   @Override
   protected FileTransferLocalBusiness getClient()
   {
      return this.client1;
   }

}
