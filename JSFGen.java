package conv;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class JSFGen {
    private static Map<String, Element> customRadioButtonDetails = new HashMap<String, Element>();
    private static String packagePath = null;


    public JSFGen() {
        super();
    }

    protected static void handlePage(String path, String app, String Dest, String repo, String src,
                                     Map filePaths) throws Exception {
        System.out.println("Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo);
        String pgName = path.substring(path.lastIndexOf("\\") + 1);
        pgName = pgName.replace(".xml", "");
        //String pathVC = Dest + "\\" + app + "\\ViewController";
        String destination = FileReaderWritter.getViewDestinationPath(path, app, Dest, src);
        String pagePath = destination.replace(".xml", ".jsf");
        String tempPath = path.replace(src+FileReaderWritter.getSeparator(), "");
        packagePath = tempPath.substring(0,  tempPath.lastIndexOf("\\"));


        try {
            File oaf = new File(path); // OAF
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document oafDoc = dBuilder.parse(oaf);
            NodeList uiNode = oafDoc.getElementsByTagName("ui:contents");
            Element ui = (Element) uiNode.item(0);
            NodeList nodes = ui.getChildNodes();

            // get pageTitle and WindowTitle
            NodeList pageLayoutList = oafDoc.getElementsByTagName("oa:pageLayout");
            Element pageLayout = (Element) pageLayoutList.item(0);
            NamedNodeMap attrs = pageLayout.getAttributes();
            String title = "";
            String windowTitle = "";
            String amDef = "";
            String jsfBeanName = "";
            for (int j = 0; j < attrs.getLength(); j++) {
                Node currentAtt = attrs.item(j);
                String strAttr = currentAtt.getNodeName();
                if (strAttr.equals("title")) {
                    title = currentAtt.getNodeValue();
                }
                if (strAttr.equals("windowTitle")) {
                    windowTitle = currentAtt.getNodeValue();
                }
                System.out.println("In JSF Gen Java:::node name::" + strAttr);
                System.out.println("In JSF Gen Java:::node value::" + currentAtt.getNodeValue());
                if (strAttr.equals("amDefName")) {
                    amDef = currentAtt.getNodeValue();
                }
                if (strAttr.equals("controllerClass")) {
                    jsfBeanName = currentAtt.getNodeValue();
                }
            }
            String beanPath =
                destination.substring(0, destination.indexOf("ViewController") + 14) +
                FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "view" +
                FileReaderWritter.getSeparator() + jsfBeanName.replace(".", FileReaderWritter.getSeparator()) + ".java";
            DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
            newDbFactory.setValidating(false);
            DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
            Document jsfDoc = newDBuilder.newDocument();

            Element headerNode = createJSF(pgName, title, windowTitle, jsfDoc);
            BeanGen.createBean(pgName, beanPath);

            //            File jsf = new File(Dest + "\\" + app + "\\ViewController\\public_html\\" + pgName + ".jsf");
            //            DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
            //            newDbFactory.setValidating(false);
            //            DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
            //            Document jsfDoc = newDBuilder.parse(jsf);
            //            NodeList formList = jsfDoc.getElementsByTagName("af:panelHeader");
            //            Element form = (Element) formList.item(0);
            // Element retElement = null;
            if (nodes.getLength() > 0) {
                recursiveNodes(nodes, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths, headerNode,
                               beanPath);
            }

            //            for (int i = 0; i < nodes.getLength(); i++) {
            //                Node currentNode = nodes.item(i);
            //                if (currentNode.getNodeType() ==
            //                    Node.ELEMENT_NODE) {
            //                    // System.out.println(currentNode.getNodeName());
            //                    Element retElement =
            //                                              convert(currentNode, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src,
            //                                                      filePaths);
            //                    if (null != retElement)
            //                        form.appendChild(retElement);
            //                }
            //            }

            FileReaderWritter.writeXMLFile(jsfDoc, pagePath);
            String beanCompletePath ="view."+jsfBeanName;
            BeanGen.createAdfConfig(destination.substring(0, destination.indexOf("ViewController") + 14), pgName, beanCompletePath);
            BeanGen.copyProcessFormRequest(path, app, beanPath);

            System.out.println("End Conv: handlePage");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void recursiveNodes(NodeList nodeList, Document jsfDoc, String Dest, String app, String pgName,
                                      String amDef, String jsfBeanName, String src, Map filePaths, Element form,
                                      String beanPath) throws Exception {
        Element retElement = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            System.out.println(node.getNodeName());
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                retElement = convert(node, jsfDoc, Dest, app, pgName, amDef, src, filePaths, false, beanPath);
                if (!node.getNodeName().equals("oa:messageLovInput") &&
                    !node.getNodeName().equals("oa:advancedTable")) {
                    NodeList childNodeList = node.getChildNodes();
                    if (childNodeList.getLength() > 0) {
                        if (retElement != null) {
                            recursiveNodes(childNodeList, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths,
                                           retElement, beanPath);
                        } else {
                            recursiveNodes(childNodeList, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths,
                                           form, beanPath);
                        }
                    }
                }
                if (null != retElement) {
                    System.out.println(form + "...." + retElement);
                    form.appendChild(retElement);
                }
            }
        }
    }

    private static Element convert(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                   String amDef, String src, Map filePaths, boolean isTableComp,
                                   String beanPath) throws Exception {
        System.out.println("Start Conv: convert");
        Element retElement = null;
        String strElement = currentNode.getNodeName(); // oaf
        String path = beanPath;
        if (strElement.equals("oa:messageStyledText")) {
            retElement = convertMessageStyledText(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp);
        } else if (strElement.equals("oa:messageTextInput")) {
            retElement = convertMessageTextInput(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp);
        } else if (strElement.equals("oa:button")) { // when none match
            retElement = convertButton(currentNode, jsfDoc, pgName, path, isTableComp);
        } else if (strElement.equals("oa:submitButton")) { // when none match
            retElement = convertSubmitButton(currentNode, jsfDoc, pgName, path, isTableComp);
        } else if (strElement.equals("oa:messageLovInput")) {
            retElement =
                convertMessageLovInput(currentNode, jsfDoc, Dest, app, src, pgName, amDef, path, filePaths,
                                       isTableComp);
        } else if (strElement.equals("oa:messageChoice")) {
            retElement =
                convertMessageChoice(currentNode, jsfDoc, Dest, app, pgName, amDef, path, filePaths, isTableComp);
        } else if (strElement.equals("oa:link")) {
            retElement = convertLink(currentNode, jsfDoc, path, isTableComp);
        } else if (strElement.equals("oa:advancedTable")) {
            retElement = convertAdvancedTable(currentNode, jsfDoc, Dest, app, pgName, amDef, path, src, filePaths);
        } else if (strElement.equals("oa:image")) {
            retElement = convertImage(currentNode, jsfDoc, path, isTableComp);
        } else if (strElement.equals("oa:messageRadioButton")) {
            retElement =
                convertMessageRadioButton(currentNode, jsfDoc, Dest, app, pgName, amDef, path, filePaths, isTableComp);
        } else if (strElement.equals("oa:tableLayout")) {
            retElement = convertTable(currentNode, jsfDoc);
        } else if (strElement.equals("oa:rowLayout")) {
            retElement = convertRowLayout(currentNode, jsfDoc);
        } else if (strElement.equals("oa:cellFormat")) {
            retElement = convertCellLayout(currentNode, jsfDoc);
        } else if (strElement.equals("oa:messageCheckBox")) {
            retElement = convertCheckBox(currentNode, jsfDoc, path, isTableComp);
        } else if (strElement.equals("oa:switcher")) {
            retElement =
                convertSwitcher(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp, src, filePaths);
        } else if (strElement.equals("oa:messageRadioButton")) {
            retElement =
                convertMessageRadioButton(currentNode, jsfDoc, Dest, app, pgName, amDef, path, filePaths, isTableComp);
        } else if (strElement.equals("oa:spacer")) {
            retElement = convertSpacer(currentNode, jsfDoc);
        }
        return retElement;
    }

    private static Element createJSF(String pgName, String title, String windowTitle,
                                     Document jsfDoc) throws Exception {
        System.out.println("Start Conv: createJSF " + pgName + " " + " " + title + " " + windowTitle);

        Element viewElement = jsfDoc.createElement("f:view");
        jsfDoc.appendChild(viewElement);

        viewElement.setAttribute("xmlns:f", "http://java.sun.com/jsf/core");
        viewElement.setAttribute("xmlns:af", "http://xmlns.oracle.com/adf/faces/rich");

        Element docElement = jsfDoc.createElement("af:document");
        viewElement.appendChild(docElement);

        docElement.setAttribute("id", "d1");
        docElement.setAttribute("title", windowTitle);
        docElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.d1}");

        Element formElement = jsfDoc.createElement("af:form");
        docElement.appendChild(formElement);

        formElement.setAttribute("id", "f1");
        formElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.f1}");

        Element panelHeaderElement = jsfDoc.createElement("af:panelHeader");
        formElement.appendChild(panelHeaderElement);

        panelHeaderElement.setAttribute("id", "ph1");
        panelHeaderElement.setAttribute("text", title);
        panelHeaderElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.ph1}");

        Element facetElement1 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement1);

        facetElement1.setAttribute("name", "context");

        Element facetElement2 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement2);

        facetElement2.setAttribute("name", "menuBar");

        Element facetElement3 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement3);

        facetElement3.setAttribute("name", "toolbar");

        Element facetElement4 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement4);

        facetElement4.setAttribute("name", "legend");

        Element separatorElement = jsfDoc.createElement("af:separator");
        facetElement4.appendChild(separatorElement);

        separatorElement.setAttribute("id", "s1");
        separatorElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.s1}");

        Element facetElement5 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement5);

        facetElement5.setAttribute("name", "info");
        //FileReaderWritter.writeXMLFile(jsfDoc, pathVC + "\\public_html\\" + pgName + ".jsf");
        System.out.println("End Conv: convert");

        return panelHeaderElement;
    }

    public static List<Node> getNode(NodeList nodeList, String nodeName) {
        List<Node> retNodeList = new ArrayList<Node>();
        Node node = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeName().equals(nodeName) ||
                (nodeName.equals("oa:column") && node.getNodeName().equals("oa:columnGroup"))) {
                retNodeList.add(node);
            } else {
                retNodeList.addAll(getNode(node.getChildNodes(), nodeName));
            }
        }
        return retNodeList;
    }

    protected static Element convertMessageStyledText(Node currentNode, Document jsfDoc, String Dest, String app,
                                                      String pgName, String amDef, String beanPath,
                                                      boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichOutputText";
        String imports = " import oracle.adf.view.rich.component.rich.output.RichOutputText; ";

        Element retElement = jsfDoc.createElement("af:outputText");
        NamedNodeMap attrs = currentNode.getAttributes();
        String bindingVO = null;
        String bindingAttr = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                itemName = currentAtt.getNodeValue();
                retElement.setAttribute("id", itemName);
                if (!isTableComp) {
                    retElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean." + itemName + "}");
                }
            }
            if (strAttr.equals("prompt")) {
                retElement.setAttribute("value", currentAtt.getNodeValue());
            }
            if (strAttr.equals("viewAttr")) {
                bindingAttr = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
        }
        if (isTableComp) {
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "tableAttr", null, null, null, packagePath);
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
            //BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        } else {
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null, null,
                                     null, packagePath);
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        System.out.println("End Conv: convert");
        return retElement;
    }

    protected static Element convertColumnElement(Document jsfDoc, String bindingVO, String bindingAttr,
                                                  String columnId, String headerText) {
        Element retElement = jsfDoc.createElement("af:column");
        if (bindingVO != null && bindingAttr != null) {
            retElement.setAttribute("sortProperty", "#{bindings." + bindingVO + ".hints." + bindingAttr + ".name}");
            retElement.setAttribute("headerText", "#{bindings." + bindingVO + ".hints." + bindingAttr + ".label}");
        } else {
            retElement.setAttribute("headerText", headerText);
        }
        retElement.setAttribute("filterable", "true");
        retElement.setAttribute("sortable", "true");
        retElement.setAttribute("id", columnId);
        return retElement;
    }

    protected static Element genericColumnMethod(Node node, Document jsfDoc, String Dest, String app, String pgName,
                                                 String amDef, String beanPath, String bindingVO, String src,
                                                 Element retElement, Map filePaths) throws Exception {
        String columnId = null;
        String bindingAttr = null;
        NodeList nodeList1 = node.getChildNodes();
        for (int i = 0; i < nodeList1.getLength(); i++) {
            Node columnNode = nodeList1.item(i);
            String nodeName = columnNode.getNodeName();
            if (nodeName.equals("ui:columnHeader")) {
                NodeList nodeList2 = columnNode.getChildNodes();
                for (int j = 0; j < nodeList2.getLength(); j++) {
                    Node sortableHdrNode = nodeList2.item(j);
                    String sortableHdrNodeName = sortableHdrNode.getNodeName();
                    if (sortableHdrNodeName.equals("oa:sortableHeader")) {
                        NamedNodeMap attr = sortableHdrNode.getAttributes();
                        for (int k = 0; k < attr.getLength(); k++) {
                            Node currentAtt = attr.item(k);
                            String strAttr = currentAtt.getNodeName();
                            if (strAttr.equals("id")) {
                                columnId = currentAtt.getNodeValue();
                            }
                            if (strAttr.equals("prompt")) {
                                System.out.println(currentAtt.getNodeValue());
                            }
                        }
                    }
                }
            } else if (nodeName.equals("ui:contents")) {
                NodeList nodeList2 = columnNode.getChildNodes();
                for (int j = 0; j < nodeList2.getLength(); j++) {
                    Node compNode = nodeList2.item(j);
                    String compNodeName = compNode.getNodeName();
                    if (compNodeName.equals("oa:messageStyledText")) {
                        NamedNodeMap compAttr = compNode.getAttributes();
                        for (int k = 0; k < compAttr.getLength(); k++) {
                            Node attrNode = compAttr.item(k);
                            String attrNodeName = attrNode.getNodeName();
                            if (attrNodeName.equals("viewAttr")) {
                                bindingAttr = attrNode.getNodeValue();
                            }
                        }


                    }
                    //                    else if (compNodeName.equals("oa:button")) {
                    //                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                    //                        columnElement.appendChild(convertButton(compNode, jsfDoc, pgName, beanPath, true));
                    //                        retElement.appendChild(columnElement);
                    //                    }
                    else if (compNodeName.equals("oa:switcher")) {
                        NamedNodeMap compAttr = compNode.getAttributes();
                        for (int k = 0; k < compAttr.getLength(); k++) {
                            Node attrNode = compAttr.item(k);
                            String attrNodeName = attrNode.getNodeName();
                            if (attrNodeName.equals("viewAttr")) {
                                bindingAttr = attrNode.getNodeValue();
                            }
                        }
                        //                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                        //                        columnElement.appendChild(convertSwitcher(compNode, jsfDoc, Dest, app, pgName, amDef, beanPath,
                        //                                                                  true, src, filePaths));
                        //                        retElement.appendChild(columnElement);
                        //                    } else if (compNodeName.equals("oa:messageLovInput")) {
                        //                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                        //                        columnElement.appendChild(convertMessageLovInput(compNode, jsfDoc, Dest, app, src, pgName,
                        //                                                                         amDef, beanPath, filePaths, true));
                        //                        retElement.appendChild(columnElement);
                        //                    } else if (compNodeName.equals("oa:messageChoice")) {
                        //                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                        //                        columnElement.appendChild(convertMessageChoice(compNode, jsfDoc, Dest, app, pgName, amDef,
                        //                                                                       beanPath, filePaths, true));
                        //                        retElement.appendChild(columnElement);
                    }
                    Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                    Element childElement =
                        convert(compNode, jsfDoc, Dest, app, pgName, amDef, src, filePaths, true, beanPath);
                    System.out.println(childElement);
                    if (childElement != null) {
                        columnElement.appendChild(childElement);
                        retElement.appendChild(columnElement);
                    }
                }
            }
        }
        return retElement;
    }

    protected static void recursiveColumnElements(List<Node> nodes, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, String bindingVO,
                                                  String src, Element retElement, Map filePaths) throws Exception {
        Element toRetElement = null;
        String columnId = null;
        String headerText = null;
        System.out.println("Length:::" + nodes.size());
        for (Node node : nodes) {
            if (node.getNodeName().equals("oa:columnGroup")) {
                NodeList nodeList1 = node.getChildNodes();
                for (int i = 0; i < nodeList1.getLength(); i++) {
                    Node columnNode = nodeList1.item(i);
                    String nodeName = columnNode.getNodeName();
                    if (nodeName.equals("ui:columnHeader")) {
                        NodeList nodeList2 = columnNode.getChildNodes();
                        for (int j = 0; j < nodeList2.getLength(); j++) {
                            Node sortableHdrNode = nodeList2.item(j);
                            String sortableHdrNodeName = sortableHdrNode.getNodeName();
                            if (sortableHdrNodeName.equals("oa:sortableHeader")) {
                                NamedNodeMap attr = sortableHdrNode.getAttributes();
                                for (int k = 0; k < attr.getLength(); k++) {
                                    Node currentAtt = attr.item(k);
                                    String strAttr = currentAtt.getNodeName();
                                    if (strAttr.equals("id")) {
                                        columnId = currentAtt.getNodeValue();
                                    }
                                    if (strAttr.equals("prompt")) {
                                        headerText = currentAtt.getNodeValue();
                                    }
                                }
                            }
                        }
                    }
                }
                toRetElement = convertColumnElement(jsfDoc, null, null, columnId, headerText);
                List<Node> groupNodes = getNode(node.getChildNodes(), "oa:column");
                recursiveColumnElements(groupNodes, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src,
                                        toRetElement, filePaths);
                retElement.appendChild(toRetElement);
            } else if (node.getNodeName().equals("oa:column")) {
                genericColumnMethod(node, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src, retElement,
                                    filePaths);
            }
        }
    }

    protected static Element convertMessageTextInput(Node currentNode, Document jsfDoc, String Dest, String app,
                                                     String pgName, String amDef, String beanPath,
                                                     boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichInputText";
        String imports = " import oracle.adf.view.rich.component.rich.input.RichInputText; ";

        Element retElement = jsfDoc.createElement("af:inputText");
        NamedNodeMap attrs = currentNode.getAttributes();
        String bindingVO = null;
        String bindingAttr = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
                retElement.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean." + itemName + "}");
            }
            if (strAttr.equals("prompt")) {
                retElement.setAttribute("value", currentAtt.getNodeValue());
            }
            if (strAttr.equals("viewAttr")) {
                bindingAttr = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
        }
        if (isTableComp) {
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
        } else {
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null, null,
                                     null, packagePath);
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener");
        System.out.println("End Conv: convert");
        return retElement;
    }

    protected static Element convertButton(Node currentNode, Document jsfDoc, String pgName, String beanPath,
                                           boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichButton";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichButton; ";

        Element retElement = jsfDoc.createElement("af:button");

        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "actionListener");

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        return retElement;
    }

    protected static Element convertSubmitButton(Node currentNode, Document jsfDoc, String pgName, String beanPath,
                                                 boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichCommandButton";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichCommandButton;";


        Element retElement = jsfDoc.createElement("af:commandButton");

        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }

            if (strAttr.equals("text")) {
                retElement.setAttribute("text", currentAtt.getNodeValue());
            }
        }

        //retElement.setAttribute("actionListener", "#{backingBeanScope." + pgName + "Bean." + itemName + "}");


        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        //BeanGen.createActionListener(itemName, beanPath);


        convertVCL(currentNode, beanPath, retElement, pgName, "actionListener");
        return retElement;
    }

    protected static Element convertMessageLovInput(Node currentNode, Document jsfDoc, String Dest, String app,
                                                    String src, String pgName, String amDef, String beanPath,
                                                    Map filePaths, boolean isTableComp) throws Exception {
        Element retElement = jsfDoc.createElement("af:inputListOfValues");
        String itemName = "";
        String itemType = "RichInputListOfValues";
        String imports = "import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;";
        String bindingVO = null;
        String bindingAttr = null;
        String regionName = null;
        String lovAttr = null;
        String lovView = null;
        boolean isExternalLov = false;
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewAttr")) {
                bindingAttr = currentAtt.getNodeValue();
            }

            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
            if (strAttr.equals("externalListOfValues")) {
                isExternalLov = true;
                regionName = src + currentAtt.getNodeValue() + ".xml";
                System.out.println(regionName);
                File regionFile = new File(regionName);

                if (regionFile.exists()) {
                    System.out.println("Region File Exists.......");
                    boolean isLovExists = false;
                    String tempId = null;
                    List<Node> pageNodes = getNode(currentNode.getChildNodes(), "lovMap");
                    for (Node node : pageNodes) {
                        NamedNodeMap namedNodeMap = node.getAttributes();
                        for (int i = 0; i < namedNodeMap.getLength(); i++) {
                            Node attrNode = namedNodeMap.item(i);
                            if (attrNode.getNodeName().equals("lovItem")) {
                                tempId = attrNode.getNodeValue();
                            }
                            if (attrNode.getNodeName().equals("resultTo")) {
                                if (attrNode.getNodeValue().equals(itemName)) {
                                    isLovExists = true;
                                   // break;
                                }
                            }

                        }
                        if(isLovExists) {
                            break;
                        }
                    }
                    DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
                    newDbFactory.setValidating(false);
                    DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
                    Document regionDoc = newDBuilder.parse(regionFile);
                    Element regionRootElement = regionDoc.getDocumentElement();
                    NodeList nodeList = regionRootElement.getChildNodes();
                    List<Node> nodes = getNode(nodeList, "oa:messageStyledText");
                    for (Node node : nodes) {
                        String tempLovAttr = ((Element) node).getAttribute("viewAttr");
                        if (tempLovAttr.equals(tempId)) {
                            lovView = ((Element) node).getAttribute("viewName");
                            lovAttr = ((Element) node).getAttribute("viewAttr");
                        }
                        
                    }
                }
                VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, lovView, lovAttr, "lovInput");
            }
        }

        if (bindingVO != null && bindingAttr != null && !isExternalLov) {
            NodeList nodeList = currentNode.getChildNodes();
            String tempId = null;
            List<Node> pageNodes = getNode(nodeList, "lovMap");
            boolean isLovExists = false;
            for (Node node : pageNodes) {
                NamedNodeMap namedNodeMap = node.getAttributes();
                for (int i = 0; i < namedNodeMap.getLength(); i++) {
                    Node attrNode = namedNodeMap.item(i);
                    if (attrNode.getNodeName().equals("lovItem")) {
                        tempId = attrNode.getNodeValue();
                    }
                    if (attrNode.getNodeName().equals("resultTo")) {
                        if (attrNode.getNodeValue().equals(itemName)) {
                            isLovExists = true;
                           // break;
                        }
                    }

                }
                if(isLovExists) {
                    break;
                }
            }

            List<Node> nodes = getNode(nodeList, "oa:messageStyledText");
            for (Node node : nodes) {
                String tempLovAttr = ((Element) node).getAttribute("id");
                if (tempLovAttr.equals(tempId)) {
                    lovView = ((Element) node).getAttribute("viewName");
                    lovAttr = ((Element) node).getAttribute("viewAttr");
                }
            }
            VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr,(String) filePaths.get(lovView) , lovAttr, "lovInput");
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "listOfValues", null, null,
                                     null, packagePath);
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
            retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
            retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
            retElement.setAttribute("model", "#{bindings." + bindingAttr + ".listOfValuesModel}");
            retElement.setAttribute("columns", "#{bindings." + bindingAttr + ".hints.displayWidth}");
            retElement.setAttribute("popupTitle", "Search and Select: #{bindings." + bindingAttr + ".hints.label}");
        } else {
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
            //            retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
            //            retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
            //            retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
            //            retElement.setAttribute("model", "#{bindings." + bindingAttr + ".listOfValuesModel}");
            //            retElement.setAttribute("columns", "#{bindings." + bindingAttr + ".hints.displayWidth}");
            //            retElement.setAttribute("popupTitle", "Search and Select: #{bindings." + bindingAttr + ".hints.label}");
        }
        return retElement;
    }

    protected static Element convertMessageChoice(Node currentNode, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, Map filePaths,
                                                  boolean isTableComp) throws Exception {
        Element retElement = jsfDoc.createElement("af:selectOneChoice");
        String itemName = "";
        String itemType = "RichSelectOneChoice";
        String imports = "import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;";
        String bindingVO = null;
        String bindingAttr = null;
        String pickListAttr = null;
        String pickListVO = null;
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());
            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewAttr")) {
                bindingAttr = currentAtt.getNodeValue();
            }
            if (strAttr.equals("pickListValAttr")) {
                pickListAttr = currentAtt.getNodeValue();
            }

            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
            if (strAttr.equals("pickListViewName")) {
                pickListVO = currentAtt.getNodeValue();
            }
        }
        if (bindingAttr != null) {
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
            retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
            retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
            VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, pickListVO, pickListAttr, "choice");
        } else {
            retElement.setAttribute("value", "#{bindings." + pickListVO + ".inputValue}");
            retElement.setAttribute("label", "#{bindings." + pickListVO + ".label}");
            retElement.setAttribute("required", "#{bindings." + pickListVO + ".hints.mandatory}");
            retElement.setAttribute("shortDesc", "#{bindings." + pickListVO + ".hints.tooltip}");
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener");
        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "list", pickListAttr, pickListVO,
                                     null, packagePath);
        }
        return retElement;
    }

    protected static Element convertLink(Node currentNode, Document jsfDoc, String beanPath,
                                         boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichLink";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichLink;";

        Element retElement = jsfDoc.createElement("af:link");
        NamedNodeMap attrs = currentNode.getAttributes();
        //                String bindingVO = null;
        //                String bindingAttr = null;
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();

            }
            if (strAttr.equals("prompt")) {
                retElement.setAttribute("text", currentAtt.getNodeValue());
            }
            if (strAttr.equals("destination")) {
                retElement.setAttribute("destination", currentAtt.getNodeValue());
            }
            //                    if (strAttr.equals("viewAttr")) {
            //                        bindingAttr = currentAtt.getNodeValue();
            //                    }
            //                    if (strAttr.equals("viewName")) {
            //                        bindingVO = currentAtt.getNodeValue();
            //                    }
        }
        //                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null,
        //                                         null);
        //                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
        //BeanGen.createGetterSetter(itemName, itemType, path, imports);
        System.out.println("End Conv: convert");
        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        return retElement;
    }

    protected static Element convertAdvancedTable(Node currentNode, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, String src,
                                                  Map filePaths) throws Exception {
        String itemName = "";
        String itemType = "RichTable";
        String imports = "import oracle.adf.view.rich.component.rich.data.RichTable;";

        String bindingVO = null;
        Element retElement = jsfDoc.createElement("af:table");
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("prompt")) {
                retElement.setAttribute("text", currentAtt.getNodeValue());
            }
            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
        }

        retElement.setAttribute("value", "#{bindings." + bindingVO + ".collectionModel}");
        retElement.setAttribute("var", "rows");
        retElement.setAttribute("rows", "#{bindings." + bindingVO + ".rangeSize}");
        retElement.setAttribute("emptyText",
                                "#{bindings." + bindingVO + ".viewable ? 'No data to display.' : 'Access Denied.'}");
        retElement.setAttribute("rowBandingInterval", "0");
        retElement.setAttribute("selectedRowKeys", "#{bindings." + bindingVO + ".collectionModel.selectedRow}");
        retElement.setAttribute("selectionListener", "#{bindings." + bindingVO + ".collectionModel.makeCurrent}");
        retElement.setAttribute("rowSelection", "single");
        retElement.setAttribute("fetchSize", "#{bindings." + bindingVO + ".rangeSize}");
        retElement.setAttribute("filterModel", "#{bindings." + bindingVO + ".queryDescriptor}");
        retElement.setAttribute("queryListener", "#{bindings." + bindingVO + ".processQuery}");
        retElement.setAttribute("filterVisible", "true");
        retElement.setAttribute("varStatus", "vs");

        NodeList nodeList = currentNode.getChildNodes();
        List<Node> nodes = getNode(nodeList, "oa:column");

        recursiveColumnElements(nodes, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src, retElement,
                                filePaths);
        NodeList treeNodeList = retElement.getChildNodes();
        List<Node> treeNodes = getNode(treeNodeList, "af:column");
        List<String> attrList = getTreeItems(treeNodes);
        System.out.println("filepaths::::" + (String) filePaths.get(bindingVO));
        PageDefXml.handlePageDef(pgName, bindingVO, attrList, Dest, app, amDef, "tree", null, null, (String)filePaths.get(bindingVO), packagePath);
        BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        return retElement;
    }

    protected static List<String> getTreeItems(List<Node> treeNodes) {
        List<String> retItemList = new ArrayList<String>();
        List<Node> tempNodeList = new ArrayList<Node>();
        for (Node node : treeNodes) {
            NodeList childNodeList = node.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node tempNode = childNodeList.item(i);
                if (!tempNode.getNodeName().equals("af:column")) {
                    NamedNodeMap tempNamedNodeMap = tempNode.getAttributes();
                    for (int j = 0; j < tempNamedNodeMap.getLength(); j++) {
                        Node tempAttrNode = tempNamedNodeMap.item(j);
                        if (tempAttrNode.getNodeName().equals("value")) {
                            String nodeValue = tempAttrNode.getNodeValue();
                            nodeValue = nodeValue.substring(nodeValue.indexOf("{row.") + 5, nodeValue.indexOf("}"));
                            if (!retItemList.contains(nodeValue)) {
                                retItemList.add(nodeValue);
                            }
                            break;
                        } else if (tempAttrNode.getNodeName().equals("facetName")) {
                            String nodeValue = tempAttrNode.getNodeValue();
                            nodeValue = nodeValue.substring(nodeValue.indexOf("{row.") + 5, nodeValue.indexOf("}"));
                            if (!retItemList.contains(nodeValue)) {
                                retItemList.add(nodeValue);
                            }
                            tempNodeList.add(tempNode);
                        }
                    }
                    if (tempNode.getNodeName().equals("f:facet")) {
                        tempNodeList.add(tempNode);
                    }
                } else {
                    tempNodeList.add(tempNode);
                }
            }
        }
        if (tempNodeList.size() > 0) {
            retItemList.addAll(getTreeItems(tempNodeList));
        }
        return retItemList;
    }

    protected static Element convertImage(Node currentNode, Document jsfDoc, String path,
                                          boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichImage";
        String imports = "import oracle.adf.view.rich.component.rich.output.RichImage;";

        Element retElement = jsfDoc.createElement("af:image");
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("source")) {
                retElement.setAttribute("source", currentAtt.getNodeValue());
            }
            if (strAttr.equals("shortDesc")) {
                retElement.setAttribute("shortDesc", currentAtt.getNodeValue());
            }
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, path, imports);
        }
        return retElement;
    }

    protected static Element convertMessageRadioButton(Node currentNode, Document jsfDoc, String Dest, String app,
                                                       String pgName, String amDef, String beanPath, Map filePaths,
                                                       boolean isTableComp) throws Exception {

        String itemName = "";
        String itemType = "RichSelectOneRadio";
        String imports = "import oracle.adf.view.rich.component.rich.input.RichSelectOneRadio;";
        String bindingVO = null;
        String bindingAttr = null;
        String pickListAttr = null;
        String pickListVO = null;
        String radioName = null;
        String selectItemPrompt = null;
        String checkedValue = null;
        String uncheckedValue = null;
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            System.out.println("Node Name:::" + strAttr);
            System.out.println("Node Value:::" + currentAtt.getNodeValue());
            if (strAttr.equals("id")) {
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("name")) {
                radioName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("prompt")) {
                selectItemPrompt = currentAtt.getNodeValue();
            }
            if (strAttr.equals("checkedValue")) {
                checkedValue = currentAtt.getNodeValue();
            }
            if (strAttr.equals("uncheckedValue")) {
                uncheckedValue = currentAtt.getNodeValue();
            }

            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
            if (strAttr.equals("pickListViewName")) {
                pickListVO = currentAtt.getNodeValue();
            }
        }
        Element retElement = null;
        if (customRadioButtonDetails.get(radioName) == null || radioName == null) {
            retElement = jsfDoc.createElement("af:selectOneRadio");
            if (radioName != null) {
                retElement.setAttribute("id", radioName);
                customRadioButtonDetails.put(radioName, retElement);
            } else {
                retElement.setAttribute("id", itemName);
                customRadioButtonDetails.put(itemName, retElement);
            }
        } else {
            retElement = customRadioButtonDetails.get(radioName);
        }
        if (checkedValue != null || uncheckedValue != null) {
            Element selectItemElement = jsfDoc.createElement("af:selectItem");
            retElement.appendChild(selectItemElement);

            selectItemElement.setAttribute("id", itemName);
            selectItemElement.setAttribute("label", selectItemPrompt);
            selectItemElement.setAttribute("value", checkedValue);
        }
        //        if (bindingAttr != null) {
        //            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
        //            retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
        //            retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
        //            retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
        //            VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, pickListVO, pickListAttr, "choice");
        //        }
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener");
        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
            //            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "list", pickListAttr, pickListVO,
            //                                     null);
        }
        return retElement;
    }

    protected static Element convertTable(Node currentNode, Document jsfDoc) {
        NamedNodeMap attrs = currentNode.getAttributes();
        String id = null;
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                id = currentAtt.getNodeValue();
            }
        }
        Element retElement = jsfDoc.createElement("af:panelGridLayout");
        retElement.setAttribute("id", id);
        return retElement;
    }

    protected static Element convertRowLayout(Node currentNode, Document jsfDoc) {
        NamedNodeMap attrs = currentNode.getAttributes();
        String id = null;
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                id = currentAtt.getNodeValue();
            }
        }
        Element retElement = jsfDoc.createElement("af:gridRow");
        retElement.setAttribute("id", id);
        retElement.setAttribute("marginTop", "5px");
        retElement.setAttribute("height", "auto");
        retElement.setAttribute("marginBottom", "5px");
        return retElement;
    }

    protected static Element convertCellLayout(Node currentNode, Document jsfDoc) {
        NamedNodeMap attrs = currentNode.getAttributes();
        String id = null;
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                id = currentAtt.getNodeValue();
            }
        }
        Element retElement = jsfDoc.createElement("af:gridCell");
        retElement.setAttribute("id", id);
        retElement.setAttribute("marginStart", "5px");
        retElement.setAttribute("width", "50%");
        retElement.setAttribute("marginEnd", "5px");
        return retElement;
    }

    protected static Element convertSwitcher(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                             String amDef, String beanPath, boolean isTableComp, String src,
                                             Map filePaths) throws Exception {
        String itemName = "";
        String itemType = "UIXSwitcher";
        String imports = "import org.apache.myfaces.trinidad.component.UIXSwitcher;";
        String bindingAttr = null;
        String bindingVO = null;
        Element retElement = jsfDoc.createElement("af:switcher");
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                itemName = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewAttr")) {
                bindingAttr = currentAtt.getNodeValue();
            }
            if (strAttr.equals("viewName")) {
                bindingVO = currentAtt.getNodeValue();
            }
        }
        if (isTableComp) {
            retElement.setAttribute("facetName", "#{row." + bindingAttr + "}");
        } else {
            retElement.setAttribute("facetName", "#{binding." + bindingAttr + ".inputValue}");
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }

        NodeList nodeList = currentNode.getChildNodes();
        List<Node> nodes = getNode(nodeList, "ui:case");
        String nameAttr = null;
        for (Node node : nodes) {
            if (node.getNodeName().equals("ui:case")) {
                NamedNodeMap namedNodeMap = node.getAttributes();
                for (int i = 0; i < namedNodeMap.getLength(); i++) {
                    Node caseNodeAttr = namedNodeMap.item(i);
                    if (caseNodeAttr.getNodeName().equals("name")) {
                        nameAttr = caseNodeAttr.getNodeValue();
                    }
                }

                Element columnElement = convertCaseElement(jsfDoc, nameAttr);
                Element convertedElement =
                    genericSwitcherCaseMethod(node, jsfDoc, Dest, app, pgName, amDef, src, beanPath, filePaths,
                                              isTableComp);
                if (convertedElement != null)
                    columnElement.appendChild(convertedElement);
                retElement.appendChild(columnElement);
            }
        }
        return retElement;
    }

    protected static Element convertCaseElement(Document jsfDoc, String text) {
        Element retElement = jsfDoc.createElement("f:facet");
        retElement.setAttribute("name", text);
        return retElement;
    }


    protected static Element genericSwitcherCaseMethod(Node node, Document jsfDoc, String Dest, String app,
                                                       String pgName, String amDef, String src, String beanPath,
                                                       Map filePaths, boolean isTableComp) throws Exception {
        Element retElement = null;
        NodeList nodeList1 = node.getChildNodes();
        for (int i = 0; i < nodeList1.getLength(); i++) {
            Node caseNode = nodeList1.item(i);
            if (!caseNode.getNodeName().equals("#text")) {
                retElement = convert(caseNode, jsfDoc, Dest, app, pgName, amDef, src, filePaths, isTableComp, beanPath);
            }
        }
        return retElement;
    }

    protected static void convertVCL(Node currentNode, String beanPath, Element retElement, String pgName,
                                     String listenerName) throws Exception {
        NodeList childNodes = currentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("ui:primaryClientAction")) {
                NodeList subChildNodes = childNode.getChildNodes();
                for (int j = 0; j < subChildNodes.getLength(); j++) {
                    Node subChildNode = subChildNodes.item(j);
                    if (subChildNode.getNodeName().equals("ui:fireAction")) {
                        NamedNodeMap attr = subChildNode.getAttributes();
                        for (int k = 0; k < attr.getLength(); k++) {
                            Node attrNode = attr.item(k);
                            if (attrNode.getNodeName().equals("event")) {
                                String methodName = attrNode.getNodeValue();
                                if (listenerName.equals("valueChangeListener")) {
                                    retElement.setAttribute("valueChangeListener",
                                                            "#{backingBeanScope." + pgName + "Bean." + methodName +
                                                            "}");
                                    BeanGen.createValueChangeListener(methodName, beanPath);
                                } else if (listenerName.equals("actionListener")) {
                                    retElement.setAttribute("actionListener",
                                                            "#{backingBeanScope." + pgName + "Bean." + methodName +
                                                            "}");
                                    retElement.setAttribute("text", methodName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected static Element convertCheckBox(Node currentNode, Document jsfDoc, String beanPath,
                                             Boolean isTableComp) throws Exception {
        String itemName = "";
        String itemType = "RichSelectManyCheckbox";
        String imports = "import oracle.adf.view.rich.component.rich.output.RichSelectManyCheckbox;";

        Element retElement = jsfDoc.createElement("af:selectManyCheckbox");
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                itemName = currentAtt.getNodeValue();
                retElement.setAttribute("id", itemName);
            }
            if (strAttr.equals("prompt")) {
                retElement.setAttribute("label", currentAtt.getNodeValue());
            }
            if (strAttr.equals("checkedValue")) {
                Element retChildElement = jsfDoc.createElement("af:selectItem");
                retChildElement.setAttribute("id", currentAtt.getNodeValue());
                retChildElement.setAttribute("label", currentAtt.getNodeValue());
                retChildElement.setAttribute("value", currentAtt.getNodeValue());
                retElement.appendChild(retChildElement);
            }
            if (strAttr.equals("uncheckedValue")) {
                Element retChildElement = jsfDoc.createElement("af:selectItem");
                retChildElement.setAttribute("id", currentAtt.getNodeValue());
                retChildElement.setAttribute("label", currentAtt.getNodeValue());
                retChildElement.setAttribute("value", currentAtt.getNodeValue());
                retElement.appendChild(retChildElement);
            }
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        }
        return retElement;
    }

    protected static Element convertSpacer(Node currentNode, Document jsfDoc) {
        Element retElement = jsfDoc.createElement("af:spacer");
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
            }
            if (strAttr.equals("width")) {
                retElement.setAttribute("width", currentAtt.getNodeValue());
            }
            if (strAttr.equals("height")) {
                retElement.setAttribute("height", currentAtt.getNodeValue());
            }
        }
        return retElement;
    }
}
