package conv;

import java.io.*;
import java.io.IOException;

import javax.xml.parsers.*;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import org.xml.sax.SAXException;


public class VOXml {
    public VOXml() {
        super();
    }

    /**
     * generate VOXml
     * @param path
     * @param app
     * @param dest
     * @param repo
     * @throws Exception
     */
    protected static void handleVOXml(String path, String app, String dest, String repo, String src) throws Exception {
        System.out.println("Start Conv: handleVOXml " + path + " " + app + " " + dest + " " + repo);
        ErrorAndLog.handleLog(app, "converting " + path);
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String voName = name.replace(".xml", "");
        String topApp = dest + FileReaderWritter.getSeparator() + app;
        String pathModel = topApp + FileReaderWritter.getSeparator() + "Model";
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";

        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        String ImplClassPath = src + FileReaderWritter.getSeparator();
        String DefClassPath = src + FileReaderWritter.getSeparator();
        String RowClassPath = src + FileReaderWritter.getSeparator();
        String RowInterface = src + FileReaderWritter.getSeparator();
        String Client = src + FileReaderWritter.getSeparator();

        createVOXml(destination, repo);

        File oafVO = new File(path); // OAF
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document oafDoc = dBuilder.parse(oafVO);
        Element ViewObjectOAf = oafDoc.getDocumentElement();

        File adfVO = new File(destination);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document adfDoc = newDBuilder.parse(adfVO);
        Element ViewObjectAdf = adfDoc.getDocumentElement();

        // update ViewObject attributes for classes
        NamedNodeMap attrs = ViewObjectOAf.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node currentAtt = attrs.item(i);
            if (currentAtt.getNodeName().equals("ComponentClass")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                ImplClassPath += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue());
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("DefClass")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                DefClassPath += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue());
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("ClientRowProxyName")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                Client += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue());
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("RowInterface")) {
                String temp = currentAtt.getNodeValue();
                temp = temp.replace(".", FileReaderWritter.getSeparator());
                RowInterface += temp;
                String val = DirCreator.changedModelClassPath(currentAtt.getNodeValue());
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), val);
            } else if (currentAtt.getNodeName().equals("Name"))
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), currentAtt.getNodeValue());
            // new code
            else if (currentAtt.getNodeName().equals("RowClass")) {
                String val = currentAtt.getNodeValue();
                if (val.equals("oracle.apps.fnd.framework.server.OAViewRowImpl")) {
                    // for entity VO
                    val = "oracle.jbo.server.ViewRowImpl";
                } else {
                    String temp = currentAtt.getNodeValue();
                    temp = temp.replace(".", FileReaderWritter.getSeparator());
                    RowClassPath += temp;
                    val = DirCreator.changedModelClassPath(currentAtt.getNodeValue());
                }
                ViewObjectAdf.setAttribute(currentAtt.getNodeName(), val);
            }
            // end of new code
        }

        // query and attributes
        NodeList nodesOaf = ViewObjectOAf.getChildNodes();
        for (int i = 0; i < nodesOaf.getLength(); i++) {
            Node currentNode = nodesOaf.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("SQLQuery")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    ViewObjectAdf.appendChild(newNode);
                } else if (currentNode.getNodeName().equals("ViewAttribute")) {
                    Node newNode = adfDoc.importNode(currentNode, true);
                    NamedNodeMap newAttrs = newNode.getAttributes();
                    for (int j = 0; j < newAttrs.getLength(); j++) {
                        Node subNode = newAttrs.item(i);
                        if (null != subNode && subNode.getNodeType() == Node.ELEMENT_NODE)
                            newNode.removeChild(subNode); // DesignTime
                    }
                    ViewObjectAdf.appendChild(newNode);
                } else if (currentNode.getNodeName().equals("EntityUsage")) {
                    Node newNode = adfDoc.importNode(currentNode, true);

                    Element x = (Element) newNode;
                    NamedNodeMap attrEnt = x.getAttributes();
                    for (int j = 0; j < attrEnt.getLength(); j++) {
                        Attr currentAtt = (Attr) attrEnt.item(j);
                        String strAttr = currentAtt.getName();
                        if (strAttr.equals("Entity")) {
                            String val = currentAtt.getValue();
                            val = DirCreator.changedModelClassPath(val);
                            currentAtt.setValue(val);
                        }
                    }
                    NodeList designList = newNode.getChildNodes();
                    for (int l = 0; l < designList.getLength(); l++) {
                        Node design = designList.item(l);
                        if (design.getNodeType() == Node.ELEMENT_NODE && design.getNodeName().equals("DesignTime")) {
                            newNode.removeChild(design); // remove design
                        }
                    }


                    ViewObjectAdf.appendChild(newNode);
                }
            }
        }

        //handle vorow
        if (!(src + FileReaderWritter.getSeparator()).equals(RowClassPath))
            VOImpl.handleVORowImpl(RowClassPath + ".java", app, dest, src);
        // handle voimpl
        if (!(src + FileReaderWritter.getSeparator()).equals(ImplClassPath))
            VOImpl.handleVOImpl(ImplClassPath + ".java", app, dest, src);
        //handle vodef
        if (!(src + FileReaderWritter.getSeparator()).equals(DefClassPath))
            VOImpl.handleVODef(DefClassPath + ".java", app, dest, src);
        //handle vowor interface
        if (!(src + FileReaderWritter.getSeparator()).equals(RowInterface))
            VOImpl.handleVORowInterface(RowInterface + ".java", app, dest, src);
        //handle client
        if (!(src + FileReaderWritter.getSeparator()).equals(Client))
            VOImpl.handleVOClient(Client + ".java", app, dest, src);

        // label bundle properties
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

        LABEL.setAttribute("ResId", impVal + voName + "_LABEL");
        String contents = impVal + voName + "_LABEL=" + voName;
        FileReaderWritter.appendFile(contents,
                                     pathModelsrc + FileReaderWritter.getSeparator() + "ModelBundle.properties");
        SchemaBasedProperties.appendChild(LABEL);
        Properties.appendChild(SchemaBasedProperties);
        ViewObjectAdf.appendChild(Properties);

        // jpx
        JPXGen.checkContainee(impVal.substring(0, impVal.lastIndexOf(".")), pathModelsrc);

        // write files
        FileReaderWritter.writeXMLFile(adfDoc, destination);

        System.out.println("End Conv: handleVOXml ");
    }

    public static void addLovDetails(String voPath, String bindingVO, String bindingAttr, String lovVO, String lovAttr) {
        try {
            File adfVO = new File(voPath);
            DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
            newDbFactory.setValidating(false);
            DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
            Document adfDoc = newDBuilder.parse(adfVO);
            Element ViewObjectAdf = adfDoc.getDocumentElement();
            Element viewAccessor = adfDoc.createElement("ViewAccessor");
            ViewObjectAdf.appendChild(viewAccessor);

            Attr name = adfDoc.createAttribute("Name");
            name.setValue(lovVO);
            name.normalize();
            viewAccessor.setAttributeNode(name);

            Attr viewObjectName = adfDoc.createAttribute("ViewObjectName");
            viewObjectName.setValue(lovVO);
            viewObjectName.normalize();
            viewAccessor.setAttributeNode(viewObjectName);

            Attr rowLevelBinds = adfDoc.createAttribute("RowLevelBinds");
            rowLevelBinds.setValue("true");
            rowLevelBinds.normalize();
            viewAccessor.setAttributeNode(rowLevelBinds);

            Element listBinding = adfDoc.createElement("ListBinding");
            ViewObjectAdf.appendChild(listBinding);

            Attr listName = adfDoc.createAttribute("Name");
            listName.setValue("LOV_" + bindingAttr);
            listName.normalize();
            listBinding.setAttributeNode(listName);

            Attr listVOName = adfDoc.createAttribute("ListVOName");
            listVOName.setValue(lovVO);
            listVOName.normalize();
            listBinding.setAttributeNode(listVOName);

            Attr listRangeSize = adfDoc.createAttribute("ListRangeSize");
            listRangeSize.setValue("-1");
            listRangeSize.normalize();
            listBinding.setAttributeNode(listRangeSize);

            Attr nullValueFlag = adfDoc.createAttribute("NullValueFlag");
            nullValueFlag.setValue("start");
            nullValueFlag.normalize();
            listBinding.setAttributeNode(nullValueFlag);

            Attr mruCount = adfDoc.createAttribute("MRUCount");
            mruCount.setValue("0");
            mruCount.normalize();
            listBinding.setAttributeNode(mruCount);

            Element attrArray = adfDoc.createElement("AttrArray");
            listBinding.appendChild(attrArray);

            Attr attrArrayName = adfDoc.createAttribute("Name");
            attrArrayName.setValue("AttrNames");
            attrArrayName.normalize();
            attrArray.setAttributeNode(attrArrayName);

            Element item = adfDoc.createElement("Item");
            attrArray.appendChild(item);

            Attr itemValue = adfDoc.createAttribute("Value");
            itemValue.setValue(bindingAttr);
            itemValue.normalize();
            item.setAttributeNode(itemValue);

            Element attrArray1 = adfDoc.createElement("AttrArray");
            listBinding.appendChild(attrArray1);

            Attr attrExpressions = adfDoc.createAttribute("Name");
            attrExpressions.setValue("AttrExpressions");
            attrExpressions.normalize();
            attrArray1.setAttributeNode(attrExpressions);

            Element attrArray2 = adfDoc.createElement("AttrArray");
            listBinding.appendChild(attrArray2);

            Attr listAttrNames = adfDoc.createAttribute("Name");
            listAttrNames.setValue("ListAttrNames");
            listAttrNames.normalize();
            attrArray2.setAttributeNode(listAttrNames);

            Element item1 = adfDoc.createElement("Item");
            attrArray2.appendChild(item1);

            Attr itemValue1 = adfDoc.createAttribute("Value");
            itemValue1.setValue(lovAttr);
            itemValue1.normalize();
            item1.setAttributeNode(itemValue1);

            Element attrArray3 = adfDoc.createElement("AttrArray");
            listBinding.appendChild(attrArray3);

            Attr listDisplayAttrNames = adfDoc.createAttribute("Name");
            listDisplayAttrNames.setValue("ListDisplayAttrNames");
            listDisplayAttrNames.normalize();
            attrArray3.setAttributeNode(listDisplayAttrNames);

            Element item2 = adfDoc.createElement("Item");
            attrArray3.appendChild(item2);

            Attr itemValue2 = adfDoc.createAttribute("Value");
            itemValue2.setValue(lovAttr);
            itemValue2.normalize();
            item2.setAttributeNode(itemValue2);

            Element displayCriteria = adfDoc.createElement("DisplayCriteria");
            listBinding.appendChild(displayCriteria);

            // String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

            FileReaderWritter.writeXMLFile(adfDoc, voPath);

        } catch (IOException ioe) {
            // TODO: Add catch code
            ioe.printStackTrace();
        } catch (SAXException saxe) {
            // TODO: Add catch code
            saxe.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // TODO: Add catch code
            pce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * generate initial file
     * @param dest
     * @param repo
     * @param src
     * @throws Exception
     */
    private static void createVOXml(String dest, String repo) throws Exception {
        System.out.println("Start Conv: createVOXml " + dest + " " + repo);
        String contents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + "<!DOCTYPE ViewObject SYSTEM \"jbo_03_01.dtd\">" + "" +
            "<ViewObject" + "  xmlns=\"http://xmlns.oracle.com/bc4j\"" + "  Version=\"12.1.3.10.47\"" +
            "  InheritPersonalization=\"merge\"" + "  BindingStyle=\"OracleName\"" + "  CustomQuery=\"true\"" +
            "  PageIterMode=\"Full\">" + "  " + "  <DesignTime>" +
            "    <Attr Name=\"_codeGenFlag2\" Value=\"Access|Def|Coll|Prog|VarAccess\"/>" +
            "    <Attr Name=\"_isExpertMode\" Value=\"true\"/>" + "    <Attr Name=\"_isCodegen\" Value=\"true\"/>" +
            "  </DesignTime>" + "  <ResourceBundle>\n" + "    <PropertiesBundle\n" +
            "      PropertiesFile=\"model.ModelBundle\"/>\n" + "  </ResourceBundle>\n" + "</ViewObject>";

        DirCreator.copyADFDTD(repo, dest);
        FileReaderWritter.writeFile(contents, dest);
        System.out.println("End Conv: createVOXml ");
    }
}
