package qub;

/**
 * A collection of functions for interacting with XML content.
 */
public interface XML
{
    /**
     * The valid <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> characters in an XML document.
     */
    char[] whitespaceCharacters = new char[] { ' ', '\t', '\r', '\n' };

    /**
     * The valid quote characters that can be used to surround an attribute value.
     */
    char[] attributeValueQuoteCharacters = new char[] { '\'', '\"' };

    static Result<XMLDocument> parse(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return Result.create(() ->
        {
            XMLDocument result;
            try (final CharacterReadStream readStream = CharacterReadStream.create(BufferedByteReadStream.create(file.getContentsReadStream().await())))
            {
                result = XML.parse(CharacterReadStream.iterate(readStream)).await();
            }
            return result;
        });
    }

    /**
     * Parse an XMLDocument from the provided text.
     * @param text The text to parse.
     * @return The parsed XMLDocument.
     */
    static Result<XMLDocument> parse(String text)
    {
        PreCondition.assertNotNull(text, "text");

        return XML.parse(Strings.iterable(text));
    }

    /**
     * Parse an XMLDocument from the provided characters.
     * @param characters The characters to parse.
     * @return The parsed XMLDocument.
     */
    static Result<XMLDocument> parse(Iterable<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return XML.parse(characters.iterate());
    }

