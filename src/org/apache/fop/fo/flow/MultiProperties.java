/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 */
public class MultiProperties extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new MultiProperties(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new MultiProperties.Maker();
    }

    protected MultiProperties(FObj parent,
                              PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:multi-properties";
    }

    public int layout(Area area) throws FOPException {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // this.properties.get("id");

        return super.layout(area);
    }
}
