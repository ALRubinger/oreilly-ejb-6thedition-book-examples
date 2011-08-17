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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases to ensure that the FileTransfer business
 * logic is intact, outside of the container.
 * 
 * This is not technically part of the SFSB examples, but is 
 * in place to ensure that everything with the example itself
 * is working as expected.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class FileTransferUnitTestCase extends FileTransferTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferUnitTestCase.class.getName());

   /**
    * The FTP Service to which we'll connect
    */
   private static FtpServerPojo ftpService;

   /**
    * Port to which the FTP Service will bind
    */
   private static final int FTP_SERVICE_BIND_PORT = 12345;

   /**
    * Name of the users configuration file for the server
    */
   private static final String FILE_NAME_USERS_CONFIG = "ftpusers.properties";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The FTP Client
    */
   private FileTransferBean ftpClient;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates, initializes, and starts the FTP Service 
    * to which our test clients will connect. 
    * Called once before any tests run.
    */
   @BeforeClass
   public static void createFtpService() throws Exception
   {
      // Create the FTP Service
      final FtpServerPojo service = new FtpServerPojo();

      // Configure
      service.setBindPort(FTP_SERVICE_BIND_PORT);
      service.setUsersConfigFileName(FILE_NAME_USERS_CONFIG);

      // Initialize
      service.initializeServer();

      // Start
      service.startServer();

      // Set (on success)
      log.info("Started up test FTP Service: " + service);
      ftpService = service;
   }

   /**
    * Stops and resets the FTP Service.  Called once after
    * all tests are done.
    * 
    * @throws Exception
    */
   @AfterClass
   public static void destroyFtpService() throws Exception
   {
      // Only run if initialization finished
      if (ftpService == null)
      {
         return;
      }

      // Stop the server
      ftpService.stopServer();

      // Reset
      ftpService = null;
      log.info("Brought down test FTP Service");
   }

   /**
    * Creates and initializes the FTP Client used in testing.
    * Fired before each test is run.
    */
   @Before
   public void createFtpClient() throws Exception
   {
      // Create client
      final FileTransferBean ftpClient = new FileTransferBean();

      // Connect
      ftpClient.connect();

      // Set
      this.ftpClient = ftpClient;
      log.info("Set FTP Client: " + ftpClient);
   }

   /**
    * Disconnects and resets the FTP Client.  Fired after each 
    * test has completed.
    * 
    * @throws Exception
    */
   @After
   public void cleanup() throws Exception
   {
      // Get client
      final FileTransferBean ftpClient = this.ftpClient;

      // If set
      if (ftpClient != null)
      {
         // Disconnect and reset
         ftpClient.disconnect();
         this.ftpClient = null;
      }
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Mocks the passivation/activation process by manually invoking
    * upon the {@link PrePassivate} and {@link PostActivate} lifecycle
    * callbacks.  The client should function properly after these calls are made,
    * reconnecting as expected, and resuming into the correct present working 
    * directory
    * 
    * @throws Exception
    */
   @Test
   public void testPassivationAndActivation() throws Exception
   {
      // Log
      log.info("testPassivationAndActivation");

      // Get the client
      final FileTransferCommonBusiness client = this.getClient();

      // Switch to home
      final String home = getFtpHome().getAbsolutePath();
      client.cd(home);

      // Test the pwd
      final String pwdBefore = client.pwd();
      TestCase.assertEquals("Present working directory should be set to home", home, pwdBefore);

      // Mock @PrePassivate
      log.info("Mock @" + PrePassivate.class.getName());
      client.disconnect();

      // Mock passivation 
      log.info("Mock passivation");
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      final ObjectOutput objectOut = new ObjectOutputStream(outStream);
      objectOut.writeObject(client);
      objectOut.close();

      // Mock activation
      log.info("Mock activation");
      final InputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
      final ObjectInput objectIn = new ObjectInputStream(inStream);

      // Get a new client from passivation/activation roundtrip
      final FileTransferCommonBusiness serializedClient = (FileTransferCommonBusiness) objectIn.readObject();
      objectIn.close();

      // Mock @PostActivate
      log.info("Mock @" + PostActivate.class.getName());
      serializedClient.connect();

      // Test the pwd
      final String pwdAfter = serializedClient.pwd();
      TestCase.assertEquals("Present working directory should be the same as before passivation/activation", home,
            pwdAfter);
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferTestCaseBase#getClient()
    */
   @Override
   protected FileTransferCommonBusiness getClient()
   {
      return this.ftpClient;
   }

}
