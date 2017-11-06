/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

import java.util.Optional;

import org.csstudio.display.builder.model.Messages;
import org.csstudio.display.builder.model.RuntimeWidgetProperty;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;

/** Widget property for 'insets'
 *
 *  <p>Used by containers like group or tab
 *  to track the offset of contained widgets
 *  from the origin of the container.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class InsetsWidgetProperty extends RuntimeWidgetProperty<int[]>
{
    public static final WidgetPropertyDescriptor<int[]> runtimePropInsets =
        new WidgetPropertyDescriptor<int[]>(
            WidgetPropertyCategory.RUNTIME, "insets", Messages.WidgetProperties_Insets)
    {
        @Override
        public WidgetProperty<int[]> createProperty(final Widget widget,
                                                    final int[] default_value)
        {
            return new InsetsWidgetProperty(widget, default_value);
        }
    };

    /** Get insets of a container
     *
     *  @param container Presumably a container widget
     *  @return Insets [x, y] or <code>null</code>
     */
    public static int[] getInsets(final Widget container)
    {
        final Optional<WidgetProperty<int[]>> insets = container.checkProperty(runtimePropInsets);
        if (insets.isPresent())
            return insets.get().getValue();
        return null;
    }

    public InsetsWidgetProperty(final Widget widget, final int[] default_value)
    {
        super(runtimePropInsets, widget, default_value);
    }

    @Override
    protected int[] restrictValue(final int[] requested_value)
    {
        if (requested_value.length != 2)
            throw new IllegalArgumentException("Need int[2], got " + requested_value);
        return requested_value;
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        if (value instanceof int[]  &&  ((int[]) value).length == 2)
            setValue((int[]) value);
        else
            throw new Exception("Need int[2], got " + value);
    }
}
