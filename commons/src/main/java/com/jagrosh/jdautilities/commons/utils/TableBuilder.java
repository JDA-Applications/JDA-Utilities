package com.jagrosh.jdautilities.commons.utils;

import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.Objects;

/**
 * A utility class that can be used to easily create String tables in Java without any extra frameworks.
 * This can be useful to display table-like structures in Discord codeblocks, for example.
 *
 * <p>If framing is activated, the tables usually look like this (but with box drawing characters):
 * <code>
 *     ------------------------------------
 *     |Table Name| Header[0] | Header[1] |
 *     -----------+-----------+------------
 *     |  Row[0]  |Value[0][0]|Value[0][1]|
 *     -----------+-----------+------------
 *     |  Row[1]  |Value[1][0]|Value[1][1]|
 *     ------------------------------------
 * </code>
 *
 * <p> Example to get a table like above:
 * <code>
 *     String table = new TableBuilder()
 *         .setAlignment(TableBuilder.CENTER) // setting center alignment (already set by default)
 *         .addHeaders("Header[0]", "Header[1]") // setting headers
 *         .setValues(new String[][] { // setting values as 2d array
 *             {"Value[0][0]", "Value[0][1]"},
 *             {"Value[1][0]", "Value[1][1]"}
 *         }).addRowNames("Row[0]", "Row[1]") // setting row names
 *         .setName("Table Name") // the name (displayed in the top left corner)
 *         .frame(true) // activating framing
 *         .build(); // building (note that this consists of unicode box drawing characters, so it might not display correctly on some devices)
 *
 *     // now do whatever you want with that
 *
 * </code>
 * <br>The characters used to display the table can be configured individually.
 *
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class TableBuilder
{

    private String[][] values;
    private String[] headers;
    private String[] rowNames;

    private Borders borders;

    private String tableName = "";

    private Alignment alignment = Alignment.CENTER;
    private int padding = 1;

    private boolean codeblock = false;
    private boolean frame = false;
    private boolean autoAdjust = true;

    /**
     * Builds a String table according to the settings made.
     *
     * @throws  IllegalArgumentException
     *          if:
     *          <ul>
     *              <li>No {@code Borders} were set</li>
     *              <li>No values were set</li>
     *              <li>The values are empty</li>
     *              <li>One or more of the values are null</li>
     *              <li>The padding is &lt; 0</li>
     *              <li>Row names were set and there are less rows in the values than row names given</li>
     *              <li>The amount of columns is not consistent throughout headers (if present) and values</li>
     *              <li>autoAdjust is false and no headers were set</li>
     *              <li>autoAdjust is false and any value is longer than the corresponding header</li>
     *          </ul>
     *
     * @return  The table as a String.
     *          If framing is activated, there will be a frame around the whole table.
     *          If codeblock is activated, there will be a {@code ```\n} at the beginning and a {@code ```} at the end.
     */
    public String build()
    {
        Checks.notNull(borders, "Borders");
        Checks.notNull(values, "Values");
        Checks.notEmpty(values, "Values");
        Checks.check(Arrays.stream(values).allMatch((row) -> Arrays.stream(row).allMatch(Objects::nonNull)), "A value may not be null");
        Checks.check(padding >= 0, "Padding must not be < 0");

        boolean headersPresent = headers != null;
        boolean rowNamesPresent = rowNames != null;

        int rows = (rowNamesPresent ? rowNames.length : values.length);
        int columns = (headersPresent ? headers.length : Arrays.stream(values).mapToInt((s) -> s.length).max().orElse(0));

        Checks.check(values.length >= rows, "The amount of rows must not be smaller than specified in the row names");
        int oldColumns = columns;
        Checks.check(Arrays.stream(values).noneMatch((row) -> row.length < oldColumns), "The amount of columns must be consistent");

        if (headersPresent)
            rows++;
        if (rowNamesPresent)
            columns++;

        // insert headers and row names (if present)
        String[][] newValues = new String[rows][columns];
        if (headersPresent && rowNamesPresent)
        {
            newValues[0][0] = tableName;
            System.arraycopy(headers, 0, newValues[0], 1, headers.length);
            for (int i = 1; i < rows; i++)
            {
                newValues[i][0] = rowNames[i - 1];
                System.arraycopy(values[i - 1], 0, newValues[i], 1, columns - 1);
            }
        }
        else if (rowNamesPresent)
        {
            for (int i = 0; i < rows; i++)
            {
                newValues[i][0] = rowNames[i];
                System.arraycopy(values[i], 0, newValues[i], 1, columns - 1);
            }
        }
        else if (headersPresent)
        {
            System.arraycopy(headers, 0, newValues[0], 0, columns);
            System.arraycopy(values, 0, newValues, 1, rows - 1);
        }
        else
        {
            newValues = this.values;
        }

        this.values = newValues;

        if (autoAdjust)
        {
            // find the max. value for each column
            int[] maxLengths = new int[columns];
            Arrays.fill(maxLengths, 0);

            for (String[] row : values)
            {
                for (int i = 0; i < row.length; i++)
                {
                    int length = row[i].length();
                    if (length > maxLengths[i])
                        maxLengths[i] = length;
                }
            }

            // align values
            for (String[] row : values)
            {
                for (int j = 0; j < row.length; j++)
                {
                    String value = row[j];
                    int adjustment = maxLengths[j] - value.length();
                    StringBuilder newValue = new StringBuilder();
                    this.setAlignment(adjustment, value, newValue);
                    row[j] = newValue.toString();
                }
            }
        }
        else
        {
            Checks.notNull(headers, "Headers");

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

        String[] firstRow = values[0];

        // outline
        if (frame)
        {
            builder.append(borders.upLeftCorner);
            for (int j = 0; j < firstRow.length; j++)
            {
                for (int k = 0; k < firstRow[j].length(); k++)
                    builder.append(borders.horizontalOutline);

                if (j == 0)
                    builder.append(borders.firstColumnUpperIntersection);
                else if (j < firstRow.length - 1)
                    builder.append(borders.upperIntersection);
            }
            builder.append(borders.upRightCorner);
            builder.append("\n");
        }

        this.appendRow(builder, firstRow); // header

        builder.append("\n");

        // header delimiter
        if (frame)
            builder.append(borders.headerLeftIntersection);

        for (int i = 0; i < firstRow.length; i++)
        {
            for (int j = 0; j < firstRow[i].length(); j++)
                builder.append(borders.headerDelimiter);

            if (i == 0)
                builder.append(borders.headerColumnCrossDelimiter);
            else if (i < firstRow.length - 1)
                builder.append(borders.headerCrossDelimiter);

        }

        if (frame)
            builder.append(borders.headerRightIntersection);

        builder.append("\n");


        // append row after row
        for (int i = 1; i < rows; i++)
        {
            String[] row = values[i];

            this.appendRow(builder, row);

            // delimiter
            if (i < values.length - 1)
            {
                builder.append("\n");

                if (frame)
                    builder.append(borders.leftIntersection);

                for (int j = 0; j < row.length; j++)
                {
                    for (int k = 0; k < row[j].length(); k++)
                        builder.append(borders.rowDelimiter);

                    if (j == 0)
                        builder.append(borders.firstColumnCrossDelimiter);
                    else if (j < row.length - 1)
                        builder.append(borders.crossDelimiter);
                }

                if (frame)
                    builder.append(borders.rightIntersection);

                builder.append("\n");
            }
        }

        // outline
        if (frame)
        {
            builder.append("\n");
            builder.append(borders.lowLeftCorner);
            for (int j = 0; j < firstRow.length; j++)
            {
                for (int k = 0; k < firstRow[j].length(); k++)
                    builder.append(borders.horizontalOutline);

                if (j == 0)
                    builder.append(borders.firstColumnLowerIntersection);
                else if (j < firstRow.length - 1)
                    builder.append(borders.lowerIntersection);
            }
            builder.append(borders.lowRightCorner);
        }

        if (codeblock)
            builder.append("```");

        return builder.toString();

    }

    private void appendRow(StringBuilder builder, String[] row)
    {
        if (frame)
            builder.append(borders.verticalOutline);

        for (int i = 0; i < row.length; i++)
        {
            builder.append(row[i]);
            if (i == 0)
                builder.append(borders.firstColumnDelimiter);
            else if (i < row.length - 1)
                builder.append(borders.columnDelimiter);
        }

        if (frame)
            builder.append(borders.verticalOutline);

    }

    private void setAlignment(int adjustment, String oldValue, StringBuilder newValueBuilder) {
        for (int i = 0; i < padding; i++) // padding left
            newValueBuilder.append(" ");

        switch(alignment)
        {
            case RIGHT:
                // first black spaces
                for (int k = 0; k < adjustment; k++)
                    newValueBuilder.append(" ");
                newValueBuilder.append(oldValue); // then value
                break;
            case LEFT:
                newValueBuilder.append(oldValue); // first value
                // then blank spaces
                for (int k = 0; k < adjustment; k++)
                    newValueBuilder.append(" ");
                break;
            case CENTER:
                boolean even = adjustment % 2 == 0;
                int half = adjustment / 2;
                for (int k = 0; k < half; k++) // append one half of black spaces
                    newValueBuilder.append(" ");

                newValueBuilder.append(oldValue); // append value

                for (int k = 0; k < half; k++) // append other half of blank spaces
                    newValueBuilder.append(" ");

                if (!even) // if the number wasn't event, one blank space is still missing
                    newValueBuilder.append(" ");
                break;
        }

        for (int i = 0; i < padding; i++) // padding right
            newValueBuilder.append(" ");
    }

    /**
     * Sets the headers for the columns specified in {@link TableBuilder#setValues(String[][]) values}, applied in the order given here.
     *
     * <p>This setting is optional. By default, there will not be any headers.
     *
     * @param  headers
     *         The headers as varargs, so either a String[] or single Strings.
     *
     * @return this
     */
    public TableBuilder addHeaders(String... headers)
    {
        this.headers = headers;
        return this;
    }

    /**
     * Sets names for the rows specified in {@link TableBuilder#setValues(String[][]) values}, applied in the order given here.
     *
     * <p>This setting is optional. By default, there will not be any row names.
     *
     * @param  rows
     *         The row names as varargs, so either a String[] or single Strings.
     *
     * @return This builder.
     *
     * @see    TableBuilder#setName(String)
     */
    public TableBuilder addRowNames(String... rows)
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
     * @return This builder.
     */
    public TableBuilder setValues(String[][] values)
    {
        this.values = values;
        return this;
    }

    /**
     * Sets the borders for this table using the nested {@link TableBuilder.Borders Borders} class.
     *
     * @param  borders
     *         An instance of the Borders class that specifies the characters to use.
     *
     * @return This builder.
     */
    public TableBuilder setBorders(Borders borders)
    {
        this.borders = borders;
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
     * @see    TableBuilder#addRowNames(String...)
     */
    public TableBuilder setName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    /**
     * Sets the alignment/orientation of all headers and values.
     * This will be applied if and only if autoAdjust is {@code true}.
     * <br>By default, this is set to {@link TableBuilder.Alignment#CENTER CENTER}.
     *
     * @param  alignment
     *         The alignment to set.
     *
     * @return This builder.
     *
     * @see    TableBuilder#autoAdjust(boolean)
     *
     * @see    TableBuilder.Alignment
     */
    public TableBuilder setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
        return this;
    }

    /**
     * Sets a paddling that is applied to each value if autoAdjust is {@code true}.
     * <br>By default, this is {@code 1}.
     *
     * @param  padding The minimum amount of blank spaces between a value and the corresponding borders.
     *
     * @return This builder.
     *
     * @see    TableBuilder#autoAdjust(boolean)
     */
    public TableBuilder setPadding(int padding)
    {
        this.padding = padding;
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
     *         This also deactivates the possibility to use an alignment or a padding.
     *
     * @return This builder.
     */
    public TableBuilder autoAdjust(boolean autoAdjust)
    {
        this.autoAdjust = autoAdjust;
        return this;
    }

    /**
     * An enum that represents the alignments possible to set
     */
    public enum Alignment 
    {
        LEFT, RIGHT, CENTER
    }

    /**
     * A data class whose instances store the characters needed to create a table with the enclosing {@link TableBuilder TableBuilder}.
     * <br>Instances can be created via the factory methods provided by this class.
     * <br>Some default instances are already given as {@code public static final} fields.
     * All of them use box drawing characters.
     */
    public static class Borders
    {

        /**
         * An instance that can be used for framed tables with a header row and a columns with row names, for it provides
         * special delimiters for the first row and column.
         */
        public static final Borders HEADER_ROW_FRAME = newHeaderRowNamesFrameBorders("─", "│", "┼",
            "├", "┤", "┬", "┴", "┌", "┐", "└",
            "┘", "═", "╪", "╞", "╡", "║",
            "╫", "╥", "╨", "╬", "─", "│");

        /**
         * An instance that can be used for framed tables with a header row, for it provides a special delimiter for the first row.
         */
        public static final Borders HEADER_FRAME = newHeaderFrameBorders("─", "│", "┼",
            "├", "┤", "┬", "┴", "┌", "┐", "└", "┘",
            "═", "╪", "╞", "╡", "─", "│");

        /**
         * An instance that can be used for framed tables without any special characters for headers or row name columns.
         */
        public static final Borders FRAME = newFrameBorders("─", "│", "┼", "├", "┤",
            "┬", "┴", "┌", "┐", "└", "┘", "─", "│");

        /**
         * An instance that can be used for tables without a frame that have a header row and a row name column,
         * for this provides special delimiters for the first row and column.
         */
        public static final Borders HEADER_ROW_PLAIN = newHeaderRowNamesPlainBorders("─", "│", "┼", "═",
            "╪", "║", "╫", "╬");

        /**
         * An instance that can be used for tables without a frame that have a header, for this provides a special
         * delimiter for the first row.
         */
        public static final Borders HEADER_PLAIN = newHeaderPlainBorders("─", "│", "┼", "═", "╪");

        /**
         * An instance that can be used for plain tables without a frame that do not have any special delimiters
         * for headers or row name columns.
         */
        public static final Borders PLAIN = newPlainBorders("─", "│", "┼");

        public static final String UNKNOWN = "�";

        public final String rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection,
            upperIntersection, lowerIntersection, upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner,
            headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection,
            firstColumnDelimiter, firstColumnCrossDelimiter, firstColumnUpperIntersection,
            firstColumnLowerIntersection, headerColumnCrossDelimiter, horizontalOutline, verticalOutline;

        // framing + headers + rows
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection,
                        String rightIntersection, String upperIntersection, String lowerIntersection, String upLeftCorner,
                        String upRightCorner, String lowLeftCorner, String lowRightCorner, String headerDelimiter,
                        String headerCrossDelimiter, String headerLeftIntersection, String headerRightIntersection,
                        String firstColumnDelimiter, String firstColumnCrossDelimiter, String firstColumnUpperIntersection,
                        String firstColumnLowerIntersection, String headerColumnCrossDelimiter, String horizontalOutline, String verticalOutline)
        {
            this.rowDelimiter = rowDelimiter;
            this.columnDelimiter = columnDelimiter;
            this.crossDelimiter = crossDelimiter;
            this.leftIntersection = leftIntersection;
            this.rightIntersection = rightIntersection;
            this.upperIntersection = upperIntersection;
            this.lowerIntersection = lowerIntersection;
            this.upLeftCorner = upLeftCorner;
            this.upRightCorner = upRightCorner;
            this.lowLeftCorner = lowLeftCorner;
            this.lowRightCorner = lowRightCorner;
            this.headerDelimiter = headerDelimiter;
            this.headerCrossDelimiter = headerCrossDelimiter;
            this.headerLeftIntersection = headerLeftIntersection;
            this.headerRightIntersection = headerRightIntersection;
            this.firstColumnDelimiter = firstColumnDelimiter;
            this.firstColumnCrossDelimiter = firstColumnCrossDelimiter;
            this.firstColumnUpperIntersection = firstColumnUpperIntersection;
            this.firstColumnLowerIntersection = firstColumnLowerIntersection;
            this.headerColumnCrossDelimiter = headerColumnCrossDelimiter;
            this.horizontalOutline = horizontalOutline;
            this.verticalOutline = verticalOutline;
        }

        // framing + headers
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                        String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                        String lowRightCorner, String headerDelimiter, String headerCrossDelimiter, String headerLeftIntersection,
                        String headerRightIntersection, String horizontalOutline, String verticalOutline)
        {
            this(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection,
                headerRightIntersection, columnDelimiter, crossDelimiter, upperIntersection, lowerIntersection, headerCrossDelimiter,
                horizontalOutline, verticalOutline);
        }

        // framing
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                        String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                        String lowRightCorner, String horizontalOutline, String verticalOutline)
        {
            this(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, rowDelimiter, crossDelimiter, leftIntersection, rightIntersection,
                horizontalOutline, verticalOutline);
        }

        // plain + headers + rows
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter,
                        String firstColumnDelimiter, String firstColumnCrossDelimiter, String headerColumnCrossDelimiter)
        {
            this(rowDelimiter, columnDelimiter, crossDelimiter, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN,
                headerDelimiter, headerCrossDelimiter, UNKNOWN, UNKNOWN, firstColumnDelimiter, firstColumnCrossDelimiter,
                UNKNOWN, UNKNOWN, headerColumnCrossDelimiter, UNKNOWN, UNKNOWN);
        }

        // plain + headers
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter)
        {
            this(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter, columnDelimiter, crossDelimiter, headerCrossDelimiter);
        }

        // plain
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter)
        {
            this(rowDelimiter, columnDelimiter, crossDelimiter, rowDelimiter, crossDelimiter);
        }

        /**
         * The factory method for {@code Borders} instances that should support framing
         * as well as special characters for a header row and a row name column.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         *
         * @param  leftIntersection
         *         The character for frame intersections at the left
         *
         * @param  rightIntersection
         *         The character for frame intersections at the right
         *
         * @param  upperIntersection
         *         The character for upper frame intersections
         *
         * @param  lowerIntersection
         *         The character for lower frame intersections
         *
         * @param  upLeftCorner
         *         The character for the upper left frame corner
         *
         * @param  upRightCorner
         *         The character for the upper right frame corner
         *
         * @param  lowLeftCorner
         *         The character for the lower left frame corner
         *
         * @param  lowRightCorner
         *         The character for the lower right frame corner
         *
         * @param  headerDelimiter
         *         The normal character to be placed between the first row and the second row
         *
         * @param  headerCrossDelimiter
         *         The character to be placed where the header delimiter and the column delimiters cross
         *
         * @param  headerLeftIntersection
         *         The character to be placed at the very left of the header delimiter
         *
         * @param  headerRightIntersection
         *         The character to be placed at the very right of the header delimiter
         *
         * @param  firstColumnDelimiter
         *         The normal character to be placed between the first and the second column
         *
         * @param  firstColumnCrossDelimiter
         *         The character to be placed after the first column where the row delimiters are crossed
         *
         * @param  firstColumnUpperIntersection
         *         The character to be placed at the very top of the first column delimiter
         *
         * @param  firstColumnLowerIntersection
         *         The character to be placed at the very bottom of the first column delimiter
         *
         * @param  headerColumnCrossDelimiter
         *         The character to be placed at the intersection first row x first column
         *
         * @param  horizontalOutline
         *         The character to use for the upper and lower frame outline
         *
         * @param  verticalOutline
         *         The character to use for the left and right frame outline
         * 
         * @return Borders instance
         */
        public static Borders newHeaderRowNamesFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection,
                                                            String rightIntersection, String upperIntersection, String lowerIntersection, String upLeftCorner,
                                                            String upRightCorner, String lowLeftCorner, String lowRightCorner, String headerDelimiter,
                                                            String headerCrossDelimiter, String headerLeftIntersection, String headerRightIntersection,
                                                            String firstColumnDelimiter, String firstColumnCrossDelimiter, String firstColumnUpperIntersection,
                                                            String firstColumnLowerIntersection, String headerColumnCrossDelimiter, String horizontalOutline, String verticalOutline)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection, upLeftCorner, upRightCorner,
                lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection, firstColumnDelimiter, firstColumnCrossDelimiter,
                firstColumnUpperIntersection, firstColumnLowerIntersection, headerColumnCrossDelimiter, horizontalOutline, verticalOutline);
        }

        /**
         * The factory method for {@code Borders} instances that should support framing and
         * special characters for a header row.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         *
         * @param  leftIntersection
         *         The character for frame intersections at the left
         *
         * @param  rightIntersection
         *         The character for frame intersections at the right
         *
         * @param  upperIntersection
         *         The character for upper frame intersections
         *
         * @param  lowerIntersection
         *         The character for lower frame intersections
         *
         * @param  upLeftCorner
         *         The character for the upper left frame corner
         *
         * @param  upRightCorner
         *         The character for the upper right frame corner
         *
         * @param  lowLeftCorner
         *         The character for the lower left frame corner
         *
         * @param  lowRightCorner
         *         The character for the lower right frame corner
         *
         * @param  headerDelimiter
         *         The normal character to be placed between the first row and the second row
         *
         * @param  headerCrossDelimiter
         *         The character to be placed where the header delimiter and the column delimiters cross
         *
         * @param  headerLeftIntersection
         *         The character to be placed at the very left of the header delimiter
         *
         * @param  headerRightIntersection
         *         The character to be placed at the very right of the header delimiter
         *
         * @param  horizontalOutline
         *         The character to use for the upper and lower frame outline
         *
         * @param  verticalOutline
         *         The character to use for the left and right frame outline
         * 
         * @return Borders instance
         */
        public static Borders newHeaderFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                                                    String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                                                    String lowRightCorner, String headerDelimiter, String headerCrossDelimiter, String headerLeftIntersection,
                                                    String headerRightIntersection, String horizontalOutline, String verticalOutline)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection,
                horizontalOutline, verticalOutline);
        }

        /**
         * The factory method for {@code Borders} instances that should support framing and don't have
         * any special characters for headers or row names.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         *
         * @param  leftIntersection
         *         The character for frame intersections at the left
         *
         * @param  rightIntersection
         *         The character for frame intersections at the right
         *
         * @param  upperIntersection
         *         The character for upper frame intersections
         *
         * @param  lowerIntersection
         *         The character for lower frame intersections
         *
         * @param  upLeftCorner
         *         The character for the upper left frame corner
         *
         * @param  upRightCorner
         *         The character for the upper right frame corner
         *
         * @param  lowLeftCorner
         *         The character for the lower left frame corner
         *
         * @param  lowRightCorner
         *         The character for the lower right frame corner
         *
         * @param  horizontalOutline
         *         The character to use for the upper and lower frame outline
         *
         * @param  verticalOutline
         *         The character to use for the left and right frame outline
         * 
         * @return Borders instance
         */
        public static Borders newFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                                              String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                                              String lowRightCorner, String horizontalOutline, String verticalOutline)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection, upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, horizontalOutline, verticalOutline);
        }

        /**
         * The factory method for {@code Borders} instances that should not support framing
         * but special characters for a header row and a row name column.
         *
         * <p>Warning: using this or the other plain Borders for a framed table will not lead to a satisfying result,
         * since all the framing characters are {@link Borders#UNKNOWN unknown unicode characters} here.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         *
         * @param  headerDelimiter
         *         The normal character to be placed between the first row and the second row
         *
         * @param  headerCrossDelimiter
         *         The character to be placed where the header delimiter and the column delimiters cross
         *
         * @param  firstColumnDelimiter
         *         The normal character to be placed between the first and the second column
         *
         * @param  firstColumnCrossDelimiter
         *         The character to be placed after the first column where the row delimiters are crossed
         *
         * @param  headerColumnCrossDelimiter
         *         The character to be placed at the intersection first row x first column
         * 
         * @return Borders instance
         */
        public static Borders newHeaderRowNamesPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter,
                                                            String firstColumnDelimiter, String firstColumnCrossDelimiter, String headerColumnCrossDelimiter)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter, firstColumnDelimiter, firstColumnCrossDelimiter, headerColumnCrossDelimiter);
        }

        /**
         * The factory method for {@code Borders} instances that should not support framing
         * but special characters for a header row.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         *
         * @param  headerDelimiter
         *         The normal character to be placed between the first row and the second row
         *
         * @param  headerCrossDelimiter
         *         The character to be placed where the header delimiter and the column delimiters cross
         * 
         * @return Borders instance
         */
        public static Borders newHeaderPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter);
        }

        /**
         * The factory method for {@code Borders} instances that should not support framing
         * and no special characters for a header row or a row name column.
         *
         * @param  rowDelimiter
         *         The character used to separate normal rows from each other
         *
         * @param  columnDelimiter
         *         The character used to separate normal columns form each other
         *
         * @param  crossDelimiter
         *         The character to be placed where the vertical and horizontal lines inside the table cross
         * 
         * @return Borders instance
         */
        public static Borders newPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter)
        {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter);
        }
    }

}
