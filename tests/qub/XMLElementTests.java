package qub;

public interface XMLElementTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLElement.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String name, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        test.assertThrows(() -> XMLElement.create(name), expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("name cannot be null."));
                createErrorTest.run("", new PreConditionFailure("name cannot be empty."));

                final Action1<String> createTest = (String name) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final XMLElement element = XMLElement.create(name);
                        test.assertNotNull(element);
                        test.assertEqual(name, element.getName());
                        test.assertEqual(Iterable.create(), element.getAttributes());
                        test.assertEqual(Iterable.create(), element.getChildren());
                        test.assertFalse(element.isSplit());
                    });
                };

                createTest.run("a");
                createTest.run("b");
            });

            runner.testGroup("create(String,boolean)", () ->
            {
                final Action3<String,Boolean,Throwable> createErrorTest = (String name, Boolean split, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(name), split), (Test test) ->
                    {
                        test.assertThrows(() -> XMLElement.create(name, split), expected);
                    });
                };

                createErrorTest.run(null, false, new PreConditionFailure("name cannot be null."));
                createErrorTest.run("", true, new PreConditionFailure("name cannot be empty."));

                final Action2<String,Boolean> createTest = (String name, Boolean split) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(name), split), (Test test) ->
                    {
                        final XMLElement element = XMLElement.create(name, split);
                        test.assertNotNull(element);
                        test.assertEqual(name, element.getName());
                        test.assertEqual(Iterable.create(), element.getAttributes());
                        test.assertEqual(Iterable.create(), element.getChildren());
                        test.assertEqual(split, element.isSplit());
                    });
                };

                createTest.run("a", false);
                createTest.run("a", true);
                createTest.run("b", false);
                createTest.run("b", true);
            });

            runner.testGroup("setAttribute(String,String)", () ->
            {
                final Action4<XMLElement,String,String,Throwable> setAttributeErrorTest = (XMLElement element, String attributeName, String attributeValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(element, attributeName, attributeValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> element.setAttribute(attributeName, attributeValue), expected);
                    });
                };

                setAttributeErrorTest.run(XMLElement.create("a"), null, "there", new PreConditionFailure("attributeName cannot be null."));
                setAttributeErrorTest.run(XMLElement.create("a"), "", "there", new PreConditionFailure("attributeName cannot be empty."));
                setAttributeErrorTest.run(XMLElement.create("a"), "hello", null, new PreConditionFailure("attributeValue cannot be null."));

                final Action3<XMLElement,String,String> setAttributeTest = (XMLElement element, String attributeName, String attributeValue) ->
                {
                    runner.test("with " + English.andList(Iterable.create(element, attributeName, attributeValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final XMLElement setAttributeResult = element.setAttribute(attributeName, attributeValue);
                        test.assertSame(element, setAttributeResult);
                        test.assertEqual(attributeValue, element.getAttributeValue(attributeName).await());
                    });
                };

                setAttributeTest.run(XMLElement.create("a"), "b", "");
                setAttributeTest.run(XMLElement.create("a"), "b", "c");
            });

            runner.testGroup("setAttribute(XMLAttribute)", () ->
            {
                final Action3<XMLElement,XMLAttribute,Throwable> setAttributeErrorTest = (XMLElement element, XMLAttribute attribute, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(element, attribute).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> element.setAttribute(attribute), expected);
                    });
                };

                setAttributeErrorTest.run(XMLElement.create("a"), null, new PreConditionFailure("attribute cannot be null."));

                final Action2<XMLElement,XMLAttribute> setAttributeTest = (XMLElement element, XMLAttribute attribute) ->
                {
                    runner.test("with " + English.andList(Iterable.create(element, attribute).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final XMLElement setAttributeResult = element.setAttribute(attribute);
                        test.assertSame(element, setAttributeResult);
                        test.assertEqual(attribute.getValue(), element.getAttributeValue(attribute.getName()).await());
                        test.assertTrue(element.getAttributes().contains(attribute));
                    });
                };

                setAttributeTest.run(XMLElement.create("a"), XMLAttribute.create("b", ""));
                setAttributeTest.run(XMLElement.create("a"), XMLAttribute.create("b", "c"));
            });

            runner.testGroup("getAttributeValue(String)", () ->
            {
                final Action3<XMLElement,String,Throwable> getAttributeValueErrorTest = (XMLElement element, String attributeName, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(element, attributeName).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> element.getAttributeValue(attributeName).await(), expected);
                    });
                };

                getAttributeValueErrorTest.run(XMLElement.create("a"), null, new PreConditionFailure("attributeName cannot be null."));
                getAttributeValueErrorTest.run(XMLElement.create("a"), "", new PreConditionFailure("attributeName cannot be empty."));
                getAttributeValueErrorTest.run(XMLElement.create("a"), "b", new NotFoundException("Couldn't find an attribute named \"b\" in the element."));
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<XMLElement,String> toStringTest = (XMLElement element, String expected) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        test.assertEqual(expected, element.toString());
                    });
                };

                toStringTest.run(XMLElement.create("a"), "<a/>");
                toStringTest.run(XMLElement.create("a", false), "<a/>");
                toStringTest.run(XMLElement.create("a", true), "<a></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", false)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", true)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c")
                        .addChild(XMLElement.create("d")
                            .setAttribute("e", "f")),
                    "<a b=\"c\"><d e=\"f\"/></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    "<a>hello</a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello\nthere")),
                    "<a>hello\nthere</a>");
                toStringTest.run(
                    XMLElement.create("hey")
                        .addChild(XMLText.create("there"))
                        .addChild(XMLElement.create("my"))
                        .addChild(XMLText.create("friend")),
                    "<hey>there<my/>friend</hey>");
            });

            runner.testGroup("toString() with consise format", () ->
            {
                final Action2<XMLElement,String> toStringTest = (XMLElement element, String expected) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        test.assertEqual(expected, element.toString(XMLFormat.consise));
                    });
                };

                toStringTest.run(XMLElement.create("a"), "<a/>");
                toStringTest.run(XMLElement.create("a", false), "<a/>");
                toStringTest.run(XMLElement.create("a", true), "<a></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", false)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", true)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c")
                        .addChild(XMLElement.create("d")
                            .setAttribute("e", "f")),
                    "<a b=\"c\"><d e=\"f\"/></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    "<a>hello</a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello\nthere")),
                    "<a>hello\nthere</a>");
                toStringTest.run(
                    XMLElement.create("hey")
                        .addChild(XMLText.create("there"))
                        .addChild(XMLElement.create("my"))
                        .addChild(XMLText.create("friend")),
                    "<hey>there<my/>friend</hey>");
            });

            runner.testGroup("toString() with pretty format", () ->
            {
                final Action2<XMLElement,String> toStringTest = (XMLElement element, String expected) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        test.assertEqual(expected, element.toString(XMLFormat.pretty));
                    });
                };

                toStringTest.run(XMLElement.create("a"), "<a/>");
                toStringTest.run(XMLElement.create("a", false), "<a/>");
                toStringTest.run(XMLElement.create("a", true), "<a></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", false)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"/>");
                toStringTest.run(
                    XMLElement.create("a", true)
                        .setAttribute("b", "c"),
                    "<a b=\"c\"></a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c")
                        .addChild(XMLElement.create("d")
                            .setAttribute("e", "f")),
                    "<a b=\"c\">\n  <d e=\"f\"/>\n</a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    "<a>hello</a>");
                toStringTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello\nthere")),
                    "<a>hello\nthere</a>");
                toStringTest.run(
                    XMLElement.create("hey")
                        .addChild(XMLText.create("there"))
                        .addChild(XMLElement.create("my"))
                        .addChild(XMLText.create("friend")),
                    "<hey>there<my/>friend</hey>");
            });
        });
    }
}
