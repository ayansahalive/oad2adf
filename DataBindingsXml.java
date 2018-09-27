package conv;

import java.io.File;
import java.io.IOException;

import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class DataBindingsXml {
    public DataBindingsXml() {
        super();
    }

    public static void handleDataBindingsPage(String dest, String app, String pgName, String filePath, String amDef) {
        try {
            String dbPage = dest + "\\" + app + "\\ViewController\\adfmsrc\\view\\DataBindings.cpx";
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
                    binds.setValue("/" + pgName + ".jsf");
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
                    StringTokenizer strToken = new StringTokenizer(filePath, "\\");
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
                } else if(node != null && node.getNodeName().equals("dataControlUsages")) {
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
            FileReaderWritter.writeXMLFile(dbPageDoc, dbPage);
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

    //    public static void main(String[] args) {
    //        File file = new File("D:\\Converter\\ADF\\hello\\ViewController\\src\\view\\pageDefs\\helloPGPageDef.xml");
    //        System.out.println(file.getAbsolutePath());
    //        String filePath = file.getAbsolutePath();
    //        filePath = filePath.substring(filePath.indexOf("src")+4);
    //        StringTokenizer strToken = new StringTokenizer(filePath, "\\");
    //        while(strToken.hasMoreTokens()) {
    //            String token = strToken.nextToken();
    //            System.out.println(token);
    //        }
    //
    //   }
}
