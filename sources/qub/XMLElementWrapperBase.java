package qub;

public abstract class XMLElementWrapperBase implements XMLElementWrapper
{
    private final XMLElement xml;

    protected XMLElementWrapperBase(XMLElement xml)
    {
        PreCondition.assertNotNull(xml, "xml");

        this.xml = xml;
    }

    @Override
    public XMLElement toXml()
    {
        return this.xml;
    }

    @Override
    public String toString()
    {
        return XMLElementWrapper.toString(this);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return XMLElementWrapper.equals(this, rhs);
    }
}
