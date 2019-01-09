package conv;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class JSFGen {
    private static Map<String, Element> customRadioButtonDetails = new HashMap<String, Element>();
    private static String packagePath = null;


    public JSFGen() {
        super();
    }

    /**
     *
     * @param path
     * @param app
     * @param Dest
     * @param repo
     * @param src
     * @param filePaths
     * @throws Exception
     */
    protected static void handlePage(String path, String app, String Dest, String repo, String src,
                                     Map filePaths) throws Exception {
        System.out.println("Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo + " " + src + " " +
                           filePaths);
        ErrorAndLog.handleLog(app,
                              "Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo + " " + src + " " +
                              filePaths);
        String pgName = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        pgName = pgName.replace(".xml", "");
        String destination = FileReaderWritter.getViewDestinationPath(path, app, Dest, src);
        String pagePath = destination.replace(".xml", ".jsf");
        String tempPath = path.replace(src + FileReaderWritter.getSeparator(), "");
        packagePath = tempPath.substring(0, tempPath.lastIndexOf(FileReaderWritter.getSeparator()));

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
        String coName = src + FileReaderWritter.getSeparator();
        ;
        for (int j = 0; j < attrs.getLength(); j++) {
            Node currentAtt = attrs.item(j);
            String strAttr = currentAtt.getNodeName();
            if (strAttr.equals("title")) {
                title = currentAtt.getNodeValue();
            }
            if (strAttr.equals("windowTitle")) {
                windowTitle = currentAtt.getNodeValue();
            }
            if (strAttr.equals("amDefName")) {
                amDef = currentAtt.getNodeValue();
            }
            if (strAttr.equals("controllerClass")) {
                jsfBeanName = currentAtt.getNodeValue();
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                coName += temp + ".java";
            }
        }
        String beanPath =
            destination.substring(0, destination.indexOf("ViewController") + 14) + FileReaderWritter.getSeparator() +
            "src" + FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
            jsfBeanName.replace(".", FileReaderWritter.getSeparator()) + ".java";
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document jsfDoc = newDBuilder.newDocument();

        Element headerNode = createJSFF(pgName, jsfDoc, app);
        BeanGen.createBean(pgName, beanPath, app);

        if (nodes.getLength() > 0) {
            recursiveNodes(nodes, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths, headerNode, beanPath);
        }

        FileReaderWritter.writeXMLFile(jsfDoc, pagePath, app);
        String beanCompletePath = "view." + jsfBeanName;
        BeanGen.createAdfConfig(destination.substring(0, destination.indexOf("ViewController") + 14), pgName,
                                beanCompletePath, app);
        BeanGen.copyProcessFormRequest(path, app, beanPath, coName);

        System.out.println("End Conv: handlePage");
    }


    /**
     *
     * @param path
     * @param app
     * @param Dest
     * @param repo
     * @param src
     * @param filePaths
     * @throws Exception
     */
    protected static void handleRegion(String path, String app, String Dest, String repo, String src,
                                       Map filePaths) throws Exception {
        System.out.println("Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo + " " + src + " " +
                           filePaths);
        ErrorAndLog.handleLog(app,
                              "Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo + " " + src + " " +
                              filePaths);
        String pgName = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        pgName = pgName.replace(".xml", "");
        String destination = FileReaderWritter.getViewDestinationPath(path, app, Dest, src);
        String pagePath = destination.replace(".xml", ".jsf");
        String tempPath = path.replace(src + FileReaderWritter.getSeparator(), "");
        packagePath = tempPath.substring(0, tempPath.lastIndexOf(FileReaderWritter.getSeparator()));

        File oaf = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oaf);
        NodeList uiNode = oafDoc.getElementsByTagName("ui:contents");
        Element ui = (Element) uiNode.item(0);
        NodeList nodes = ui.getChildNodes();

        // get pageTitle and WindowTitle
        String amDef = "";
        String jsfBeanName = "";
        String coName = src + FileReaderWritter.getSeparator();

        NodeList pageLayoutList = oafDoc.getChildNodes();
        for (int k = 0; k < pageLayoutList.getLength() && pageLayoutList.equals(Node.ELEMENT_NODE); k++) {
            Element pageLayout = (Element) pageLayoutList.item(0);
            NamedNodeMap attrs = pageLayout.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Node currentAtt = attrs.item(j);
                String strAttr = currentAtt.getNodeName();
                if (strAttr.equals("amDefName")) {
                    amDef = currentAtt.getNodeValue();
                }
                if (strAttr.equals("controllerClass")) {
                    jsfBeanName = currentAtt.getNodeValue();
                    String temp = currentAtt.getNodeValue();
                    temp = temp.replace(".", FileReaderWritter.getSeparator());
                    coName += temp + ".java";
                }
            }
        }
        String beanPath =
            destination.substring(0, destination.indexOf("ViewController") + 14) + FileReaderWritter.getSeparator() +
            "src" + FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
            jsfBeanName.replace(".", FileReaderWritter.getSeparator()) + ".java";
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document jsfDoc = newDBuilder.newDocument();

        Element headerNode = createJSFF(pgName, jsfDoc, app);
        BeanGen.createBean(pgName, beanPath, app);

        if (nodes.getLength() > 0) {
            recursiveNodes(nodes, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths, headerNode, beanPath);
        }

        FileReaderWritter.writeXMLFile(jsfDoc, pagePath, app);
        String beanCompletePath = "view." + jsfBeanName;
        BeanGen.createAdfConfig(destination.substring(0, destination.indexOf("ViewController") + 14), pgName,
                                beanCompletePath, app);
        if(!"".equals(jsfBeanName))
            BeanGen.copyProcessFormRequest(path, app, beanPath, coName);

        System.out.println("End Conv: handlePage");
    }

    /**
     *
     * @param nodeList
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param jsfBeanName
     * @param src
     * @param filePaths
     * @param form
     * @param beanPath
     * @throws Exception
     */
    public static void recursiveNodes(NodeList nodeList, Document jsfDoc, String Dest, String app, String pgName,
                                      String amDef, String jsfBeanName, String src, Map filePaths, Element form,
                                      String beanPath) throws Exception {
        //        System.out.println("Start Conv: recursiveNodes " + Dest + " " + app + " " + pgName + " " + amDef + " " +



        //                           jsfBeanName + " " + src + " " + beanPath);
        //        ErrorAndLog.handleLog(app,
        //                              "Start Conv: recursiveNodes " + Dest + " " + app + " " + pgName + " " + amDef + " " +
        //                              jsfBeanName + " " + src + " " + beanPath);
        Element retElement = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
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
                    form.appendChild(retElement);
                }
            }
        }
        //        System.out.println("End Conv: recursiveNodes ");
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param src
     * @param filePaths
     * @param isTableComp
     * @param beanPath
     * @return
     * @throws Exception
     */
    private static Element convert(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                   String amDef, String src, Map filePaths, boolean isTableComp,
                                   String beanPath) throws Exception {
        System.out.println("Start Conv: convert " + currentNode.toString() + " " + Dest + " " + app + " " + pgName +
                           " " + amDef + " " + src + " " + isTableComp + " " + beanPath);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convert " + currentNode.toString() + " " + Dest + " " + app + " " + pgName +
                              " " + amDef + " " + src + " " + isTableComp + " " + beanPath);
        Element retElement = null;
        String strElement = currentNode.getNodeName(); // oaf
        String path = beanPath;
        if (strElement.equals("oa:messageStyledText")) {
            retElement = convertMessageStyledText(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp);
        } else if (strElement.equals("oa:messageTextInput")) {
            retElement = convertMessageTextInput(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp);
        } else if (strElement.equals("oa:button")) { // when none match
            retElement = convertButton(currentNode, jsfDoc, pgName, path, isTableComp, app);
        } else if (strElement.equals("oa:submitButton")) { // when none match
            retElement = convertSubmitButton(currentNode, jsfDoc, pgName, path, isTableComp, app);
        }/* else if (strElement.equals("oa:messageLovInput")) {
            retElement =
                convertMessageLovInput(currentNode, jsfDoc, Dest, app, src, pgName, amDef, path, filePaths,
                                       isTableComp);
        } else if (strElement.equals("oa:messageChoice")) {
            retElement =
                convertMessageChoice(currentNode, jsfDoc, Dest, app, pgName, amDef, path, filePaths, isTableComp);
        }*/ else if (strElement.equals("oa:link")) {
            retElement = convertLink(currentNode, jsfDoc, path, isTableComp, app);
        } else if (strElement.equals("oa:advancedTable")) {
            retElement = convertAdvancedTable(currentNode, jsfDoc, Dest, app, pgName, amDef, path, src, filePaths);
        } else if (strElement.equals("oa:image")) {
            retElement = convertImage(currentNode, jsfDoc, path, isTableComp, app);
        } else if (strElement.equals("oa:messageRadioButton")) {
            retElement = convertMessageRadioButton(currentNode, jsfDoc, app, pgName, path, isTableComp);
        } else if (strElement.equals("oa:tableLayout")) {
            retElement = convertTable(currentNode, jsfDoc, app);
        } else if (strElement.equals("oa:rowLayout")) {
            retElement = convertRowLayout(currentNode, jsfDoc, app);
        } else if (strElement.equals("oa:cellFormat")) {
            retElement = convertCellLayout(currentNode, jsfDoc, app);
        } else if (strElement.equals("oa:messageCheckBox")) {
            retElement = convertCheckBox(currentNode, jsfDoc, path, isTableComp, app);
        } else if (strElement.equals("oa:switcher")) {
            retElement =
                convertSwitcher(currentNode, jsfDoc, Dest, app, pgName, amDef, path, isTableComp, src, filePaths);
        } else if (strElement.equals("oa:messageRadioButton")) {
            retElement = convertMessageRadioButton(currentNode, jsfDoc, app, pgName, path, isTableComp);
        } else if (strElement.equals("oa:spacer")) {
            retElement = convertSpacer(currentNode, jsfDoc, app);
        }
        System.out.println("End Conv: convert ");
        return retElement;
    }

    /**
     *
     * @param app
     * @param destination
     * @throws Exception
     */
    protected static void createJSF(String app, String destination) throws Exception {
        System.out.println("Start Conv: createJSF " + app + " " + destination);
        ErrorAndLog.handleLog(app, "Start Conv: createJSF " + app + " " + destination);

        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document jsfDoc = newDBuilder.newDocument();

        Element viewElement = jsfDoc.createElement("f:view");
        jsfDoc.appendChild(viewElement);

        viewElement.setAttribute("xmlns:f", "http://java.sun.com/jsf/core");
        viewElement.setAttribute("xmlns:af", "http://xmlns.oracle.com/adf/faces/rich");

        Element docElement = jsfDoc.createElement("af:document");
        viewElement.appendChild(docElement);

        docElement.setAttribute("id", "d1");
        docElement.setAttribute("title", app);

        Element formElement = jsfDoc.createElement("af:form");
        docElement.appendChild(formElement);

        formElement.setAttribute("id", "f1");

        Element panelHeaderElement = jsfDoc.createElement("af:panelHeader");
        formElement.appendChild(panelHeaderElement);

        panelHeaderElement.setAttribute("id", "ph1");
        panelHeaderElement.setAttribute("text", app);

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

        Element facetElement5 = jsfDoc.createElement("f:facet");
        panelHeaderElement.appendChild(facetElement5);

        facetElement5.setAttribute("name", "info");

        FileReaderWritter.writeXMLFile(jsfDoc, destination , app);

        System.out.println("End Conv: createJSF ");
    }

    /**
     *
     * @param nodeList
     * @param nodeName
     * @return
     */
    public static List<Node> getNode(NodeList nodeList, String nodeName, String app) {
        List<Node> retNodeList = new ArrayList<Node>();
        Node node = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeName().equals(nodeName) ||
                (nodeName.equals("oa:column") && node.getNodeName().equals("oa:columnGroup"))) {
                retNodeList.add(node);
            } else {
                retNodeList.addAll(getNode(node.getChildNodes(), nodeName, app));
            }
        }
        return retNodeList;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertMessageStyledText(Node currentNode, Document jsfDoc, String Dest, String app,
                                                      String pgName, String amDef, String beanPath,
                                                      boolean isTableComp) throws Exception {
        System.out.println("Start Conv: convertMessageStyledText " + Dest + " " + app + " " + pgName + " " + amDef +
                           " " + beanPath + " " + isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageStyledText " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + beanPath + " " + isTableComp);
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
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "tableAttr", null, null, null,
                                     packagePath);
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
        } else {
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null, null,
                                     null, packagePath);
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        System.out.println("End Conv: convertMessageStyledText ");
        return retElement;
    }

    /**
     *
     * @param jsfDoc
     * @param bindingVO
     * @param bindingAttr
     * @param columnId
     * @param headerText
     * @return
     */
    protected static Element convertColumnElement(Document jsfDoc, String bindingVO, String bindingAttr,
                                                  String columnId, String headerText, String app) {
        System.out.println("Start Conv: convertColumnElement " + bindingVO + " " + bindingAttr + " " + columnId + " " +
                           headerText + " " + app);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertColumnElement " + bindingVO + " " + bindingAttr + " " + columnId +
                              " " + headerText + " " + app);
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
        System.out.println("End Conv: convertColumnElement ");
        return retElement;
    }

    /**
     *
     * @param node
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param bindingVO
     * @param src
     * @param retElement
     * @param filePaths
     * @return
     * @throws Exception
     */
    protected static Element genericColumnMethod(Node node, Document jsfDoc, String Dest, String app, String pgName,
                                                 String amDef, String beanPath, String bindingVO, String src,
                                                 Element retElement, Map filePaths) throws Exception {
        System.out.println("Start Conv: genericColumnMethod " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                           beanPath + " " + bindingVO + " " + src);
        ErrorAndLog.handleLog(app,
                              "Start Conv: genericColumnMethod " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                              beanPath + " " + bindingVO + " " + src);
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
                                ; // nothing to do
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


                    } else if (compNodeName.equals("oa:switcher")) {
                        NamedNodeMap compAttr = compNode.getAttributes();
                        for (int k = 0; k < compAttr.getLength(); k++) {
                            Node attrNode = compAttr.item(k);
                            String attrNodeName = attrNode.getNodeName();
                            if (attrNodeName.equals("viewAttr")) {
                                bindingAttr = attrNode.getNodeValue();
                            }
                        }
                    }
                    Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null, app);
                    Element childElement =
                        convert(compNode, jsfDoc, Dest, app, pgName, amDef, src, filePaths, true, beanPath);
                    if (childElement != null) {
                        columnElement.appendChild(childElement);
                        retElement.appendChild(columnElement);
                    }
                }
            }
        }
        System.out.println("End Conv: genericColumnMethod ");
        return retElement;
    }

    /**
     *
     * @param nodes
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param bindingVO
     * @param src
     * @param retElement
     * @param filePaths
     * @throws Exception
     */
    protected static void recursiveColumnElements(List<Node> nodes, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, String bindingVO,
                                                  String src, Element retElement, Map filePaths) throws Exception {
        System.out.println("Start Conv: recursiveColumnElements " + Dest + " " + app + " " + pgName + " " + amDef +
                           " " + beanPath + " " + bindingVO + " " + src);
        ErrorAndLog.handleLog(app,
                              "Start Conv: recursiveColumnElements " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + beanPath + " " + bindingVO + " " + src);
        Element toRetElement = null;
        String columnId = null;
        String headerText = null;
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
                toRetElement = convertColumnElement(jsfDoc, null, null, columnId, headerText, app);
                List<Node> groupNodes = getNode(node.getChildNodes(), "oa:column", app);
                recursiveColumnElements(groupNodes, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src,
                                        toRetElement, filePaths);
                retElement.appendChild(toRetElement);
            } else if (node.getNodeName().equals("oa:column")) {
                genericColumnMethod(node, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src, retElement,
                                    filePaths);
            }
        }
        System.out.println("End Conv: recursiveColumnElements ");
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertMessageTextInput(Node currentNode, Document jsfDoc, String Dest, String app,
                                                     String pgName, String amDef, String beanPath,
                                                     boolean isTableComp) throws Exception {
        System.out.println("Start Conv: convertMessageTextInput " + Dest + " " + app + " " + pgName + " " + amDef +
                           " " + beanPath + " " + isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageTextInput " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + beanPath + " " + isTableComp);
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
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener", app);
        System.out.println("End Conv: convertMessageTextInput ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param pgName
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertButton(Node currentNode, Document jsfDoc, String pgName, String beanPath,
                                           boolean isTableComp, String app) throws Exception {
        System.out.println("Start Conv: convertMessageTextInput " + pgName + beanPath + " " + isTableComp + " " + app);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageTextInput " + pgName + beanPath + " " + isTableComp + " " +
                              app);
        String itemName = "";
        String itemType = "RichButton";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichButton; ";

        Element retElement = jsfDoc.createElement("af:button");

        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "actionListener", app);

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        System.out.println("End Conv: convertMessageTextInput ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param pgName
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertSubmitButton(Node currentNode, Document jsfDoc, String pgName, String beanPath,
                                                 boolean isTableComp, String app) throws Exception {
        System.out.println("Start Conv: convertSubmitButton " + pgName + beanPath + " " + isTableComp + " " + app);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertSubmitButton " + pgName + beanPath + " " + isTableComp + " " + app);
        String itemName = "";
        String itemType = "RichCommandButton";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichCommandButton;";


        Element retElement = jsfDoc.createElement("af:commandButton");

        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();

            if (strAttr.equals("id")) {
                retElement.setAttribute("id", currentAtt.getNodeValue());
                itemName = currentAtt.getNodeValue();
            }

            if (strAttr.equals("text")) {
                retElement.setAttribute("text", currentAtt.getNodeValue());
            }
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "actionListener", app);
        System.out.println("End Conv: convertSubmitButton ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param src
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param filePaths
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertMessageLovInput(Node currentNode, Document jsfDoc, String Dest, String app,
                                                    String src, String pgName, String amDef, String beanPath,
                                                    Map filePaths, boolean isTableComp) throws Exception {
        System.out.println("Start Conv: convertMessageLovInput " + Dest + " " + app + " " + src + " " + pgName + " " +
                           amDef + " " + beanPath + " " + isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageLovInput " + Dest + " " + app + " " + src + " " + pgName +
                              " " + amDef + " " + beanPath + " " + isTableComp);
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
                File regionFile = new File(regionName);

                if (regionFile.exists()) {
                    boolean isLovExists = false;
                    String tempId = null;
                    List<Node> pageNodes = getNode(currentNode.getChildNodes(), "lovMap", app);
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
                                }
                            }

                        }
                        if (isLovExists) {
                            break;
                        }
                    }
                    DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
                    newDbFactory.setValidating(false);
                    DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
                    Document regionDoc = newDBuilder.parse(regionFile);
                    Element regionRootElement = regionDoc.getDocumentElement();
                    NodeList nodeList = regionRootElement.getChildNodes();
                    List<Node> nodes = getNode(nodeList, "oa:messageStyledText", app);
                    for (Node node : nodes) {
                        String tempLovAttr = ((Element) node).getAttribute("viewAttr");
                        if (tempLovAttr.equals(tempId)) {
                            lovView = ((Element) node).getAttribute("viewName");
                            lovAttr = ((Element) node).getAttribute("viewAttr");
                        }

                    }
                }
                if (null != bindingVO && null != bindingAttr && null != lovView && null != lovAttr)
                    VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, lovView, lovAttr, "lovInput",
                                        app);
            }
        }

        if (bindingVO != null && bindingAttr != null && !isExternalLov) {
            NodeList nodeList = currentNode.getChildNodes();
            String tempId = null;
            List<Node> pageNodes = getNode(nodeList, "lovMap", app);
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
                        }
                    }

                }
                if (isLovExists) {
                    break;
                }
            }

            List<Node> nodes = getNode(nodeList, "oa:messageStyledText", app);
            for (Node node : nodes) {
                String tempLovAttr = ((Element) node).getAttribute("id");
                if (tempLovAttr.equals(tempId)) {
                    lovView = ((Element) node).getAttribute("viewName");
                    lovAttr = ((Element) node).getAttribute("viewAttr");
                }
            }
            VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, (String) filePaths.get(lovView),
                                lovAttr, "lovInput", app);
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "listOfValues", null, null, null,
                                     packagePath);
            retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
            retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
            retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
            retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
            retElement.setAttribute("model", "#{bindings." + bindingAttr + ".listOfValuesModel}");
            retElement.setAttribute("columns", "#{bindings." + bindingAttr + ".hints.displayWidth}");
            retElement.setAttribute("popupTitle", "Search and Select: #{bindings." + bindingAttr + ".hints.label}");
        } else {
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
        }

        System.out.println("End Conv: convertMessageLovInput ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param filePaths
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertMessageChoice(Node currentNode, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, Map filePaths,
                                                  boolean isTableComp) throws Exception {
        System.out.println("Start Conv: convertMessageChoice " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                           beanPath + " " + isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageChoice " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + beanPath + " " + isTableComp);
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
            VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingAttr, pickListVO, pickListAttr, "choice",
                                app);
        } else {
            retElement.setAttribute("value", "#{bindings." + pickListVO + ".inputValue}");
            retElement.setAttribute("label", "#{bindings." + pickListVO + ".label}");
            retElement.setAttribute("required", "#{bindings." + pickListVO + ".hints.mandatory}");
            retElement.setAttribute("shortDesc", "#{bindings." + pickListVO + ".hints.tooltip}");
        }
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener", app);
        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "list", pickListAttr, pickListVO,
                                     null, packagePath);
        }
        System.out.println("End Conv: convertMessageChoice ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertLink(Node currentNode, Document jsfDoc, String beanPath, boolean isTableComp,
                                         String app) throws Exception {
        System.out.println("Start Conv: convertLink " + beanPath + " " + isTableComp + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertLink " + beanPath + " " + isTableComp + " " + app);

        String itemName = "";
        String itemType = "RichLink";
        String imports = "import oracle.adf.view.rich.component.rich.nav.RichLink;";

        Element retElement = jsfDoc.createElement("af:link");
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
            if (strAttr.equals("destination")) {
                retElement.setAttribute("destination", currentAtt.getNodeValue());
            }
        }

        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        System.out.println("End Conv: convertLink ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param src
     * @param filePaths
     * @return
     * @throws Exception
     */
    protected static Element convertAdvancedTable(Node currentNode, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, String src,
                                                  Map filePaths) throws Exception {
        System.out.println("Start Conv: convertAdvancedTable " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                           beanPath + " " + src);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertAdvancedTable " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + beanPath + " " + src);
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
        List<Node> nodes = getNode(nodeList, "oa:column", app);

        recursiveColumnElements(nodes, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, src, retElement,
                                filePaths);
        NodeList treeNodeList = retElement.getChildNodes();
        List<Node> treeNodes = getNode(treeNodeList, "af:column", app);
        List<String> attrList = getTreeItems(treeNodes, app);

        PageDefXml.handlePageDef(pgName, bindingVO, attrList, Dest, app, amDef, "tree", null, null,
                                 (String) filePaths.get(bindingVO), packagePath);
        BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);

        System.out.println("End Conv: convertAdvancedTable ");
        return retElement;
    }

    /**
     *
     * @param treeNodes
     * @return
     */
    protected static List<String> getTreeItems(List<Node> treeNodes, String app) {
        System.out.println("Start Conv: getTreeItems ");
        ErrorAndLog.handleLog(app, "Start Conv: getTreeItems ");
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
            retItemList.addAll(getTreeItems(tempNodeList, app));
        }
        System.out.println("End Conv: getTreeItems ");
        return retItemList;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param path
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertImage(Node currentNode, Document jsfDoc, String path, boolean isTableComp,
                                          String app) throws Exception {
        System.out.println("Start Conv: convertImage " + path + " " + isTableComp + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertImage " + path + " " + isTableComp + " " + app);
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
            BeanGen.createGetterSetter(itemName, itemType, path, imports, app);
        }
        System.out.println("End Conv: convertImage ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param filePaths
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertMessageRadioButton(Node currentNode, Document jsfDoc, String app, String pgName,
                                                       String beanPath, boolean isTableComp) throws Exception {
        System.out.println("Start Conv: convertMessageRadioButton " + app + " " + pgName + " " + beanPath + " " +
                           isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertMessageRadioButton " + app + " " + pgName + " " + beanPath + " " +
                              isTableComp);
        String itemName = "";
        String itemType = "RichSelectOneRadio";
        String imports = "import oracle.adf.view.rich.component.rich.input.RichSelectOneRadio;";
        String bindingVO = null;
        String pickListVO = null;
        String radioName = null;
        String selectItemPrompt = null;
        String checkedValue = null;
        String uncheckedValue = null;
        NamedNodeMap attrs = currentNode.getAttributes();
        for (int a = 0; a < attrs.getLength(); a++) {
            Node currentAtt = attrs.item(a);
            String strAttr = currentAtt.getNodeName();

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
        convertVCL(currentNode, beanPath, retElement, pgName, "valueChangeListener", app);
        if (!isTableComp) {
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }

        System.out.println("End Conv: convertMessageRadioButton ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @return
     */
    protected static Element convertTable(Node currentNode, Document jsfDoc, String app) {
        System.out.println("Start Conv: convertTable " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertTable " + app);
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
        System.out.println("End Conv: convertTable ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @return
     */
    protected static Element convertRowLayout(Node currentNode, Document jsfDoc, String app) {
        System.out.println("Start Conv: convertRowLayout ");
        ErrorAndLog.handleLog(app, "Start Conv: convertRowLayout ");
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
        System.out.println("End Conv: convertRowLayout ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @return
     */
    protected static Element convertCellLayout(Node currentNode, Document jsfDoc, String app) {
        System.out.println("Start Conv: convertCellLayout ");
        ErrorAndLog.handleLog(app, "Start Conv: convertCellLayout ");
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
        System.out.println("End Conv: convertCellLayout ");
        return retElement;
    }


    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param beanPath
     * @param isTableComp
     * @param src
     * @param filePaths
     * @return
     * @throws Exception
     */
    protected static Element convertSwitcher(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                             String amDef, String beanPath, boolean isTableComp, String src,
                                             Map filePaths) throws Exception {
        System.out.println("Start Conv: convertSwitcher " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                           beanPath + " " + isTableComp + " " + src);
        ErrorAndLog.handleLog(app,
                              "Start Conv: convertSwitcher " + Dest + " " + app + " " + pgName + " " + amDef + " " +
                              beanPath + " " + isTableComp + " " + src);
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
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }

        NodeList nodeList = currentNode.getChildNodes();
        List<Node> nodes = getNode(nodeList, "ui:case", app);
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

                Element columnElement = convertCaseElement(jsfDoc, nameAttr, app);
                Element convertedElement =
                    genericSwitcherCaseMethod(node, jsfDoc, Dest, app, pgName, amDef, src, beanPath, filePaths,
                                              isTableComp);
                if (convertedElement != null)
                    columnElement.appendChild(convertedElement);
                retElement.appendChild(columnElement);
            }
        }
        System.out.println("End Conv: convertSwitcher ");
        return retElement;
    }

    /**
     *
     * @param jsfDoc
     * @param text
     * @return
     */
    protected static Element convertCaseElement(Document jsfDoc, String text, String app) {
        System.out.println("Start Conv: convertCaseElement " + text + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertCaseElement " + text + " " + app);
        Element retElement = jsfDoc.createElement("f:facet");
        retElement.setAttribute("name", text);
        System.out.println("End Conv: convertCaseElement ");
        return retElement;
    }

    /**
     *
     * @param node
     * @param jsfDoc
     * @param Dest
     * @param app
     * @param pgName
     * @param amDef
     * @param src
     * @param beanPath
     * @param filePaths
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element genericSwitcherCaseMethod(Node node, Document jsfDoc, String Dest, String app,
                                                       String pgName, String amDef, String src, String beanPath,
                                                       Map filePaths, boolean isTableComp) throws Exception {
        System.out.println("Start Conv: genericSwitcherCaseMethod " + Dest + " " + app + " " + pgName + " " + amDef +
                           " " + src + " " + beanPath + " " + isTableComp);
        ErrorAndLog.handleLog(app,
                              "Start Conv: genericSwitcherCaseMethod " + Dest + " " + app + " " + pgName + " " + amDef +
                              " " + src + " " + beanPath + " " + isTableComp);
        Element retElement = null;
        NodeList nodeList1 = node.getChildNodes();
        for (int i = 0; i < nodeList1.getLength(); i++) {
            Node caseNode = nodeList1.item(i);
            if (!caseNode.getNodeName().equals("#text")) {
                retElement = convert(caseNode, jsfDoc, Dest, app, pgName, amDef, src, filePaths, isTableComp, beanPath);
            }
        }
        System.out.println("End Conv: genericSwitcherCaseMethod ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param beanPath
     * @param retElement
     * @param pgName
     * @param listenerName
     * @throws Exception
     */
    protected static void convertVCL(Node currentNode, String beanPath, Element retElement, String pgName,
                                     String listenerName, String app) throws Exception {
        System.out.println("Start Conv: convertVCL " + pgName + " " + listenerName + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertVCL " + pgName + " " + listenerName + " " + app);
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
                                    BeanGen.createValueChangeListener(methodName, beanPath, app);
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
        System.out.println("End Conv: convertVCL ");
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @param beanPath
     * @param isTableComp
     * @return
     * @throws Exception
     */
    protected static Element convertCheckBox(Node currentNode, Document jsfDoc, String beanPath, Boolean isTableComp,
                                             String app) throws Exception {
        System.out.println("Start Conv: convertCheckBox " + beanPath + " " + isTableComp + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertCheckBox " + beanPath + " " + isTableComp + " " + app);
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
            BeanGen.createGetterSetter(itemName, itemType, beanPath, imports, app);
        }
        System.out.println("End Conv: convertCheckBox ");
        return retElement;
    }

    /**
     *
     * @param currentNode
     * @param jsfDoc
     * @return
     */
    protected static Element convertSpacer(Node currentNode, Document jsfDoc, String app) {
        System.out.println("Start Conv: convertSpacer " + app);
        ErrorAndLog.handleLog(app, "Start Conv: convertSpacer " + app);
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
        System.out.println("End Conv: convertSpacer ");
        return retElement;
    }

    /**
     *
     * @param pgName
     * @param jsfDoc
     * @param app
     * @return
     * @throws Exception
     */
    protected static Element createJSFF(String pgName, Document jsfDoc, String app) throws Exception {
        System.out.println("Start Conv: createJSFF " + pgName + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createJSFF " + pgName + " " + app);

        Element uiElement = jsfDoc.createElement("ui:composition");
        uiElement.setAttribute("xmlns:ui", "http://java.sun.com/jsf/facelets");
        uiElement.setAttribute("xmlns:af", "http://xmlns.oracle.com/adf/faces/rich");
        jsfDoc.appendChild(uiElement);

        Element panelGridLayout = jsfDoc.createElement("af:panelGridLayout");
        panelGridLayout.setAttribute("id", "pgl1");
        panelGridLayout.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.pgl1}");
        uiElement.appendChild(panelGridLayout);

        Element gridRow = jsfDoc.createElement("af:gridRow");
        gridRow.setAttribute("id", "gr1");
        gridRow.setAttribute("height", "100%");
        gridRow.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.gr1}");
        panelGridLayout.appendChild(gridRow);

        Element panelCell = jsfDoc.createElement("af:panelGridLayout");
        panelCell.setAttribute("id", "gc1");
        gridRow.setAttribute("width", "100%");
        gridRow.setAttribute("halign", "stretch");
        gridRow.setAttribute("valign", "stretch");
        panelCell.setAttribute("binding", "#{backingBeanScope." + pgName + "Bean.gc1}");
        gridRow.appendChild(panelCell);

        System.out.println("End Conv: createJSFF ");
        return panelCell;
    }

}
