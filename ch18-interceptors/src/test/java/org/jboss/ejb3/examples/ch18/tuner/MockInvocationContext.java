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
package org.jboss.ejb3.examples.ch18.tuner;

import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * {@link InvocationContext} implementation which throws {@link UnsupportedOperationException}
 * for all required methods except {@link InvocationContext#proceed()}, which will always return null,
 * {@link InvocationContext#getMethod()}, and {@link InvocationContext#getParameters()}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
class MockInvocationContext implements InvocationContext
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Message used to denote that the operation is not supported 
    */
   private static final String MSG_UNSUPPORTED = "Not supported in mock implementation";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Method invoked
    */
   private final Method method;

   /**
    * Parameters in the request
    */
   private final Object[] params;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new instance with the specified required arguments
    * @param method
    * @param params
    */
   MockInvocationContext(final Method method, final Object[] params)
   {

      assert method != null : "method must be specified";
      assert params != null : "params must be specified";
      this.method = method;
      this.params = params;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Override
   public Map<String, Object> getContextData()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public Method getMethod()
   {
      return method;
   }

   @Override
   public Object[] getParameters()
   {
      return params;
   }

   @Override
   public Object getTarget()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public Object proceed() throws Exception
   {
      return null;
   }

   @Override
   public void setParameters(final Object[] arg0)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   @Override
   public Object getTimer() {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }
}
