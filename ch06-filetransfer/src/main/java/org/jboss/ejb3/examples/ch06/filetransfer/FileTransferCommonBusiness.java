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

/**
 * Contains the contract for operations common to all
 * business interfaces of the FileTransferEJB.
 * 
 * Includes support for switching present working directories,
 * printing the current working directory, and making directories.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface FileTransferCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Makes a directory of the specified name
    * 
    * @throws IllegalStateException If the client connection has not been initialized
    */
   void mkdir(String directory) throws IllegalStateException;

   /**
    * Changes into the named directory
    * 
    * @param directory
    * @throws IllegalStateException If the client connection has not been initialized
    */
   void cd(String directory) throws IllegalStateException;

   /**
    * Obtains the name of the current working directory
    * 
    * @return
    * @throws IllegalStateException If the client connection has not been initialized
    */
   String pwd() throws IllegalStateException;

   /**
    * Denotes that the client is done using this service; flushes
    * any pending operations and does all appropriate cleanup.  If 
    * already disconnected, this is a no-op.
    */
   void disconnect();

   /**
    * Opens the underlying connections to the target FTP Server, 
    * performs any other tasks required before commands may be sent
    * (ie. login, etc)
    * 
    * @throws IllegalStateException If already initialized/connected
    */
   void connect() throws IllegalStateException;

}
