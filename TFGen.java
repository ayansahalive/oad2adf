package conv;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TFGen {
    public TFGen() {
        super();
    }

    protected static void createBTF(String app, String destination) throws Exception {
        System.out.println("Start Conv: createBTF " + app + " " + destination);
        ErrorAndLog.handleLog(app, "Start Conv: createBTF " + app + " " + destination);

        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        Document jsfDoc = newDBuilder.newDocument();

        Element config = jsfDoc.createElement("adfc-config");
        config.setAttribute("xmlns", "http://xmlns.oracle.com/adf/controller");
        config.setAttribute("version", "1.2");
        jsfDoc.appendChild(config);

        Element def = jsfDoc.createElement("task-flow-definition");
        def.setAttribute("id", "BTF" + app);
        config.appendChild(def);

        Element act = jsfDoc.createElement("default-activity");
        act.setTextContent("taskFlowReturn1" + app);
        def.appendChild(act);

        Element dcs = jsfDoc.createElement("data-control-scope");
        Element dcsChild = jsfDoc.createElement("shared");
        dcs.appendChild(dcsChild);
        def.appendChild(dcs);

        String beanDest =
            destination.substring(0, destination.indexOf("ViewController") + 14) + FileReaderWritter.getSeparator() +
            "src" + FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() + "backing" +
            FileReaderWritter.getSeparator();
        createBTFBean(app, beanDest);

        Element mbean = jsfDoc.createElement("managed-bean");
        mbean.setAttribute("id", "BTF" + app + "Bean");
        Element mbeanChild1 = jsfDoc.createElement("managed-bean-name");
        mbeanChild1.setTextContent("BTF" + app + "Bean");
        Element mbeanChild2 = jsfDoc.createElement("managed-bean-class");
        mbeanChild2.setTextContent("view.backing.BTF" + app + "Bean");
        Element mbeanChild3 = jsfDoc.createElement("managed-bean-scope");
        mbeanChild3.setTextContent("pageFlow");
        mbean.appendChild(mbeanChild1);
        mbean.appendChild(mbeanChild2);
        mbean.appendChild(mbeanChild3);
        def.appendChild(mbean);

        Element ret = jsfDoc.createElement("task-flow-return");
        ret.setAttribute("id", "taskFlowReturn1" + app);
        Element retChild1 = jsfDoc.createElement("outcome");
        Element retChild2 = jsfDoc.createElement("name");
        retChild2.setTextContent("taskFlowReturn1" + app);
        retChild1.appendChild(retChild2);
        ret.appendChild(retChild1);
        def.appendChild(ret);

        Element jsff = jsfDoc.createElement("use-page-fragments");
        def.appendChild(jsff);

        FileReaderWritter.writeXMLFile(jsfDoc, destination, app);

        System.out.println("End Conv: createBTF ");
    }

    private static void createBTFBean(String app, String destination) throws Exception {
        System.out.println("Start Conv: createBTFBean " + app + " " + destination);
        ErrorAndLog.handleLog(app, "Start Conv: createBTFBean " + app + " " + destination);

        String contents =
            "package view.backing; " + "public class BTF" + app + "Bean {" + "    public BTF" + app + "Bean() {" +
            "        super();" + "    }" + "}";

        FileReaderWritter.writeFile(contents, destination + "BTF" + app + "Bean.java", app);
        System.out.println("End Conv: createBTFBean ");
    }
}
