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

/**
 * Holds the list of roles with which users of the school 
 * may be affiliated.  EJB Security is role-based, so this
 * is how we'll determine access.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface Roles
{
   // ---------------------------------------------------------------------------||
   // Constants -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * Roles of callers to the system
    */

   /**
    * Role denoting the user is a school administrator
    */
   String ADMIN = "Administrator";

   /**
    * Role denoting the user is a student
    */
   String STUDENT = "Student";

   /**
    * Role denoting the user is a janitor
    */
   String JANITOR = "Janitor";

}
