package qub;

public class XMLDocumentWrapperBase implements XMLDocumentWrapper
{
    private final XMLDocument xml;

    protected XMLDocumentWrapperBase(XMLDocument xml)
    {
        PreCondition.assertNotNull(xml, "xml");

        this.xml = xml;
    }

    @Override
    public XMLDocument toXml()
    {
        return this.xml;
    }

    @Override
    public String toString()
    {
        return XMLDocumentWrapper.toString(this);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return XMLDocumentWrapper.equals(this, rhs);
    }
}
