package qub;

public class XMLElement implements XMLElementChild
{
    private boolean split;
    private final String name;
    private final MutableMap<String,String> attributes;
    private final List<XMLElementChild> children;

    private XMLElement(String name, boolean split)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.name = name;
        this.attributes = Map.create();
        this.split = split;
        this.children = List.create();
    }

    public static XMLElement create(String name)
    {
        return XMLElement.create(name, false);
    }

    public static XMLElement create(String name, boolean split)
    {
        return new XMLElement(name, split);
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isSplit()
    {
        return this.split;
    }

    /**
     * Set whether or not this XMLElement is split into a start and end tag. If split is false, then
     * this XMLElement cannot have any child elements.
     * @param split Whether or not to split this XMLElement into a start and end tag.
     * @return This object for method chaining.
     */
    public XMLElement setSplit(boolean split)
    {
        PreCondition.assertTrue(split || !this.getChildren().contains((XMLElementChild child) -> child instanceof XMLElement || !((XMLText)child).isWhitespace()), "split || !this.getChildren().contains((XMLElementChild child) -> child instanceof XMLElement || !((XMLText)child).isWhitespace())");

        this.split = split;

        return this;
    }

    public XMLElement setAttribute(String attributeName, String attributeValue)
    {
        PreCondition.assertNotNullAndNotEmpty(attributeName, "attributeName");
        PreCondition.assertNotNull(attributeValue, "attributeValue");

        this.attributes.set(attributeName, attributeValue);
        return this;
    }

    public XMLElement setAttribute(XMLAttribute attribute)
    {
        PreCondition.assertNotNull(attribute, "attribute");

        return this.setAttribute(attribute.getName(), attribute.getValue());
    }

    public XMLElement setAttributes(Iterable<XMLAttribute> attributes)
    {
        PreCondition.assertNotNull(attributes, "attributes");

        for (final XMLAttribute attribute : attributes)
        {
            this.setAttribute(attribute);
        }
        return this;
    }

    public Iterable<XMLAttribute> getAttributes()
    {
        return this.attributes.map(XMLAttribute::create);
    }

    public Result<String> getAttributeValue(String attributeName)
    {
        PreCondition.assertNotNullAndNotEmpty(attributeName, "attributeName");

        return this.attributes.get(attributeName)
            .convertError(NotFoundException.class, () -> new NotFoundException("Couldn't find an attribute named " + Strings.escapeAndQuote(attributeName) + " in the element."));
    }

    /**
     * Remove all of the attributes from this XMLElement.
     * @return This object for method chaining.
     */
    public XMLElement clearAttributes()
    {
        this.attributes.clear();
        return this;
    }

    /**
     * Remove the attribute with the provided name from this XMLElement.
     * @param attributeName The name of the attribute to remove.
     * @return The removed attribute.
     */
    public Result<XMLAttribute> removeAttribute(String attributeName)
    {
        PreCondition.assertNotNullAndNotEmpty(attributeName, "attributeName");

        return Result.create(() ->
        {
            final String removedAttributeValue = this.attributes.remove(attributeName)
                .convertError(NotFoundException.class, () -> new NotFoundException("No attribute with the name " + Strings.escapeAndQuote(attributeName) + " was found in this XMLElement."))
                .await();
            return XMLAttribute.create(attributeName, removedAttributeValue);
        });
    }

    /**
     * Get the children of this XMLElement.
     * @return The children of this XMLElement.
     */
    public Indexable<XMLElementChild> getChildren()
    {
        return this.children;
    }

    /**
     * Get the children of this XMLElement that are XMLElements.
     * @return The children of this XMLElement that are XMLElements.
     */
    public Iterable<XMLElement> getElementChildren()
    {
        return this.getChildren().instanceOf(XMLElement.class);
    }

    /**
     * Get the children of this XMLElement that are XMLElements and have the provided name.
     * @param name The name of the XMLElement children to return.
     * @return The children of this XMLElement that are XMLElements and have the provided name.
     */
    public Iterable<XMLElement> getElementChildren(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        return this.getElementChildren((XMLElement child) -> child.getName().equals(name));
    }

    /**
     * Get the children of this XMLElement that are XMLElements and that satisfy the provided
     * condition.
     * @param condition The condition that the XMLElement children must satisfy.
     * @return The children of this XMLElement that are XMLElements and that satisfy the provided
     * condition.
     */
    public Iterable<XMLElement> getElementChildren(Function1<XMLElement,Boolean> condition)
    {
        PreCondition.assertNotNull(condition, "condition");

        return this.getElementChildren()
            .where(condition);
    }

    /**
     * Get the first XML element child with the provided name. If no XML element child is found with
     * the provided name, then return a NotFoundException.
     * @param name The name of the XML element child to return.
     * @return The first XML element child with the provided name or a NotFoundException if no XML
     * element child with the provided name is found.
     */
    public Result<XMLElement> getFirstElementChild(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        return this.getFirstElementChild((XMLElement element) -> element.getName().equals(name))
            .convertError(NotFoundException.class, () -> new NotFoundException("No XML element children found with the name " + Strings.escapeAndQuote(name) + "."));
    }

    /**
     * Get the first XML element child that matches the provided condition. If no matching XML
     * element child is found, then return a NotFoundException.
     * @param condition The condition that the XML element child must match.
     * @return The first XML element child that matches the provided condition or a
     * NotFoundException if no XML element child that matches the provided condition is found.
     */
    public Result<XMLElement> getFirstElementChild(Function1<XMLElement,Boolean> condition)
    {
        PreCondition.assertNotNull(condition, "condition");

        return Result.create(() ->
        {
            final Iterable<XMLElement> elements = this.getElementChildren(condition);
            if (Iterable.isNullOrEmpty(elements))
            {
                throw new NotFoundException("No XML element children found that match the provided condition.");
            }
            return elements.first();
        });
    }

    /**
     * Get the first XML element child with the provided name. If no XML element child is found with
     * the provided name, then a new XML element child will be created, added to this XML element,
     * and then returned.
     * @param name The name of the XML element child to return.
     * @return The first XML element child with the provided name or a new XML element child if no
     * XML element child with the provided name is found.
     */
    public XMLElement getFirstOrCreateElementChild(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        return this.getFirstOrCreateElementChild(name, () -> XMLElement.create(name));
    }

    /**
     * Get the first XML element child with the provided name. If no XML element child is found with
     * the provided name, then a new XML element child will be created, added to this XML element,
     * and then returned.
     * @param name The name of the XML element child to return.
     * @return The first XML element child with the provided name or a new XML element child if no
     * XML element child with the provided name is found.
     */
    public XMLElement getFirstOrCreateElementChild(String name, Function0<XMLElement> elementCreator)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");
        PreCondition.assertNotNull(elementCreator, "elementCreator");

        XMLElement result = this.getFirstElementChild(name)
            .catchError(NotFoundException.class)
            .await();
        if (result == null)
        {
            result = elementCreator.run();
            this.addChild(result);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Get the first XML element child that matches the provided condition. If no matching XML
     * element child is found that matches the provided condition, then a new XML element child will
     * be created, added to this XML element, and then returned.
     * @param condition The condition that the XML element child must match.
     * @param elementCreator The function that will create the new XML element child if no existing
     *                       child is found that matches the provided condition.
     * @return The first XML element child with the provided name or a new XML element child if no
     * XML element child that matches the provided condition is found.
     */
    public XMLElement getFirstOrCreateElementChild(Function1<XMLElement,Boolean> condition, Function0<XMLElement> elementCreator)
    {
        PreCondition.assertNotNull(condition, "condition");
        PreCondition.assertNotNull(elementCreator, "elementCreator");

        XMLElement result = this.getFirstElementChild(condition)
            .catchError(NotFoundException.class)
            .await();
        if (result == null)
        {
            result = elementCreator.run();
            this.addChild(result);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Add the provided XMLElementChild to this XML element.
     * @param child The XMLElementChild to add.
     * @return This object for method chaining.
     */
    public XMLElement addChild(XMLElementChild child)
    {
        PreCondition.assertNotNull(child, "child");

        this.children.add(child);
        this.split = true;
        return this;
    }

    public XMLElement addChildren(Iterable<XMLElementChild> children)
    {
        PreCondition.assertNotNull(children, "children");

        for (final XMLElementChild child : children)
        {
            this.addChild(child);
        }
        return this;
    }

    public XMLElement addChildren(XMLElementChild... children)
    {
        PreCondition.assertNotNull(children, "children");

        for (final XMLElementChild child : children)
        {
            this.addChild(child);
        }
        return this;
    }

    /**
     * Remove all of the children of this XMLElement.
     * @return This object for method chaining.
     */
    public XMLElement clearChildren()
    {
        this.children.clear();
        return this;
    }

    /**
     * Remove the provided child from this XMLElement.
     * @param child The child to remove from this XMLElement.
     * @return The result of removing the child.
     */
    public Result<Void> removeChild(XMLElementChild child)
    {
        PreCondition.assertNotNull(child, "child");

        return Result.create(() ->
        {
            if (!this.children.remove(child))
            {
                throw new NotFoundException("Could not remove the child " + child + " because it didn't exist.");
            }
        });
    }

    /**
     * Remove all element children from this XMLElement with the provided name.
     * @param childElementName The name of the element children to remove.
     * @return The result of removing the children.
     */
    public Result<Iterable<XMLElement>> removeElementChildren(String childElementName)
    {
        PreCondition.assertNotNullAndNotEmpty(childElementName, "childElementName");

        return this.removeElementChildren((XMLElement element) -> element.getName().equals(childElementName))
            .convertError(NotFoundException.class, () -> new NotFoundException("No child element found with the name " + Strings.escapeAndQuote(childElementName) + "."));
    }

    /**
     * Remove all element children from this XMLElement that satisfy the provided condition.
     * @param condition The condition to check against each of the XMLElement children.
     * @return The result of removing the children.
     */
    public Result<Iterable<XMLElement>> removeElementChildren(Function1<XMLElement,Boolean> condition)
    {
        PreCondition.assertNotNull(condition, "condition");

        return Result.create(() ->
        {
            final List<XMLElement> result = List.create();
            XMLElement removedElement;
            while (true)
            {
                removedElement = (XMLElement)this.children.removeFirst((XMLElementChild child) -> child instanceof XMLElement && condition.run((XMLElement)child));
                if (removedElement != null)
                {
                    result.add(removedElement);
                }
                else
                {
                    break;
                }
            }

            if (!result.any())
            {
                throw new NotFoundException("No child element found that matched the provided condition.");
            }

            return result;
        });
    }

    @Override
    public String toString()
    {
        return XML.toString((Function2<IndentedCharacterWriteStream,XMLFormat,Result<Integer>>)this::toString);
    }

    public String toString(XMLFormat format)
    {
        return XML.toString(format, this::toString);
    }

    @Override
    public Result<Integer> toString(IndentedCharacterWriteStream stream, XMLFormat format)
    {
        PreCondition.assertNotNull(stream, "stream");
        PreCondition.assertNotDisposed(stream, "stream");
        PreCondition.assertNotNull(format, "format");

        return Result.create(() ->
        {
            int result = 0;

            result += stream.write('<').await();
            result += stream.write(this.name).await();

            for (final MapEntry<String,String> attribute : this.attributes)
            {
                result += stream.write(' ').await();
                result += stream.write(attribute.getKey()).await();
                result += stream.write("=\"").await();
                result += stream.write(attribute.getValue()).await();
                result += stream.write('\"').await();
            }

            if (!this.split)
            {
                result += stream.write("/>").await();
            }
            else
            {
                result += stream.write('>').await();

                if (this.children.any())
                {
                    stream.setSingleIndent(format.getSingleIndent());
                    final String newLine = format.getNewLine();

                    boolean previousChildWasText = false;
                    for (final XMLElementChild child : this.children)
                    {
                        if (child instanceof XMLText)
                        {
                            result += child.toString(stream, format).await();
                            previousChildWasText = true;
                        }
                        else
                        {
                            stream.increaseIndent();
                            try
                            {
                                if (!previousChildWasText)
                                {
                                    result += stream.write(newLine).await();
                                }
                                result += child.toString(stream, format).await();
                            }
                            finally
                            {
                                stream.decreaseIndent();
                            }
                            previousChildWasText = false;
                        }
                    }
                    if (!previousChildWasText)
                    {
                        result += stream.write(newLine).await();
                    }
                }

                result += stream.write("</").await();
                result += stream.write(this.name).await();
                result += stream.write('>').await();
            }

            return result;
        });
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof XMLElement && this.equals((XMLElement)rhs);
    }

    public boolean equals(XMLElement rhs)
    {
        return rhs != null &&
            this.split == rhs.split &&
            this.name.equals(rhs.name) &&
            this.attributes.equals(rhs.attributes) &&
            this.children.equals(rhs.children);
    }
}
