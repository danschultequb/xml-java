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
                test.assertNull(document.getDeclaration());
                test.assertNull(document.getRoot());
            });

            runner.testGroup("setDeclaration(XMLDeclaration)", () ->
            {
                final Action2<XMLDocument,XMLDeclaration> setDeclarationTest = (XMLDocument document, XMLDeclaration declaration) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(document), declaration), (Test test) ->
                    {
                        final XMLDocument setDeclarationResult = document.setDeclaration(declaration);
                        test.assertSame(document, setDeclarationResult);
                        test.assertSame(declaration, document.getDeclaration());
                    });
                };

                setDeclarationTest.run(XMLDocument.create(), null);
                setDeclarationTest.run(XMLDocument.create(), XMLDeclaration.create());
                setDeclarationTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("a")),
                    XMLDeclaration.create()
                        .setVersion("b"));
            });

            runner.testGroup("setRoot(XMLElement)", () ->
            {
                final Action2<XMLDocument,XMLElement> setRootTest = (XMLDocument document, XMLElement root) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(document), root), (Test test) ->
                    {
                        final XMLDocument setRootResult = document.setRoot(root);
                        test.assertSame(document, setRootResult);
                        test.assertSame(root, document.getRoot());
                    });
                };

                setRootTest.run(XMLDocument.create(), null);
                setRootTest.run(XMLDocument.create(), XMLElement.create("a"));
                setRootTest.run(
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")),
                    XMLElement.create("b"));
            });

            runner.testGroup("toString() with no format", () ->
            {
                final Action2<XMLDocument,String> toStringTest = (XMLDocument document, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(document), (Test test) ->
                    {
                        test.assertEqual(expected, document.toString());
                    });
                };

                toStringTest.run(XMLDocument.create(), "");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create().setVersion("1.1")),
                    "<?xml version=\"1.1\"?>");
                toStringTest.run(
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")),
                    "<a/>");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("a")
                            .setEncoding("b")
                            .setStandalone("yes"))
                        .setRoot(XMLElement.create("a")
                            .addChild(XMLElement.create("b")
                                .addChild(XMLText.create("Hello There!")))),
                    "<?xml version=\"a\" encoding=\"b\" standalone=\"yes\"?><a><b>Hello There!</b></a>");
            });

            runner.testGroup("toString() with consise format", () ->
            {
                final Action2<XMLDocument,String> toStringTest = (XMLDocument document, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(document), (Test test) ->
                    {
                        test.assertEqual(expected, document.toString(XMLFormat.consise));
                    });
                };

                toStringTest.run(XMLDocument.create(), "");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create().setVersion("1.1")),
                    "<?xml version=\"1.1\"?>");
                toStringTest.run(
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")),
                    "<a/>");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("a")
                            .setEncoding("b")
                            .setStandalone("yes"))
                        .setRoot(XMLElement.create("a")
                            .addChild(XMLElement.create("b")
                                .addChild(XMLText.create("Hello There!")))),
                    "<?xml version=\"a\" encoding=\"b\" standalone=\"yes\"?><a><b>Hello There!</b></a>");
            });

            runner.testGroup("toString() with pretty format", () ->
            {
                final Action2<XMLDocument,String> toStringTest = (XMLDocument document, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(document), (Test test) ->
                    {
                        test.assertEqual(expected, document.toString(XMLFormat.pretty));
                    });
                };

                toStringTest.run(XMLDocument.create(), "");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create().setVersion("1.1")),
                    "<?xml version=\"1.1\" ?>");
                toStringTest.run(
                    XMLDocument.create()
                        .setRoot(XMLElement.create("a")),
                    "<a/>");
                toStringTest.run(
                    XMLDocument.create()
                        .setDeclaration(XMLDeclaration.create()
                            .setVersion("a")
                            .setEncoding("b")
                            .setStandalone("yes"))
                        .setRoot(XMLElement.create("a")
                            .addChild(XMLElement.create("b")
                                .addChild(XMLText.create("Hello There!")))),
                    "<?xml version=\"a\" encoding=\"b\" standalone=\"yes\" ?>\n<a>\n  <b>Hello There!</b>\n</a>");
            });

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLDocument,Object,Boolean> equalsTest = (XMLDocument document, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(document), Strings.escapeAndQuote(document)), (Test test) ->
                    {
                        test.assertEqual(expected, document.equals(rhs));
                    });
                };

                equalsTest.run(XMLDocument.create(), null, false);
                equalsTest.run(XMLDocument.create(), "xml", false);
                equalsTest.run(XMLDocument.create(), XMLDocument.create(), true);
                equalsTest.run(XMLDocument.create().setDeclaration(XMLDeclaration.create()), XMLDocument.create(), false);
                equalsTest.run(XMLDocument.create().setDeclaration(XMLDeclaration.create()), XMLDocument.create().setDeclaration(XMLDeclaration.create()), true);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create(), false);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create().setRoot(XMLElement.create("b")), false);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create().setRoot(XMLElement.create("a")), true);
            });

            runner.testGroup("equals(XMLDocument)", () ->
            {
                final Action3<XMLDocument,XMLDocument,Boolean> equalsTest = (XMLDocument document, XMLDocument rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(document), Strings.escapeAndQuote(document)), (Test test) ->
                    {
                        test.assertEqual(expected, document.equals(rhs));
                    });
                };

                equalsTest.run(XMLDocument.create(), null, false);
                equalsTest.run(XMLDocument.create(), XMLDocument.create(), true);
                equalsTest.run(XMLDocument.create().setDeclaration(XMLDeclaration.create()), XMLDocument.create(), false);
                equalsTest.run(XMLDocument.create().setDeclaration(XMLDeclaration.create()), XMLDocument.create().setDeclaration(XMLDeclaration.create()), true);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create(), false);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create().setRoot(XMLElement.create("b")), false);
                equalsTest.run(XMLDocument.create().setRoot(XMLElement.create("a")), XMLDocument.create().setRoot(XMLElement.create("a")), true);
            });
        });
    }
}
