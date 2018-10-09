package conv;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class JSFGen {
    public JSFGen() {
        super();
    }

    protected static void handlePage(String path, String app, String Dest, String repo, String src,
                                     Map filePaths) throws Exception {
        System.out.println("Start Conv: handlePage " + path + " " + app + " " + Dest + " " + repo);
        String pgName = path.substring(path.lastIndexOf("\\") + 1);
        pgName = pgName.replace(".xml", "");
        String pathVC = Dest + "\\" + app + "\\ViewController";

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

            createJSF(pgName, pathVC, title, windowTitle);
            BeanGen.createBean(pgName, pathVC);

            File jsf = new File(Dest + "\\" + app + "\\ViewController\\public_html\\" + pgName + ".jsf");
            DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
            newDbFactory.setValidating(false);
            DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
            Document jsfDoc = newDBuilder.parse(jsf);
            NodeList formList = jsfDoc.getElementsByTagName("af:form");
            Element form = (Element) formList.item(0);
            // Element retElement = null;
            if (nodes.getLength() > 0) {
                recursiveNodes(nodes, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths, form);
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

            FileReaderWritter.writeXMLFile(jsfDoc, pathVC + "\\public_html\\" + pgName + ".jsf");
            BeanGen.createAdfConfig(pathVC, pgName);
            BeanGen.copyProcessFormRequest(path, app, Dest, src);

            System.out.println("End Conv: handlePage");
        } catch (Exception e) {
            throw e;
        }
    }

    public static void recursiveNodes(NodeList nodeList, Document jsfDoc, String Dest, String app, String pgName,
                                      String amDef, String jsfBeanName, String src, Map filePaths,
                                      Element form) throws Exception {
        Element retElement = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            System.out.println(node.getNodeName());
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (!node.getNodeName().equals("oa:messageLovInput") &&
                    !node.getNodeName().equals("oa:advancedTable")) {
                    NodeList childNodeList = node.getChildNodes();
                    if (childNodeList.getLength() > 0) {
                        recursiveNodes(childNodeList, jsfDoc, Dest, app, pgName, amDef, jsfBeanName, src, filePaths,
                                       form);
                    }
                }
                retElement = convert(node, jsfDoc, Dest, app, pgName, amDef, src, filePaths);
                if (null != retElement) {
                    form.appendChild(retElement);
                }
            }
        }
        // return retElement;
    }

    private static Element convert(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                   String amDef, String src, Map filePaths) throws Exception {
        System.out.println("Start Conv: convert");
        try {
            String strElement = currentNode.getNodeName(); // oaf
            String path = Dest + "\\" + app + "\\ViewController\\src\\view\\backing\\" + pgName + "Bean.java";
            if (strElement.equals("oa:messageStyledText")) {
                return convertMessageStyledText(currentNode, jsfDoc, Dest, app, pgName, amDef, path, false);
            } else if (strElement.equals("oa:messageTextInput")) {
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
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null,
                                         null);
                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                System.out.println("End Conv: convert");
                return retElement;
            } else if (strElement.equals("oa:button")) { // when none match
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

                                        //                                        StringBuilder beanPath =
                                        //                                            new StringBuilder(System.getenv("OAF_SOURCE") + "\\" + app + "\\" +
                                        //                                                              jsfBeanName.replace(".", "\\") + ".java");
                                        //                                        JavaCodeExtractor obj = new JavaCodeExtractor();
                                        //                                        Vector vec = obj.start(beanPath.toString());
                                        //                                        System.out.println("In JSF Gean java class ::methodname from xml::" +
                                        //                                                           methodName);
                                        //                                        for (int l = 0; l < vec.size(); l++) {
                                        //                                            String javaLine = (String) vec.get(l);
                                        //                                            System.out.println("In JSF Gean java class ::methodname from java::" +
                                        //                                                               javaLine);
                                        //                                            if (javaLine.contains("pageContext.getParameter(\"event\").equals(\"" +
                                        //                                                                  methodName + "\")")) {
                                        //                                                StringTokenizer strToken = new StringTokenizer(javaLine, ";");
                                        //                                                while (strToken.hasMoreTokens()) {
                                        //                                                    String token = strToken.nextToken();
                                        //                                                    if (token.contains("invokeMethod")) {
                                        //                                                        String pageDefMethod =
                                        //                                                            token.substring(token.indexOf("invokeMethod(") + 14,
                                        //                                                                            token.lastIndexOf("\")"));
                                        //                                                        System.out.println(pageDefMethod);
                                        //                                                        retElement.setAttribute("value",
                                        //                                                                                "#{bindings." + pageDefMethod +
                                        //                                                                                ".execute}");
                                        //                                                        retElement.setAttribute("text", pageDefMethod);
                                        //                                                        retElement.setAttribute("disabled",
                                        //                                                                                "#{!bindings." + pageDefMethod +
                                        //                                                                                ".enabled}");
                                        //                                                        PageDefXml.handlePageDef(pgName, null, pageDefMethod, Dest, app,
                                        //                                                                                 amDef, "invokeMethod", null, null);
                                        //                                                    }
                                        //                                                }
                                        //                                            }
                                        //                                        }
                                        retElement.setAttribute("actionListener",
                                                                "#{backingBeanScope." + pgName + "Bean." + methodName +
                                                                "}");
                                        retElement.setAttribute("text", methodName);
                                        BeanGen.createActionListener(methodName, path);
                                    }
                                }
                            }
                        }
                    }
                }
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                return retElement;
            } else if (strElement.equals("oa:submitButton")) { // when none match
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
                }

                retElement.setAttribute("actionListener", "#{backingBeanScope." + pgName + "Bean." + itemName + "}");
                retElement.setAttribute("text", itemName);
                //retElement.setAttribute("disabled", "#{!bindings." + itemName + ".enabled}");
                //PageDefXml.handlePageDef(pgName, null, itemName, Dest, app, amDef, "invokeMethod", null, null);
                //                NodeList childNodes = currentNode.getChildNodes();
                //                for (int i = 0; i < childNodes.getLength(); i++) {
                //                    Node childNode = childNodes.item(i);
                //                    if (childNode.getNodeName().equals("ui:primaryClientAction")) {
                //                        NodeList subChildNodes = childNode.getChildNodes();
                //                        for (int j = 0; j < subChildNodes.getLength(); j++) {
                //                            Node subChildNode = subChildNodes.item(j);
                //                            if (subChildNode.getNodeName().equals("ui:fireAction")) {
                //                                NamedNodeMap attr = subChildNode.getAttributes();
                //                                for (int k = 0; k < attr.getLength(); k++) {
                //                                    Node attrNode = attr.item(k);
                //                                    if (attrNode.getNodeName().equals("event")) {
                //                                        String methodName = attrNode.getNodeValue();
                //
                //                                        StringBuilder beanPath =
                //                                            new StringBuilder(System.getenv("OAF_SOURCE") + "\\" + app + "\\" +
                //                                                              jsfBeanName.replace(".", "\\") + ".java");
                //                                        JavaCodeExtractor obj = new JavaCodeExtractor();
                //                                        Vector vec = obj.start(beanPath.toString());
                //                                        System.out.println("In JSF Gean java class ::methodname from xml::" +
                //                                                           methodName);
                //                                        for (int l = 0; l < vec.size(); l++) {
                //                                            String javaLine = (String) vec.get(l);
                //                                            System.out.println("In JSF Gean java class ::methodname from java::" +
                //                                                               javaLine);
                //                                            if (javaLine.contains("pageContext.getParameter(\"event\").equals(\"" +
                //                                                                  methodName + "\")")) {
                //                                                StringTokenizer strToken = new StringTokenizer(javaLine, ";");
                //                                                while (strToken.hasMoreTokens()) {
                //                                                    String token = strToken.nextToken();
                //                                                    if (token.contains("invokeMethod")) {
                //                                                        String pageDefMethod =
                //                                                            token.substring(token.indexOf("invokeMethod(") + 14,
                //                                                                            token.lastIndexOf("\")"));
                //                                                        System.out.println(pageDefMethod);
                //                                                        retElement.setAttribute("value",
                //                                                                                "#{bindings." + pageDefMethod +
                //                                                                                ".execute}");
                //                                                        retElement.setAttribute("text", pageDefMethod);
                //                                                        retElement.setAttribute("disabled",
                //                                                                                "#{!bindings." + pageDefMethod +
                //                                                                                ".enabled}");
                //                                                        PageDefXml.handlePageDef(pgName, null, pageDefMethod, Dest, app,
                //                                                                                 amDef, "invokeMethod", null, null);
                //                                                    }
                //                                                }
                //                                            }
                //                                        }
                //                                    }
                //                                }
                //                            }
                //                        }
                //                    }
                //                }
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                BeanGen.createActionListener(itemName, path);
                return retElement;
            } else if (strElement.equals("oa:messageLovInput")) {
                Element retElement = jsfDoc.createElement("af:inputListOfValues");
                String itemName = "";
                String itemType = "RichInputListOfValues";
                String imports = "import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;";
                String bindingVO = null;
                String bindingAttr = null;
                String regionName = null;
                String lovAttr = null;
                String lovView = null;
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
                    } else {
                        bindingAttr = "TempView";
                    }

                    if (strAttr.equals("viewName")) {
                        bindingVO = currentAtt.getNodeValue();
                    } else {
                        bindingVO = "VOB";
                    }
                    if (strAttr.equals("externalListOfValues")) {
                        regionName = src + currentAtt.getNodeValue() + ".xml";
                        System.out.println(regionName);
                        File regionFile = new File(regionName);

                        if (regionFile.exists()) {
                            System.out.println("Region File Exists.......");

                            List<Node> pageNodes = getNode(currentNode.getChildNodes(), "lovMap");
                            for (Node node : pageNodes) {
                                NamedNodeMap namedNodeMap = node.getAttributes();
                                for (int i = 0; i < namedNodeMap.getLength(); i++) {
                                    Node attrNode = namedNodeMap.item(i);
                                    if (attrNode.getNodeName().equals("lovItem")) {
                                        lovAttr = attrNode.getNodeValue();
                                    }

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
                                if (tempLovAttr.equals(lovAttr)) {
                                    lovView = ((Element) node).getAttribute("viewName");
                                }
                            }
                        }
                        //                        VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingVO, bindingAttr, lovView,
                        //                                          lovAttr);
                    } else {
                        NodeList nodeList = currentNode.getChildNodes();
                        List<Node> pageNodes = getNode(nodeList, "lovMap");
                        for (Node node : pageNodes) {
                            NamedNodeMap namedNodeMap = node.getAttributes();
                            for (int i = 0; i < namedNodeMap.getLength(); i++) {
                                Node attrNode = namedNodeMap.item(i);
                                if (attrNode.getNodeName().equals("lovItem")) {
                                    lovAttr = attrNode.getNodeValue();
                                }

                            }
                        }

                        List<Node> nodes = getNode(nodeList, "oa:messageStyledText");
                        for (Node node : nodes) {
                            String tempLovAttr = ((Element) node).getAttribute("viewAttr");
                            if (tempLovAttr.equals(lovAttr)) {
                                lovView = ((Element) node).getAttribute("viewName");
                            }
                        }
                        //                        VOXml.addLovDetails((String) filePaths.get(bindingVO), bindingVO, bindingAttr, lovView,
                        //                                            lovAttr);
                    }
                }
                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
                retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
                retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
                retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
                retElement.setAttribute("model", "#{bindings." + bindingAttr + ".listOfValuesModel}");
                retElement.setAttribute("columns", "#{bindings." + bindingAttr + ".hints.displayWidth}");
                retElement.setAttribute("popupTitle", "Search and Select: #{bindings." + bindingAttr + ".hints.label}");
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "listOfValues", null, null);
                return retElement;
            } else if (strElement.equals("oa:messageChoice")) {
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
                } else {
                    retElement.setAttribute("value", "#{bindings." + pickListVO + ".inputValue}");
                    retElement.setAttribute("label", "#{bindings." + pickListVO + ".label}");
                    retElement.setAttribute("required", "#{bindings." + pickListVO + ".hints.mandatory}");
                    retElement.setAttribute("shortDesc", "#{bindings." + pickListVO + ".hints.tooltip}");
                }
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
                                        BeanGen.createValueChangeListener(methodName, path);
                                        //                                        StringBuilder beanPath =
                                        //                                            new StringBuilder(System.getenv("OAF_SOURCE") + "\\" + app + "\\" +
                                        //                                                              jsfBeanName.replace(".", "\\") + ".java");
                                        //                                        JavaCodeExtractor obj = new JavaCodeExtractor();
                                        //                                        Vector vec = obj.start(beanPath.toString());
                                        //                                        System.out.println("In JSF Gean java class ::methodname from xml::" +
                                        //                                                           methodName);
                                        //                                        for (int l = 0; l < vec.size(); l++) {
                                        //                                            String javaLine = (String) vec.get(l);
                                        //                                            System.out.println("In JSF Gean java class ::methodname from java::" +
                                        //                                                               javaLine);
                                        //                                            if (javaLine.contains(methodName)) {
                                        //                                                StringTokenizer strToken = new StringTokenizer(javaLine, ";");
                                        //                                                while (strToken.hasMoreTokens()) {
                                        //                                                    String token = strToken.nextToken();
                                        //                                                    System.out.println("Token details::::" + token);
                                        //                                                    if (token.contains("invokeMethod")) {
                                        //                                                        String pageDefMethod =
                                        //                                                            token.substring(token.indexOf("invokeMethod(") + 14,
                                        //                                                                            token.lastIndexOf("\")"));
                                        //                                                        System.out.println(pageDefMethod);
                                        //                                                        retElement.setAttribute("valueChangeListener",
                                        //                                                                                "#{requestScope.manageBean." +
                                        //                                                                                pageDefMethod + "}");
                                        //
                                        //
                                        //                                                    }
                                        //                                                }
                                        //                                            }
                                        //                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "list", pickListAttr,
                                         pickListVO);
                return retElement;
            } else if (strElement.equals("oa:link")) {
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
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                return retElement;
            } else if (strElement.equals("oa:advancedTable")) {
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

                NodeList nodeList = currentNode.getChildNodes();
                List<Node> nodes = getNode(nodeList, "oa:column");
                PageDefXml.handlePageDef(pgName, bindingVO, null, Dest, app, amDef, "tree", null,
                                         null);
                recursiveColumnElements(nodes, jsfDoc, Dest, app, pgName, amDef, path, bindingVO, retElement);
                return retElement;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    private static void createJSF(String pgName, String pathVC, String title, String windowTitle) throws Exception {
        System.out.println("Start Conv: createJSF " + pgName + " " + pathVC + " " + title + " " + windowTitle);
        String jsf =
            "<?xml version='1.0' encoding='UTF-8'?> " + "<!DOCTYPE html> " +
            "<f:view xmlns:f=\"http://java.sun.com/jsf/core\" xmlns:af=\"http://xmlns.oracle.com/adf/faces/rich\"> " +
            "<af:document id=\"d1\" title=\"" + "" + windowTitle + "\" binding=\"#{backingBeanScope." + pgName +
            "Bean.d1}\"> " + "<af:form id=\"f1\" binding=\"#{backingBeanScope." + pgName + "Bean.f1}\"> " +
            "<af:panelHeader text=\"" + "" + title + "\" id=\"ph1\" binding=\"#{backingBeanScope." + pgName +
            "Bean.ph1}\"> " + "<f:facet name=\"context\"/> " + "<f:facet name=\"menuBar\"/> " +
            "<f:facet name=\"toolbar\"/> " + "<f:facet name=\"legend\"> " +
            "<af:separator id=\"s1\" binding=\"#{backingBeanScope." + pgName + "Bean.s1}\"/> " + "</f:facet> " +
            "<f:facet name=\"info\"/> " + "</af:panelHeader> " + " </af:form> " + "</af:document> " + "</f:view>";

        FileReaderWritter.writeFile(jsf, pathVC + "\\public_html\\" + pgName + ".jsf");
        System.out.println("End Conv: convert");
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
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "tableAttr", null, null);
            retElement.setAttribute("value", "#{row." + bindingAttr + "}");
            //BeanGen.createGetterSetter(itemName, itemType, beanPath, imports);
        } else {
            PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues", null, null);
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
                                                 String amDef, String beanPath, String bindingVO,
                                                 Element retElement) throws Exception {
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
                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                        columnElement.appendChild(convertMessageStyledText(compNode, jsfDoc, Dest, app, pgName, amDef,
                                                                           beanPath, true));
                        retElement.appendChild(columnElement);
                    } else if (compNodeName.equals("oa:button")) {
                        Element columnElement = convertColumnElement(jsfDoc, bindingVO, bindingAttr, columnId, null);
                        retElement.appendChild(columnElement);
                    }
                }
            }
        }
        return retElement;
    }


    protected static void recursiveColumnElements(List<Node> nodes, Document jsfDoc, String Dest, String app,
                                                  String pgName, String amDef, String beanPath, String bindingVO,
                                                  Element retElement) throws Exception {
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
                recursiveColumnElements(groupNodes, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO,
                                        toRetElement);
                retElement.appendChild(toRetElement);
            } else if (node.getNodeName().equals("oa:column")) {
                genericColumnMethod(node, jsfDoc, Dest, app, pgName, amDef, beanPath, bindingVO, retElement);
            }
        }
    }
}
