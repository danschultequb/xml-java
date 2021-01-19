package qub;

public interface XMLElementChild
{
    /**
     * Get the String representation of this XMLElementChild.
     * @param stream The stream to write the String representation of this XMLElementChild to.
     * @param format The format to use when converting this XMLElementChild to a String.
     * @return The number of characters that were written to the stream.
     */
    Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format);
}
