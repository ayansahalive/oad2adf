package conv;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class BeanGen {
    public BeanGen() {
        super();
    }

    /**
     * Temp class
     * @param pgName
     * @param pathVC
     * @param app
     * @throws Exception
     */
    protected static void createBean(String jsfBeanName, String beanPath, String app) throws Exception {
        System.out.println("Start Conv: createBean " + jsfBeanName + " " + beanPath + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createBean " + jsfBeanName + " " + beanPath + " " + app);
        String className = jsfBeanName.substring(jsfBeanName.lastIndexOf(".")+1);
        String bean =
            "package view.backing;" +
            "import view.util.*;"+
            "public class " + className +
            "{}";

        FileReaderWritter.writeFile(bean, beanPath, app);
        
        System.out.println("End Conv: createBean ");
    }

    /**
     * getter and setters of beans
     * @param itemName
     * @param itemType
     * @param path
     * @param imports
     * @param app
     * @throws Exception
     */
    protected static void createGetterSetter(String itemName, String itemType, String path, String imports,
                                             String app) throws Exception {
        System.out.println("Start Conv: createGetterSetter " + itemName + " " + itemType + " " + path + " " + imports +
                           " " + app);
        ErrorAndLog.handleLog(app,
                              "Start Conv: createGetterSetter " + itemName + " " + itemType + " " + path + " " +
                              imports + " " + app);
        String contents = FileReaderWritter.getCharContents(path, app);
        int importLoc = contents.indexOf(";") + 1;

        String getter =
            " \npublic " + itemType + " get" + "" + itemName + "() {\n" + " return " + itemName + ";\n }\n\n";
        String setter =
            " \npublic void set" + "" + itemName + "(" + itemType + " " + itemName + ") {\n" + " this." + itemName +
            "= " + itemName + ";\n }\n\n ";
        String declare = "\nprivate " + itemType + " " + itemName + ";";
        String temp = "";
        if (!contents.contains(imports)) {
            temp = contents.substring(0, importLoc) + "\n" + imports + contents.substring(importLoc);
            contents = temp;
        }

        int lastBrace = contents.lastIndexOf("}");
        temp = "";
        temp = contents.substring(0, lastBrace) + getter + setter + contents.substring(lastBrace);
        contents = temp;

        int firstBrace = contents.indexOf("{") + 1;
        temp = "";
        temp = contents.substring(0, firstBrace) + declare + contents.substring(firstBrace);
        contents = temp;
        temp = "";

        FileReaderWritter.writeFile(contents, path, app);
        System.out.println("End Conv: createGetterSetter ");
    }

    /**
     * Action listener of event
     * @param content
     * @param path
     * @param app
     * @throws Exception
     */
    protected static void createActionListener(String content, String path, String app) throws Exception {
        System.out.println("Start Conv: createActionListener " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createActionListener " + path + " " + app);
        String contents = FileReaderWritter.getCharContents(path, app);
        int importLoc = contents.indexOf(";") + 1;

        String alContent = "public void " + content + "(ActionEvent actionEvent) {\n }\n";
        String temp = "";
        if (!contents.contains("import javax.faces.event.ActionEvent;")) {
            temp =
                contents.substring(0, importLoc) + "\nimport javax.faces.event.ActionEvent;" +
                contents.substring(importLoc);
            contents = temp;
        }
        int lastBrace = contents.lastIndexOf("}");
        temp = "";
        temp = contents.substring(0, lastBrace) + alContent + contents.substring(lastBrace);
        contents = temp;
        FileReaderWritter.writeFile(contents, path, app);
        System.out.println("End Conv: createActionListener ");
    }

    /**
     * Value change listener of events
     * @param content
     * @param path
     * @param app
     * @throws Exception
     */
    protected static void createValueChangeListener(String content, String path, String app) throws Exception {
        System.out.println("Start Conv: createValueChangeListener " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createValueChangeListener " + path + " " + app);
        String contents = FileReaderWritter.getCharContents(path, app);
        int importLoc = contents.indexOf(";") + 1;

        String alContent = "public void " + content + "(ValueChangeEvent valueChangeEvent) {\n }\n";
        String temp = "";
        if (!contents.contains("import javax.faces.event.ValueChangeEvent;")) {
            temp =
                contents.substring(0, importLoc) + "\nimport javax.faces.event.ValueChangeEvent;" +
                contents.substring(importLoc);
            contents = temp;
        }
        int lastBrace = contents.lastIndexOf("}");
        temp = "";
        temp = contents.substring(0, lastBrace) + alContent + contents.substring(lastBrace);
        contents = temp;
        FileReaderWritter.writeFile(contents, path, app);
        System.out.println("End Conv: createValueChangeListener ");
    }

    /**
     * Imnports
     * @param contents
     * @param importStmt
     * @param app
     * @return
     * @throws Exception
     */
    private static String placeImports(String contents, String importStmt, String app) throws Exception {
        System.out.println("Start Conv: placeImports " + importStmt + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: placeImports " + importStmt + " " + app);
        int importLoc = contents.indexOf(";") + 1;
        String temp = "";
        if (!contents.contains("import oracle.jbo.JboException")) {
            temp = contents.substring(0, importLoc) + "\n" + importStmt + contents.substring(importLoc);
            contents = temp;
        }
        System.out.println("End Conv: placeImports ");
        return contents;
    }

    /**
     * Config XML handle
     * @param pathVC
     * @param pgName
     * @param beanPath
     * @throws Exception
     */
    protected static void createAdfConfig(String pathVC, String pgName, String beanPath, String app) throws Exception {
        System.out.println("Start Conv: createAdfConfig " + pathVC + " " + pgName + " " + beanPath + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createAdfConfig " + pathVC + " " + pgName + " " + beanPath + " " + app);
        File inputFile =
            new File(pathVC + FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                     "WEB-INF" + FileReaderWritter.getSeparator() + "adfc-config.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        Element root = doc.getDocumentElement();

        Element bean = doc.createElement("managed-bean");
        bean.setAttribute("id", pgName);
        Element beanName = doc.createElement("managed-bean-name");
        beanName.setTextContent(pgName);
        Element beanClass = doc.createElement("managed-bean-class");
        beanClass.setTextContent(beanPath);
        Element beanScope = doc.createElement("managed-bean-scope");
        beanScope.setTextContent("backingBean");

        root.appendChild(bean);
        bean.appendChild(beanName);
        bean.appendChild(beanClass);
        bean.appendChild(beanScope);

        FileReaderWritter.writeXMLFile(doc,
                                       pathVC + FileReaderWritter.getSeparator() + "public_html" +
                                       FileReaderWritter.getSeparator() + "WEB-INF" + FileReaderWritter.getSeparator() +
                                       "adfc-config.xml", app);
        System.out.println("End Conv: createAdfConfig ");
    }

    /**
     * Copy OAF CO completely
     * @param path
     * @param app
     * @param beanPath
     * @param coPath
     * @throws Exception
     */
    protected static void copyProcessFormRequest(String path, String app, String beanPath,
                                                 String coPath) throws Exception {
        System.out.println("Start Conv: copyProcessFormRequest " + path + " " + app + " " + beanPath + " " + coPath);
        ErrorAndLog.handleLog(app,
                              "Start Conv: copyProcessFormRequest " + path + " " + app + " " + beanPath + " " + coPath);
        String pageName = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()));
        pageName = pageName.replace(".xml", "");
        String str = "";
        str = FileReaderWritter.getCharContents(coPath, app);

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
                if (line.lastIndexOf(");") == -1) {
                    ret = ret + line + "\n";
                    line = reader.readLine();
                    line = "//" + line;
                }
            }
            // added
            else if (line != null && line.contains("webBean.findChildRecursive(\"")) {
                String[] s = line.split("webBean.findChildRecursive");
                String tempStr = s[1].substring(2, s[1].indexOf("\"", 2));
                line = s[0] + "get" + tempStr + "()" + s[1].substring(s[1].indexOf(")") + 1);
            } else if (line != null && line.contains("webBean.findIndexedChildRecursive(\"")) {
                String[] s = line.split("webBean.findIndexedChildRecursive");
                String tempStr = s[1].substring(2, s[1].indexOf("\"", 2));
                line = s[0] + "get" + tempStr + "()" + s[1].substring(s[1].indexOf(")") + 1);
            } else if (line != null && line.contains("new OAException(")) {
                if (line.lastIndexOf(");") == -1) {
                    if (-1 != line.lastIndexOf(",")) { // mod new
                        line = line.substring(0, line.lastIndexOf(","));
                        ret = ret + line + "\n";
                        line = reader.readLine();
                        if (line.contains("OAException.ERROR")) {
                            line = line.replace("OAException.ERROR", "");
                        }
                    }
                } else {
                    if (line.contains(", OAException.ERROR")) {
                        line = line.replace(", OAException.ERROR", "");
                    } else if (line.contains(",OAException.ERROR")) {
                        line = line.replace(",OAException.ERROR", "");
                    }
                }
            }
        }
        if (reader != null) {
            reader.close();
        }

        String jsfStr = FileReaderWritter.getCharContents(beanPath, app);
        jsfStr = jsfStr.subSequence(0, jsfStr.lastIndexOf("}")).toString();
        jsfStr = jsfStr + ret + "}";
        jsfStr = placeImports(jsfStr, "import oracle.jbo.JboException;", app);
        FileReaderWritter.writeFile(jsfStr, beanPath, app);
        DirCreator.WebBeanReplacements(beanPath, app);
        System.out.println("End Conv: copyProcessFormRequest");
    }
}
