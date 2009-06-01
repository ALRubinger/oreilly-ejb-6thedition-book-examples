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

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.jboss.logging.Logger;

/**
 * FileTransferBean
 * 
 * Bean Implementation class of the FileTransferEJB, modeled
 * as a Stateful Session Bean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful(name = FileTransferBean.EJB_NAME)
@Remote(FileTransferRemoteBusiness.class)
public class FileTransferBean implements FileTransferRemoteBusiness, Serializable
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferBean.class);

   /**
    * Name of the environment entry containing the host to which we'll connect
    */
   private static final String ENV_ENTRY_NAME_CONNECT_HOST = "connectHost";

   /**
    * Name of the environment entry containing the port to which we'll connect
    */
   private static final String ENV_ENTRY_NAME_CONNECT_PORT = "connectPort";

   /**
    * Name of the EJB, referenced from ejb-jar.xml and used in Global JNDI addresses
    */
   public static final String EJB_NAME = "FileTransferEJB";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The underlying FTP Client.  We don't want its state
    * getting Serialized during passivation.  We'll
    * reinitialize this client and its connections
    * upon activation.
    */
   private FTPClient client;

   /**
    * The name of the host to which we'll connect.
    * Initialized from the environment entry named
    * {@link FileTransferBean#ENV_ENTRY_NAME_CONNECT_HOST}
    */
   @Resource(name = ENV_ENTRY_NAME_CONNECT_HOST)
   private String connectHost;

   /**
    * The port to which we'll connect.
    * Initialized from the environment entry named
    * {@link FileTransferBean#ENV_ENTRY_NAME_CONNECT_PORT}
    */
   @Resource(name = ENV_ENTRY_NAME_CONNECT_PORT)
   private int connectPort;

   /**
    * Name of the present working directory.  In cases where
    * we're passivated, if this is specified
    * we'll change into this directory upon activation.
    */
   private String presentWorkingDirectory;

   //-------------------------------------------------------------------------------------||
   // Lifecycle Callbacks ----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Called by the container when the instance is about to be passivated or brought 
    * out of service entirely.
    * 
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferCommonBusiness#disconnect()
    */
   @PrePassivate
   @PreDestroy
   @Override
   public void disconnect()
   {
      // Obtain FTP Client
      final FTPClient client = this.getClient();

      // If exists
      if (client != null)
      {
         // If connected
         if (client.isConnected())
         {
            // Logout
            try
            {
               client.logout();
               log.info("Logged out of: " + client);
            }
            catch (final IOException ioe)
            {
               log.warn("Exception encountered in logging out of the FTP client", ioe);
            }

            // Disconnect
            try
            {
               log.debug("Disconnecting: " + client);
               client.disconnect();
               log.info("Disconnected: " + client);
            }
            catch (final IOException ioe)
            {
               log.warn("Exception encountered in disconnecting the FTP client", ioe);
            }

            // Null out the client so it's not serialized 
            this.client = null;
         }
      }
   }

   /**
    * Called by the container when the instance has been created or re-activated
    * (brought out of passivated state).  Will construct the underlying FTP Client
    * and open all appropriate connections.
    * 
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferCommonBusiness#connect()
    */
   @PostConstruct
   @PostActivate
   @Override
   public void connect() throws IllegalStateException, FileTransferException
   {
      /*
       * Precondition checks
       */
      final FTPClient clientBefore = this.getClient();
      if (clientBefore != null && clientBefore.isConnected())
      {
         throw new IllegalStateException("FTP Client is already initialized");
      }

      // Get the connection properties
      final String connectHost = this.getConnectHost();
      final int connectPort = this.getConnectPort();

      // Create the client
      final FTPClient client = new FTPClient();
      final String canonicalServerName = connectHost + ":" + connectPort;
      log.debug("Connecting to FTP Server at " + canonicalServerName);
      try
      {
         client.connect(connectHost, connectPort);
      }
      catch (final IOException ioe)
      {
         throw new FileTransferException("Error in connecting to " + canonicalServerName, ioe);
      }

      // Set
      log.info("Connected to FTP Server at: " + canonicalServerName);
      this.setClient(client);

      // Check that the last operation succeeded
      this.checkLastOperation();

      try
      {
         // Login
         client.login("user", "password");

         // Check that the last operation succeeded
         this.checkLastOperation();
      }
      catch (final Exception e)
      {
         throw new FileTransferException("Could not log in", e);
      }

      // If there's a pwd defined, cd into it.
      final String pwd = this.getPresentWorkingDirectory();
      if (pwd != null)
      {
         this.cd(pwd);
      }

   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferCommonBusiness#cd(java.lang.String)
    */
   @Override
   public void cd(final String directory)
   {
      // Get the client
      final FTPClient client = this.getClient();

      // Exec cd
      try
      {
         // Exec cd
         client.changeWorkingDirectory(directory);

         // Check reply for success
         this.checkLastOperation();
      }
      catch (final Exception e)
      {
         throw new FileTransferException("Could not change working directory to \"" + directory + "\"", e);
      }

      // Set the pwd (used upon activation)
      log.info("cd > " + directory);
      this.setPresentWorkingDirectory(directory);
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferCommonBusiness#mkdir(java.lang.String)
    */
   @Override
   public void mkdir(final String directory)
   {
      // Get the client
      final FTPClient client = this.getClient();

      // Exec cd
      try
      {
         // Exec mkdir
         client.makeDirectory(directory);

         // Check reply for success
         this.checkLastOperation();
      }
      catch (final Exception e)
      {
         throw new FileTransferException("Could not make directory \"" + directory + "\"", e);
      }

   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferCommonBusiness#pwd()
    */
   @Override
   public String pwd()
   {
      // Get the client
      final FTPClient client = this.getClient();

      // Exec pwd
      try
      {
         final FTPFile[] files = client.listFiles();
         for (final FTPFile file : files)
         {
            log.info(file);
         }

         // Exec pwd
         return client.printWorkingDirectory();

      }
      catch (final IOException ioe)
      {
         throw new FileTransferException("Could not print working directory", ioe);
      }
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that the last operation succeeded with a positive
    * reply code.  Otherwise a {@link FileTransferException} 
    * is raised, noting the reply code denoting the error.
    * 
    * @throws FileTransferException
    */
   protected void checkLastOperation() throws FileTransferException
   {
      // Get the client
      final FTPClient client = this.getClient();

      // Obtain and check the reply from the connection
      final int connectReply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(connectReply))
      {
         // Indicate the problem
         throw new FileTransferException("Did not receive positive completion code from server, instead code was: "
               + connectReply);
      }

   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferRemoteBusiness#endSession()
    */
   @Remove
   @Override
   public void endSession()
   {
      log.info("Session Ending...");
   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the connectHost
    */
   public String getConnectHost()
   {
      final String connectHost = this.connectHost;
      if (connectHost == null || connectHost.length() == 0)
      {
         throw new IllegalStateException("Connect host must have been defined by env-entry name: "
               + ENV_ENTRY_NAME_CONNECT_HOST);
      }
      return connectHost;
   }

   /**
    * @param connectHost the connectHost to set
    */
   public void setConnectHost(final String connectHost)
   {
      this.connectHost = connectHost;
   }

   /**
    * @return the connectPort
    */
   public int getConnectPort()
   {
      final int connectPort = this.connectPort;
      if (connectPort <= 0)
      {
         throw new IllegalStateException("Connect port must have been defined by env-entry name \""
               + ENV_ENTRY_NAME_CONNECT_PORT + "\" and must be a positive integer");
      }
      return connectPort;
   }

   /**
    * @param connectPort the connectPort to set
    */
   public void setConnectPort(final int connectPort)
   {
      this.connectPort = connectPort;
   }

   /**
    * @return the client
    */
   protected final FTPClient getClient()
   {
      return client;
   }

   /**
    * @param client the client to set
    */
   private void setClient(final FTPClient client)
   {
      this.client = client;
   }

   /**
    * @return the presentWorkingDirectory
    */
   private String getPresentWorkingDirectory()
   {
      return presentWorkingDirectory;
   }

   /**
    * @param presentWorkingDirectory the presentWorkingDirectory to set
    */
   private void setPresentWorkingDirectory(String presentWorkingDirectory)
   {
      this.presentWorkingDirectory = presentWorkingDirectory;
   }

}
