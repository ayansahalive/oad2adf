package conv;

import java.io.*;
import java.io.IOException;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class BeanGen {
    public BeanGen() {
        super();
    }

    protected static void createBean(String pgName, String pathVC) throws Exception {
        System.out.println("Start Conv: createBean " + pgName + " " + pathVC);
        String bean =
            "package view.backing;\n" + "" + "import oracle.adf.view.rich.component.rich.RichDocument;\n" +
            "import oracle.adf.view.rich.component.rich.RichForm;\n" +
            "import oracle.adf.view.rich.component.rich.layout.RichPanelHeader;\n" +
            "import oracle.adf.view.rich.component.rich.output.RichSeparator;\n" + "public class " + pgName + "Bean {\n" +
            "private RichForm f1;\n" + "    private RichDocument d1;\n" + "    private RichPanelHeader ph1;\n" +
            "    private RichSeparator s1;\n\n" + "" + "    public void setF1(RichForm f1) {\n" + "        this.f1 = f1;\n" +
            "    }\n\n" + "" + "    public RichForm getF1() {\n" + "        return f1;\n" + "    }\n\n" + "" +
            "    public void setD1(RichDocument d1) {\n" + "        this.d1 = d1;\n" + "    }\n\n" + "" +
            "    public RichDocument getD1() {\n" + "        return d1;\n" + "    }\n\n" + "" +
            "    public void setPh1(RichPanelHeader ph1) {\n" + "        this.ph1 = ph1;\n" + "    }\n\n" + "" +
            "    public RichPanelHeader getPh1() {\n" + "        return ph1;\n" + "    }\n\n" + "" +
            "    public void setS1(RichSeparator s1) {\n" + "        this.s1 = s1;\n" + "    }\n\n" + "" +
            "    public RichSeparator getS1() {\n" + "        return s1;\n" + "    }\n" + "}";

        FileReaderWritter.writeFile(bean, pathVC + "\\src\\view\\backing\\" + pgName + "Bean.java");
        System.out.println("End Conv: createBean ");
    }

    protected static void createGetterSetter(String itemName, String itemType, String path,
                                             String imports) throws Exception {
        System.out.println("Start Conv: createGetterSetter " + itemName + " " + itemType + " " + path + " " + imports);
        try {
            String contents = FileReaderWritter.getCharContents(path);
            int importLoc = contents.indexOf(";") + 1;

            String getter = " \npublic " + itemType + " get" + "" + itemName + "() {\n" + " return " + itemName + ";\n }\n\n";
            String setter =
                " \npublic void set" + "" + itemName + "(" + itemType + " " + itemName + ") {\n" + " this." + itemName +
                "= " + itemName + ";\n }\n\n ";
            String declare = "\nprivate " + itemType + " " + itemName + ";";
            String temp = "";
            temp = contents.substring(0, importLoc)+"\n" + imports + contents.substring(importLoc);
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

    protected static void createActionListener(String content, String path) throws Exception {
        String contents = FileReaderWritter.getCharContents(path);
        int importLoc = contents.indexOf(";") + 1;

        String alContent = "public void " + content + "(ActionEvent actionEvent) {\n }\n";
        String temp = "";
        temp =
            contents.substring(0, importLoc) + "\nimport javax.faces.event.ActionEvent;" + contents.substring(importLoc);
        contents = temp;

        int lastBrace = contents.lastIndexOf("}");
        temp = "";
        temp = contents.substring(0, lastBrace) + alContent + contents.substring(lastBrace);
        contents = temp;
        FileReaderWritter.writeFile(contents, path);
        System.out.println("End Conv: createActionListener ");
    }
    
    protected static void createValueChangeListener(String content, String path) throws Exception {
        String contents = FileReaderWritter.getCharContents(path);
        int importLoc = contents.indexOf(";") + 1;

        String alContent = "public void " + content + "(ValueChangeEvent valueChangeEvent) {\n }\n";
        String temp = "";
        temp =
            contents.substring(0, importLoc) + "\nimport javax.faces.event.ValueChangeEvent;" + contents.substring(importLoc);
        contents = temp;

        int lastBrace = contents.lastIndexOf("}");
        temp = "";
        temp = contents.substring(0, lastBrace) + alContent + contents.substring(lastBrace);
        contents = temp;
        FileReaderWritter.writeFile(contents, path);
        System.out.println("End Conv: createActionListener ");
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

    protected static void copyProcessFormRequest(String path, String app, String dest, String src) {
        try {
            //System.out.println("Start Conv: copyProcessFormRequest " + path + " " + app + " " + dest + " " + src);
            ErrorAndLog.handleLog(app, "converting " + path);
            String coPath = path.replace("PG.xml", "CO.java");
            String pageName = path.substring(path.lastIndexOf("\\"));
            pageName = pageName.replace(".xml", "");
            String str = "";
            str = FileReaderWritter.getCharContents(coPath);

            str = str.subSequence(str.indexOf("{") + 1, str.lastIndexOf("}")).toString();

            BufferedReader reader;
            String ret = "";
            reader = new BufferedReader(new StringReader(str));
            String line = reader.readLine();
            while (line != null) {
                ret = ret + line + "\n";
                line = reader.readLine();
                if (line != null && line.contains("pageContext.getParameter(\"")) {
                    String[] s = line.split("pageContext.getParameter");
                    String tempStr = s[1].substring(2, s[1].indexOf("\"", 2));
                    line = s[0] + "get" + tempStr + "().getValue()" + s[1].substring(s[1].indexOf(")") + 1);
                } else if (line != null && line.contains("pageContext.putParameter(\"")) {
                    String[] s = line.split("pageContext.putParameter");
                    String tempStr = s[1].substring(2, s[1].indexOf("\"", 2));
                    line = s[0] + "get" + tempStr + "().setValue(" + s[1].substring(s[1].indexOf(",") + 1);
                } else if (line != null && line.contains("forwardImmediatelyToCurrentPage(")) {
                    line = "//" + line;
                    System.out.println(line.lastIndexOf(");"));
                    if (line.lastIndexOf(");") == -1) {
                        ret = ret + line + "\n";
                        line = reader.readLine();
                        line = "//" + line;
                    }
                }
            }
            System.out.println(ret);
            if (reader != null) {
                reader.close();
            }
            String jsfBeanPath =
                dest + "\\" + app + "\\ViewCOntroller" + "\\src\\view\\backing\\" + pageName + "Bean.java";
            String jsfStr = FileReaderWritter.getCharContents(jsfBeanPath);
            jsfStr = jsfStr.subSequence(0, jsfStr.lastIndexOf("}")).toString();
            jsfStr = jsfStr + ret + "}";
            System.out.println("jsfBean Path::::" + jsfBeanPath);
            FileReaderWritter.writeFile(jsfStr, jsfBeanPath);
        } catch (IOException ioe) {
            // TODO: Add catch code
            ioe.printStackTrace();
        } catch (Exception e) {
            // TODO: Add catch code
            e.printStackTrace();
        }
    }

}
