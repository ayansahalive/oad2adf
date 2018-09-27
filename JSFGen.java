package conv;

import java.io.*;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class JSFGen {
    public JSFGen() {
        super();
    }

    protected static void handlePage(String path, String app, String Dest, String repo) throws Exception {
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

            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    // System.out.println(currentNode.getNodeName());
                    Element retElement = convert(currentNode, jsfDoc, Dest, app, pgName, amDef, jsfBeanName);
                    if (null != retElement)
                        form.appendChild(retElement);
                }
            }

            FileReaderWritter.writeXMLFile(jsfDoc, pathVC + "\\public_html\\" + pgName + ".jsf");
            BeanGen.createAdfConfig(pathVC, pgName);

            System.out.println("End Conv: handlePage");
        } catch (Exception e) {
            throw e;
        }
    }
    
//    public Element recursiveNodes() {
//        
//    }

    private static Element convert(Node currentNode, Document jsfDoc, String Dest, String app, String pgName,
                                   String amDef, String jsfBeanName) throws Exception {
        System.out.println("Start Conv: convert");
        try {
            String strElement = currentNode.getNodeName(); // oaf
            String path = Dest + "\\" + app + "\\ViewController\\src\\view\\backing\\" + pgName + "Bean.java";
            if (strElement.equals("oa:messageStyledText")) {
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
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues");
                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                System.out.println("End Conv: convert");
                return retElement;
            } else if (strElement.equals("oa:messageInputText")) {
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
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "attributeValues");
                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
                BeanGen.createGetterSetter(itemName, itemType, path, imports);
                System.out.println("End Conv: convert");
                return retElement;
            } else if (strElement.equals("oa:button")) { // when none match
                String itemName = "";
                //String itemType = "RichOutputText";
                //String imports = " import oracle.adf.view.rich.component.rich.output.RichOutputText; ";

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

                                        StringBuilder beanPath =
                                            new StringBuilder(System.getenv("OAF_SOURCE") + "\\" + app + "\\" +
                                                              jsfBeanName.replace(".", "\\") + ".java");
                                        JavaCodeExtractor obj = new JavaCodeExtractor();
                                        Vector vec = obj.start(beanPath.toString());
                                        System.out.println("In JSF Gean java class ::methodname from xml::" +
                                                           methodName);
                                        for (int l = 0; l < vec.size(); l++) {
                                            String javaLine = (String) vec.get(l);
                                            System.out.println("In JSF Gean java class ::methodname from java::" +
                                                               javaLine);
                                            if (javaLine.contains("pageContext.getParameter(\"event\").equals(\"" +
                                                                  methodName + "\")")) {
                                                StringTokenizer strToken = new StringTokenizer(javaLine, ";");
                                                while (strToken.hasMoreTokens()) {
                                                    String token = strToken.nextToken();
                                                    if (token.contains("invokeMethod")) {
                                                        String pageDefMethod =
                                                            token.substring(token.indexOf("invokeMethod(") + 14,
                                                                            token.lastIndexOf("\")"));
                                                        System.out.println(pageDefMethod);
                                                        retElement.setAttribute("value",
                                                                                "#{bindings." + pageDefMethod +
                                                                                ".execute}");
                                                        retElement.setAttribute("text", pageDefMethod);
                                                        retElement.setAttribute("disabled",
                                                                                "#{!bindings." + pageDefMethod +
                                                                                ".enabled}");
                                                        PageDefXml.handlePageDef(pgName, null, pageDefMethod, Dest, app,
                                                                                 amDef, "invokeMethod");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return retElement;
            } else if (strElement.equals("oa:messageLovInput")) {
                Element retElement = jsfDoc.createElement("af:inputListOfValues");
                String itemName = "";
                String bindingVO = null;
                String bindingAttr = null;
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
                }
                retElement.setAttribute("value", "#{bindings." + bindingAttr + ".inputValue}");
                retElement.setAttribute("label", "#{bindings." + bindingAttr + ".label}");
                retElement.setAttribute("required", "#{bindings." + bindingAttr + ".hints.mandatory}");
                retElement.setAttribute("shortDesc", "#{bindings." + bindingAttr + ".hints.tooltip}");
                retElement.setAttribute("model", "#{bindings." + bindingAttr + ".listOfValuesModel}");
                retElement.setAttribute("columns", "#{bindings." + bindingAttr + ".hints.displayWidth}");
                retElement.setAttribute("popupTitle", "Search and Select: #{bindings." + bindingAttr + ".hints.label}");
                
                PageDefXml.handlePageDef(pgName, bindingVO, bindingAttr, Dest, app, amDef, "list");
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


}
