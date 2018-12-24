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


public class JPXGen {
    public JPXGen() {
        super();
    }

    /**
     * check if the containee for a folder is present int he XML
     * @param pkg
     * @param pathModelsrc
     * @throws Exception
     */
    protected static void checkContainee(String pkg, String pathModelsrc, String app) throws Exception {
        System.out.println("Start Conv: checkContainee " + pkg + " " + pathModelsrc + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: checkContainee " + pkg + " " + pathModelsrc + " " + app);
        int count = countContainee(pkg, pathModelsrc, app);
        if (count == 0)
            addContainee(pkg, pathModelsrc, pkg.substring(pkg.lastIndexOf(".")), app);
        System.out.println("End Conv: checkContainee");
    }

    /**
     * add containee element per folder
     * @param pkg
     * @param pathModelsrc
     * @param name
     * @throws Exception
     */
    private static void addContainee(String pkg, String pathModelsrc, String name, String app) throws Exception {
        System.out.println("Start Conv: addContainee " + pkg + " " + pathModelsrc + " " + name + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: addContainee " + pkg + " " + pathModelsrc + " " + name + " " + app);
        File jpx = new File(pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx");
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(false);
        DocumentBuilder db = fact.newDocumentBuilder();
        Document jpxDoc = db.parse(jpx);
        Element JboProject = jpxDoc.getDocumentElement();

        Element Containee = jpxDoc.createElement("Containee");
        Containee.setAttribute("Name", name);
        Containee.setAttribute("PackageName", pkg);
        Containee.setAttribute("ObjectType", "JboPackage");
        Element DesignTime = jpxDoc.createElement("DesignTime");
        Element attrAM = jpxDoc.createElement("Attr");
        attrAM.setAttribute("Name", "_AM");
        attrAM.setAttribute("Value", "true");
        Element attrVO = jpxDoc.createElement("Attr");
        attrVO.setAttribute("Name", "_VO");
        attrVO.setAttribute("Value", "true");
        Element attrVL = jpxDoc.createElement("Attr");
        attrVL.setAttribute("Name", "_VL");
        attrVL.setAttribute("Value", "true");
        Element attrEO = jpxDoc.createElement("Attr");
        attrEO.setAttribute("Name", "_EO");
        attrEO.setAttribute("Value", "true");
        Element attrAS = jpxDoc.createElement("Attr");
        attrAS.setAttribute("Name", "_AS");
        attrAS.setAttribute("Value", "true");

        DesignTime.appendChild(attrAM);
        DesignTime.appendChild(attrVO);
        DesignTime.appendChild(attrVL);
        DesignTime.appendChild(attrEO);
        DesignTime.appendChild(attrAS);
        Containee.appendChild(DesignTime);
        JboProject.appendChild(Containee);

        FileReaderWritter.writeXMLFile(jpxDoc, pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx", app);
        System.out.println("End Conv: addContainee");
    }

    /**
     * get the count of containee of a folder
     * @param pkg
     * @param pathModelsrc
     * @return
     * @throws Exception
     */
    private static int countContainee(String pkg, String pathModelsrc, String app) throws Exception {
        System.out.println("Start Conv: countContainee " + pkg + " " + pathModelsrc + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: countContainee " + pkg + " " + pathModelsrc + " " + app);
        int count = 0;
        File jpx = new File(pathModelsrc + FileReaderWritter.getSeparator() + "Model.jpx");
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating(false);
        DocumentBuilder db = fact.newDocumentBuilder();
        Document jpxDoc = db.parse(jpx);
        NodeList ContaineeList = jpxDoc.getElementsByTagName("Containee");
        if (null != ContaineeList) {
            for (int i = 0; i < ContaineeList.getLength(); i++) {
                Node eachContainee = ContaineeList.item(i);
                NamedNodeMap attrList = eachContainee.getAttributes();
                for (int j = 0; j < attrList.getLength(); j++) {
                    Attr eachAttr = (Attr) attrList.item(j);
                    if (eachAttr.getNodeType() == Node.ATTRIBUTE_NODE && eachAttr.getNodeName().equals("PackageName")) {
                        String val = eachAttr.getNodeValue();
                        if (pkg.equals(val))
                            count++;
                    }
                }
            }
        }
        System.out.println("End Conv: countContainee");
        return count;
    }
}
