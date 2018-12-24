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


public class AOXml {
    public AOXml() {
        super();
    }

    /**
     * AO handle
     * @param path
     * @param app
     * @param dest
     * @param repo
     * @param src
     * @throws Exception
     */
    protected static void handleAOXml(String path, String app, String dest, String repo, String src) throws Exception {
        System.out.println("Start Conv: handleAOXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleAOXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        String topApp = dest + FileReaderWritter.getSeparator() + app;
        String pathModel = topApp + FileReaderWritter.getSeparator() + "Model";
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        createAOXml(destination, repo, app);

        File oafAO = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oafAO);
        Element AssociationOaf = oafDoc.getDocumentElement();

        File adfAO = new File(destination);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document adfDoc = newDBuilder.parse(adfAO);
        Element AssociationAdf = adfDoc.getDocumentElement();

        // update name
        NamedNodeMap attrs = AssociationOaf.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            if (currentAtt.getNodeName().equals("Name"))
                AssociationAdf.setAttribute(currentAtt.getNodeName(), currentAtt.getNodeValue());
        }

        NodeList nodesOaf = AssociationOaf.getChildNodes();
        for (int i = 0; i < nodesOaf.getLength(); i++) {
            Node currentNode = nodesOaf.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("AssociationEnd")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    AssociationAdf.appendChild(newNode);
                }
            }
        }

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

        // update pacakges
        NodeList AssociationEndList = AssociationAdf.getChildNodes();
        for (int i = 0; i < AssociationEndList.getLength(); i++) {
            Node AssociationEnd = AssociationEndList.item(i); // DesignTime,AssociationEnd
            if (AssociationEnd.getNodeName().equals("AssociationEnd")) {

                NamedNodeMap AssociationEndAttrs = AssociationEnd.getAttributes();
                for (int k = 0; k < AssociationEndAttrs.getLength(); k++) {
                    Attr attr = (Attr) AssociationEndAttrs.item(k);
                    if (attr.getNodeName().equals("Owner")) {
                        String val = DirCreator.changedModelClassPath(attr.getNodeValue(), app);
                        attr.setNodeValue(val);
                    } else if (attr.getNodeName().equals("LockContainer")) {
                        Element x = (Element) AssociationEnd;
                        x.removeAttribute("LockContainer");
                    } else if (attr.getNodeName().equals("LockTopContainer")) {
                        Element x = (Element) AssociationEnd;
                        x.removeAttribute("LockTopContainer");
                    }
                }

                NodeList AssociationEndChildren = AssociationEnd.getChildNodes();
                for (int j = 0; j < AssociationEndChildren.getLength(); j++) {
                    Node childElement = AssociationEndChildren.item(j); // AttrArray DesignTime
                    if (childElement.getNodeType() == Node.ELEMENT_NODE &&
                        childElement.getNodeName().equals("AttrArray")) {
                        NodeList subChildren = childElement.getChildNodes();
                        for (int k = 0; k < subChildren.getLength(); k++) {
                            Node Item = subChildren.item(k);
                            if (Item.getNodeType() == Node.ELEMENT_NODE && Item.getNodeName().equals("Item")) {
                                NamedNodeMap itemAttrs = Item.getAttributes();
                                for (int z = 0; z < itemAttrs.getLength(); z++) {
                                    Attr valNode = (Attr) itemAttrs.item(z);
                                    if (valNode.getNodeName().equals("Value")) {
                                        String val =
                                            DirCreator.changedModelClassPath(valNode.getNodeValue(),
                                                                             app); //changedVLAOClassPath
                                        valNode.setNodeValue(val);
                                    }
                                }
                            }
                        }
                    }
                    if (childElement.getNodeType() == Node.ELEMENT_NODE &&
                        childElement.getNodeName().equals("DesignTime")) {
                        NodeList designElements = childElement.getChildNodes();
                        for (int k = 0; k < designElements.getLength(); k++) {
                            Node Attr = designElements.item(k);
                            if (Attr.getNodeType() == Node.ELEMENT_NODE && Attr.getNodeName().equals("Attr")) {
                                NamedNodeMap attrList = Attr.getAttributes();
                                for (int z = 0; z < attrList.getLength(); z++) {
                                    Node nameAttr = attrList.item(z);

                                    if (nameAttr.getNodeName().equals("Name") &&
                                        nameAttr.getNodeValue().equals("_foreignKey")) {
                                        Node nameAttrNew = attrList.item(z + 1);
                                        if (null != nameAttrNew) {
                                            String val =
                                                DirCreator.changedModelClassPath(nameAttrNew.getNodeValue(), app);
                                            nameAttrNew.setNodeValue(val);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // write files
        FileReaderWritter.writeXMLFile(adfDoc, destination, app);

        System.out.println("End Conv: handleAOXml ");
    }

    /**
     * Temp XML
     * @param dest
     * @param repo
     * @param app
     * @throws Exception
     */
    private static void createAOXml(String dest, String repo, String app) throws Exception {
        System.out.println("Start Conv: createAOXml " + dest + " " + repo + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createAOXml " + dest + " " + repo + " " + app);
        String contents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> " + "<!DOCTYPE Association SYSTEM \"jbo_03_01.dtd\"> " +
            "<Association " + "  xmlns=\"http://xmlns.oracle.com/bc4j\" " + "  Version=\"12.1.3.10.47\" " +
            "  InheritPersonalization=\"merge\"> " + "  <DesignTime> " +
            "    <Attr Name=\"_isCodegen\" Value=\"true\"/> " + "  </DesignTime>  </Association>";

        DirCreator.copyADFDTD(repo, dest, app);
        FileReaderWritter.writeFile(contents, dest, app);
        System.out.println("End Conv: createAOXml ");
    }

}
