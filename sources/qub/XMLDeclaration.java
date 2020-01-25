package qub;

/**
 * An <a href="https://www.w3.org/TR/xml/#NT-XMLDecl">XML declaration</a> at the top of an XML document.
 */
public class XMLDeclaration
{
    private static final String[] standaloneOptions = new String[] { "", "yes", "no" };

    private String version;
    private String encoding;
    private String standalone;

    private XMLDeclaration()
    {
        this.version = "1.0";
        this.encoding = "";
        this.standalone = "";
    }

    /**
     * Create a new <a href="https://www.w3.org/TR/xml/#NT-XMLDecl">XML declaration</a> object.
     * @return A new <a href="https://www.w3.org/TR/xml/#NT-XMLDecl">XML declaration</a> object.
     */
    public static XMLDeclaration create()
    {
        return new XMLDeclaration();
    }

    /**
     * Get the <a href="https://www.w3.org/TR/xml/#NT-VersionInfo">XML version</a> that the XML
     * document follows.
     * @return The <a href="https://www.w3.org/TR/xml/#NT-VersionInfo">XML version</a> that the XML
     * document follows.
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Set the <a href="https://www.w3.org/TR/xml/#NT-VersionInfo">XML version</a> that the XML
     * document follows.
     * @param version The <a href="https://www.w3.org/TR/xml/#NT-VersionInfo">XML version</a> that
     *                the XML document follows.
     * @return This object for method chaining.
     */
    public XMLDeclaration setVersion(String version)
    {
        PreCondition.assertNotNullAndNotEmpty(version, "version");

        this.version = version;
        return this;
    }

    /**
     * Get the <a href="https://www.w3.org/TR/xml/#NT-EncodingDecl">encoding</a> that the XML
     * document uses.
     * @return The <a href="https://www.w3.org/TR/xml/#NT-EncodingDecl">encoding</a> that the XML
     * document uses.
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * Set the <a href="https://www.w3.org/TR/xml/#NT-EncodingDecl">encoding</a> that the XML
     * document uses.
     * @param encoding The <a href="https://www.w3.org/TR/xml/#NT-EncodingDecl">encoding</a> that
     *                 the XML document uses.
     * @return This object for method chaining.
     */
    public XMLDeclaration setEncoding(String encoding)
    {
        PreCondition.assertNotNull(encoding, "encoding");

        this.encoding = encoding;
        return this;
    }

    /**
     * Get the <a href="https://www.w3.org/TR/xml/#NT-SDDecl">standalone</a> value that determines
     * whether or not there are declarations which appear external to the
     * <a href="https://www.w3.org/TR/xml/#dt-docent">document entity</a>.
     * @return The <a href="https://www.w3.org/TR/xml/#NT-SDDecl">standalone</a> value that
     * determines whether or not there are declarations which appear external to the
     * <a href="https://www.w3.org/TR/xml/#dt-docent">document entity</a>.
     */
    public String getStandalone()
    {
        return this.standalone;
    }

    /**
     * Set the <a href="https://www.w3.org/TR/xml/#NT-SDDecl">standalone</a> value that determines
     * whether or not there are declarations which appear external to the
     * <a href="https://www.w3.org/TR/xml/#dt-docent">document entity</a>.
     * @param standalone The <a href="https://www.w3.org/TR/xml/#NT-SDDecl">standalone</a> value
     *                   that determines whether or not there are declarations which appear external
     *                   to the <a href="https://www.w3.org/TR/xml/#dt-docent">document entity</a>.
     */
    public XMLDeclaration setStandalone(String standalone)
    {
        PreCondition.assertNotNull(standalone, "standalone");
        PreCondition.assertOneOf(standalone, XMLDeclaration.standaloneOptions, "standalone");

        this.standalone = standalone;
        return this;
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

            result += stream.write("<?xml version=").await();
            result += stream.write(Strings.escapeAndQuote(this.version)).await();

            if (!Strings.isNullOrEmpty(this.encoding))
            {
                result += stream.write(" encoding=").await();
                result += stream.write(Strings.escapeAndQuote(this.encoding)).await();
            }

            if (!Strings.isNullOrEmpty(this.standalone))
            {
                result += stream.write(" standalone=").await();
                result += stream.write(Strings.escapeAndQuote(this.standalone)).await();
            }

            result += stream.write(format.getSpaceBeforeDeclarationEnd()).await();
            result += stream.write("?>").await();

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLDeclaration && this.equals((XMLDeclaration)rhs);
    }

    public boolean equals(XMLDeclaration rhs)
    {
        return rhs != null &&
            this.version.equals(rhs.version) &&
            this.encoding.equals(rhs.encoding) &&
            this.standalone.equals(rhs.standalone);
    }
}
