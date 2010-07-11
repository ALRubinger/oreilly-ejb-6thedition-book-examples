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
package org.jboss.ejb3.examples.ch08.statusupdate.mdb;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * A Mock {@link ObjectMessage} which supports only the 
 * {@link ObjectMessage#getObject()} method; used in testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class MockObjectMessage implements ObjectMessage
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static final String MESSAGE_UNSUPPORTED = "This mock implementation does not support this operation";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Object contained in this message
    */
   private final Serializable object;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new instance with the specified backing object 
    * to be returned by {@link ObjectMessage#getObject()}
    */
   MockObjectMessage(final Serializable object)
   {
      this.object = object;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see javax.jms.ObjectMessage#getObject()
    */
   @Override
   public Serializable getObject() throws JMSException
   {
      return this.object;
   }

   //-------------------------------------------------------------------------------------||
   // Unsupported ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /*
    * Everything below this line will throw an exception when invoked
    */

   /* (non-Javadoc)
    * @see javax.jms.ObjectMessage#setObject(java.io.Serializable)
    */
   @Override
   public void setObject(Serializable object) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#acknowledge()
    */
   @Override
   public void acknowledge() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#clearBody()
    */
   @Override
   public void clearBody() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#clearProperties()
    */
   @Override
   public void clearProperties() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getBooleanProperty(java.lang.String)
    */
   @Override
   public boolean getBooleanProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getByteProperty(java.lang.String)
    */
   @Override
   public byte getByteProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getDoubleProperty(java.lang.String)
    */
   @Override
   public double getDoubleProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getFloatProperty(java.lang.String)
    */
   @Override
   public float getFloatProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getIntProperty(java.lang.String)
    */
   @Override
   public int getIntProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSCorrelationID()
    */
   @Override
   public String getJMSCorrelationID() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
    */
   @Override
   public byte[] getJMSCorrelationIDAsBytes() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSDeliveryMode()
    */
   @Override
   public int getJMSDeliveryMode() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSDestination()
    */
   @Override
   public Destination getJMSDestination() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSExpiration()
    */
   @Override
   public long getJMSExpiration() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSMessageID()
    */
   @Override
   public String getJMSMessageID() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSPriority()
    */
   @Override
   public int getJMSPriority() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSRedelivered()
    */
   @Override
   public boolean getJMSRedelivered() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSReplyTo()
    */
   @Override
   public Destination getJMSReplyTo() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSTimestamp()
    */
   @Override
   public long getJMSTimestamp() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getJMSType()
    */
   @Override
   public String getJMSType() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getLongProperty(java.lang.String)
    */
   @Override
   public long getLongProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getObjectProperty(java.lang.String)
    */
   @Override
   public Object getObjectProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getPropertyNames()
    */
   @Override
   public Enumeration getPropertyNames() throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getShortProperty(java.lang.String)
    */
   @Override
   public short getShortProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#getStringProperty(java.lang.String)
    */
   @Override
   public String getStringProperty(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#propertyExists(java.lang.String)
    */
   @Override
   public boolean propertyExists(String name) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
    */
   @Override
   public void setBooleanProperty(String name, boolean value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
    */
   @Override
   public void setByteProperty(String name, byte value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
    */
   @Override
   public void setDoubleProperty(String name, double value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
    */
   @Override
   public void setFloatProperty(String name, float value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setIntProperty(java.lang.String, int)
    */
   @Override
   public void setIntProperty(String name, int value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
    */
   @Override
   public void setJMSCorrelationID(String correlationID) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
    */
   @Override
   public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSDeliveryMode(int)
    */
   @Override
   public void setJMSDeliveryMode(int deliveryMode) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
    */
   @Override
   public void setJMSDestination(Destination destination) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSExpiration(long)
    */
   @Override
   public void setJMSExpiration(long expiration) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSMessageID(java.lang.String)
    */
   @Override
   public void setJMSMessageID(String id) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSPriority(int)
    */
   @Override
   public void setJMSPriority(int priority) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSRedelivered(boolean)
    */
   @Override
   public void setJMSRedelivered(boolean redelivered) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
    */
   @Override
   public void setJMSReplyTo(Destination replyTo) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSTimestamp(long)
    */
   @Override
   public void setJMSTimestamp(long timestamp) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setJMSType(java.lang.String)
    */
   @Override
   public void setJMSType(String type) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setLongProperty(java.lang.String, long)
    */
   @Override
   public void setLongProperty(String name, long value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setObjectProperty(java.lang.String, java.lang.Object)
    */
   @Override
   public void setObjectProperty(String name, Object value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setShortProperty(java.lang.String, short)
    */
   @Override
   public void setShortProperty(String name, short value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see javax.jms.Message#setStringProperty(java.lang.String, java.lang.String)
    */
   @Override
   public void setStringProperty(String name, String value) throws JMSException
   {
      throw new UnsupportedOperationException(MESSAGE_UNSUPPORTED);
   }
}
