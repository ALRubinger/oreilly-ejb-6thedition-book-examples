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
package org.jboss.ejb3.examples.employeeregistry.ch14.listener;

import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;

/**
 * Represents an Employee which is able to receive JPA
 * events.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class EntityListenerEmployee extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EntityListenerEmployee.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name of the employee
    */
   private String name;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * No-arg constructor, required by JPA
    */
   public EntityListenerEmployee()
   {

   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(final String name)
   {
      this.name = name;
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /** 
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return EntityListenerEmployee.class.getSimpleName() + " [name=" + name + ", getId()=" + getId() + "]";
   }

   //-------------------------------------------------------------------------------------||
   // Event Listeners --------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /*
    * Event Listeners; fired by JPA and track state in the EventTracker
    */

   @PrePersist
   @SuppressWarnings("unused")
   private void prePersist()
   {
      EventTracker.prePersist = true;
      log.info("prePersist: " + this);
   }

   @PostPersist
   @SuppressWarnings("unused")
   private void postPersist()
   {
      EventTracker.postPersist = true;
      log.info("postPersist: " + this);
   }

   @PostLoad
   @SuppressWarnings("unused")
   private void postLoad()
   {
      EventTracker.postLoad = true;
      log.info("postLoad: " + this);
   }

   @PreUpdate
   @SuppressWarnings("unused")
   private void preUpdate()
   {
      EventTracker.preUpdate = true;
      log.info("preUpdate: " + this);
   }

   @PostUpdate
   @SuppressWarnings("unused")
   private void postUpdate()
   {
      EventTracker.postUpdate = true;
      log.info("postUpdate: " + this);
   }

   @PreRemove
   @SuppressWarnings("unused")
   private void preRemove()
   {
      EventTracker.preRemove = true;
      log.info("preRemove: " + this);
   }

   @PostRemove
   @SuppressWarnings("unused")
   private void postRemove()
   {
      EventTracker.postRemove = true;
      log.info("postRemove: " + this);
   }

}
