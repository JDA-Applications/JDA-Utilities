package com.jagrosh.jdautilities.commons.utils;

import net.dv8tion.jda.core.utils.Checks;

import java.util.Arrays;

/**
 * A utility class that can be used to easily create String tables in Java without any extra frameworks.
 * This can be useful to display table-like structures in Discord codeblocks, for example.
 * <p>The tables usually look like this:
 * <code>
 *     Header[0]  |Header[1]  |Header[2]
 *     -----------+-----------+-----------
 *     Value[0][1]|Value[0][1]|Value[0][2]
 *     Value[1][0]|Value[1][1]|Value[1][2]
 * </code>
 * <br>If framing is activated, it looks like this:
 * <code>
 *     -------------------------------------
 *     |Header[0]  |Header[1]  |Header[2]  |
 *     ------------+-----------+------------
 *     |Value[0][1]|Value[0][1]|Value[0][2]|
 *     ------------+-----------+------------
 *     |Value[1][0]|Value[1][1]|Value[1][2]|
 *     -------------------------------------
 * </code>
 * <br>The characters used to display the table can be configured individually.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class TableBuilder
{

    /**
     * Represents a left alignment.
     */
    public static final int LEFT = -1;

    /**
     * Represents a right alignment.
     */
    public static final int RIGHT = 1;

    /**
     * Represents a centered alignment.
     */
    public static final int CENTER = 0;

    private String[][] values;
    private String[] headers;
    private String[] rowNames;

    private String tableName = "";

    private char headerDelimiter = '═';
    private char rowDelimiter = headerDelimiter;
    private char columnDelimiter = '║';
    private char firstColumnDelimiter = columnDelimiter;
    private char crossDelimiter = '╬';
    private char headerCrossDelimiter = crossDelimiter;

    private char horizontalOutline = rowDelimiter;
    private char verticalOutline = columnDelimiter;

    private char leftIntersection = '╠';
    private char rightIntersection = '╣';
    private char upperIntersection = '╦';
    private char lowerIntersection = '╩';

    private char upLeftCorner = '╔';
    private char upRightCorner = '╗';
    private char lowLeftCorner = '╚';
    private char lowRightCorner = '╝';

    private int alignment = CENTER;

    private boolean codeblock = false;
    private boolean frame = false;
    private boolean autoAdjust = true;

    /**
     * Builds a String table according to the settings made.
     *
     * @throws  IllegalArgumentException if:
     *          <ul>
     *              <li>No headers were set</li>
     *              <li>No values were set</li>
     *              <li>The values specify more columns than headers provided</li>
     *              <li>Row names were set and there are more rows in the values than row names given</li>
     *              <li>autoAdjust is false and any value is longer than the corresponding header</li>
     *          </ul>
     *          Note that empty values are fine. Empty headers however cannot work if any values are given.
     *
     * @return  The table as a String.
     *          If framing is activated, there are strict borders around each cell.
     *          If codeblock is activated, there will be a {@code ```\n} at the beginning and a {@code ```} at the end.
     */
    public String build()
    {
        Checks.check(headers != null, "Must set headers");
        Checks.check(values != null, "Must set values");
        Checks.check(Arrays.stream(values).noneMatch((row) -> row.length > headers.length), "Values must not have more columns than headers provided");

        if (rowNames != null)
        {
            Checks.check(tableName != null, "Table name must not be null");
            Checks.check(values.length <= rowNames.length, "Values must not have more rows than specified by optional row names");

            String[] newHeaders = new String[headers.length + 1];
            newHeaders[0] = tableName;
            if (headers.length > 0)
                System.arraycopy(headers, 0, newHeaders, 1, headers.length);
            this.headers = newHeaders;

            String[][] newValues = new String[values.length][headers.length];
            for (int i = 0; i < newValues.length && i < values.length; i++)
            {
                newValues[i][0] = rowNames[i];
                if (values[i].length > 0)
                    System.arraycopy(values[i], 0, newValues[i], 1, values[i].length);
            }
            this.values = newValues;
        }

        if (autoAdjust)
        {
            // find the max. value for each column and align headers
            for (int i = 0; i < headers.length; i++)
            {
                String header = headers[i];
                int max = 0;
                for (String[] row : values)
                {
                    String value = row[i];
                    if (value.length() > max)
                        max = value.length();
                }

                int length = header.length();
                if (max > length)
                {
                    StringBuilder newHeader = new StringBuilder();
                    int adjustment = max - length;
                    this.setAlignment(adjustment, header, newHeader);

                    headers[i] = newHeader.toString();
                }
            }

            // align values
            for (String[] row : values)
            {
                for (int j = 0; j < row.length; j++)
                {
                    int adjustment = headers[j].length() - row[j].length();
                    String value = row[j];
                    StringBuilder newValue = new StringBuilder();
                    this.setAlignment(adjustment, value, newValue);
                    row[j] = newValue.toString();
                }
            }
        }
        else
        {
            boolean check = true;
            out:
            for (String[] row : values)
            {
                for (int i = 0; i < headers.length; i++)
                {
                    if (row[i].length() > headers[i].length())
                    {
                        check = false;
                        break out;
                    }
                }
            }
            Checks.check(check, "Length of values must not be longer than length of headers");
        }

        StringBuilder builder = new StringBuilder();

        if (codeblock)
            builder.append("```\n");

        if (frame)
        {
            this.appendHorizontalOutline(builder, upLeftCorner, upperIntersection, upRightCorner); // append upper outline
            builder.append("\n");
        }

        if (frame)
            builder.append(verticalOutline);

        // append headers
        for (int i = 0; i < headers.length; i++)
        {
            builder.append(headers[i]);
            if (i < headers.length - 1)
                builder.append(columnDelimiter);
        }

        if (frame)
            builder.append(verticalOutline);

        builder.append("\n");

        this.appendHorizontalDelimiter(builder, headerDelimiter, headerCrossDelimiter); // line below headers

        builder.append("\n");

        // append row after row
        for (int i = 0; i < values.length; i++)
        {
            String[] row = values[i];
            if (frame)
                builder.append(verticalOutline);

            for (int j = 0; j < row.length; j++)
            {
                builder.append(row[j]);

                if (j == 0)
                    builder.append(firstColumnDelimiter);
                else if (j < row.length - 1)
                    builder.append(columnDelimiter);
            }

            if (frame)
                builder.append(verticalOutline);

            builder.append("\n");

            // if framing is activated: line between rows
            if (frame && i < values.length - 1)
            {
                this.appendHorizontalDelimiter(builder, rowDelimiter, crossDelimiter);
                builder.append("\n");
            }
        }

        if (frame)
            this.appendHorizontalOutline(builder, lowLeftCorner, lowerIntersection, lowRightCorner); // append lower outline

        if (codeblock)
            builder.append("```");

        return builder.toString();

    }

    private void setAlignment(int adjustment, String oldValue, StringBuilder newValueBuilder) {
        if (alignment > 0) // right alignment
        {
            // first black spaces
            for (int k = 0; k < adjustment; k++)
                newValueBuilder.append(" ");
            newValueBuilder.append(oldValue); // then value
        }
        else if (alignment < 0) // left alignment
        {
            newValueBuilder.append(oldValue); // first value
            // then blank spaces
            for (int k = 0; k < adjustment; k++)
                newValueBuilder.append(" ");
        }
        else
        {
            boolean even = adjustment % 2 == 0;
            int half = adjustment / 2;
            for (int k = 0; k < half; k++) // append one half of black spaces
                newValueBuilder.append(" ");

            newValueBuilder.append(oldValue); // append value

            for (int k = 0; k < half; k++) // append other half of blank spaces
                newValueBuilder.append(" ");

            if (!even) // if the number wasn't event, one blank space is still missing
                newValueBuilder.append(" ");
        }
    }

    private void appendHorizontalOutline(StringBuilder builder, char leftCorner, char intersection, char rightCorner)
    {
        builder.append(leftCorner);
        appendHorizontally(builder, horizontalOutline, intersection);
        builder.append(rightCorner);
    }

    private void appendHorizontalDelimiter(StringBuilder builder, char rowDelimiter, char crossDelimiter)
    {
        if (frame)
            builder.append(leftIntersection);

        appendHorizontally(builder, rowDelimiter, crossDelimiter);

        if (frame)
            builder.append(rightIntersection);
    }

    private void appendHorizontally(StringBuilder builder, char rowDelimiter, char crossDelimiter) {
        for (int j = 0; j < headers.length; j++)
        {
            String header = headers[j];
            for (int k = 0; k < header.length(); k++)
                builder.append(rowDelimiter);

            if (j < headers.length - 1)
                builder.append(crossDelimiter);
        }
    }

    /**
     * Sets the headers of this table.
     *
     * @param  headers
     *         The headers as varargs, so either a String[] or single Strings.
     *
     * @return this
     */
    public TableBuilder setHeaders(String... headers)
    {
        this.headers = headers;
        return this;
    }

    /**
     * Sets names for the rows specified in {@link this#setValues(String[][]) values}, applied in the order given here.
     * <br>This is optional. By default, there will not be any row names.
     *
     * @param  rows
     *         The row names as varargs, so either a String[] or single Strings.
     *
     * @return This builder.
     *
     * @see    this#setFirstColumnDelimiter(char)
     *
     * @see    this#setName(String)
     */
    public TableBuilder setRowNames(String... rows)
    {
        this.rowNames = rows;
        return this;
    }

    /**
     * Sets the values of this table.
     *
     * @param  values
     *         The values as a 2D-Array. The arrays inside of that array each represent a row.
     *         Each value inside a row will be placed in the table according to its index, which describes the Y-position.
     *
     * @return this
     */
    public TableBuilder setValues(String[][] values)
    {
        this.values = values;
        return this;
    }

    /**
     * Sets the name of the table. This will be displayed in the upper left corner cell if row names were set.
     *
     * @param  tableName
     *         The name of the table as a String. Default: empty String
     *
     * @return This builder.
     *
     * @see    this#setRowNames(String...)
     */
    public TableBuilder setName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    /**
     * Sets the delimiter to be placed between the rows if framing is activated.
     *
     * @param  rowDelimiter
     *         The character used to separate the rows from each other. Default: {@code ═}
     *
     * @return this
     */
    public TableBuilder setRowDelimiter(char rowDelimiter)
    {
        this.rowDelimiter = rowDelimiter;
        return this;
    }

    /**
     * Sets the delimiter to be placed between the columns.
     *
     * @param  columnDelimiter
     *         The character used to separate the columns form each other. Default: {@code ║}
     *
     * @return this
     */
    public TableBuilder setColumnDelimiter(char columnDelimiter)
    {
        this.columnDelimiter = columnDelimiter;
        return this;
    }

    /**
     * The character to be placed between the first and the second column. This can be useful in combination with row names.
     *
     * @param  firstColumnDelimiter
     *         The character after the first column. Default: {@code ║} (same as column delimiter)
     *
     * @return This builder.
     *
     * @see    this#setRowNames(String...)
     */
    public TableBuilder setFirstColumnDelimiter(char firstColumnDelimiter) {
        this.firstColumnDelimiter = firstColumnDelimiter;
        return this;
    }

    /**
     * Sets the delimiter to be placed where the vertical and horizontal lines inside the table
     * would cross, if framing is activated.
     *
     * @param  crossDelimiter
     *         The character to use for that purpose. Default: {@code ╬}
     *
     * @return this
     */
    public TableBuilder setCrossDelimiter(char crossDelimiter)
    {
        this.crossDelimiter = crossDelimiter;
        return this;
    }

    /**
     * Sets the delimiter to be placed where the header delimiters and column delimiters would cross.
     *
     * @param  headerCrossDelimiter
     *         The character to be used for that purpose. Default: {@code ╬} (same as cross delimiter)
     *
     * @return this
     */
    public TableBuilder setHeaderCrossDelimiter(char headerCrossDelimiter)
    {
        this.headerCrossDelimiter = headerCrossDelimiter;
        return this;
    }

    /**
     * Sets the delimiter to be placed below the headers.
     *
     * @param  headerDelimiter
     *         The character used to separate the headers from the rest of the table. Default: {@code ═} (same as row delimiter)
     *
     * @return This builder.
     *
     * @see    this#setRowDelimiter(char)
     */
    public TableBuilder setHeaderDelimiter(char headerDelimiter)
    {
        this.headerDelimiter = headerDelimiter;
        return this;
    }

    /**
     * Sets the char used as the vertical outline of the table (i.e. upper and lower border) if framing is activated.
     *
     * @param  verticalOutline
     *         The character to use for that purpose. Default: {@code ═} (same as row delimiter)
     *
     * @return This builder.
     */
    public TableBuilder setVerticalOutline(char verticalOutline)
    {
        this.verticalOutline = verticalOutline;
        return this;
    }

    /**
     * Sets the char used as the horizontal outline of the table (i.e. left and right border) if framing is activated.
     *
     * @param  horizontalOutline
     *         The character to use for that purpose. Default: {@code ║} (same as column delimiter)
     *
     * @return This builder.
     */
    public TableBuilder setHorizontalOutline(char horizontalOutline)
    {
        this.horizontalOutline = horizontalOutline;
        return this;
    }

    /**
     * Sets the characters to be placed where the outlines cross the row or column delimiters if framing is activated.
     *
     * @param  left
     *         The character for intersections at the left. Default: {@code ╠}
     *
     * @param  right
     *         The character for intersections at the right. Default: {@code ╣}
     *
     * @param  upper
     *         The character for upper intersections. Default: {@code ╦}
     *
     * @param  lower
     *         The character for lower intersections. Default: {@code ╩}
     *
     * @return This builder.
     */
    public TableBuilder setIntersections(char left, char right, char upper, char lower)
    {
        this.leftIntersection = left;
        this.rightIntersection = right;
        this.upperIntersection = upper;
        this.lowerIntersection = lower;
        return this;
    }

    /**
     * Sets the characters to be placed in the corners of the table if framing is activated.
     *
     * @param  upLeft
     *         The character for the upper left corner. Default: {@code ╔}
     *
     * @param  upRight
     *         The character for the upper right corner. Default: {@code ╗}
     *
     * @param  lowLeft
     *         The character for the lower left corner. Default: {@code ╚}
     *
     * @param  lowRight
     *         The character for the lower right corner. Default: {@code ╝}
     *
     * @return This builder.
     */
    public TableBuilder setCorners(char upLeft, char upRight, char lowLeft, char lowRight)
    {
        this.upLeftCorner = upLeft;
        this.upRightCorner = upRight;
        this.lowLeftCorner = lowLeft;
        this.lowRightCorner = lowRight;
        return this;
    }

    /**
     * Sets the alignment/orientation of all headers and values.
     * This will be applied if and only if autoAdjust is {@code true}.
     * <br>By default, this is set to {@link TableBuilder#CENTER CENTER}.
     *
     * @param  alignment
     *         The alignment n to set.
     *         <ul>
     *             <li>n = 0 results in a centered alignment.</li>
     *             <li>n < 0 results in a left alignment.</li>
     *             <li>n > 0 results in a right alignment.</li>
     *         </ul>
     *         It is recommended to use the predefined constants of this class for that purpose.
     *
     * @return This builder.
     *
     * @see    this#autoAdjust(boolean)
     * @see    this#CENTER
     * @see    this#LEFT
     * @see    this#RIGHT
     */
    public TableBuilder setAlignment(int alignment)
    {
        this.alignment = alignment;
        return this;
    }

    /**
     * Sets whether the table should be embedded in a markdown code block (no special semantics).
     * By default, this is {@code false}.
     *
     * @param  codeblock
     *         {@code true}, if the table should be in a markdown code block.
     *
     * @return This builder.
     */
    public TableBuilder codeblock(boolean codeblock)
    {
        this.codeblock = codeblock;
        return this;
    }

    /**
     * Sets whether the table should be framed.
     * By default, this is {@code false}.
     *
     * @param  frame
     *         {@code true}, if the table should have outlines and the cells should have borders.
     *
     * @return This builder.
     */
    public TableBuilder frame(boolean frame)
    {
        this.frame = frame;
        return this;
    }

    /**
     * Sets whether the table should be adjusted automatically according to the lengths of the values.
     * By default, this is {@code true}.
     *
     * @param  autoAdjust
     *         {@code true}, if the table should evaluate the space needed automatically.
     *         {@code false}, if you want every value to have max. the same length as the corresponding header and it should be fixed.
     *
     * @return This builder.
     */
    public TableBuilder autoAdjust(boolean autoAdjust)
    {
        this.autoAdjust = autoAdjust;
        return this;
    }

    // testing
    /*public static void main(String[] args) {
        String table = new TableBuilder().setHeaders("Header 1", "Header 2", "Header 3")
            .setName("Sample Name")
            .setValues(new String[][] {
                {"Item 1", "Item 2", "Item 3"},
                {"Item 4", "Item 5", "Item 6"},
                {"Item 7", "Item 8", "Item 9"}
            }).setRowNames("Row 1", "Row 2", "Row 3").frame(true).build();
        System.out.println(table);
    }*/

}
