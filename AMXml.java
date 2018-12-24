package conv;

import java.io.File;

import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AMXml {
    public AMXml() {
        super();
    }

    /**
     * AM XML handle
     * @param path
     * @param app
     * @param dest
     * @param repo
     * @param src
     * @throws Exception
     */
    protected static void handleAMXml(String path, String app, String dest, String repo, String src) throws Exception {
        System.out.println("Start Conv: handleAMXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleAMXml " + path + " " + app + " " + dest + " " + repo + " " + src);
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String amName = name.replace(".xml", "");
        String topApp = dest + FileReaderWritter.getSeparator() + app;
        String pathModel = topApp + FileReaderWritter.getSeparator() + "Model";
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        String ImplClassPath = src + FileReaderWritter.getSeparator(); // for processing AMImpl and ClientInterface
        String DefClassPath = src + FileReaderWritter.getSeparator();

        createAMXml(amName, destination, repo, app);

        File oafAm = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oafAm);
        Element AppModule = oafDoc.getDocumentElement();
        NodeList nodes = AppModule.getChildNodes();

        File adfAm = new File(destination);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document adfDoc = newDBuilder.parse(adfAm);
        Element AdfAppModule = adfDoc.getDocumentElement();


        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = currentNode.getNodeName();
                if (nodeName.equals("ViewUsage")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    Element x = (Element) newNode;
                    NamedNodeMap attrs = x.getAttributes();
                    for (int j = 0; j < attrs.getLength(); j++) {
                        Node currentAtt = attrs.item(j);
                        String strAttr = currentAtt.getNodeName();
                        if (strAttr.equals("ViewObjectName")) {
                            String val = currentAtt.getNodeValue();
                            val = DirCreator.changedModelClassPath(val, app);
                            currentAtt.setNodeValue(val);
                        }
                    }
                    AdfAppModule.appendChild(x);
                } else if (nodeName.equals("AppModuleUsage")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    Element x = (Element) newNode;
                    NamedNodeMap attrs = x.getAttributes();
                    for (int j = 0; j < attrs.getLength(); j++) {
                        Attr currentAtt = (Attr) attrs.item(j);
                        String strAttr = currentAtt.getName();
                        if (strAttr.equals("FullName")) {
                            String val = currentAtt.getValue();
                            val = DirCreator.changedModelClassPath(val, app);
                            currentAtt.setValue(val);
                        }
                    }
                    x.setAttribute("SharedScope", "0");
                    AdfAppModule.appendChild(x);
                } else if (nodeName.equals("ViewLinkUsage")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    Element x = (Element) newNode;
                    NamedNodeMap attrs = x.getAttributes();
                    for (int j = 0; j < attrs.getLength(); j++) {
                        Attr currentAtt = (Attr) attrs.item(j);
                        String strAttr = currentAtt.getName();
                        if (strAttr.equals("ViewLinkObjectName") || strAttr.equals("SrcViewUsageName") ||
                            strAttr.equals("DstViewUsageName")) {
                            String val = currentAtt.getValue();
                            val = DirCreator.changedModelClassPath(val, app);
                            currentAtt.setValue(val);
                        }
                        NodeList designList = newNode.getChildNodes();
                        for (int l = 0; l < designList.getLength(); l++) {
                            Node design = designList.item(l);
                            if (design.getNodeType() == Node.ELEMENT_NODE &&
                                design.getNodeName().equals("DesignTime")) {
                                newNode.removeChild(design); // remove design
                            }
                        }
                    }
                    AdfAppModule.appendChild(x);
                }
            }
        }


        //classes
        NamedNodeMap attrs = AppModule.getAttributes();
        for (int j = 0; j < attrs.getLength(); j++) {
            Node currentAtt = attrs.item(j);
            if (currentAtt.getNodeName().equals("ComponentClass")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                ImplClassPath += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue(), app);
                AdfAppModule.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("DefClass")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                DefClassPath += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue(), app);
                AdfAppModule.setAttribute(currentAtt.getNodeName(), val);
            }
        }

        // add entry to jpx
        DirCreator.copyADFDTD(repo, pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx", app);
        File jpx = new File(pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx");
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(false);
        DocumentBuilder db = fact.newDocumentBuilder();
        Document jpxDoc = db.parse(jpx);
        NodeList formList = jpxDoc.getElementsByTagName("DesignTime");
        Element designProj = (Element) formList.item(0);

        // find the occurance and increase the number
        String jpxContents =
            FileReaderWritter.getCharContents(pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx", app);
        String findStr = "_appModuleNames";
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {
            lastIndex = jpxContents.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }

        Element newElement1 = jpxDoc.createElement("Attr");
        newElement1.setAttribute("Name", "_appModuleNames" + count);

        String impVal = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        impVal =
            impVal.replace(dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model" +
                           FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                           FileReaderWritter.getSeparator(), "");
        impVal = impVal.substring(0, impVal.lastIndexOf(FileReaderWritter.getSeparator()));
        impVal = impVal.replace(FileReaderWritter.getSeparator(), ".");
        impVal = "model." + impVal + ".";

        newElement1.setAttribute("Value", impVal + amName);


        designProj.appendChild(newElement1);

        JPXGen.checkContainee(impVal.substring(0, impVal.lastIndexOf(".") - 1), pathModelsrc, app);

        // update bundle
        String contents = impVal + amName + "_LABEL=" + amName;
        FileReaderWritter.appendFile(contents,
                                     pathModelsrc + FileReaderWritter.getSeparator() + "ModelBundle.properties", app);


        // update ResId in am xml
        NodeList allNodes = AdfAppModule.getChildNodes();
        for (int i = 0; i < allNodes.getLength(); i++) {
            Node eachNode = allNodes.item(i);
            if (eachNode.getNodeType() == Node.ELEMENT_NODE && eachNode.getNodeName().equals("Properties")) {
                NodeList nextNodes = eachNode.getChildNodes();
                for (int j = 0; j < nextNodes.getLength(); j++) {
                    Node nextEachNode = nextNodes.item(j);
                    if (nextEachNode.getNodeType() == Node.ELEMENT_NODE &&
                        nextEachNode.getNodeName().equals("SchemaBasedProperties")) {
                        NodeList LABELList = nextEachNode.getChildNodes();
                        for (int k = 0; k < LABELList.getLength(); k++) {
                            Node LABEL = LABELList.item(k);
                            if (LABEL.getNodeType() == Node.ELEMENT_NODE && LABEL.getNodeName().equals("LABEL")) {
                                Element lab = (Element) LABEL;
                                lab.setAttribute("ResId", impVal + amName + "_LABEL");
                            }
                        }
                    }
                }
            }
        }

        // handle Impl File and Interface File
        HashMap hm = AMImpl.handleAMImpl(ImplClassPath + ".java", app, dest, src);

        // handle def class
        if (!(src + FileReaderWritter.getSeparator()).equals(DefClassPath))
            AMImpl.handleAMImpl(DefClassPath + ".java", app, dest, src);

        // write files
        FileReaderWritter.writeXMLFile(jpxDoc, pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx", app);

        FileReaderWritter.writeXMLFile(adfDoc, destination, app);

        // add bc4j entry
        BC4JGen.handleBC4J(destination, repo, app);

        System.out.println("End Conv: handleAMXml");
    }

    /**
     * Temp xml
     * @param amName
     * @param dest
     * @param repo
     * @param app
     * @throws Exception
     */
    private static void createAMXml(String amName, String dest, String repo, String app) throws Exception {
        System.out.println("Start Conv: createAMXml " + amName + " " + dest + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createAMXml " + amName + " " + dest + " " + app);
        String contents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> " + "<!DOCTYPE AppModule SYSTEM \"jbo_03_01.dtd\"> " +
            "<AppModule" + "    xmlns=\"http://xmlns.oracle.com/bc4j\"" + "    Name=\"" + "" + amName + "\"" +
            "    Version=\"12.1.3.10.47\"" + "    InheritPersonalization=\"merge\"" +
            "    ClearCacheOnRollback=\"true\"> " + "   <DesignTime>" +
            "        <Attr Name=\"_isCodegen\" Value=\"true\"/>" +
            "        <Attr Name=\"_isDefCodegen\" Value=\"true\"/>" + "    </DesignTime> " + "<ResourceBundle>" +
            "     <PropertiesBundle" + "            PropertiesFile=\"model.ModelBundle\"/>" + "    </ResourceBundle> " +
            " <Properties>" + "        <SchemaBasedProperties>" + " <LABEL/> </SchemaBasedProperties>" +
            "    </Properties> " + "</AppModule>";

        DirCreator.copyADFDTD(repo, dest, app);
        FileReaderWritter.writeFile(contents, dest, app);
        System.out.println("End Conv: createAMXml");
    }

}
