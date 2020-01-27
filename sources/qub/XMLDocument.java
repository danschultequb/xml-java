package qub;

/**
 * An XML document.
 */
public class XMLDocument
{
    private XMLDeclaration declaration;
    private XMLElement root;

    private XMLDocument()
    {
    }

    /**
     * Create a new empty XMLDocument.
     * @return A new empty XMLDocument.
     */
    public static XMLDocument create()
    {
        return new XMLDocument();
    }

    public XMLDocument setDeclaration(XMLDeclaration declaration)
    {
        this.declaration = declaration;
        return this;
    }

    public XMLDeclaration getDeclaration()
    {
        return this.declaration;
    }

    public XMLDocument setRoot(XMLElement root)
    {
        this.root = root;
        return this;
    }

    public XMLElement getRoot()
    {
        return this.root;
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

    public Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        PreCondition.assertNotNull(stream, "stream");
        PreCondition.assertNotNull(format, "format");

        return Result.create(() ->
        {
            int result = 0;

            final boolean hasRoot = this.root != null;

            if (this.declaration != null)
            {
                result += this.declaration.toString(stream, format).await();

                if (hasRoot)
                {
                    result += stream.write(format.getNewLine()).await();
                }
            }

            if (hasRoot)
            {
                result += this.root.toString(stream, format).await();
            }

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLDocument && this.equals((XMLDocument)rhs);
    }

    public boolean equals(XMLDocument rhs)
    {
        return rhs != null &&
            Comparer.equal(this.declaration, rhs.declaration) &&
            Comparer.equal(this.root, rhs.root);
    }
}
