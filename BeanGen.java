package conv;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class BeanGen {
    public BeanGen() {
        super();
    }

    protected static void createBean(String pgName, String pathVC) throws Exception {
        System.out.println("Start Conv: createBean " + pgName + " " + pathVC);
        String bean =
            "package view.backing;" + "" + "import oracle.adf.view.rich.component.rich.RichDocument;" +
            "import oracle.adf.view.rich.component.rich.RichForm;" +
            "import oracle.adf.view.rich.component.rich.layout.RichPanelHeader;" +
            "import oracle.adf.view.rich.component.rich.output.RichSeparator;" + "public class " + pgName + "Bean {" +
            "private RichForm f1;" + "    private RichDocument d1;" + "    private RichPanelHeader ph1;" +
            "    private RichSeparator s1;" + "" + "    public void setF1(RichForm f1) {" + "        this.f1 = f1;" +
            "    }" + "" + "    public RichForm getF1() {" + "        return f1;" + "    }" + "" +
            "    public void setD1(RichDocument d1) {" + "        this.d1 = d1;" + "    }" + "" +
            "    public RichDocument getD1() {" + "        return d1;" + "    }" + "" +
            "    public void setPh1(RichPanelHeader ph1) {" + "        this.ph1 = ph1;" + "    }" + "" +
            "    public RichPanelHeader getPh1() {" + "        return ph1;" + "    }" + "" +
            "    public void setS1(RichSeparator s1) {" + "        this.s1 = s1;" + "    }" + "" +
            "    public RichSeparator getS1() {" + "        return s1;" + "    }" + "}";

        FileReaderWritter.writeFile(bean, pathVC + "\\src\\view\\backing\\" + pgName + "Bean.java");
        System.out.println("End Conv: createBean ");
    }

    protected static void createGetterSetter(String itemName, String itemType, String path,
                                             String imports) throws Exception {
        System.out.println("Start Conv: createGetterSetter " + itemName + " " + itemType + " " + path + " " + imports);
        try {
            String contents = FileReaderWritter.getCharContents(path);
            int importLoc = contents.indexOf(";") + 1;

            String getter = " public " + itemType + " get" + "" + itemName + "() {" + " return " + itemName + "; } ";
            String setter =
                " public void set" + "" + itemName + "(" + itemType + " " + itemName + ") {" + " this." + itemName +
                "= " + itemName + "; } ";
            String declare = "private " + itemType + " " + itemName + "; ";
            String temp = "";
            temp = contents.substring(0, importLoc) + imports + contents.substring(importLoc);
            contents = temp;

            int lastBrace = contents.lastIndexOf("}");
            temp = "";
            temp = contents.substring(0, lastBrace) + getter + setter + contents.substring(lastBrace);
            contents = temp;

            int firstBrace = contents.indexOf("{") + 1;
            temp = "";
            temp = contents.substring(0, firstBrace) + declare + contents.substring(firstBrace);
            contents = temp;
            temp = "";

            FileReaderWritter.writeFile(contents, path);
            System.out.println("End Conv: createGetterSetter ");
        } catch (Exception e) {
            throw e;
        }
    }

    private static void createActionListener(String contents, String path) {

    }

    private static void createDataBindings() {

    }

    protected static void createAdfConfig(String pathVC, String pgName) throws Exception {
        System.out.println("Start Conv: createAdfConfig " + pathVC + " " + pgName);
        try {
            File inputFile = new File(pathVC + "\\public_html\\WEB-INF\\adfc-config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            Element root = doc.getDocumentElement();

            Element bean = doc.createElement("managed-bean");
            bean.setAttribute("id", pgName + "Bean");
            Element beanName = doc.createElement("managed-bean-name");
            beanName.setTextContent(pgName + "Bean");
            Element beanClass = doc.createElement("managed-bean-class");
            beanClass.setTextContent("view.backing." + pgName + "Bean");
            Element beanScope = doc.createElement("managed-bean-scope");
            beanScope.setTextContent("backingBean");

            root.appendChild(bean);
            bean.appendChild(beanName);
            bean.appendChild(beanClass);
            bean.appendChild(beanScope);

            FileReaderWritter.writeXMLFile(doc, pathVC + "\\public_html\\WEB-INF\\adfc-config.xml");
            System.out.println("End Conv: createAdfConfig ");
        } catch (Exception e) {
            throw e;
        }
    }


}
