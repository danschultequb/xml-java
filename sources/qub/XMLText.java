package qub;

public class XMLText implements XMLElementChild
{
    private final String text;
    private final boolean isWhitespace;

    private XMLText(String text, boolean isWhitespace)
    {
        PreCondition.assertNotNullAndNotEmpty(text, "text");

        this.text = text;
        this.isWhitespace = isWhitespace;
    }

    public static XMLText create(String text)
    {
        PreCondition.assertNotNullAndNotEmpty(text, "text");

        return XMLText.create(text, XMLText.isWhitespace(text));
    }

    public static XMLText create(String text, boolean isWhitespace)
    {
        PreCondition.assertNotNullAndNotEmpty(text, "text");

        return new XMLText(text, isWhitespace);
    }

    public String getText()
    {
        return this.text;
    }

    public boolean isWhitespace()
    {
        return this.isWhitespace;
    }

    @Override
    public String toString()
    {
        return XML.toString(this::toString);
    }

    @Override
    public Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        PreCondition.assertNotNull(stream, "stream");
        PreCondition.assertNotDisposed(stream, "stream");
        PreCondition.assertNotNull(format, "format");

        return stream.write(this.text);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLText && this.equals((XMLText)rhs);
    }

    public boolean equals(XMLText rhs)
    {
        return rhs != null &&
            this.text.equals(rhs.text);
    }

    public static boolean isWhitespace(String text)
    {
        PreCondition.assertNotNull(text, "text");

        boolean result = true;
        for (final char c : text.toCharArray())
        {
            if (!Characters.isWhitespace(c))
            {
                result = false;
                break;
            }
        }

        return result;
    }
}
