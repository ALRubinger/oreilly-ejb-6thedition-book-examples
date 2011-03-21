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
package org.jboss.ejb3.examples.ch08.messagedestinationlink.api;

/**
 * Contains constants used in referring to resources shared
 * by clients of the MessageDestinationLink EJBs.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface MessageDestinationLinkConstants
{
   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * JNDI Name of the JMS Connection Factory
    */
   String JNDI_NAME_CONNECTION_FACTORY = "ConnectionFactory";

   /**
    * Name of the Message Destination Link Ref
    */
   String NAME_MESSAGE_DESTINATION_LINK_REF = "queue/MessageDestinationLinkQueue"; // To match XML message-destination-ref-name

   /**
    * The type of destination used by StatusUpdate MDB implementations
    */
   String TYPE_DESTINATION = "javax.jms.Queue";

}
