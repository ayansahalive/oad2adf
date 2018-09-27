package conv;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class VLXml {
    public VLXml() {
        super();
    }

    /**
     * convert view link
     * @param path
     * @param app
     * @param dest
     * @param repo
     * @throws Exception
     */
    protected static void handleVLXml(String path, String app, String dest, String repo, String src) throws Exception {
        System.out.println("Start Conv: handleVLXml " + path + " " + app + " " + dest + " " + repo);
        ErrorAndLog.handleLog(app, "converting " + path);
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String vlName = name.replace(".xml", "");
        String topApp = dest + FileReaderWritter.getSeparator() + app;
        String pathModel = topApp + FileReaderWritter.getSeparator() + "Model";
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        createVLXml(destination, repo);

        File oafVL = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oafVL);
        Element ViewLinkOaf = oafDoc.getDocumentElement();

        File adfVL = new File(destination);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document adfDoc = newDBuilder.parse(adfVL);
        Element ViewLinkAdf = adfDoc.getDocumentElement();

        // name
        NamedNodeMap attrs = ViewLinkOaf.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr currentAtt = (Attr) attrs.item(i);
            if (currentAtt.getNodeName().equals("Name"))
                ViewLinkAdf.setAttribute(currentAtt.getNodeName(), currentAtt.getNodeValue());
        }

        NodeList nodesOaf = ViewLinkOaf.getChildNodes();
        for (int i = 0; i < nodesOaf.getLength(); i++) {
            Node currentNode = nodesOaf.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("ViewLinkDefEnd")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    ViewLinkAdf.appendChild(newNode);
                }
            }
        }

        // package change
        NodeList childrenNodes = ViewLinkAdf.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node child = childrenNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("ViewLinkDefEnd")) {
                NamedNodeMap childAttrs = child.getAttributes();
                for (int j = 0; j < childAttrs.getLength(); j++) {
                    Attr att = (Attr) childAttrs.item(j);
                    if (att.getName().equals("Owner")) {
                        String val = DirCreator.changedModelClassPath(att.getValue());
                        att.setValue(val);
                    }
                }
                NodeList subChildren = child.getChildNodes();
                for (int k = 0; k < subChildren.getLength(); k++) {
                    Node attarr = subChildren.item(k);
                    if (attarr.getNodeType() == Node.ELEMENT_NODE && attarr.getNodeName().equals("AttrArray")) {
                        NodeList subSub = attarr.getChildNodes();
                        for (int l = 0; l < subSub.getLength(); l++) {
                            Node Item = subSub.item(l);
                            if (Item.getNodeType() == Node.ELEMENT_NODE && Item.getNodeName().equals("Item")) {
                                NamedNodeMap attrList = Item.getAttributes();
                                for (int m = 0; m < attrList.getLength(); m++) {
                                    Attr attrItem = (Attr) attrList.item(m);
                                    if (attrItem.getName().equals("Value")) {
                                        String val =
                                            DirCreator.changedModelClassPath(attrItem.getValue()); // changedVLAOClassPath
                                        attrItem.setValue(val);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        // label bundle and properties
        Node Properties = adfDoc.createElement("Properties");
        Node SchemaBasedProperties = adfDoc.createElement("SchemaBasedProperties");
        Element LABEL = adfDoc.createElement("LABEL");

        String impVal = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        impVal =
            impVal.replace(dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model" +
                           FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                           FileReaderWritter.getSeparator(), "");
        impVal = impVal.substring(0, impVal.lastIndexOf(FileReaderWritter.getSeparator()));
        impVal = impVal.replace(FileReaderWritter.getSeparator(), ".");
        impVal = "model." + impVal + ".";

        LABEL.setAttribute("ResId", impVal + vlName + "_LABEL");
        String contents = impVal + vlName + "_LABEL=" + vlName;
        FileReaderWritter.appendFile(contents,
                                     pathModelsrc + FileReaderWritter.getSeparator() + "ModelBundle.properties");
        SchemaBasedProperties.appendChild(LABEL);
        Properties.appendChild(SchemaBasedProperties);
        ViewLinkAdf.appendChild(Properties);

        // jpx
        JPXGen.checkContainee(impVal.substring(0, impVal.lastIndexOf(".")), pathModelsrc);

        // write files
        FileReaderWritter.writeXMLFile(adfDoc, destination);

        System.out.println("End Conv: handleVLXml ");
    }

    /**
     * create empty xml
     * @param dest
     * @param repo
     * @throws Exception
     */
    private static void createVLXml(String dest, String repo) throws Exception {
        System.out.println("Start Conv: createAOXml " + dest + " " + repo);
        String contents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<!DOCTYPE ViewLink SYSTEM \"jbo_03_01.dtd\">" +
            "<ViewLink" + "  xmlns=\"http://xmlns.oracle.com/bc4j\"" + "  Name=\"VLAB\"" +
            "  Version=\"12.1.3.10.47\"" + "  InheritPersonalization=\"merge\">   <ResourceBundle>" +
            "    <PropertiesBundle" + "      PropertiesFile=\"model.ModelBundle\"/>" +
            "  </ResourceBundle> </ViewLink>";

        DirCreator.copyADFDTD(repo, dest);
        FileReaderWritter.writeFile(contents, dest);
        System.out.println("End Conv: createAOXml ");
    }
}
