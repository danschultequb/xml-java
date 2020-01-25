package qub;

public interface XMLCommentTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLComment.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String text, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        test.assertThrows(() -> XMLComment.create(text), expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("text cannot be null."));

                final Action1<String> createTest = (String text) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(text), (Test test) ->
                    {
                        final XMLComment comment = XMLComment.create(text);
                        test.assertNotNull(comment);
                        test.assertEqual(text, comment.getText());
                        test.assertEqual("<!--" + text + "-->", comment.toString());
                    });
                };

                createTest.run("");
                createTest.run("a");
                createTest.run(" a ");
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLComment,Object,Boolean> equalsTest = (XMLComment comment, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(comment, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, comment.equals(rhs));
                    });
                };

                equalsTest.run(XMLComment.create(""), null, false);
                equalsTest.run(XMLComment.create(""), "", false);
                equalsTest.run(XMLComment.create(""), XMLComment.create(""), true);
                equalsTest.run(XMLComment.create("abc"), XMLComment.create(""), false);
            });

            runner.testGroup("equals(XMLComment)", () ->
            {
                final Action3<XMLComment,XMLComment,Boolean> equalsTest = (XMLComment comment, XMLComment rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(comment, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, comment.equals(rhs));
                    });
                };

                equalsTest.run(XMLComment.create(""), null, false);
                equalsTest.run(XMLComment.create(""), XMLComment.create(""), true);
                equalsTest.run(XMLComment.create("abc"), XMLComment.create(""), false);
            });
        });
    }
}
