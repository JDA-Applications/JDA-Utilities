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

    private String[][] values;
    private String[] headers;

    private char headerDelimiter = '-';
    private char rowDelimiter = headerDelimiter;
    private char columnDelimiter = '|';
    private char crossDelimiter = '+';
    private char headerCrossDelimiter = crossDelimiter;
    private char horizontalOutline = rowDelimiter;
    private char verticalOutline = columnDelimiter;

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
     *              <li>autoAdjust is false and any value is longer than the corresponding header</li>
     *          </ul>
     *          Note that empty values are fine. Empty headers cannot work if any values are given.
     *
     * @return  The table as a String.
     *          If framing is activated, there are strict borders around each cell.
     *          If codeblock is activated, there will be a {@code ```\n} at the beginning and a {@code ```} at the end.
     */
    public String build()
    {
        Checks.check(headers != null, "Must set headers");
        Checks.check(values != null, "Must set values");
        Checks.check(Arrays.stream(values).noneMatch((row) -> row.length > headers.length), "There must not be more columns than headers!");
        if (autoAdjust)
        {
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
                if (max > header.length())
                {
                    StringBuilder newHeader = new StringBuilder().append(header);
                    for (int j = 0; j < max - header.length(); j++)
                        newHeader.append(" ");

                    headers[i] = newHeader.toString();
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
            Checks.check(check, "Length of values must not be longer than length of headers!");
        }

        StringBuilder builder = new StringBuilder();

        if (codeblock)
            builder.append("```\n");

        int totalLength = Arrays.stream(headers).mapToInt(String::length).sum() + headers.length + (frame ? 1 : -1);

        if (frame)
        {
            for (int i = 0; i < totalLength; i++)
                builder.append(horizontalOutline);

            builder.append("\n");
        }

        if (frame)
            builder.append(verticalOutline);

        for (int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            builder.append(header);
            if (i < headers.length - 1)
                builder.append(columnDelimiter);
        }

        if (frame)
            builder.append(verticalOutline);

        builder.append("\n");

        if (frame)
            builder.append(headerDelimiter);

        this.appendHorizontalDelimiter(builder, headerDelimiter, headerCrossDelimiter);

        if (frame)
            builder.append(headerDelimiter);

        builder.append("\n");

        for (int i = 0; i < values.length; i++)
        {
            String[] row = values[i];
            if (frame)
                builder.append(verticalOutline);

            for (int j = 0; j < row.length; j++)
            {
                builder.append(row[j]);
                for (int k = 0; k < headers[j].length() - row[j].length(); k++)
                    builder.append(" ");

                if (j < row.length - 1)
                    builder.append(columnDelimiter);
            }

            if (frame)
                builder.append(verticalOutline);

            builder.append("\n");

            if (frame && i < values.length - 1)
            {
                builder.append(rowDelimiter);
                this.appendHorizontalDelimiter(builder, rowDelimiter, crossDelimiter);
                builder.append(rowDelimiter);
                builder.append("\n");
            }
        }

        if (frame)
        {
            for (int j = 0; j < totalLength; j++)
                builder.append(horizontalOutline);
        }

        if (codeblock)
            builder.append("```");

        return builder.toString();

    }

    private void appendHorizontalDelimiter(StringBuilder builder, char rowDelimiter, char crossDelimiter)
    {
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
     * Sets the delimiter to be placed between the rows if framing is activated.
     * By default, this is {@code -}.
     *
     * @param  rowDelimiter
     *         The character used to separate the rows from each other.
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
     * By default, this is {@code |}.
     *
     * @param  columnDelimiter
     *         The character used to separate the columns form each other.
     *
     * @return this
     */
    public TableBuilder setColumnDelimiter(char columnDelimiter)
    {
        this.columnDelimiter = columnDelimiter;
        return this;
    }

    /**
     * Sets the delimiter to be placed where the vertical and horizontal lines inside the table
     * would cross, if framing is activated.
     * By default, this is {@code +}.
     *
     * @param  crossDelimiter
     *         The character to use for that purpose.
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
     * By default, this is {@code +}.
     *
     * @param  headerCrossDelimiter
     *         The character to be used for that purpose
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
     * By default, this is {@code -}.
     *
     * @param  headerDelimiter
     *         The character used to separate the headers from the rest of the table.
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
     * By default, this is {@code -}.
     *
     * @param  verticalOutline
     *         The character to use for that purpose.
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
     * By default, this is {@code |}.
     *
     * @param  horizontalOutline
     *         The character to use for that purpose.
     *
     * @return This builder.
     */
    public TableBuilder setHorizontalOutline(char horizontalOutline)
    {
        this.horizontalOutline = horizontalOutline;
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

}
