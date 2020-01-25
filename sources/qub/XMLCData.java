package qub;

public class XMLCData implements XMLElementChild
{
    private final String text;

    private XMLCData(String text)
    {
        PreCondition.assertNotNull(text, "text");

        this.text = text;
    }

    public static XMLCData create(String text)
    {
        PreCondition.assertNotNull(text, "text");

        return new XMLCData(text);
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

            result += stream.write("<![CDATA[").await();
            result += stream.write(this.text).await();
            result += stream.write("]]>").await();

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLCData && this.equals((XMLCData)rhs);
    }

    public boolean equals(XMLCData rhs)
    {
        return rhs != null &&
            this.text.equals(rhs.text);
    }
}
