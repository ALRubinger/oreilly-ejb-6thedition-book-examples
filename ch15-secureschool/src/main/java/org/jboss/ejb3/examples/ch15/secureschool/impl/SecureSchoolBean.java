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
package org.jboss.ejb3.examples.ch15.secureschool.impl;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.ejb3.examples.ch15.secureschool.api.SchoolClosedException;
import org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness;

/**
 * A secure school which may block requests to 
 * open doors depending upon the EJB Security 
 * model's configuration
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Singleton
@Local(SecureSchoolLocalBusiness.class)
// Declare the roles in the system
@DeclareRoles(
{Roles.ADMIN, Roles.STUDENT, Roles.JANITOR})
// By default allow no one access, we'll enable access at a finer-grained level
@RolesAllowed(
{})
@Startup
public class SecureSchoolBean implements SecureSchoolLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(SecureSchoolBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Whether or not the school is open
    */
   private boolean open;

   /**
    * Hook to the container to get security information
    */
   @Resource
   private SessionContext context;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness#openFrontDoor()
    */
   // Give everyone access to this method, we may restrict them later
   @RolesAllowed(
   {Roles.ADMIN, Roles.STUDENT, Roles.JANITOR})
   @Override
   public void openFrontDoor()
   {
      // If we've reached this point, EJB security has let us through.  However,
      // we may want to apply some contextual rules.  Because EJB security is
      // declarative at the method level, we use the API to enforce specific logic.

      // Get the caller
      final String callerName = context.getCallerPrincipal().getName();

      // Ensure the school is open
      if (!open)
      {
         // School's closed, so only let admins open the door
         if (!context.isCallerInRole(Roles.ADMIN))
         {
            // Kick 'em out
            throw SchoolClosedException
                  .newInstance("Attempt to open the front door after hours is prohibited to all but admins, denied to: "
                        + callerName);
         }
      }

      // Log
      log.info("Opening front door for: " + callerName);
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness#openServiceDoor()
    */
   @RolesAllowed(
   {Roles.ADMIN, Roles.JANITOR})
   // Students cannot open this door
   @Override
   public void openServiceDoor()
   {
      log.info("Opening service door for: " + context.getCallerPrincipal().getName());
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness#close()
    */
   @RolesAllowed(Roles.ADMIN)
   // Only let admins open and close the school
   @Override
   public void close()
   {
      this.open = false;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness#open()
    */
   @Override
   @PostConstruct
   // School is open when created  
   @RolesAllowed(Roles.ADMIN)
   // Only let admins open and close the school
   public void open()
   {
      this.open = true;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness#isOpen()
    */
   @Override
   @PermitAll
   // Anyone can check if school is open
   public boolean isOpen()
   {
      return open;
   }
}
