package qub;

public interface XMLDocumentTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLDocument.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final XMLDocument document = XMLDocument.create();
                test.assertNotNull(document);
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLDocument,Object,Boolean> equalsTest = (XMLDocument document, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(document, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, document.equals(rhs));
                    });
                };

                equalsTest.run(XMLDocument.create(), null, false);
                equalsTest.run(XMLDocument.create(), "xml", false);
                equalsTest.run(XMLDocument.create(), XMLDocument.create(), true);
            });

            runner.testGroup("equals(XMLDocument)", () ->
            {
                final Action3<XMLDocument,XMLDocument,Boolean> equalsTest = (XMLDocument document, XMLDocument rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(document, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, document.equals(rhs));
                    });
                };

                equalsTest.run(XMLDocument.create(), null, false);
                equalsTest.run(XMLDocument.create(), XMLDocument.create(), true);
            });
        });
    }
}
