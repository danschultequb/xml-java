package qub;

/**
 * An object that can be used to format an XML document.
 */
public class XMLFormat
{
    public static final XMLFormat consise = XMLFormat.create()
        .setSpaceBeforeDeclarationEnd("");

    public static final XMLFormat pretty = XMLFormat.create()
        .setSpaceBeforeDeclarationEnd(" ");

    private String spaceBeforeDeclarationEnd;

    private XMLFormat()
    {
        spaceBeforeDeclarationEnd = "";
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
}
