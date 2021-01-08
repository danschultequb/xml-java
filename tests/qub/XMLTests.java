package qub;

public interface XMLTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XML.class, () ->
        {
            runner.testGroup("parse(File)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> XML.parse((File)null),
                        new PreConditionFailure("file cannot be null."));
                });

                runner.test("with non-existing file root", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    final File file = fileSystem.getFile("/folder/file.xml").await();
                    test.assertThrows(() -> XML.parse(file).await(),
                        new RootNotFoundException("/"));
                });

                runner.test("with non-existing file parent", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final File file = fileSystem.getFile("/folder/file.xml").await();
                    test.assertThrows(() -> XML.parse(file).await(),
                        new FileNotFoundException("/folder/file.xml"));
                });

                runner.test("with non-existing file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    fileSystem.createFolder("/folder/").await();
                    final File file = fileSystem.getFile("/folder/file.xml").await();
                    test.assertThrows(() -> XML.parse(file).await(),
                        new FileNotFoundException("/folder/file.xml"));
                });

                runner.test("with existing non-XML file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final File file = fileSystem.getFile("/folder/file.xml").await();
                    file.setContentsAsString("hello there").await();
                    test.assertThrows(() -> XML.parse(file).await(),
                        new ParseException("Expected only whitespace and elements at the root of the document."));
                });

                runner.test("with existing XML file", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final File file = fileSystem.getFile("/folder/file.xml").await();
                    file.setContentsAsString("<a/>").await();
                    test.assertEqual(
                        XMLDocument.create()
                            .setRoot(XMLElement.create("a")),
                        XML.parse(file).await());
                });
            });

            runner.testGroup("parse(String)", () ->
            {
                final Action2<String,Throwable> parseErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> XML.parse(text).await(), expected);
                    });
                };

                parseErrorTest.run(null, new PreConditionFailure("text cannot be null."));
                parseErrorTest.run("@", new ParseException("Expected only whitespace and elements at the root of the document."));
                parseErrorTest.run(null, new PreConditionFailure("text cannot be null."));
                parseErrorTest.run("a", new ParseException("Expected only whitespace and elements at the root of the document."));
                parseErrorTest.run("<", new ParseException("Missing tag name."));
                parseErrorTest.run("<@", new ParseException("Unexpected tag name start character: \"@\""));
                parseErrorTest.run("<a", new ParseException("Missing start tag right angle bracket ('>')."));
                parseErrorTest.run("<a/", new ParseException("Missing empty element right angle bracket ('>')."));
                parseErrorTest.run("<a ", new ParseException("Missing start tag right angle bracket ('>')."));
                parseErrorTest.run("<a @", new ParseException("Expected start tag right angle bracket ('>')."));
                parseErrorTest.run("<a>", new ParseException("Missing end tag."));
                parseErrorTest.run("<a><", new ParseException("Missing tag name."));
                parseErrorTest.run("<a></", new ParseException("Missing end tag name."));
                parseErrorTest.run("<a></>", new ParseException("Expected end tag name."));
                parseErrorTest.run("<a></b", new ParseException("Missing end tag right angle bracket ('>')."));
                parseErrorTest.run("<a></b>", new ParseException("Expected an end tag with the name same as the current element."));
                parseErrorTest.run("<a></a", new ParseException("Missing end tag right angle bracket ('>')."));
                parseErrorTest.run("</", new ParseException("An XML document cannot have an end tag without a start tag."));
                parseErrorTest.run("</a", new ParseException("An XML document cannot have an end tag without a start tag."));
                parseErrorTest.run("</a   f", new ParseException("An XML document cannot have an end tag without a start tag."));
                parseErrorTest.run("<a/><b/>", new ParseException("An XML document can only have one root element."));
                parseErrorTest.run("<a>hello", new ParseException("Missing end tag."));
                parseErrorTest.run("<a><?", new ParseException("Expected the XML declaration to be the first character in the document."));
                parseErrorTest.run("<a><@", new ParseException("Unexpected tag name start character: \"@\""));
                parseErrorTest.run("<!", new ParseException("Missing comment first left dash ('-') or CDATA first left square bracket ('[')."));
                parseErrorTest.run("<!-", new ParseException("Missing comment second left dash ('-')."));
                parseErrorTest.run("<!--", new ParseException("Missing comment first right dash ('-')."));
                parseErrorTest.run("<!---", new ParseException("Missing comment second right dash ('-')."));
                parseErrorTest.run("<!----", new ParseException("Missing comment right angle bracket ('>')."));
                parseErrorTest.run("<!--  hello  --", new ParseException("Missing comment right angle bracket ('>')."));
                parseErrorTest.run("<![", new ParseException("Missing CDATA tag name start character."));
                parseErrorTest.run("<![@", new ParseException("Expected CDATA tag name start character."));
                parseErrorTest.run("<![ ", new ParseException("Expected CDATA tag name start character."));
                parseErrorTest.run("<![C", new ParseException("Expected CDATA tag name (\"CDATA\")."));
                parseErrorTest.run("<![CD", new ParseException("Expected CDATA tag name (\"CDATA\")."));
                parseErrorTest.run("<![CDA", new ParseException("Expected CDATA tag name (\"CDATA\")."));
                parseErrorTest.run("<![CDAT", new ParseException("Expected CDATA tag name (\"CDATA\")."));
                parseErrorTest.run("<![CDATa", new ParseException("Expected CDATA tag name (\"CDATA\")."));
                parseErrorTest.run("<![CDATA", new ParseException("Missing CDATA tag second left square bracket ('[')."));
                parseErrorTest.run("<![CDATA ", new ParseException("Expected CDATA tag second left square bracket ('[')."));
                parseErrorTest.run("<![CDATA[", new ParseException("Missing CDATA tag first right square bracket (']')."));
                parseErrorTest.run("<![CDATA[]", new ParseException("Missing CDATA tag second right square bracket (']')."));
                parseErrorTest.run("<![CDATA[]]", new ParseException("Missing CDATA tag right angle bracket ('>')."));
                parseErrorTest.run("<![CDATA[]] >", new ParseException("Missing CDATA tag first right square bracket (']')."));
                parseErrorTest.run("<![CDATA[ ] ]] > ]>", new ParseException("Missing CDATA tag first right square bracket (']')."));
                parseErrorTest.run("<![CDATA[]]>", new ParseException("An XML document cannot have a CDATA tag at its root."));
                parseErrorTest.run("<?", new ParseException("Missing declaration name (\"xml\")."));
                parseErrorTest.run("<?a", new ParseException("Expected declaration name (\"xml\")."));
                parseErrorTest.run("<?xml", new ParseException("Missing whitespace."));
                parseErrorTest.run("<?xml'", new ParseException("Expected whitespace."));
                parseErrorTest.run("<?xml>", new ParseException("Expected whitespace."));
                parseErrorTest.run("<?xml?>", new ParseException("Expected whitespace."));
                parseErrorTest.run("<?xml?>", new ParseException("Expected whitespace."));
                parseErrorTest.run("<?xml ", new ParseException("Missing declaration version attribute name start character."));
                parseErrorTest.run("<?xml <", new ParseException("Expected declaration version attribute name start character."));
                parseErrorTest.run("<?xml >", new ParseException("Expected declaration version attribute name start character."));
                parseErrorTest.run("<?xml test", new ParseException("Expected declaration version attribute."));
                parseErrorTest.run("<?xml version", new ParseException("Missing declaration version attribute equals sign ('=')."));
                parseErrorTest.run("<?xml version>", new ParseException("Expected declaration version attribute equals sign ('=')."));
                parseErrorTest.run("<?xml version=", new ParseException("Missing declaration version attribute value quoted string start quote character (' or \")."));
                parseErrorTest.run("<?xml version=>", new ParseException("Expected declaration version attribute value quoted string start quote character (' or \")."));
                parseErrorTest.run("<?xml version='", new ParseException("Missing declaration version attribute value quoted string end quote character (')."));
                parseErrorTest.run("<?xml version=\"", new ParseException("Missing declaration version attribute value quoted string end quote character (\")."));
                parseErrorTest.run("<?xml version=\"1.0", new ParseException("Missing declaration version attribute value quoted string end quote character (\")."));
                parseErrorTest.run("<?xml version=\"1.0\"", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" ", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\">", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" >", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\"?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" ?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" test", new ParseException("Expected declaration encoding attribute, standalone attribute, or right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" echo", new ParseException("Expected declaration encoding attribute."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding", new ParseException("Missing declaration encoding attribute equals sign ('=')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=", new ParseException("Missing declaration encoding attribute value quoted string start quote character (' or \")."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding='", new ParseException("Missing declaration encoding attribute value quoted string end quote character (')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"", new ParseException("Missing declaration encoding attribute value quoted string end quote character (\")."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=''", new ParseException("Expected declaration encoding attribute value to be not empty."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"\"", new ParseException("Expected declaration encoding attribute value to be not empty."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\"", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" ", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\">", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\"?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" >", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" ?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" test", new ParseException("Expected declaration standalone attribute or right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" stuff", new ParseException("Expected declaration standalone attribute."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone", new ParseException("Missing declaration standalone attribute equals sign ('=')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=", new ParseException("Missing declaration standalone attribute value quoted string start quote character (' or \")."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='", new ParseException("Missing declaration standalone attribute value quoted string end quote character (')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"", new ParseException("Missing declaration standalone attribute value quoted string end quote character (\")."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=''", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"\"", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='hello'", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='no'", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='yes'", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='yes'?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='yes' ?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='yes'>", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone='yes' >", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" stuff", new ParseException("Expected declaration standalone attribute."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone", new ParseException("Missing declaration standalone attribute equals sign ('=')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone=", new ParseException("Missing declaration standalone attribute value quoted string start quote character (' or \")."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='", new ParseException("Missing declaration standalone attribute value quoted string end quote character (')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone=\"", new ParseException("Missing declaration standalone attribute value quoted string end quote character (\")."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone=''", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone=\"\"", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='hello'", new ParseException("Expected declaration standalone attribute value to be \"no\" or \"yes\"."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='no'", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes'", new ParseException("Missing declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes'?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes' ?", new ParseException("Missing declaration right angle bracket ('>')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes'>", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes' >", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes'encoding", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run("<?xml version=\"1.0\" standalone='yes' encoding", new ParseException("Expected declaration right question mark ('?')."));
                parseErrorTest.run(" <?xml version='1.0'?>", new ParseException("Expected the XML declaration to be the first character in the document."));
                parseErrorTest.run("<?xml version='1.0'?><?xml", new ParseException("Missing whitespace."));
                parseErrorTest.run("<?xml version='1.0'?><?xml version='1.0'?>", new ParseException("An XML document can only have one declaration."));

                final Action2<String,XMLDocument> parseTest = (String text, XMLDocument expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(expected, XML.parse(text).await());
                    });
                };

                parseTest.run("", XMLDocument.create());
                parseTest.run("   ", XMLDocument.create());
                parseTest.run(
                    "<?xml version='1.0'?>",
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("1.0")));
                parseTest.run(
                    "<?xml version='a' encoding='utf-8' standalone='no'?>",
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("a")
                            .setEncoding("utf-8")
                            .setStandalone("no")));
                parseTest.run(
                    "<?xml version='1.0'?>\n\n\n\n",
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("1.0")));
                parseTest.run(
                    "<!-- hello -->",
                    XMLDocument.create());
                parseTest.run(
                    "<!--- hello --->",
                    XMLDocument.create());
                parseTest.run(
                    "<!-- > -> -- -->",
                    XMLDocument.create());
                parseTest.run(
                    "<a/>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")));
                parseTest.run(
                    "<a b='c'/>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")
                            .setAttribute("b", "c")));
                parseTest.run(
                    "<a></a>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a", true)));
                parseTest.run(
                    "<a></a>\n",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a", true)));
                parseTest.run(
                    "<a><b></b></a>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a", true)
                            .addChild(XMLElement.create("b", true))));
                parseTest.run(
                    "<a><!-- b --></a>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a", true)));
                parseTest.run(
                    "<a><![CDATA[data]]></a>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")
                            .addChild(XMLCData.create("data"))));
                parseTest.run(
                    "<a><![CDATA[ ] ]] ]> ]]] ]]></a>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")
                            .addChild(XMLCData.create(" ] ]] ]> ]]] "))));
                parseTest.run(
                    "<b>bold</b>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("b")
                            .addChild(XMLText.create("bold"))));
                parseTest.run(
                    "<b> bo ld </b>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("b")
                            .addChild(XMLText.create(" bo ld "))));
                parseTest.run(
                    "<b> </b>",
                    XMLDocument.create()
                        .setRoot(XMLElement.create("b", true)));
            });

            runner.testGroup("isWhitespaceCharacter(char)", () ->
            {
                final Action2<Character,Boolean> isWhitespaceCharacterTest = (Character character, Boolean expected) ->
                {
                    runner.test("with " + Characters.escapeAndQuote(character), (Test test) ->
                    {
                        test.assertEqual(expected, XML.isWhitespaceCharacter(character));
                    });
                };

                isWhitespaceCharacterTest.run(' ', true);
                isWhitespaceCharacterTest.run('\t', true);
                isWhitespaceCharacterTest.run('\n', true);
                isWhitespaceCharacterTest.run('\r', true);
                isWhitespaceCharacterTest.run('a', false);
            });

            runner.testGroup("isNameStartCharacter(char)", () ->
            {
                final Action2<Character,Boolean> isNameStartCharacterTest = (Character character, Boolean expected) ->
                {
                    runner.test("with " + Characters.escapeAndQuote(character) + " (0x" + Integers.toHexString((int)character, true) + ")", (Test test) ->
                    {
                        test.assertEqual(expected, XML.isNameStartCharacter(character));
                    });
                };

                isNameStartCharacterTest.run('a', true);
                isNameStartCharacterTest.run('z', true);
                isNameStartCharacterTest.run('A', true);
                isNameStartCharacterTest.run('Z', true);
                isNameStartCharacterTest.run('_', true);
                isNameStartCharacterTest.run(':', true);
                isNameStartCharacterTest.run('0', false);
                isNameStartCharacterTest.run('9', false);
                isNameStartCharacterTest.run('.', false);
                isNameStartCharacterTest.run(' ', false);
                isNameStartCharacterTest.run('\n', false);
                isNameStartCharacterTest.run('-', false);
                isNameStartCharacterTest.run('+', false);
                isNameStartCharacterTest.run('@', false);
                isNameStartCharacterTest.run('#', false);
                isNameStartCharacterTest.run('!', false);
                isNameStartCharacterTest.run('\'', false);
                isNameStartCharacterTest.run('\"', false);
                isNameStartCharacterTest.run((char)0x00BF, false);
                isNameStartCharacterTest.run((char)0x00C0, true);
                isNameStartCharacterTest.run((char)0x00D6, true);
                isNameStartCharacterTest.run((char)0x00D7, false);
                isNameStartCharacterTest.run((char)0x00D8, true);
                isNameStartCharacterTest.run((char)0x00F6, true);
                isNameStartCharacterTest.run((char)0x00F7, false);
                isNameStartCharacterTest.run((char)0x00F8, true);
                isNameStartCharacterTest.run((char)0x02FF, true);
                isNameStartCharacterTest.run((char)0x0300, false);
                isNameStartCharacterTest.run((char)0x0369, false);
                isNameStartCharacterTest.run((char)0x0370, true);
                isNameStartCharacterTest.run((char)0x037D, true);
                isNameStartCharacterTest.run((char)0x037E, false);
                isNameStartCharacterTest.run((char)0x037F, true);
                isNameStartCharacterTest.run((char)0x1FFF, true);
                isNameStartCharacterTest.run((char)0x2000, false);
                isNameStartCharacterTest.run((char)0x200B, false);
                isNameStartCharacterTest.run((char)0x200C, true);
                isNameStartCharacterTest.run((char)0x200D, true);
                isNameStartCharacterTest.run((char)0x200E, false);
                isNameStartCharacterTest.run((char)0x206F, false);
                isNameStartCharacterTest.run((char)0x2070, true);
                isNameStartCharacterTest.run((char)0x218F, true);
                isNameStartCharacterTest.run((char)0x2190, false);
                isNameStartCharacterTest.run((char)0x2BFF, false);
                isNameStartCharacterTest.run((char)0x2C00, true);
                isNameStartCharacterTest.run((char)0x2FEF, true);
                isNameStartCharacterTest.run((char)0x2FF0, false);
                isNameStartCharacterTest.run((char)0x3000, false);
                isNameStartCharacterTest.run((char)0x3001, true);
                isNameStartCharacterTest.run((char)0xD7FF, true);
                isNameStartCharacterTest.run((char)0xD800, false);
                isNameStartCharacterTest.run((char)0xF8FF, false);
                isNameStartCharacterTest.run((char)0xF900, true);
                isNameStartCharacterTest.run((char)0xFDCF, true);
                isNameStartCharacterTest.run((char)0xFDD0, false);
                isNameStartCharacterTest.run((char)0xFDEF, false);
                isNameStartCharacterTest.run((char)0xFDF0, true);
                isNameStartCharacterTest.run((char)0xFFFD, true);
                isNameStartCharacterTest.run((char)0xFFFE, false);
                isNameStartCharacterTest.run((char)0xFFFF, false);
            });

            runner.testGroup("isNameCharacter(char)", () ->
            {
                final Action2<Character,Boolean> isNameCharacterTest = (Character character, Boolean expected) ->
                {
                    runner.test("with " + Characters.escapeAndQuote(character) + " (0x" + Integers.toHexString((int)character, true) + ")", (Test test) ->
                    {
                        test.assertEqual(expected, XML.isNameCharacter(character));
                    });
                };

                isNameCharacterTest.run('a', true);
                isNameCharacterTest.run('z', true);
                isNameCharacterTest.run('A', true);
                isNameCharacterTest.run('Z', true);
                isNameCharacterTest.run('_', true);
                isNameCharacterTest.run(':', true);
                isNameCharacterTest.run('0', true);
                isNameCharacterTest.run('9', true);
                isNameCharacterTest.run('.', true);
                isNameCharacterTest.run('-', true);
                isNameCharacterTest.run('+', false);
                isNameCharacterTest.run(' ', false);
                isNameCharacterTest.run('\n', false);
                isNameCharacterTest.run((char)0x00B6, false);
                isNameCharacterTest.run((char)0x00B7, true);
                isNameCharacterTest.run((char)0x00B8, false);
                isNameCharacterTest.run((char)0x00BF, false);
                isNameCharacterTest.run((char)0x00C0, true);
                isNameCharacterTest.run((char)0x00D6, true);
                isNameCharacterTest.run((char)0x00D7, false);
                isNameCharacterTest.run((char)0x00D8, true);
                isNameCharacterTest.run((char)0x00F6, true);
                isNameCharacterTest.run((char)0x00F7, false);
                isNameCharacterTest.run((char)0x00F8, true);
                isNameCharacterTest.run((char)0x02FF, true);
                isNameCharacterTest.run((char)0x0300, true);
                isNameCharacterTest.run((char)0x0369, true);
                isNameCharacterTest.run((char)0x036F, true);
                isNameCharacterTest.run((char)0x0370, true);
                isNameCharacterTest.run((char)0x037D, true);
                isNameCharacterTest.run((char)0x037E, false);
                isNameCharacterTest.run((char)0x037F, true);
                isNameCharacterTest.run((char)0x1FFF, true);
                isNameCharacterTest.run((char)0x2000, false);
                isNameCharacterTest.run((char)0x200B, false);
                isNameCharacterTest.run((char)0x200C, true);
                isNameCharacterTest.run((char)0x200D, true);
                isNameCharacterTest.run((char)0x200E, false);
                isNameCharacterTest.run((char)0x203E, false);
                isNameCharacterTest.run((char)0x203F, true);
                isNameCharacterTest.run((char)0x2040, true);
                isNameCharacterTest.run((char)0x2041, false);
                isNameCharacterTest.run((char)0x206F, false);
                isNameCharacterTest.run((char)0x2070, true);
                isNameCharacterTest.run((char)0x218F, true);
                isNameCharacterTest.run((char)0x2190, false);
                isNameCharacterTest.run((char)0x2BFF, false);
                isNameCharacterTest.run((char)0x2C00, true);
                isNameCharacterTest.run((char)0x2FEF, true);
                isNameCharacterTest.run((char)0x2FF0, false);
                isNameCharacterTest.run((char)0x3000, false);
                isNameCharacterTest.run((char)0x3001, true);
                isNameCharacterTest.run((char)0xD7FF, true);
                isNameCharacterTest.run((char)0xD800, false);
                isNameCharacterTest.run((char)0xF8FF, false);
                isNameCharacterTest.run((char)0xF900, true);
                isNameCharacterTest.run((char)0xFDCF, true);
                isNameCharacterTest.run((char)0xFDD0, false);
                isNameCharacterTest.run((char)0xFDEF, false);
                isNameCharacterTest.run((char)0xFDF0, true);
                isNameCharacterTest.run((char)0xFFFD, true);
                isNameCharacterTest.run((char)0xFFFE, false);
                isNameCharacterTest.run((char)0xFFFF, false);
            });
        });
    }
}
