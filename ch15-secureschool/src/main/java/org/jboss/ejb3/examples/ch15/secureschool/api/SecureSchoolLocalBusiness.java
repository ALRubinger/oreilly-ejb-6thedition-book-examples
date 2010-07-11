/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.examples.ch15.secureschool.api;

import org.jboss.ejb3.examples.ch15.secureschool.impl.Roles;

/**
 * Represents a school holding doors which may be 
 * opened by various users.  Using the EJB Security model,
 * access to open a particular door may be blocked
 * to certain users.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface SecureSchoolLocalBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Closes the school for business.  At this point the
    * front door will be unlocked for all.
    * This method may only be called by users in role
    * {@link Roles#ADMIN}.
    */
   void open();

   /**
    * Closes the school for business.  At this point the
    * front door will be locked for all but users
    * in role {@link Roles#ADMIN}
    * This method may only be called by admins.
    */
   void close();

   /**
    * Opens the front door.  While school is open, 
    * any authenticated user may open the door, else 
    * only the {@link Roles#ADMIN} may open.
    * 
    * @throws SchoolClosedException If the current user
    * is not in {@link Roles#ADMIN} and is attempting to open 
    * the door while {@link SecureSchoolLocalBusiness#isOpen()}
    * is false.
    */
   void openFrontDoor() throws SchoolClosedException;

   /**
    * Opens the service door. Users in {@link Roles#STUDENT}
    * role may not open this door, but {@link Roles#ADMIN}
    * and {@link Roles#JANITOR} may.
    */
   void openServiceDoor();

   /**
    * Returns whether or not the school is open.  When closed, only
    * the {@link Roles#ADMIN} is allowed access to all doors.  Anyone, 
    * even unauthenticated users, may check if school is open. 
    * @return
    */
   boolean isOpen();

}
