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

            runner.testGroup("clearAttributes()", () ->
            {
                final Action1<XMLElement> clearAttributesTest = (XMLElement element) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        final XMLElement clearAttributesResult = element.clearAttributes();
                        test.assertSame(element, clearAttributesResult);
                        test.assertEqual(Iterable.create(), element.getAttributes());
                    });
                };

                clearAttributesTest.run(XMLElement.create("a"));
                clearAttributesTest.run(XMLElement.create("a").setAttribute("b", "c"));
                clearAttributesTest.run(XMLElement.create("a").setAttribute("b", "c").setAttribute("d", "e"));
            });

            runner.testGroup("removeAttribute(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    test.assertThrows(() -> element.removeAttribute(null),
                        new PreConditionFailure("attributeName cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    test.assertThrows(() -> element.removeAttribute(""),
                        new PreConditionFailure("attributeName cannot be empty."));
                });

                runner.test("with non-existing attribute", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    test.assertThrows(() -> element.removeAttribute("b").await(),
                        new NotFoundException("No attribute with the name \"b\" was found in this XMLElement."));
                });

                runner.test("with non-existing attribute", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a")
                        .setAttribute("b", "c");
                    final XMLAttribute removedAttribute = element.removeAttribute("b").await();
                    test.assertEqual(XMLAttribute.create("b", "c"), removedAttribute);
                    test.assertEqual(Iterable.create(), element.getAttributes());
                });
            });

            runner.testGroup("getElementChildren()", () ->
            {
                final Action2<XMLElement,Iterable<XMLElement>> getElementChildrenTest = (XMLElement element, Iterable<XMLElement> expected) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        test.assertEqual(expected, element.getElementChildren());
                    });
                };

                getElementChildrenTest.run(
                    XMLElement.create("a"),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a", true),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("b")),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    Iterable.create(
                        XMLElement.create("b")));
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    Iterable.create(
                        XMLElement.create("b"),
                        XMLElement.create("c")));
            });

            runner.testGroup("getElementChildren(String)", () ->
            {
                final Action3<XMLElement,String,Throwable> getElementChildrenErrorTest = (XMLElement element, String name, Throwable expected) ->
                {
                    runner.test("with " + English.andList(element, Strings.escapeAndQuote(name)), (Test test) ->
                    {
                        test.assertThrows(() -> element.getElementChildren(name), expected);
                    });
                };

                getElementChildrenErrorTest.run(XMLElement.create("a"), null, new PreConditionFailure("name cannot be null."));
                getElementChildrenErrorTest.run(XMLElement.create("a"), "", new PreConditionFailure("name cannot be empty."));

                final Action3<XMLElement,String,Iterable<XMLElement>> getElementChildrenTest = (XMLElement element, String name, Iterable<XMLElement> expected) ->
                {
                    runner.test("with " + English.andList(element, Strings.escapeAndQuote(name)), (Test test) ->
                    {
                        test.assertEqual(expected, element.getElementChildren(name));
                    });
                };

                getElementChildrenTest.run(
                    XMLElement.create("a"),
                    "b",
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a", true),
                    "b",
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("b")),
                    "b",
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    "b",
                    Iterable.create(
                        XMLElement.create("b")));
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    "b",
                    Iterable.create(
                        XMLElement.create("b")));
            });

            runner.testGroup("getElementChildren(Function1<XMLElement,Boolean>)", () ->
            {
                final Action3<XMLElement,Function1<XMLElement,Boolean>,Throwable> getElementChildrenErrorTest = (XMLElement element, Function1<XMLElement,Boolean> condition, Throwable expected) ->
                {
                    runner.test("with " + English.andList(element, condition), (Test test) ->
                    {
                        test.assertThrows(() -> element.getElementChildren(condition), expected);
                    });
                };

                getElementChildrenErrorTest.run(XMLElement.create("a"), null, new PreConditionFailure("condition cannot be null."));

                final Action3<XMLElement,Function1<XMLElement,Boolean>,Iterable<XMLElement>> getElementChildrenTest = (XMLElement element, Function1<XMLElement,Boolean> condition, Iterable<XMLElement> expected) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        test.assertEqual(expected, element.getElementChildren(condition));
                    });
                };

                getElementChildrenTest.run(
                    XMLElement.create("a"),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a", true),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("b")),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create());
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create(
                        XMLElement.create("b")));
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create(
                        XMLElement.create("b")));
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")
                            .setAttribute("yummy", "no")),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create(
                        XMLElement.create("b")));
                getElementChildrenTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")
                            .setAttribute("yummy", "yes")),
                    (XMLElement element) -> element.getName().equals("b") || Comparer.equal(element.getAttributeValue("yummy").catchError().await(), "yes"),
                    Iterable.create(
                        XMLElement.create("b"),
                        XMLElement.create("c")
                            .setAttribute("yummy", "yes")));
            });

            runner.testGroup("clearChildren()", () ->
            {
                final Action1<XMLElement> clearChildrenTest = (XMLElement element) ->
                {
                    runner.test("with " + element, (Test test) ->
                    {
                        final XMLElement clearChildrenResult = element.clearChildren();
                        test.assertSame(element, clearChildrenResult);
                        test.assertEqual(Iterable.create(), element.getChildren());
                    });
                };

                clearChildrenTest.run(XMLElement.create("a"));
                clearChildrenTest.run(XMLElement.create("a", true));
                clearChildrenTest.run(XMLElement.create("a").addChild(XMLText.create("b")));
                clearChildrenTest.run(XMLElement.create("a").addChild(XMLElement.create("b")));
                clearChildrenTest.run(XMLElement.create("a")
                    .addChild(XMLElement.create("b"))
                    .addChild(XMLElement.create("c")));
            });

            runner.testGroup("removeChild(XMLElementChild)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    test.assertThrows(() -> element.removeChild(null),
                        new PreConditionFailure("child cannot be null."));
                });

                runner.test("with non-existing child", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    final XMLElement child = XMLElement.create("b");
                    test.assertThrows(() -> element.removeChild(child).await(),
                        new NotFoundException("Could not remove the child <b/> because it didn't exist."));
                });

                runner.test("with same existing child", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a");
                    final XMLElement child = XMLElement.create("b");
                    element.addChild(child);

                    test.assertNull(element.removeChild(child).await());
                    test.assertEqual(Iterable.create(), element.getChildren());
                });

                runner.test("with equal existing child", (Test test) ->
                {
                    final XMLElement element = XMLElement.create("a")
                        .addChild(XMLElement.create("b"));

                    test.assertNull(element.removeChild(XMLElement.create("b")).await());
                    test.assertEqual(Iterable.create(), element.getChildren());
                });
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

            runner.testGroup("equals(Object)", () ->
            {
                final Action3<XMLElement,Object,Boolean> equalsTest = (XMLElement element, Object rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(element, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, element.equals(rhs));
                    });
                };

                equalsTest.run(XMLElement.create("a"), null, false);
                equalsTest.run(XMLElement.create("a"), "a", false);
                equalsTest.run(XMLElement.create("a"), XMLElement.create("a"), true);
                equalsTest.run(XMLElement.create("a"), XMLElement.create("A"), false);
                equalsTest.run(
                    XMLElement.create("a", false),
                    XMLElement.create("a", true),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a")
                        .setAttribute("b", "d"),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello there")),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c"))
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c"))
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    true);
            });

            runner.testGroup("equals(XMLElement)", () ->
            {
                final Action3<XMLElement,XMLElement,Boolean> equalsTest = (XMLElement element, XMLElement rhs, Boolean expected) ->
                {
                    runner.test("with " + English.andList(element, rhs), (Test test) ->
                    {
                        test.assertEqual(expected, element.equals(rhs));
                    });
                };

                equalsTest.run(XMLElement.create("a"), null, false);
                equalsTest.run(XMLElement.create("a"), XMLElement.create("a"), true);
                equalsTest.run(XMLElement.create("a"), XMLElement.create("A"), false);
                equalsTest.run(
                    XMLElement.create("a", false),
                    XMLElement.create("a", true),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a")
                        .setAttribute("b", "d"),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    XMLElement.create("a")
                        .setAttribute("b", "c"),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello there")),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    XMLElement.create("a")
                        .addChild(XMLText.create("hello")),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a"),
                    false);
                equalsTest.run(
                    XMLElement.create("a"),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    true);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c"))
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("c"))
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b")),
                    false);
                equalsTest.run(
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    XMLElement.create("a")
                        .addChild(XMLElement.create("b"))
                        .addChild(XMLElement.create("c")),
                    true);
            });
        });
    }
}
