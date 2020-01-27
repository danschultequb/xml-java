package qub;

/**
 * An object that can be used to format an XML document.
 */
public class XMLFormat
{
    public static final XMLFormat consise = XMLFormat.create();

    public static final XMLFormat pretty = XMLFormat.create()
        .setSpaceBeforeDeclarationEnd(" ")
        .setNewLine("\n")
        .setSingleIndent("  ");

    private String spaceBeforeDeclarationEnd;
    private String newLine;
    private String singleIndent;

    private XMLFormat()
    {
        this.spaceBeforeDeclarationEnd = "";
        this.newLine = "";
        this.singleIndent = "";
    }

    /**
     * Create a new XMLFormat object.
     * @return A new XMLFormat object.
     */
    public static XMLFormat create()
    {
        return new XMLFormat();
    }

    /**
     * Set the space that will be inserted before the end of the XML declaration.
     * @param spaceBeforeDeclarationEnd The space that will be inserted before the end of a tag.
     * @return This object for method chaining.
     */
    public XMLFormat setSpaceBeforeDeclarationEnd(String spaceBeforeDeclarationEnd)
    {
        PreCondition.assertNotNull(spaceBeforeDeclarationEnd, "spaceBeforeDeclarationEnd");
        PreCondition.assertContainsOnly(spaceBeforeDeclarationEnd, XML.whitespaceCharacters, "spaceBeforeDeclarationEnd");

        this.spaceBeforeDeclarationEnd = spaceBeforeDeclarationEnd;
        return this;
    }

    /**
     * Get the space that will be inserted before the end of the XML declaration.
     * @return The space that will be inserted before the end of the XML declaration.
     */
    public String getSpaceBeforeDeclarationEnd()
    {
        return this.spaceBeforeDeclarationEnd;
    }

    public XMLFormat setNewLine(String newLine)
    {
        PreCondition.assertNotNull(newLine, "newLine");
        PreCondition.assertContainsOnly(newLine, XML.whitespaceCharacters, "newLine");

        this.newLine = newLine;
        return this;
    }

    public String getNewLine()
    {
        return this.newLine;
    }

    public XMLFormat setSingleIndent(String singleIndent)
    {
        PreCondition.assertNotNull(singleIndent, "singleIndent");
        PreCondition.assertContainsOnly(singleIndent, XML.whitespaceCharacters, "singleIndent");

        this.singleIndent = singleIndent;
        return this;
    }

    public String getSingleIndent()
    {
        return this.singleIndent;
    }
}
