package qub;

public interface XMLFormatTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLFormat.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final XMLFormat format = XMLFormat.create();
                test.assertNotNull(format);
                test.assertEqual("", format.getNewLine());
                test.assertEqual("", format.getSingleIndent());
                test.assertEqual("", format.getSpaceBeforeDeclarationEnd());
            });

            runner.test("consise", (Test test) ->
            {
                final XMLFormat consise = XMLFormat.consise;
                test.assertNotNull(consise);
                test.assertEqual("", consise.getNewLine());
                test.assertEqual("", consise.getSingleIndent());
                test.assertEqual("", consise.getSpaceBeforeDeclarationEnd());
            });

            runner.test("pretty", (Test test) ->
            {
                final XMLFormat pretty = XMLFormat.pretty;
                test.assertNotNull(pretty);
                test.assertEqual("\n", pretty.getNewLine());
                test.assertEqual("  ", pretty.getSingleIndent());
                test.assertEqual(" ", pretty.getSpaceBeforeDeclarationEnd());
            });

            runner.testGroup("setSpaceBeforeDeclarationEnd(String)", () ->
            {
                final Action2<String,Throwable> setSpaceBeforeDeclarationEndErrorTest = (String value, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(value), (Test test) ->
                    {
                        final XMLFormat format = XMLFormat.create();
                        test.assertThrows(() -> format.setSpaceBeforeDeclarationEnd(value), expected);
                    });
                };

                setSpaceBeforeDeclarationEndErrorTest.run(null, new PreConditionFailure("spaceBeforeDeclarationEnd cannot be null."));
                setSpaceBeforeDeclarationEndErrorTest.run("a", new PreConditionFailure("spaceBeforeDeclarationEnd (a) must contain only [' ','\t','\r','\n']."));

                final Action1<String> setSpaceBeforeDeclarationEndTest = (String value) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(value), (Test test) ->
                    {
                        final XMLFormat format = XMLFormat.create();
                        final XMLFormat result = format.setSpaceBeforeDeclarationEnd(value);
                        test.assertSame(format, result);
                        test.assertEqual(value, format.getSpaceBeforeDeclarationEnd());
                    });
                };

                setSpaceBeforeDeclarationEndTest.run("");
                setSpaceBeforeDeclarationEndTest.run("    ");
            });
        });
    }
}
