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

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * FileTransferUnitTestCase
 * 
 * Test cases to ensure that the FileTransfer business
 * logic is intact, outside of the container.
 * 
 * This is not technically part of the SFSB examples, but is 
 * in place to ensure that everything with the example itself
 * is working as expected.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class FileTransferUnitTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferUnitTestCase.class);

   /**
    * The FTP Service to which we'll connect
    */
   private static FtpServerPojo ftpService;

   /**
    * Port to which the FTP Service will bind
    */
   private static final int FTP_SERVICE_BIND_PORT = 12345;

   /**
    * Host to which the FTP Client will connect
    */
   private static final String FTP_CLIENT_CONNECT_HOST = "127.0.0.1";

   /**
    * Name of the users configuration file for the server
    */
   private static final String FILE_NAME_USERS_CONFIG = "ftpusers.properties";

   /**
    * The name of the directory under the writable temp filesystem which
    * will act as the home for these tests
    */
   private static final String RELATIVE_LOCATION_HOME = "ejb31_ch06-example-ftpHome";

   /**
    * The name of the system property denoting the I/O temp directory
    */
   private static final String SYS_PROP_NAME_IO_TMP_DIR = "java.io.tmpdir";

   /**
    * The File we'll use as the writeable home for FTP operations.  Created and
    * destroyed alongside test lifecycle.
    */
   private static File ftpHome;

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
    * Creates the directory which we'll use as the writeable home 
    * for FTP operations.  Fired before each test is run.
    */
   @Before
   public void initialize() throws Exception
   {
      // Create the writeable home
      this.createFtpHome();

      // Create client
      final FileTransferBean ftpClient = new FileTransferBean();

      // Set properties
      ftpClient.setConnectHost(FTP_CLIENT_CONNECT_HOST);
      ftpClient.setConnectPort(FTP_SERVICE_BIND_PORT);

      // Connect
      ftpClient.connect();

      // Set
      this.ftpClient = ftpClient;
      log.info("Set FTP Client: " + ftpClient);
   }

   /**
    * Disconnects and resets the FTP Client.  Removes the writeable
    * home for FTP operations.  Fired after each 
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

      // Remove the writeable home
      this.deleteFtpHome();
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that the a new directory can be made, we can switch 
    * into it, and we can obtain the present working directory of our newly-created
    * directory
    */
   @Test
   public void testMkdirCdAndPwd() throws Exception
   {
      // Log
      log.info("testMkdirAndPwd");

      // Get the client
      final FileTransferCommonBusiness client = this.ftpClient;

      // Switch to home
      final String home = getFtpHome().getAbsolutePath();
      client.cd(home);

      // Ensure we're home
      final String pwdBefore = client.pwd();
      TestCase.assertEquals("Present working directory should be our home", home, pwdBefore);

      // Make the directory
      final String newDir = "newDirectory";
      client.mkdir(newDir);

      // cd into the new dir
      client.cd(newDir);

      // Ensure we're in the new directory
      final String pwdAfter = client.pwd();
      TestCase.assertEquals("Present working directory should be our new directory", home + File.separator + newDir,
            pwdAfter);
   }

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
      final FileTransferCommonBusiness client = this.ftpClient;

      // Switch to home
      final String home = getFtpHome().getAbsolutePath();
      client.cd(home);

      // Test the pwd
      final String pwdBefore = client.pwd();
      TestCase.assertEquals("Present working directory should be set to home", home, pwdBefore);

      // Mock passivation
      log.info("Mock passivation");
      client.disconnect();

      // Mock activation
      log.info("Mock activation");
      client.connect();

      // Test the pwd
      final String pwdAfter = client.pwd();
      TestCase.assertEquals("Present working directory should be the same as before passivation/activation", home,
            pwdAfter);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Deletes the writable FTP Home
    * 
    * @throws Exception
    */
   protected void deleteFtpHome() throws Exception
   {
      final File ftpHome = getFtpHome();
      if (!ftpHome.exists())
      {
         throw new RuntimeException("Error in test setup; FTP Home should exist: " + ftpHome.getAbsolutePath());
      }
      final boolean removed = this.deleteRecursive(ftpHome);
      if (!removed)
      {
         throw new RuntimeException("Request to remove the FTP Home failed: " + ftpHome.getAbsolutePath());
      }
      log.info("Removed FTP Home: " + ftpHome.getAbsolutePath());
   }

   /**
    * Recursively deletes all contents of the specified root, 
    * including the root itself.  If the specified root does not exist, 
    * no action is taken.
    * 
    * @param root
    * @return true if deleted, false otherwise
    */
   protected boolean deleteRecursive(final File root)
   {
      // Ensure exists
      if (!root.exists())
      {
         return false;
      }

      // Get all children
      final File[] children = root.listFiles();
      // If it's a directory
      if (children != null)
      {
         // Remove all children
         for (final File child : children)
         {
            this.deleteRecursive(child);
         }
      }

      // Delete me
      final boolean success = root.delete();
      log.info("Deleted: " + root);
      return success;
   }

   /**
    * Creates the writable FTP Home
    * 
    * @throws Exception
    */
   protected void createFtpHome() throws Exception
   {
      final File ftpHome = getFtpHome();
      if (ftpHome.exists())
      {
         throw new RuntimeException("Error in test setup; FTP Home should not yet exist: " + ftpHome.getAbsolutePath());
      }
      final boolean created = ftpHome.mkdir();
      if (!created)
      {
         throw new RuntimeException("Request to create the FTP Home failed: " + ftpHome.getAbsolutePath());
      }
      log.info("Created FTP Home: " + ftpHome.getAbsolutePath());
   }

   /**
    * Obtains the writeable home for these tests, set under the namespace of the
    * IO Temp directory
    */
   private static File getFtpHome() throws Exception
   {
      // If the home is not defined
      if (ftpHome == null)
      {

         // Get the property
         final String sysPropIoTempDir = SYS_PROP_NAME_IO_TMP_DIR;
         final String ioTempDir = System.getProperty(sysPropIoTempDir);
         if (ioTempDir == null)
         {
            throw new RuntimeException("I/O temp directory was not specified by system property: " + sysPropIoTempDir);
         }

         // Make the File
         final File ioTempDirFile = new File(ioTempDir);
         if (!ioTempDirFile.exists())
         {
            throw new RuntimeException("I/O Temp directory does not exist: " + ioTempDirFile.getAbsolutePath());
         }

         // Append the suffix for our home
         final File home = new File(ioTempDirFile, RELATIVE_LOCATION_HOME);
         ftpHome = home;
      }

      // Return
      return ftpHome;
   }
}
