package qub;

public interface XMLTextTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLText.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> XMLText.create(text), expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("text cannot be null."));
                createErrorTest.run("", new PreConditionFailure("text cannot be empty."));

                final Action1<String> createTest = (String text) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final XMLText xmlText = XMLText.create(text);
                        test.assertNotNull(xmlText);
                        test.assertEqual(text, xmlText.getText());
                        test.assertEqual(text, xmlText.toString());
                        test.assertEqual(XMLText.isWhitespace(text), xmlText.isWhitespace());
                    });
                };

                createTest.run(" ");
                createTest.run("\n");
                createTest.run("\r");
                createTest.run("\t");
                createTest.run("a");
                createTest.run(" a ");
            });



            runner.testGroup("toString()", () ->
            {
                final Action1<String> toStringTest = (String text) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(text, XMLText.create(text).toString());
                    });
                };

                toStringTest.run("   ");
                toStringTest.run("abc");
                toStringTest.run(" abc ");
            });

            runner.testGroup("toString(XMLFormat)", () ->
            {
                final Action2<String,XMLFormat> toStringTest = (String text, XMLFormat format) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertEqual(text, XMLText.create(text).toString(format));
                    });
                };

                toStringTest.run("   ", XMLFormat.consise);
                toStringTest.run("abc", XMLFormat.consise);
                toStringTest.run(" abc ", XMLFormat.consise);

                toStringTest.run("   ", XMLFormat.pretty);
                toStringTest.run("abc", XMLFormat.pretty);
                toStringTest.run(" abc ", XMLFormat.pretty);
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLText,Object,Boolean> equalsTest = (XMLText xmlText, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(xmlText, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, xmlText.equals(rhs));
                    });
                };

                equalsTest.run(XMLText.create("a"), null, false);
                equalsTest.run(XMLText.create("ab"), "ab", false);
                equalsTest.run(XMLText.create("abc"), XMLText.create("abc"), true);
                equalsTest.run(XMLText.create("abc"), XMLText.create("d"), false);
            });

            runner.testGroup("equals(XMLText)", () ->
            {
                final Action3<XMLText,XMLText,Boolean> equalsTest = (XMLText xmlText, XMLText rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(xmlText, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, xmlText.equals(rhs));
                    });
                };

                equalsTest.run(XMLText.create("a"), null, false);
                equalsTest.run(XMLText.create("ab"), XMLText.create("ab"), true);
                equalsTest.run(XMLText.create("abc"), XMLText.create("d"), false);
            });
        });
    }
}
