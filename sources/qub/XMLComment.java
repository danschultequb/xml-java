package qub;

public class XMLComment implements XMLElementChild
{
    private final String text;

    private XMLComment(String text)
    {
        PreCondition.assertNotNull(text, "text");

        this.text = text;
    }

    public static XMLComment create(String text)
    {
        PreCondition.assertNotNull(text, "text");

        return new XMLComment(text);
    }

    public String getText()
    {
        return this.text;
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

            result += stream.write("<!--").await();
            result += stream.write(this.text).await();
            result += stream.write("-->").await();

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLComment && this.equals((XMLComment)rhs);
    }

    public boolean equals(XMLComment rhs)
    {
        return rhs != null &&
            this.text.equals(rhs.text);
    }
}
