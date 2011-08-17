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

import javax.ejb.Remove;

/**
 * Local Business interface for the FileTransferEJB.
 * Because this will only be used in EJB environments, we define
 * a method to end the current session.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface FileTransferLocalBusiness extends FileTransferCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ends the current session; will result in a SFSB @Remove call
    * as the bean implementation class will annotate this with
    * {@link Remove}
    */
   void endSession();
}
