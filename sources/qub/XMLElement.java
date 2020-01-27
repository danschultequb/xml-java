package qub;

public class XMLElement implements XMLElementChild
{
    private boolean split;
    private final String name;
    private final MutableMap<String,String> attributes;
    private final List<XMLElementChild> children;

    private XMLElement(String name, boolean split)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.name = name;
        this.attributes = Map.create();
        this.split = split;
        this.children = List.create();
    }

    public static XMLElement create(String name)
    {
        return XMLElement.create(name, false);
    }

    public static XMLElement create(String name, boolean split)
    {
        return new XMLElement(name, split);
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isSplit()
    {
        return this.split;
    }

    public XMLElement setAttribute(String attributeName, String attributeValue)
    {
        PreCondition.assertNotNullAndNotEmpty(attributeName, "attributeName");
        PreCondition.assertNotNull(attributeValue, "attributeValue");

        this.attributes.set(attributeName, attributeValue);
        return this;
    }

    public XMLElement setAttribute(XMLAttribute attribute)
    {
        PreCondition.assertNotNull(attribute, "attribute");

        return this.setAttribute(attribute.getName(), attribute.getValue());
    }

    public XMLElement setAttributes(Iterable<XMLAttribute> attributes)
    {
        PreCondition.assertNotNull(attributes, "attributes");

        for (final XMLAttribute attribute : attributes)
        {
            this.setAttribute(attribute);
        }
        return this;
    }

    public Iterable<XMLAttribute> getAttributes()
    {
        return this.attributes.map(XMLAttribute::create);
    }

    public Result<String> getAttributeValue(String attributeName)
    {
        PreCondition.assertNotNullAndNotEmpty(attributeName, "attributeName");

        return this.attributes.get(attributeName)
            .convertError(NotFoundException.class, () -> new NotFoundException("Couldn't find an attribute named " + Strings.escapeAndQuote(attributeName) + " in the element."));
    }

    public Indexable<XMLElementChild> getChildren()
    {
        return this.children;
    }

    public XMLElement addChild(XMLElementChild child)
    {
        PreCondition.assertNotNull(child, "child");

        this.children.add(child);
        this.split = true;
        return this;
    }

    @Override
    public String toString()
    {
        return XML.toString((Function2<IndentedCharacterWriteStream,XMLFormat,Result<Integer>>)this::toString);
    }

    public String toString(XMLFormat format)
    {
        return XML.toString(format, this::toString);
    }

    @Override
    public Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        PreCondition.assertNotNull(stream, "stream");
        PreCondition.assertNotDisposed(stream, "stream");
        PreCondition.assertNotNull(format, "format");

        return Result.create(() ->
        {
            int result = 0;

            result += stream.write('<').await();
            result += stream.write(this.name).await();

            for (final MapEntry<String,String> attribute : this.attributes)
            {
                result += stream.write(' ').await();
                result += stream.write(attribute.getKey()).await();
                result += stream.write("=\"").await();
                result += stream.write(attribute.getValue()).await();
                result += stream.write('\"').await();
            }

            if (!this.split)
            {
                result += stream.write("/>").await();
            }
            else
            {
                result += stream.write('>').await();

                if (this.children.any())
                {
                    stream.setSingleIndent(format.getSingleIndent());
                    final String newLine = format.getNewLine();

                    boolean previousChildWasText = false;
                    for (final XMLElementChild child : this.children)
                    {
                        if (child instanceof XMLText)
                        {
                            result += child.toString(stream, format).await();
                            previousChildWasText = true;
                        }
                        else
                        {
                            stream.increaseIndent();
                            try
                            {
                                if (!previousChildWasText)
                                {
                                    result += stream.write(newLine).await();
                                }
                                result += child.toString(stream, format).await();
                            }
                            finally
                            {
                                stream.decreaseIndent();
                            }
                            previousChildWasText = false;
                        }
                    }
                    if (!previousChildWasText)
                    {
                        result += stream.write(newLine).await();
                    }
                }

                result += stream.write("</").await();
                result += stream.write(this.name).await();
                result += stream.write('>').await();
            }

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLElement && this.equals((XMLElement)rhs);
    }

    public boolean equals(XMLElement rhs)
    {
        return rhs != null &&
            this.split == rhs.split &&
            this.name.equals(rhs.name) &&
            this.attributes.equals(rhs.attributes) &&
            this.children.equals(rhs.children);
    }
}
