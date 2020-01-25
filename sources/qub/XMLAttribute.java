package qub;

/**
 * An <a href="https://www.w3.org/TR/xml/#NT-Attribute">attribute</a> that can be found within an XML tag.
 */
public class XMLAttribute
{
    private final String name;
    private final String value;
    private final char valueQuoteCharacter;

    private XMLAttribute(String name, String value, char valueQuoteCharacter)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");
        PreCondition.assertNotNull(value, "value");
        PreCondition.assertOneOf(valueQuoteCharacter, XML.attributeValueQuoteCharacters, "valueQuoteCharacter");

        this.name = name;
        this.value = value;
        this.valueQuoteCharacter = valueQuoteCharacter;
    }

    public static XMLAttribute createWithQuotedValue(String name, String quotedValue)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");
        PreCondition.assertNotNullAndNotEmpty(quotedValue, "quotedValue");
        PreCondition.assertTrue(Strings.isQuoted(quotedValue), "Strings.isQuoted(quotedValue)");

        final String unquotedValue = Strings.unquote(quotedValue);
        final char valueQuoteCharacter = quotedValue.charAt(0);
        return XMLAttribute.create(name, unquotedValue, valueQuoteCharacter);
    }

    public static XMLAttribute create(MapEntry<String,String> attribute)
    {
        PreCondition.assertNotNull(attribute, "attribute");

        return XMLAttribute.create(attribute.getKey(), attribute.getValue());
    }

    public static XMLAttribute create(String name, String value)
    {
        return XMLAttribute.create(name, value, '\"');
    }

    public static XMLAttribute create(String name, String value, char valueQuoteCharacter)
    {
        return new XMLAttribute(name, value, valueQuoteCharacter);
    }

    /**
     * Get the name of this attribute.
     * @return The name of this attribute.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Get the value of this attribute.
     * @return The value of this attribute.
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Get the quote character used to surround the value.
     * @return The quote character used to surround the value.
     */
    public char getValueQuoteCharacter()
    {
        return this.valueQuoteCharacter;
    }

    @Override
    public String toString()
    {
        return XML.toString(this::toString);
    }

    public Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        PreCondition.assertNotNull(stream, "stream");
        PreCondition.assertNotDisposed(stream, "stream");
        PreCondition.assertNotNull(format, "format");

        return Result.create(() ->
        {
            int result = 0;

            result += stream.write(this.name).await();
            result += stream.write('=').await();
            result += stream.write(this.valueQuoteCharacter).await();

            final int valueLength = this.value.length();
            int startIndex = 0;
            int quoteIndex = this.value.indexOf(this.valueQuoteCharacter);
            while (startIndex < valueLength)
            {
                if (quoteIndex == -1)
                {
                    result += stream.write(this.value.substring(startIndex)).await();
                    startIndex = valueLength;
                }
                else
                {
                    result += stream.write(this.value.substring(startIndex, quoteIndex)).await();
                    result += stream.write("&#x").await();
                    result += stream.write(Integers.toHexString(this.valueQuoteCharacter, true)).await();
                    result += stream.write(';').await();

                    startIndex = quoteIndex + 1;
                    quoteIndex = this.value.indexOf(this.valueQuoteCharacter, startIndex);
                }
            }

            result += stream.write(this.valueQuoteCharacter).await();

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLAttribute && this.equals((XMLAttribute)rhs);
    }

    public boolean equals(XMLAttribute rhs)
    {
        return rhs != null &&
            this.name.equals(rhs.name) &&
            this.value.equals(rhs.value) &&
            this.valueQuoteCharacter == rhs.valueQuoteCharacter;
    }
}
