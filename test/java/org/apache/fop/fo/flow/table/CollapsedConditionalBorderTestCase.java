/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.flow.table;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode.FONodeIterator;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.junit.Test;

/**
 * A testcase for the resolution of collapsed borders in the FO tree, taking
 * conditionality into account. The resolved borders are generated by the
 * collapsed-conditional-borders_test-generator.py Python script.
 */
public class CollapsedConditionalBorderTestCase extends AbstractTableTest {

    private final Integer border0pt = new Integer(0);

    private final Integer border4pt = new Integer(4000);

    private final Integer border6pt = new Integer(6000);

    private final Integer border8pt = new Integer(8000);

    /**
     * Resolved borders for tables without headers and footers, generated from the Python
     * script.
     */
    private Object[][][] resolvedBorders = {
            {{border0pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.magenta}},
            {{border6pt, Color.blue}, {border6pt, Color.yellow}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.magenta}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.blue}},
            {{border6pt, Color.magenta}, {border6pt, Color.blue}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.yellow}},
            {{border6pt, Color.magenta}, {border6pt, Color.yellow}},
            {{border6pt, Color.blue}, {border6pt, Color.yellow}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border6pt, Color.red}},
            {{border6pt, Color.magenta}, {border6pt, Color.blue}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.magenta}},
            {{border6pt, Color.yellow}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.blue}},
            {{border6pt, Color.yellow}, {border6pt, Color.magenta}},
            {{border6pt, Color.blue}, {border6pt, Color.yellow}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border6pt, Color.red}, {border6pt, Color.magenta}},
            {{border6pt, Color.blue}, {border6pt, Color.yellow}},
            {{border8pt, Color.black}, {border8pt, Color.black}},
            {{border8pt, Color.black}, {border8pt, Color.black}}
    };

    /**
     * Resolved borders for tables with headers and footers, generated from the Python
     * script.
     */
    private Object[][][] resolvedBordersHF = {
{{border8pt, Color.black}, {border6pt, Color.black}, {border8pt, Color.black}, {border6pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border8pt, Color.blue}, {border6pt, Color.black}, {border4pt, Color.magenta}, {border8pt, Color.blue}, {border8pt, Color.blue}, {border4pt, Color.red}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border8pt, Color.blue}, {border6pt, Color.black}, {border6pt, Color.black}, {border8pt, Color.blue}, {border8pt, Color.blue}, {border4pt, Color.red}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.red}, {border8pt, Color.red}, {border8pt, Color.red}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.red}, {border8pt, Color.red}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.red}, {border8pt, Color.red}, {border8pt, Color.red}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.magenta}, {border6pt, Color.blue}, {border8pt, Color.red}, {border8pt, Color.red}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.black}, {border8pt, Color.black}, {border6pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border8pt, Color.black}, {border8pt, Color.black}, {border4pt, Color.magenta}, {border8pt, Color.black}, {border6pt, Color.blue}, {border4pt, Color.red}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border8pt, Color.black}, {border8pt, Color.black}, {border4pt, Color.magenta}, {border8pt, Color.black}, {border6pt, Color.blue}, {border6pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.blue}, {border4pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border6pt, Color.black}, {border8pt, Color.magenta}, {border8pt, Color.magenta}, {border6pt, Color.black}, {border4pt, Color.blue}, {border4pt, Color.red}, {border8pt, Color.magenta}, {border8pt, Color.magenta}, {border8pt, Color.magenta}},
{{border8pt, Color.black}, {border6pt, Color.blue}, {border8pt, Color.black}, {border6pt, Color.blue}, {border4pt, Color.black}, {border4pt, Color.black}, {border4pt, Color.red}, {border8pt, Color.black}, {border8pt, Color.black}, {border8pt, Color.black}, {border8pt, Color.black}, {border4pt, Color.blue}, {border4pt, Color.red}, {border6pt, Color.magenta}, {border6pt, Color.magenta}, {border6pt, Color.magenta}}
    };

    public CollapsedConditionalBorderTestCase() throws Exception {
        super();
    }

    private static GridUnit getGridUnit(TablePart part) {
        return (GridUnit) ((List) ((List) part.getRowGroups().get(0)).get(0)).get(0);
    }

    private static void checkBorder(String errorMsge, BorderSpecification border,
            int expectedLength, Color expectedColor) {
        BorderInfo borderInfo = border.getBorderInfo();
        if (expectedLength == 0) {
            assertEquals(errorMsge, Constants.EN_NONE, borderInfo.getStyle());
        } else {
            assertEquals(errorMsge, expectedLength, borderInfo.getWidth().getLengthValue());
            assertEquals(errorMsge, expectedColor, borderInfo.getColor());
        }
    }

    private static void checkBorder(String errorMsge, BorderSpecification border,
            Object[] resolvedBorder) {
        checkBorder(errorMsge, border,
                ((Integer) resolvedBorder[0]).intValue(),
                (Color) resolvedBorder[1]);
    }

    @Test
    public void testCollapsedConditionalBorders() throws Exception {
        setUp("table/collapsed-conditional-borders.fo");
        int tableNum = 0;
        Iterator tableIterator = getTableIterator();
        do {
            String baseErrorMsge = "table " + Integer.toString(tableNum) + " (0-based), ";
            Table table = (Table) tableIterator.next();
            TablePart part = (TablePart) table.getChildNodes().nextNode();
            GridUnit gu = getGridUnit(part);

            String errorMsge = baseErrorMsge + "border-before";
            checkBorder(errorMsge, gu.borderBefore.normal, 8000, Color.black);
            checkBorder(errorMsge, gu.borderBefore.leadingTrailing, 8000, Color.black);
            checkBorder(errorMsge, gu.borderBefore.rest, resolvedBorders[tableNum][0]);

            errorMsge = baseErrorMsge + "border-after";
            checkBorder(errorMsge, gu.borderAfter.normal, 8000, Color.black);
            checkBorder(errorMsge, gu.borderAfter.leadingTrailing, 8000, Color.black);
            checkBorder(errorMsge, gu.borderAfter.rest, resolvedBorders[tableNum][1]);

            tableNum++;
        } while (tableIterator.hasNext());
    }

    @Test
    public void testCollapsedConditionalBordersHeaderFooter() throws Exception {
        setUp("table/collapsed-conditional-borders_header-footer.fo");
        int tableNum = 0;
        Iterator tableIterator = getTableIterator();
        do {
            String errorMsge = "table " + Integer.toString(tableNum) + " (0-based)";
            int borderNum = 0;
            Table table = (Table) tableIterator.next();

            TableHeader header = table.getTableHeader();
            GridUnit gu = getGridUnit(header);
            checkBorder(errorMsge, gu.borderBefore.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderBefore.rest,
                    resolvedBordersHF[tableNum][borderNum++]);

            TableFooter footer = table.getTableFooter();
            gu = getGridUnit(footer);
            checkBorder(errorMsge, gu.borderAfter.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.rest,
                    resolvedBordersHF[tableNum][borderNum++]);

            FONodeIterator bodyIter = table.getChildNodes();
            TableBody body = (TableBody) bodyIter.nextNode();
            gu = getGridUnit(body);
            checkBorder(errorMsge, gu.borderBefore.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderBefore.leadingTrailing,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderBefore.rest,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.leadingTrailing,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.rest,
                    resolvedBordersHF[tableNum][borderNum++]);

            body = (TableBody) bodyIter.nextNode();
            gu = getGridUnit(body);
            checkBorder(errorMsge, gu.borderBefore.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderBefore.leadingTrailing,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderBefore.rest,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.normal,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.leadingTrailing,
                    resolvedBordersHF[tableNum][borderNum++]);
            checkBorder(errorMsge, gu.borderAfter.rest,
                    resolvedBordersHF[tableNum][borderNum++]);

            tableNum++;
        } while (tableIterator.hasNext());
    }
}
