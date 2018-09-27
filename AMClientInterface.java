package conv;

import java.io.*;

import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class AMClientInterface {
    public AMClientInterface() {
        super();
    }

    protected static void searchAndCreate(String dest, String app) {
        System.out.println("Start Conv: searchAndCreate " + dest + " " + app);
        String path = dest + FileReaderWritter.getSeparator() + app;
        readDir(path, 0, path);
        // pathOriginal = D:\Converter\ADF\hello
        System.out.println("End Conv: searchAndCreate ");
    }


    private static void readDir(String path, int level, String pathOriginal) {
        //        System.out.println("Start Conv: readDir " + path + " " + level + " " + pathOriginal);
        try {
            File dir = new File(path);
            File[] firstLevelFiles = dir.listFiles();
            if (firstLevelFiles != null && firstLevelFiles.length > 0) {
                for (File aFile : firstLevelFiles) {
                    if (aFile.isDirectory()) {
                        // dir
                        readDir(aFile.getAbsolutePath(), level + 1, pathOriginal);
                    } else {
                        // file
                        if (aFile.toString().contains(".xml")) {
                            File inputFile = new File(path);
                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            dbFactory.setValidating(false);
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(inputFile); // handle to AMXML
                            Element AdfAppModule = doc.getDocumentElement();
                            String type = AdfAppModule.getTagName();
                            if (type.equals("AppModule")) {
                                String implClassPkg = "";
                                NamedNodeMap attrs = AdfAppModule.getAttributes();
                                for (int j = 0; j < attrs.getLength(); j++) {
                                    Node currentAtt = attrs.item(j);
                                    if (currentAtt.getNodeName().equals("ComponentClass")) {
                                        implClassPkg =
                                            DirCreator.changedClassPath(currentAtt.getNodeValue()); // model.xxnuc.oracle.apps.inv.hello.server.HelloAMImpl
                                    }
                                }
                                // handle Impl add \Model\src\ to pathOriginal
                                String location = implClassPkg.replace(".", FileReaderWritter.getSeparator());
                                String destination =
                                    pathOriginal + FileReaderWritter.getSeparator() + "Model" +
                                    FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() +
                                    location;
                                File impl = new File(destination);
                                if (impl.exists() && !impl.isDirectory()) {
                                    String xmlName =
                                        path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
                                    xmlName = xmlName.replace(".xml", "");
                                    generateInterface(destination, implClassPkg, xmlName);
                                }

                            }
                        }
                    }
                }
            }
            //            System.out.println("End Conv: readDir");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected static int generateInterface(String destination, String implClassPkg, String xmlName) throws Exception {
        System.out.println("Start Conv: generateInterface " + destination + " " + implClassPkg + " " + xmlName);

        String name = destination.substring(destination.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String amName = name.replace(".java", "");

        JavaCodeExtractor obj = new JavaCodeExtractor();
        Vector vec = obj.start(destination);

        Vector intfc = new Vector();
        int countAdded = 0;

        for (int i = 0; i < vec.size(); i++) {
            String str = vec.get(i) + "";

            int startBraces = str.indexOf("{");
            int semicolon = str.indexOf(";");

            if (startBraces == -1 && semicolon != -1)
                startBraces = semicolon + 1;
            else if (semicolon == -1 && startBraces != -1)
                semicolon = startBraces + 1;

            if (semicolon > startBraces && !str.contains("constructor") && !str.contains("void main")) {
                startBraces = str.indexOf("{");
                String methodHeader = str.subSequence(0, startBraces - 1) + ";";
                if (!methodHeader.contains("get") && !methodHeader.contains("set") &&
                    !methodHeader.contains("protected") && !(methodHeader.contains("private"))) {
                    intfc.addElement(methodHeader.replace("public", ""));
                    countAdded++;
                }
            }
        }


        // logic to get package
        if (countAdded > 0) {
            String pkgVal = implClassPkg;
            pkgVal = pkgVal.substring(0, pkgVal.lastIndexOf(".") + 1) + xmlName;


            String contents = "package " + pkgVal + ".common" + "; \n";
            contents += "import oracle.jbo.ApplicationModule; \n";
            contents += "public interface " + amName + " extends ApplicationModule { \n";

            for (int i = 0; i < intfc.size(); i++) {
                contents += intfc.get(i) + "\n";
            }

            contents += "}";

            // write a new file
            //            String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
            String x = destination.substring(0, destination.lastIndexOf(FileReaderWritter.getSeparator()));
            x = x + FileReaderWritter.getSeparator() + "common" + FileReaderWritter.getSeparator() + amName + ".java";
            FileReaderWritter.writeFile(contents, x);
        }
        System.out.println("End Conv: generateInterface");
        return countAdded;
    }
}
