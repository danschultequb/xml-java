package qub;

public interface XMLCDataTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLCData.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> XMLCData.create(text), expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("text cannot be null."));

                final Action1<String> createTest = (String text) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final XMLCData cdata = XMLCData.create(text);
                        test.assertNotNull(cdata);
                        test.assertEqual(text, cdata.getText());
                        test.assertEqual("<![CDATA[" + text + "]]>", cdata.toString());
                    });
                };

                createTest.run("");
                createTest.run("a");
                createTest.run(" a ");
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLCData,Object,Boolean> equalsTest = (XMLCData cdata, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(cdata, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, cdata.equals(rhs));
                    });
                };

                equalsTest.run(XMLCData.create(""), null, false);
                equalsTest.run(XMLCData.create(""), "", false);
                equalsTest.run(XMLCData.create(""), XMLCData.create(""), true);
                equalsTest.run(XMLCData.create("abc"), XMLCData.create(""), false);
            });

            runner.testGroup("equals(XMLCData)", () ->
            {
                final Action3<XMLCData,XMLCData,Boolean> equalsTest = (XMLCData cdata, XMLCData rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(cdata, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, cdata.equals(rhs));
                    });
                };

                equalsTest.run(XMLCData.create(""), null, false);
                equalsTest.run(XMLCData.create(""), XMLCData.create(""), true);
                equalsTest.run(XMLCData.create("abc"), XMLCData.create(""), false);
            });
        });
    }
}
