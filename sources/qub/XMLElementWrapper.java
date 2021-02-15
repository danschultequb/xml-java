package qub;

public interface XMLElementWrapper
{
    /**
     * Get the XML element representation of this object.
     * @return The XML element representation of this object.
     */
    XMLElement toXml();

    /**
     * Get the String representation of the provided object.
     * @param wrapper The object to get the String representation of.
     * @return The String representation of the provided object.
     */
    static String toString(XMLElementWrapper wrapper)
    {
        PreCondition.assertNotNull(wrapper, "wrapper");

        return wrapper.toXml().toString();
    }

    default String toString(XMLFormat format)
    {
        return this.toXml().toString(format);
    }

    default Result<Integer> toString(CharacterWriteStream stream)
    {
        return this.toXml().toString(stream);
    }

    default Result<Integer> toString(CharacterWriteStream stream, XMLFormat format)
    {
        return this.toXml().toString(stream, format);
    }

    default Result<Integer> toString(IndentedCharacterWriteStream stream)
    {
        return this.toXml().toString(stream);
    }

    default Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        return this.toXml().toString(stream, format);
    }

    static boolean equals(XMLElementWrapper wrapper, Object rhs)
    {
        PreCondition.assertNotNull(wrapper, "wrapper");

        return wrapper.getClass().equals(Types.getType(rhs)) &&
            wrapper.toXml().equals(((XMLElementWrapper)rhs).toXml());
    }
}
