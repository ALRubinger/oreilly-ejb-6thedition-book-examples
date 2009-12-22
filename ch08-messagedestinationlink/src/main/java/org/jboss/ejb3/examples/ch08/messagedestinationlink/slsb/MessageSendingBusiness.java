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
package org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb;

import javax.jms.TextMessage;

/**
 * Business interface to send a message to a message
 * destination link (to be picked up by an MDB as configured
 * down the chain) 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface MessageSendingBusiness
{
   //-------------------------------------------------------------------------------------||
   // Constants --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * EJB Name
    */
   String NAME_EJB = "MessageSendingEJB";

   /**
    * Location to which we'll bind in JNDI
    */
   String NAME_JNDI = NAME_EJB + "/local";

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sends a {@link TextMessage} with the specified contents to the
    * message destination link as configured by ejb-jar.xml
    * 
    * @throws IllegalArgumentException If the contents are not specified
    */
   void sendMessage(String contents) throws IllegalArgumentException;
}