    /**
     * Parse an XMLDocument from the provided characters.
     * @param characters The characters to parse.
     * @return The parsed XMLDocument.
     */
    static Result<XMLDocument> parse(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            characters.start();

            boolean isFirstSegment = true;
            final XMLDocument result = XMLDocument.create();
            while (characters.hasCurrent())
            {
                switch (characters.getCurrent())
                {
                    case '<':
                        characters.next();

                        if (!characters.hasCurrent())
                        {
                            throw XML.missing("tag name");
                        }

                        switch (characters.getCurrent())
                        {
                            case '?':
                                characters.next();

                                XML.expect(characters, XML::isNameStartCharacter, "declaration name (\"xml\")");

                                final String declarationName = XML.parseName(characters).await();
                                if (!declarationName.equals("xml"))
                                {
                                    throw XML.expected("declaration name (\"xml\")");
                                }

                                XML.parseWhitespace(characters).await();

                                final XMLAttribute declarationVersion = XML.parseAttribute(characters, "version", "declaration version").await();

                                if (!characters.hasCurrent())
                                {
                                    throw XML.missing("declaration right question mark ('?')");
                                }

                                XMLAttribute declarationEncoding = null;
                                XMLAttribute declarationStandalone = null;
                                while (characters.hasCurrent() && XML.isWhitespaceCharacter(characters.getCurrent()))
                                {
                                    XML.parseWhitespace(characters).await();

                                    if (!characters.hasCurrent() ||
                                        !XML.isNameStartCharacter(characters.getCurrent()) ||
                                        declarationStandalone != null)
                                    {
                                        break;
                                    }

                                    if (declarationEncoding == null)
                                    {
                                        if (characters.getCurrent() == 'e')
                                        {
                                            declarationEncoding = XML.parseAttribute(characters, "encoding", "declaration encoding").await();
                                            if (Strings.isNullOrEmpty(declarationEncoding.getValue()))
                                            {
                                                throw XML.expected("declaration encoding attribute value to be not empty");
                                            }
                                        }
                                        else if (characters.getCurrent() == 's')
                                        {
                                            declarationStandalone = XML.parseAttribute(characters, "standalone", "declaration standalone").await();
                                            if (!Iterable.create("no", "yes").contains(declarationStandalone.getValue()))
                                            {
                                                throw XML.expected("declaration standalone attribute value to be \"no\" or \"yes\"");
                                            }
                                        }
                                        else
                                        {
                                            throw XML.expected("declaration encoding attribute, standalone attribute, or right question mark ('?')");
                                        }
                                    }
                                    else
                                    {
                                        if (characters.getCurrent() == 's')
                                        {
                                            declarationStandalone = XML.parseAttribute(characters, "standalone", "declaration standalone").await();
                                            if (!Iterable.create("no", "yes").contains(declarationStandalone.getValue()))
                                            {
                                                throw XML.expected("declaration standalone attribute value to be \"no\" or \"yes\"");
                                            }
                                        }
                                        else
                                        {
                                            throw XML.expected("declaration standalone attribute or right question mark ('?')");
                                        }
                                    }
                                }

                                XML.expectAndTake(characters, '?', "declaration right question mark ('?')");
                                XML.expectAndTake(characters, '>', "declaration right angle bracket ('>')");

                                if (result.getDeclaration() != null)
                                {
                                    throw new ParseException("An XML document can only have one declaration.");
                                }
                                if (!isFirstSegment)
                                {
                                    throw XML.expected("the XML declaration to be the first character in the document");
                                }

                                final XMLDeclaration declaration = XMLDeclaration.create()
                                    .setVersion(declarationVersion.getValue());
                                if (declarationEncoding != null)
                                {
                                    declaration.setEncoding(declarationEncoding.getValue());
                                }
                                if (declarationStandalone != null)
                                {
                                    declaration.setStandalone(declarationStandalone.getValue());
                                }
                                result.setDeclaration(declaration);
                                break;

                            case '/':
                                throw new ParseException("An XML document cannot have an end tag without a start tag.");

                            case '!':
                                characters.next();

                                XML.expect(characters, new char[] { '-', '[' }, "comment first left dash ('-') or CDATA first left square bracket ('[')");

                                if (characters.getCurrent() == '-')
                                {
                                    XML.parseCommentAtFirstLeftDash(characters).await();
                                }
                                else // if (characters.getCurrent() == '[')
                                {
                                    XML.parseCDataAtFirstLeftSquareBracket(characters).await();
                                    throw new ParseException("An XML document cannot have a CDATA tag at its root.");
                                }
                                break;

                            default:
                                if (XML.isNameStartCharacter(characters.getCurrent()))
                                {
                                    final XMLElement element = XML.parseElementAtName(characters).await();
                                    if (result.getRoot() != null)
                                    {
                                        throw new ParseException("An XML document can only have one root element.");
                                    }
                                    result.setRoot(element);
                                }
                                else
                                {
                                    throw new ParseException("Unexpected tag name start character: " + Characters.escapeAndQuote(characters.getCurrent()));
                                }
                                break;
                        }
                        break;

                    default:
                        final XMLText text = XML.parseText(characters).await();
                        if (!text.isWhitespace())
                        {
                            throw XML.expected("only whitespace and elements at the root of the document");
                        }
                        break;
                }
                isFirstSegment = false;
            }

            return result;
        });
    }

    static Result<XMLElement> parseElementAtName(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            final String name = XML.parseName(characters, "start tag or empty element").await();
            final List<XMLAttribute> attributes = List.create();

            while (characters.hasCurrent() && XML.isWhitespaceCharacter(characters.getCurrent()))
            {
                XML.parseWhitespace(characters).await();

                if (characters.hasCurrent() && XML.isNameStartCharacter(characters.getCurrent()))
                {
                    attributes.add(XML.parseAttribute(characters, "start tag").await());
                }
            }

            XMLElement result;
            boolean isEmptyElement = characters.hasCurrent() && characters.getCurrent() == '/';
            if (isEmptyElement)
            {
                characters.next();
            }
            result = XMLElement.create(name, !isEmptyElement)
                .setAttributes(attributes);

            XML.expectAndTake(characters, '>', (isEmptyElement ? "empty element" : "start tag") + " right angle bracket ('>')");

            if (result.isSplit())
            {
                boolean foundEndTag = false;
                while (characters.hasCurrent() && !foundEndTag)
                {
                    switch (characters.getCurrent())
                    {
                        case '<':
                            characters.next();

                            if (!characters.hasCurrent())
                            {
                                throw XML.missing("tag name");
                            }

                            switch (characters.getCurrent())
                            {
                                case '?':
                                    throw XML.expected("the XML declaration to be the first character in the document");

                                case '/':
                                    characters.next();

                                    XML.expect(characters, XML::isNameStartCharacter, "end tag name");

                                    final String endTagName = XML.parseName(characters, "end tag").await();

                                    XML.parseOptionalWhitespace(characters);

                                    XML.expectAndTake(characters, '>', "end tag right angle bracket ('>')");

                                    if (endTagName.equals(name))
                                    {
                                        foundEndTag = true;
                                    }
                                    else
                                    {
                                        throw XML.expected("an end tag with the name same as the current element");
                                    }
                                    break;

                                case '!':
                                    characters.next();

                                    XML.expect(characters, new char[] { '-', '[' }, "comment first left dash ('-') or CDATA first left square bracket ('[')");

                                    if (characters.getCurrent() == '-')
                                    {
                                        XML.parseCommentAtFirstLeftDash(characters).await();
                                    }
                                    else // if (characters.getCurrent() == '[')
                                    {
                                        final XMLCData cdata = XML.parseCDataAtFirstLeftSquareBracket(characters).await();
                                        result.addChild(cdata);
                                    }
                                    break;

                                default:
                                    if (XML.isNameStartCharacter(characters.getCurrent()))
                                    {
                                        final XMLElement element = XML.parseElementAtName(characters).await();
                                        result.addChild(element);
                                    }
                                    else
                                    {
                                        throw new ParseException("Unexpected tag name start character: " + Characters.escapeAndQuote(characters.getCurrent()));
                                    }
                                    break;
                            }
                            break;

                        default:
                            final XMLText text = XML.parseText(characters).await();
                            if (!text.isWhitespace())
                            {
                                result.addChild(text);
                            }
                            break;
                    }
                }

                if (!foundEndTag)
                {
                    throw XML.missing("end tag");
                }
            }

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    static Result<XMLComment> parseCommentAtFirstLeftDash(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            XML.expectAndTake(characters, '-', "comment first left dash ('-')");
            XML.expectAndTake(characters, '-', "comment second left dash ('-')");

            final CharacterList commentText = CharacterList.create();
            int rightDashCount = 0;
            while (characters.hasCurrent())
            {
                if (characters.getCurrent() == '-')
                {
                    characters.next();
                    if (rightDashCount < 2)
                    {
                        ++rightDashCount;
                    }
                    else
                    {
                        commentText.add('-');
                    }
                }
                else if (characters.getCurrent() == '>' && rightDashCount == 2)
                {
                    break;
                }
                else
                {
                    while (rightDashCount > 0)
                    {
                        commentText.add('-');
                        --rightDashCount;
                    }

                    commentText.add(characters.takeCurrent());
                }
            }

            if (rightDashCount == 0)
            {
                throw XML.missing("comment first right dash ('-')");
            }
            else if (rightDashCount == 1)
            {
                throw XML.missing("comment second right dash ('-')");
            }

            XML.expectAndTake(characters, '>', "comment right angle bracket ('>')");

            return XMLComment.create(commentText.toString(true));
        });
    }

    static Result<XMLCData> parseCDataAtFirstLeftSquareBracket(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            XML.expectAndTake(characters, '[', "CDATA tag first left square bracket ('[')");

            final String name = XML.parseName(characters, "CDATA tag").await();
            if (!name.equals("CDATA"))
            {
                throw XML.expected("CDATA tag name (\"CDATA\")");
            }

            XML.expectAndTake(characters, '[', "CDATA tag second left square bracket ('[')");

            final CharacterList cdataText = CharacterList.create();
            int rightSquareBracketCount = 0;
            while (characters.hasCurrent())
            {
                if (characters.getCurrent() == ']')
                {
                    characters.next();
                    if (rightSquareBracketCount < 2)
                    {
                        ++rightSquareBracketCount;
                    }
                    else
                    {
                        cdataText.add(']');
                    }
                }
                else if (characters.getCurrent() == '>' && rightSquareBracketCount == 2)
                {
                    break;
                }
                else
                {
                    while (rightSquareBracketCount > 0)
                    {
                        cdataText.add(']');
                        --rightSquareBracketCount;
                    }

                    cdataText.add(characters.takeCurrent());
                }
            }

            if (rightSquareBracketCount == 0)
            {
                throw XML.missing("CDATA tag first right square bracket (']')");
            }
            else if (rightSquareBracketCount == 1)
            {
                throw XML.missing("CDATA tag second right square bracket (']')");
            }

            XML.expectAndTake(characters, '>', "CDATA tag right angle bracket ('>')");

            return XMLCData.create(cdataText.toString(true));
        });
    }

    static Result<XMLAttribute> parseAttribute(Iterator<Character> characters, String attributeDescription)
    {
        PreCondition.assertNotNull(characters, "characters");

        return XML.parseAttribute(characters, null, attributeDescription);
    }

    static Result<XMLAttribute> parseAttribute(Iterator<Character> characters, String expectedAttributeName, String attributeDescription)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            final String description = XML.join(attributeDescription, "attribute");

            final String name = XML.parseName(characters, description).await();
            if (!Strings.isNullOrEmpty(expectedAttributeName) && !expectedAttributeName.equals(name))
            {
                throw XML.expected(description);
            }

            XML.parseOptionalWhitespace(characters).await();

            XML.expectAndTake(characters, '=', description + " equals sign ('=')");

            XML.parseOptionalWhitespace(characters).await();

            final String value = XML.parseQuotedString(characters, description + " value").await();

            return XMLAttribute.createWithQuotedValue(name, value);
        });
    }

    /**
     * Parse <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> from the provided characters.
     * @param characters The characters to parse
     *                   <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> from.
     * @return The parsed <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> string.
     */
    static Result<String> parseWhitespace(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            characters.start();

            XML.expect(characters, XML::isWhitespaceCharacter, "whitespace");

            return XML.parseOptionalWhitespace(characters).await();
        });
    }

    /**
     * Parse <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> from the provided characters. If the characters
     * don't start with whitespace, then this will return immediately.
     * @param characters The characters to parse
     *                   <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> from.
     * @return The parsed <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> string.
     */
    static Result<String> parseOptionalWhitespace(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            characters.start();

            final CharacterList whitespace = CharacterList.create();
            while (characters.hasCurrent() && XML.isWhitespaceCharacter(characters.getCurrent()))
            {
                whitespace.add(characters.takeCurrent());
            }

            final String result = whitespace.toString(true);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    static Result<String> parseQuotedString(Iterator<Character> characters, String quotedStringDescription)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            characters.start();

            final String description = XML.join(quotedStringDescription, "quoted string");

            final char quoteCharacter = XML.expectAndTake(characters, XML.attributeValueQuoteCharacters, description + " start quote character (' or \")");

            final CharacterList list = CharacterList.create(quoteCharacter);
            while (characters.hasCurrent() && characters.getCurrent() != quoteCharacter)
            {
                list.add(characters.takeCurrent());
            }

            if (!characters.hasCurrent())
            {
                throw XML.missing(description + " end quote character (" + quoteCharacter + ")");
            }
            else
            {
                list.add(characters.takeCurrent());
            }

            final String result = list.toString(true);

            PostCondition.assertNotNullAndNotEmpty(result, "result");
            PostCondition.assertTrue(Strings.isQuoted(result), "Strings.isQuoted(result)");

            return result;
        });
    }

    /**
     * Parse an <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a> from the provided characters.
     * @param characters The characters to parse an <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a> from.
     * @return The parsed <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a>.
     */
    static Result<String> parseName(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");

        return XML.parseName(characters, "");
    }

    /**
     * Parse an <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a> from the provided characters.
     * @param characters The characters to parse an <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a> from.
     * @return The parsed <a href="https://www.w3.org/TR/xml/#NT-Name">XML name</a>.
     */
    static Result<String> parseName(Iterator<Character> characters, String nameDescription)
    {
        PreCondition.assertNotNull(characters, "characters");

        return Result.create(() ->
        {
            characters.start();

            XML.expect(characters, XML::isNameStartCharacter, XML.join(nameDescription, "name start character"));

            final CharacterList name = CharacterList.create(characters.takeCurrent());
            while (characters.hasCurrent() && XML.isNameCharacter(characters.getCurrent()))
            {
                name.add(characters.takeCurrent());
            }

            final String result = name.toString(true);

            PostCondition.assertNotNullAndNotEmpty(result, "result");

            return result;
        });
    }

    static Result<XMLText> parseText(Iterator<Character> characters)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertTrue(characters.hasCurrent(), "characters.hasCurrent()");

        return Result.create(() ->
        {
            final CharacterList text = CharacterList.create();

            boolean isWhitespace = true;
            while (characters.hasCurrent() && characters.getCurrent() != '<')
            {
                if (isWhitespace && !XML.isWhitespaceCharacter(characters.getCurrent()))
                {
                    isWhitespace = false;
                }

                text.add(characters.takeCurrent());
            }

            return XMLText.create(text.toString(true), isWhitespace);
        });
    }

    /**
     * Get whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-NameStartChar">name start character</a>.
     * @param character The character to check.
     * @return Whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-NameStartChar">name start character</a>.
     */
    static boolean isNameStartCharacter(char character)
    {
        return character == ':' ||
            ('A' <= character && character <= 'Z') ||
            character == '_' ||
            ('a' <= character && character <= 'z') ||
            (0x00C0 <= character && character <= 0x00D6) ||
            (0x00D8 <= character && character <= 0x00F6) ||
            (0x00F8 <= character && character <= 0x02FF) ||
            (0x0370 <= character && character <= 0x037D) ||
            (0x037F <= character && character <= 0x1FFF) ||
            (0x200C <= character && character <= 0x200D) ||
            (0x2070 <= character && character <= 0x218F) ||
            (0x2C00 <= character && character <= 0x2FEF) ||
            (0x3001 <= character && character <= 0xD7FF) ||
            (0xF900 <= character && character <= 0xFDCF) ||
            (0xFDF0 <= character && character <= 0xFFFD);
    }

    /**
     * Get whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-NameChar">name character</a>.
     * @param character The character to check.
     * @return Whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-NameChar">name character</a>.
     */
    static boolean isNameCharacter(char character)
    {
        return XML.isNameStartCharacter(character) ||
            character == '-' ||
            character == '.' ||
            ('0' <= character && character <= '9') ||
            character == 0x00B7 ||
            (0x0300 <= character && character <= 0x036F) ||
            (0x203F <= character && character <= 0x2040);
    }

    /**
     * Get whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> character.
     * @param character The character to check.
     * @return Whether or not the provided character is a
     * <a href="https://www.w3.org/TR/xml/#NT-S">whitespace</a> character.
     */
    static boolean isWhitespaceCharacter(char character)
    {
        return character == ' ' ||
            character == '\n' ||
            character == '\r' ||
            character == '\t';
    }

    static char expectAndTake(Iterator<Character> characters, char expectedCharacter, String description)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        return XML.expectAndTake(characters, (Character character) -> character == expectedCharacter, description);
    }

    static void expect(Iterator<Character> characters, char[] expectedCharacters, String description)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertNotNullAndNotEmpty(expectedCharacters, "expectedCharacters");
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        XML.expect(characters, (Character character) -> Array.contains(expectedCharacters, character), description);
    }

    static char expectAndTake(Iterator<Character> characters, char[] expectedCharacters, String description)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertNotNullAndNotEmpty(expectedCharacters, "expectedCharacters");
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        return XML.expectAndTake(characters, (Character character) -> Array.contains(expectedCharacters, character), description);
    }

    static void expect(Iterator<Character> characters, Function1<Character,Boolean> expectation, String description)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertNotNull(expectation, "expectation");
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        if (!characters.hasCurrent())
        {
            throw XML.missing(description);
        }
        if (!expectation.run(characters.getCurrent()))
        {
            throw XML.expected(description);
        }
    }

    static char expectAndTake(Iterator<Character> characters, Function1<Character,Boolean> expectation, String description)
    {
        PreCondition.assertNotNull(characters, "characters");
        PreCondition.assertNotNull(expectation, "expectation");
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        XML.expect(characters, expectation, description);
        return characters.takeCurrent();
    }

    static String join(String... values)
    {
        PreCondition.assertNotNullAndNotEmpty(values, "values");

        final CharacterList builder = CharacterList.create();
        for (final String value : values)
        {
            if (!Strings.isNullOrEmpty(value))
            {
                if (builder.any())
                {
                    builder.add(' ');
                }
                builder.addAll(value);
            }
        }
        final String result = builder.toString(true);

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    static ParseException missing(String description)
    {
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        return new ParseException("Missing " + description + ".");
    }

    static ParseException expected(String description)
    {
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        return new ParseException("Expected " + description + ".");
    }

    static String toString(Function2<IndentedCharacterWriteStream,XMLFormat,Result<Integer>> toStringFunction)
    {
        return XML.toString(XMLFormat.consise, toStringFunction);
    }

    static String toString(XMLFormat format, Function2<IndentedCharacterWriteStream,XMLFormat,Result<Integer>> toStringFunction)
    {
        PreCondition.assertNotNull(format, "format");
        PreCondition.assertNotNull(toStringFunction, "toStringFunction");

        return XML.toString((IndentedCharacterWriteStream stream) -> toStringFunction.run(stream, format));
    }

    static String toString(Function1<IndentedCharacterWriteStream,Result<Integer>> toStringFunction)
    {
        PreCondition.assertNotNull(toStringFunction, "toStringFunction");

        final InMemoryCharacterStream stream = InMemoryCharacterStream.create();
        final IndentedCharacterWriteStream indentedStream = IndentedCharacterWriteStream.create(stream);
        toStringFunction.run(indentedStream).await();
        return stream.getText().await();
    }
}
