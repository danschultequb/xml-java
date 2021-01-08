package qub;

public interface XMLDeclarationTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(XMLDeclaration.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final XMLDeclaration declaration = XMLDeclaration.create();
                test.assertNotNull(declaration);
                test.assertEqual("1.0", declaration.getVersion());
                test.assertEqual("", declaration.getEncoding());
                test.assertEqual("", declaration.getStandalone());
            });

            runner.testGroup("setVersion(String)", () ->
            {
                final Action3<XMLDeclaration,String,Throwable> setVersionErrorTest = (XMLDeclaration declaration, String version, Throwable expected) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(version)), (Test test) ->
                    {
                        test.assertThrows(() -> declaration.setVersion(version), expected);
                    });
                };

                setVersionErrorTest.run(XMLDeclaration.create(), null, new PreConditionFailure("version cannot be null."));
                setVersionErrorTest.run(XMLDeclaration.create(), "", new PreConditionFailure("version cannot be empty."));

                final Action2<XMLDeclaration,String> setVersionTest = (XMLDeclaration declaration, String version) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(version)), (Test test) ->
                    {
                        final XMLDeclaration setVersionResult = declaration.setVersion(version);
                        test.assertSame(declaration, setVersionResult);
                        test.assertEqual(version, declaration.getVersion());
                    });
                };

                setVersionTest.run(XMLDeclaration.create(), "1.0");
                setVersionTest.run(XMLDeclaration.create(), "1.1");
                setVersionTest.run(XMLDeclaration.create(), "hello");
            });

            runner.testGroup("setEncoding(String)", () ->
            {
                final Action3<XMLDeclaration,String,Throwable> setEncodingErrorTest = (XMLDeclaration declaration, String encoding, Throwable expected) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(encoding)), (Test test) ->
                    {
                        test.assertThrows(() -> declaration.setEncoding(encoding), expected);
                    });
                };

                setEncodingErrorTest.run(XMLDeclaration.create(), null, new PreConditionFailure("encoding cannot be null."));

                final Action2<XMLDeclaration,String> setEncodingTest = (XMLDeclaration declaration, String encoding) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(encoding)), (Test test) ->
                    {
                        final XMLDeclaration setEncodingResult = declaration.setEncoding(encoding);
                        test.assertSame(declaration, setEncodingResult);
                        test.assertEqual(encoding, declaration.getEncoding());
                    });
                };

                setEncodingTest.run(XMLDeclaration.create(), "");
                setEncodingTest.run(XMLDeclaration.create(), "UTF-8");
                setEncodingTest.run(XMLDeclaration.create(), "UTF-16");
                setEncodingTest.run(XMLDeclaration.create(), "1.0");
                setEncodingTest.run(XMLDeclaration.create(), "1.1");
                setEncodingTest.run(XMLDeclaration.create(), "hello");
            });

            runner.testGroup("setStandalone(String)", () ->
            {
                final Action3<XMLDeclaration,String,Throwable> setStandaloneErrorTest = (XMLDeclaration declaration, String standalone, Throwable expected) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(standalone)), (Test test) ->
                    {
                        test.assertThrows(() -> declaration.setStandalone(standalone), expected);
                    });
                };

                setStandaloneErrorTest.run(XMLDeclaration.create(), null, new PreConditionFailure("standalone cannot be null."));
                setStandaloneErrorTest.run(XMLDeclaration.create(), "abc", new PreConditionFailure("standalone (\"abc\") must be \"\", \"yes\", or \"no\"."));
                setStandaloneErrorTest.run(XMLDeclaration.create(), "Yes", new PreConditionFailure("standalone (\"Yes\") must be \"\", \"yes\", or \"no\"."));
                setStandaloneErrorTest.run(XMLDeclaration.create(), "NO", new PreConditionFailure("standalone (\"NO\") must be \"\", \"yes\", or \"no\"."));

                final Action2<XMLDeclaration,String> setStandaloneTest = (XMLDeclaration declaration, String standalone) ->
                {
                    runner.test("with " + English.andList(declaration, Strings.escapeAndQuote(standalone)), (Test test) ->
                    {
                        final XMLDeclaration setStandaloneResult = declaration.setStandalone(standalone);
                        test.assertSame(declaration, setStandaloneResult);
                        test.assertEqual(standalone, declaration.getStandalone());
                    });
                };

                setStandaloneTest.run(XMLDeclaration.create(), "");
                setStandaloneTest.run(XMLDeclaration.create(), "yes");
                setStandaloneTest.run(XMLDeclaration.create(), "no");
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<XMLDeclaration,String> toStringTest = (XMLDeclaration declaration, String expected) ->
                {
                    runner.test("with " + declaration, (Test test) ->
                    {
                        test.assertEqual(expected, declaration.toString());
                    });
                };

                toStringTest.run(
                    XMLDeclaration.create(),
                    "<?xml version=\"1.0\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("hello"),
                    "<?xml version=\"hello\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0"),
                    "<?xml version=\"1.0\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setEncoding("UTF-8"),
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setStandalone("yes"),
                    "<?xml version=\"1.0\" standalone=\"yes\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            });

            runner.testGroup("toString(XMLFormat)", () ->
            {
                final Action3<XMLDeclaration,XMLFormat,String> toStringTest = (XMLDeclaration declaration, XMLFormat format, String expected) ->
                {
                    runner.test("with " + declaration, (Test test) ->
                    {
                        test.assertEqual(expected, declaration.toString(format));
                    });
                };

                toStringTest.run(
                    XMLDeclaration.create(),
                    XMLFormat.consise,
                    "<?xml version=\"1.0\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("hello"),
                    XMLFormat.consise,
                    "<?xml version=\"hello\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0"),
                    XMLFormat.consise,
                    "<?xml version=\"1.0\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setEncoding("UTF-8"),
                    XMLFormat.consise,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setStandalone("yes"),
                    XMLFormat.consise,
                    "<?xml version=\"1.0\" standalone=\"yes\"?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    XMLFormat.consise,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");

                toStringTest.run(
                    XMLDeclaration.create(),
                    XMLFormat.pretty,
                    "<?xml version=\"1.0\" ?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("hello"),
                    XMLFormat.pretty,
                    "<?xml version=\"hello\" ?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0"),
                    XMLFormat.pretty,
                    "<?xml version=\"1.0\" ?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setEncoding("UTF-8"),
                    XMLFormat.pretty,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setStandalone("yes"),
                    XMLFormat.pretty,
                    "<?xml version=\"1.0\" standalone=\"yes\" ?>");
                toStringTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    XMLFormat.pretty,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLDeclaration,Object,Boolean> equalsTest = (XMLDeclaration declaration, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(declaration, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, declaration.equals(rhs));
                    });
                };

                equalsTest.run(XMLDeclaration.create(), null, false);
                equalsTest.run(XMLDeclaration.create(), "hello", false);
                equalsTest.run(
                    XMLDeclaration.create(),
                    XMLDeclaration.create(),
                    true);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.1"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setEncoding("UTF-8"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setStandalone("no"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    true);
            });

            runner.testGroup("equals(XMLDeclaration)", () ->
            {
                final Action3<XMLDeclaration,XMLDeclaration,Boolean> equalsTest = (XMLDeclaration declaration, XMLDeclaration rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(declaration, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, declaration.equals(rhs));
                    });
                };

                equalsTest.run(XMLDeclaration.create(), null, false);
                equalsTest.run(
                    XMLDeclaration.create(),
                    XMLDeclaration.create(),
                    true);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.1"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setEncoding("UTF-8"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setStandalone("no"),
                    XMLDeclaration.create(),
                    false);
                equalsTest.run(
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    XMLDeclaration.create()
                        .setVersion("1.0")
                        .setEncoding("UTF-8")
                        .setStandalone("yes"),
                    true);
            });
        });
    }
}
