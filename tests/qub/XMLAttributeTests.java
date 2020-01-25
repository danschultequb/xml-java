package qub;

public interface XMLAttributeTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLAttribute.class, () ->
        {
            runner.testGroup("createWithQuotedValue(String,String,String,char)", () ->
            {
                final Action3<String,String,Throwable> createWithQuotedValueErrorTest = (String name, String quotedValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, quotedValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> XMLAttribute.createWithQuotedValue(name, quotedValue), expected);
                    });
                };

                createWithQuotedValueErrorTest.run(null, "'hello'", new PreConditionFailure("name cannot be null."));
                createWithQuotedValueErrorTest.run("", "'hello'", new PreConditionFailure("name cannot be empty."));
                createWithQuotedValueErrorTest.run("a", null, new PreConditionFailure("quotedValue cannot be null."));
                createWithQuotedValueErrorTest.run("a", "", new PreConditionFailure("quotedValue cannot be empty."));
                createWithQuotedValueErrorTest.run("a", "b", new PreConditionFailure("Strings.isQuoted(quotedValue) cannot be false."));

                final Action4<String,String,String,Character> createWithQuotedValueTest = (String name, String quotedValue, String expectedValue, Character expectedValueQuoteCharacter) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, quotedValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final XMLAttribute attribute = XMLAttribute.createWithQuotedValue(name, quotedValue);
                        test.assertNotNull(attribute);
                        test.assertEqual(name, attribute.getName());
                        test.assertEqual(expectedValue, attribute.getValue());
                        test.assertEqual(expectedValueQuoteCharacter, attribute.getValueQuoteCharacter());
                    });
                };

                createWithQuotedValueTest.run("a", "'b'", "b", '\'');
                createWithQuotedValueTest.run("a", "\"b\"", "b", '\"');
            });

            runner.testGroup("create(String,String)", () ->
            {
                final Action3<String,String,Throwable> createErrorTest = (String name, String value, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> XMLAttribute.create(name, value), expected);
                    });
                };

                createErrorTest.run(null, "hello", new PreConditionFailure("name cannot be null."));
                createErrorTest.run("", "hello", new PreConditionFailure("name cannot be empty."));
                createErrorTest.run("a", null, new PreConditionFailure("value cannot be null."));

                final Action2<String,String> createTest = (String name, String value) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final XMLAttribute attribute = XMLAttribute.create(name, value);
                        test.assertNotNull(attribute);
                        test.assertEqual(name, attribute.getName());
                        test.assertEqual(value, attribute.getValue());
                        test.assertEqual('\"', attribute.getValueQuoteCharacter());
                    });
                };

                createTest.run("a", "b");
                createTest.run("a", "\"");
                createTest.run("a", "'");
                createTest.run("a", "\n");
            });

            runner.testGroup("create(String,String,char)", () ->
            {
                final Action4<String,String,Character,Throwable> createErrorTest = (String name, String value, Character valueQuoteCharacter, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value, valueQuoteCharacter).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> XMLAttribute.create(name, value, valueQuoteCharacter), expected);
                    });
                };

                createErrorTest.run(null, "hello", '\"', new PreConditionFailure("name cannot be null."));
                createErrorTest.run("", "hello", '\"', new PreConditionFailure("name cannot be empty."));
                createErrorTest.run("a", null, '\"', new PreConditionFailure("value cannot be null."));
                createErrorTest.run("a", "", 'a', new PreConditionFailure("valueQuoteCharacter (a) must be either ' or \"."));

                final Action3<String,String,Character> createTest = (String name, String value, Character valueQuoteCharacter) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value, valueQuoteCharacter).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final XMLAttribute attribute = XMLAttribute.create(name, value, valueQuoteCharacter);
                        test.assertNotNull(attribute);
                        test.assertEqual(name, attribute.getName());
                        test.assertEqual(value, attribute.getValue());
                        test.assertEqual(valueQuoteCharacter, attribute.getValueQuoteCharacter());
                    });
                };

                createTest.run("a", "b", '\"');
                createTest.run("a", "b", '\'');
                createTest.run("a", "\"", '\"');
                createTest.run("a", "\"", '\'');
                createTest.run("a", "'", '\"');
                createTest.run("a", "'", '\'');
                createTest.run("a", "\n", '\"');
                createTest.run("a", "\n", '\'');
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<XMLAttribute,String> toStringTest = (XMLAttribute attribute, String expected) ->
                {
                    runner.test("with " + attribute, (Test test) ->
                    {
                        test.assertEqual(expected, attribute.toString());
                    });
                };

                toStringTest.run(XMLAttribute.create("a", "b"), "a=\"b\"");
                toStringTest.run(XMLAttribute.create("a", "\""), "a=\"&#x22;\"");
                toStringTest.run(XMLAttribute.create("a", "'"), "a=\"'\"");
                toStringTest.run(XMLAttribute.create("a", "<"), "a=\"<\"");
                toStringTest.run(XMLAttribute.create("a", "'", '\''), "a='&#x27;'");
                toStringTest.run(XMLAttribute.create("a", "apples", '\''), "a='apples'");
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLAttribute,Object,Boolean> equalsTest = (XMLAttribute attribute, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(attribute, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, attribute.equals(rhs));
                    });
                };

                equalsTest.run(XMLAttribute.create("a", "b"), null, false);
                equalsTest.run(XMLAttribute.create("a", "b"), "hello", false);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("a", "b"), true);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("A", "b"), false);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("a", "B"), false);
            });

            runner.testGroup("equals(XMLAttribute)", () ->
            {
                final Action3<XMLAttribute,XMLAttribute,Boolean> equalsTest = (XMLAttribute attribute, XMLAttribute rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(attribute, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, attribute.equals(rhs));
                    });
                };

                equalsTest.run(XMLAttribute.create("a", "b"), null, false);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("a", "b"), true);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("A", "b"), false);
                equalsTest.run(XMLAttribute.create("a", "b"), XMLAttribute.create("a", "B"), false);
                equalsTest.run(XMLAttribute.create("a", "b", '\''), XMLAttribute.create("a", "b", '\''), true);
                equalsTest.run(XMLAttribute.create("a", "b", '\''), XMLAttribute.create("a", "b", '\"'), false);
            });
        });
    }
}
