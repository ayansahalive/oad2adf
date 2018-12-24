package conv;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class EOXml {
    public EOXml() {
        super();
    }

    /**
     * Eo XML handle
     * @param path
     * @param app
     * @param dest
     * @param repo
     * @param src
     * @throws Exception
     */
    protected static void handleEOXml(String path, String app, String dest, String repo, String src) throws Exception {
        System.out.println("Start Conv: handleEOXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleEOXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        String topApp = dest + FileReaderWritter.getSeparator() + app;
        String pathModel = topApp + FileReaderWritter.getSeparator() + "Model";
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        createEOXml(destination, repo, app);

        String CollClassPath = src + FileReaderWritter.getSeparator();
        String DefClassPath = src + FileReaderWritter.getSeparator();
        String RowClassPath = src + FileReaderWritter.getSeparator();

        File oafEO = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oafEO);
        Element EntityOaf = oafDoc.getDocumentElement();

        File adfEO = new File(destination);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document adfDoc = newDBuilder.parse(adfEO);
        Element EntityAdf = adfDoc.getDocumentElement();

        // update Entity attributes for classes
        NamedNodeMap attrs = EntityOaf.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            if (currentAtt.getNodeName().equals("CollClass")) {
                String val = currentAtt.getNodeValue();
                if (val.equals("oracle.apps.fnd.framework.server.OAEntityCache")) {
                    val = "oracle.jbo.server.EntityCache";
                } else {
                    String temp = currentAtt.getNodeValue();
                    temp = temp.replace(".", FileReaderWritter.getSeparator());
                    CollClassPath += temp;
                    val = DirCreator.changedModelClassPath(currentAtt.getNodeValue(), app);
                }
                EntityAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("DefClass")) {
                String val = currentAtt.getNodeValue();
                if (val.equals("oracle.apps.fnd.framework.server.OAEntityDefImpl")) {
                    val = "oracle.jbo.server.EntityDefImpl";
                } else {
                    String temp = currentAtt.getNodeValue();
                    temp = temp.replace(".", FileReaderWritter.getSeparator());
                    DefClassPath += temp;
                    val = DirCreator.changedModelClassPath(currentAtt.getNodeValue(), app);
                }
                EntityAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("RowClass")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                RowClassPath += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue(), app);
                EntityAdf.setAttribute(currentAtt.getNodeName(), val);
            }

            else if (currentAtt.getNodeName().equals("Name") || currentAtt.getNodeName().equals("DBObjectType") ||
                     currentAtt.getNodeName().equals("DBObjectName") || currentAtt.getNodeName().equals("AliasName") ||
                     currentAtt.getNodeName().equals("BindingStyle")) {
                EntityAdf.setAttribute(currentAtt.getNodeName(), currentAtt.getNodeValue());
            }
        }


        // accessor and enrity attributes
        NodeList nodesOaf = EntityOaf.getChildNodes();
        for (int i = 0; i < nodesOaf.getLength(); i++) {
            Node currentNode = nodesOaf.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("Attribute")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    NamedNodeMap newAttrs = newNode.getAttributes();
                    for (int j = 0; j < newAttrs.getLength(); j++) {
                        Node subNode = newAttrs.item(i);
                        if (null != subNode && subNode.getNodeType() == Node.ELEMENT_NODE &&
                            subNode.getNodeName().equals("DesignTime")) {
                            newNode.removeChild(subNode); // DesignTime
                        }
                    }
                    EntityAdf.appendChild(newNode);
                } else if (currentNode.getNodeName().equals("AccessorAttribute")) {
                    Element newNode = (Element) adfDoc.importNode(currentNode, true);
                    NamedNodeMap Attrs = currentNode.getAttributes();
                    for (int j = 0; j < Attrs.getLength(); j++) {
                        Node subNode = Attrs.item(j);
                        if (subNode.getNodeName().equals("Association") ||
                            subNode.getNodeName().equals("AssociationEnd") ||
                            subNode.getNodeName().equals("AssociationOtherEnd")) {
                            String val = DirCreator.changedModelClassPath(subNode.getNodeValue(), app);
                            newNode.setAttribute(subNode.getNodeName(), val);
                        }
                        // new logic
                        else if (subNode.getNodeName().equals("Type")) {
                            NamedNodeMap ReAttrs = currentNode.getAttributes();
                            for (int x = 0; x < ReAttrs.getLength(); x++) {
                                Node ResubNode = ReAttrs.item(x);
                                if (ResubNode.getNodeName().equals("IsUpdateable")) {
                                    String checkVal = ResubNode.getNodeValue();
                                    if (checkVal.equals("true")) {
                                        String val = DirCreator.changedModelClassPath(subNode.getNodeValue(), app);
                                        newNode.setAttribute(subNode.getNodeName(), val);
                                    }
                                }
                            }
                        }
                        // end of new logic
                    }
                    EntityAdf.appendChild(newNode);
                } else if (currentNode.getNodeName().equals("Key")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    EntityAdf.appendChild(newNode);
                    // change value of pkg
                    NodeList subNodeList = newNode.getChildNodes();
                    for (int x = 0; x < subNodeList.getLength(); x++) {
                        Node subNode = subNodeList.item(x);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE && subNode.getNodeName().equals("AttrArray")) {
                            NodeList underList = subNode.getChildNodes();
                            for (int y = 0; y < underList.getLength(); y++) {
                                Node underNode = underList.item(y);
                                if (underNode.getNodeType() == Node.ELEMENT_NODE &&
                                    underNode.getNodeName().equals("Item")) {
                                    NamedNodeMap attrList = underNode.getAttributes();
                                    for (int z = 0; z < attrList.getLength(); z++) {
                                        Attr Value = (Attr) attrList.item(z);
                                        if (Value.getName().equals("Value")) {
                                            Value.setValue("model." + Value.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else if (currentNode.getNodeName().equals("DesignTime")) {
                    NodeList childList = currentNode.getChildNodes();
                    for (int x = 0; x < childList.getLength(); x++) {
                        Node child = childList.item(x); // attr
                        NamedNodeMap childAttrList = child.getAttributes();
                        if (null != childAttrList) {
                            for (int y = 0; y < childAttrList.getLength(); y++) {
                                Node childAttr = childAttrList.item(y); // name, value
                                if (childAttr.getNodeValue().contains("_codeGenFlag")) { // _codeGenFlag2
                                    // find and update design
                                    NodeList adfList = EntityAdf.getChildNodes();
                                    for (int z = 0; z < adfList.getLength(); z++) {
                                        Node designNode = adfList.item(z);
                                        if (designNode.getNodeName().equals("DesignTime")) {
                                            Node newNode = adfDoc.importNode(child, true);
                                            designNode.appendChild(newNode);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        // handle voimpl
        if (!(src + FileReaderWritter.getSeparator()).equals(CollClassPath))
            EOImpl.handleEOCol(CollClassPath + ".java", app, dest, src);
        // handle voimpl
        if (!(src + FileReaderWritter.getSeparator()).equals(DefClassPath))
            EOImpl.handleEODef(DefClassPath + ".java", app, dest, src);
        // handle voimpl
        if (!(src + FileReaderWritter.getSeparator()).equals(RowClassPath))
            EOImpl.handleEOImpl(RowClassPath + ".java", app, dest, src);

        // jpx
        String impVal = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        impVal =
            impVal.replace(dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model" +
                           FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                           FileReaderWritter.getSeparator(), "");
        impVal = impVal.substring(0, impVal.lastIndexOf(FileReaderWritter.getSeparator()));
        impVal = impVal.replace(FileReaderWritter.getSeparator(), ".");
        impVal = "model." + impVal;
        JPXGen.checkContainee(impVal, pathModelsrc, app);

        // write files
        FileReaderWritter.writeXMLFile(adfDoc, destination, app);

        System.out.println("End Conv: handleEOXml ");

    }

    /**
     * Temp XML
     * @param dest
     * @param repo
     * @param app
     * @throws Exception
     */
    private static void createEOXml(String dest, String repo, String app) throws Exception {
        System.out.println("Start Conv: createEOXml " + dest + " " + repo + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createEOXml " + dest + " " + repo + " " + app);
        String contents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<!DOCTYPE Entity SYSTEM \"jbo_03_01.dtd\">" + "<Entity" +
            "  xmlns=\"http://xmlns.oracle.com/bc4j\"" + "  Version=\"12.1.3.10.47\"" +
            "  InheritPersonalization=\"merge\" >  <DesignTime>" + "    <Attr Name=\"_isCodegen\" Value=\"true\"/>" +
            "  </DesignTime>  </Entity>";

        DirCreator.copyADFDTD(repo, dest, app);
        FileReaderWritter.writeFile(contents, dest, app);
        System.out.println("End Conv: createEOXml ");
    }

}
