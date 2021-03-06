package conv;

import java.io.File;

import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataBindingsXml {
    public DataBindingsXml() {
        super();
    }

    /**
     * databindings XML handle
     * @param dest
     * @param app
     * @param pgName
     * @param filePath
     * @param amDef
     * @param packagePath
     * @throws Exception
     */
    public static void handleDataBindingsPage(String dest, String app, String pgName, String filePath, String amDef,
                                              String packagePath) throws Exception {
        System.out.println("Start Conv: handleDataBindingsPage " + dest + " " + app + " " + pgName + " " + filePath +
                           " " + amDef + " " + packagePath);
        ErrorAndLog.handleLog(app,
                              "Start Conv: handleDataBindingsPage " + dest + " " + app + " " + pgName + " " + filePath +
                              " " + amDef + " " + packagePath);
        String dbPage =
            dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "ViewController" +
            FileReaderWritter.getSeparator() + "adfmsrc" + FileReaderWritter.getSeparator() + "view" +
            FileReaderWritter.getSeparator() + "DataBindings.cpx";
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document dbPageDoc = newDBuilder.parse(new File(dbPage));
        Element rootElement = dbPageDoc.getDocumentElement();
        NodeList nodeList = rootElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null && node.getNodeName().equals("pageMap")) {
                Element pageDtls = dbPageDoc.createElement("page");
                node.appendChild(pageDtls);

                Attr binds = dbPageDoc.createAttribute("path");
                binds.setValue("/view/" + packagePath.replace(FileReaderWritter.getSeparator(), "/") + "/" + pgName +
                               ".jsf");
                binds.normalize();
                pageDtls.setAttributeNode(binds);

                Attr usageId = dbPageDoc.createAttribute("usageId");
                usageId.setValue("view_" + pgName + "PageDef");
                usageId.normalize();
                pageDtls.setAttributeNode(usageId);
            } else if (node != null && node.getNodeName().equals("pageDefinitionUsages")) {
                Element pageDtls = dbPageDoc.createElement("page");
                node.appendChild(pageDtls);

                Attr binds = dbPageDoc.createAttribute("path");
                StringBuilder pathVal = new StringBuilder();
                filePath = filePath.substring(filePath.indexOf("src") + 4);
                StringTokenizer strToken = new StringTokenizer(filePath, FileReaderWritter.getSeparator());
                while (strToken.hasMoreTokens()) {
                    String token = strToken.nextToken();
                    if (!token.contains(".xml")) {
                        pathVal.append(token);
                        pathVal.append(".");
                    }
                }
                pathVal.append(pgName + "PageDef");
                binds.setValue(pathVal.toString());
                binds.normalize();
                pageDtls.setAttributeNode(binds);

                Attr id = dbPageDoc.createAttribute("id");
                id.setValue("view_" + pgName + "PageDef");
                id.normalize();
                pageDtls.setAttributeNode(id);
            } else if (node != null && node.getNodeName().equals("dataControlUsages")) {
                boolean valueExists = false;
                NodeList dataControlUsgaesList = node.getChildNodes();
                for (int j = 0; j < dataControlUsgaesList.getLength(); j++) {
                    Node childNode = dataControlUsgaesList.item(j);
                    if (childNode.getNodeName().equals("BC4JDataControl")) {
                        NamedNodeMap attrNodes = childNode.getAttributes();
                        for (int k = 0; k < attrNodes.getLength(); k++) {
                            Node attrNode = attrNodes.item(k);
                            String itemNode = attrNode.getNodeName();
                            if (itemNode.equals("Configuration")) {
                                String value = attrNode.getNodeValue();
                                if (value.equals(amDef + "Local")) {
                                    valueExists = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (valueExists) {
                        break;
                    }
                }

                if (!valueExists) {
                    Element bc4jdc = dbPageDoc.createElement("BC4JDataControl");
                    node.appendChild(bc4jdc);

                    Attr id = dbPageDoc.createAttribute("id");
                    id.setValue(amDef + "DataControl");
                    id.normalize();
                    bc4jdc.setAttributeNode(id);

                    Attr packageVal = dbPageDoc.createAttribute("Package");
                    packageVal.setValue("Model");
                    packageVal.normalize();
                    bc4jdc.setAttributeNode(packageVal);

                    Attr factoryClass = dbPageDoc.createAttribute("FactoryClass");
                    factoryClass.setValue("oracle.adf.model.bc4j.DataControlFactoryImpl");
                    factoryClass.normalize();
                    bc4jdc.setAttributeNode(factoryClass);

                    Attr supportsTransactions = dbPageDoc.createAttribute("SupportsTransactions");
                    supportsTransactions.setValue("true");
                    supportsTransactions.normalize();
                    bc4jdc.setAttributeNode(supportsTransactions);

                    Attr supportsFindMode = dbPageDoc.createAttribute("SupportsFindMode");
                    supportsFindMode.setValue("true");
                    supportsFindMode.normalize();
                    bc4jdc.setAttributeNode(supportsFindMode);

                    Attr supportsRangesize = dbPageDoc.createAttribute("SupportsRangesize");
                    supportsRangesize.setValue("true");
                    supportsRangesize.normalize();
                    bc4jdc.setAttributeNode(supportsRangesize);

                    Attr supportsResetState = dbPageDoc.createAttribute("SupportsResetState");
                    supportsResetState.setValue("true");
                    supportsResetState.normalize();
                    bc4jdc.setAttributeNode(supportsResetState);

                    Attr supportsSortCollection = dbPageDoc.createAttribute("SupportsSortCollection");
                    supportsSortCollection.setValue("true");
                    supportsSortCollection.normalize();
                    bc4jdc.setAttributeNode(supportsSortCollection);

                    Attr configuration = dbPageDoc.createAttribute("Configuration");
                    configuration.setValue(amDef + "Local");
                    configuration.normalize();
                    bc4jdc.setAttributeNode(configuration);

                    Attr syncMode = dbPageDoc.createAttribute("syncMode");
                    syncMode.setValue("Immediate");
                    syncMode.normalize();
                    bc4jdc.setAttributeNode(syncMode);

                    Attr xmlns = dbPageDoc.createAttribute("xmlns");
                    xmlns.setValue("http://xmlns.oracle.com/adfm/datacontrol");
                    xmlns.normalize();
                    bc4jdc.setAttributeNode(xmlns);
                }
            }
        }
        FileReaderWritter.writeXMLFile(dbPageDoc, dbPage, app);
        System.out.println("End Conv: handleDataBindingsPage");
    }
}
