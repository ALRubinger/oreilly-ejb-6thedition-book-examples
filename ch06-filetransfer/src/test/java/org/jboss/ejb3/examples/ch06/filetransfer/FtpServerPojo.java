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

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

/**
 * POJO Responsible for starting/stopping 
 * the Embedded FTP Server.
 * 
 * This should be considered part of the test execution environment
 * and is not really part of the SFSB examples themselves.
 * The SFSBs for the examples are a client of the FTP server
 * started by this simple bean.
 * 
 * Not thread-safe.  Intended to be used in single-Threaded environments
 * (or perform your own external synchronization).
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public final class FtpServerPojo
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FtpServerPojo.class.getName());

   /**
    * Name of the Server's default listener
    */
   private static final String LISTENER_NAME_DEFAULT = "default";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Port to which the FTP Server will bind.
    */
   private int bindPort;

   /**
    * The underlying server.  Must not be exported.
    */
   private FtpServer server;

   /**
    * The name of the users/password configuration filename.
    */
   private String usersConfigFileName;

   //-------------------------------------------------------------------------------------||
   // Lifecycle Methods ------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates and initializes the underlying server.  Should be 
    * called along lifecycle when this POJO is created.
    * 
    * @throws IllegalStateException If the properties for the server have not
    *       been properly initialized
    */
   public void initializeServer() throws IllegalStateException
   {
      // Extract properties
      final int bindPort = this.getBindPort();

      /*
       * Precondition checks
       */

      if (bindPort <= 0)
      {
         throw new IllegalStateException("Property for bind port has not been set to a valid value above 0.");
      }

      // Initialize
      final FtpServerFactory serverFactory = new FtpServerFactory();
      final ListenerFactory factory = new ListenerFactory();

      // Set properties
      log.fine("Using FTP bind port: " + bindPort);
      factory.setPort(bindPort);

      // Add default listener to the server factory
      serverFactory.addListener(LISTENER_NAME_DEFAULT, factory.createListener());

      // Get the current CL
      final ClassLoader tccl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         @Override
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });

      // Load the properties file to get its URI
      final String usersConfigFileName = this.getUsersConfigFileName();
      log.info("Using users configuration file: " + usersConfigFileName);
      final URL usersConfigUrl = tccl.getResource(usersConfigFileName);
      if (usersConfigUrl == null)
      {
         throw new RuntimeException("Could not find specified users configuration file upon the classpath: "
               + usersConfigFileName);
      }

      // Configure the user auth mechanism
      final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
      userManagerFactory.setUrl(usersConfigUrl);
      userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
      final UserManager userManager = userManagerFactory.createUserManager();
      serverFactory.setUserManager(userManager);

      // Create the server
      final FtpServer server = serverFactory.createServer();
      this.setServer(server);
      log.info("Created FTP Server: " + server);
   }

   /**
    * Starts the server.
    * 
    * @throws IllegalStateException If the server has not been initialized or
    *       if the server has already been started
    * @throws FtpException If there was an error in starting the server
    */
   public void startServer() throws IllegalStateException, FtpException
   {
      // Get the server
      final FtpServer server = this.getServer();

      /*
       * Precondition checks
       */

      // Ensure initialized
      if (server == null)
      {
         throw new IllegalStateException("The server has not yet been initialized");
      }

      // Ensure not already running or in some other state
      if (!server.isStopped())
      {
         throw new IllegalStateException("Server cannot be started if it is not currently stopped");
      }

      // Start
      log.fine("Starting the FTP Server: " + server);
      server.start();
      log.info("FTP Server Started: " + server);
   }

   /**
    * Stops the server. 
    * 
    * @throws IllegalStateException If the server is already stopped or the server is
    *       not initialized 
    * @throws FtpException
    */
   public void stopServer() throws IllegalStateException
   {
      // Get the server
      final FtpServer server = this.getServer();

      /*
       * Precondition checks
       */

      // Ensure initialized
      if (server == null)
      {
         throw new IllegalStateException("The server has not yet been initialized");
      }

      // Ensure not already running or in some other state
      if (server.isStopped())
      {
         throw new IllegalStateException("Server cannot be stopped if it's already stopped");
      }

      // Stop
      log.fine("Stopping the FTP Server: " + server);
      server.stop();
      log.info("FTP Server stopped: " + server);
   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the port to which we'll bind
    */
   public int getBindPort()
   {
      return bindPort;
   }

   /**
    * Sets the port to which we'll bind.
    * 
    * @param bindPort
    */
   public void setBindPort(final int bindPort)
   {
      this.bindPort = bindPort;
   }

   /**
    * Obtains the underlying FTP Server
    * 
    * @return
    */
   protected FtpServer getServer()
   {
      return server;
   }

   /**
    * Sets the underlying FTP Server. 
    * 
    * @param server
    */
   private void setServer(final FtpServer server)
   {
      this.server = server;
   }

   /**
    * Obtains the name of the users configuration file
    * 
    * @return the usersConfigFileName
    */
   public String getUsersConfigFileName()
   {
      return usersConfigFileName;
   }

   /**
    * Sets the name of the users configuration file.
    * 
    * @param usersConfigFileName the usersConfigFileName to set
    */
   public void setUsersConfigFileName(final String usersConfigFileName)
   {
      this.usersConfigFileName = usersConfigFileName;
   }
}
