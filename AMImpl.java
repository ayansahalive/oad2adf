package conv;

import java.util.*;

public class AMImpl {
    public AMImpl() {
        super();
    }

    /**
     * Generate and write AMImpl file
     * @param path
     * @param app
     * @param dest
     */
    /*   protected static HashMap handleAMImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleAMImpl " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "converting " + path);
        HashMap hm = new HashMap();
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        JavaCodeExtractor obj = new JavaCodeExtractor();
        Vector vec = obj.start(path);
        String str = "";
        String interfaces = "";
        String imports = "";

        // remove main
        for (int i = 0; i < vec.size(); i++) {
            String contents = vec.get(i) + "";
            if (contents.contains("void main")) {
                continue;
            } else if (i == 0 || i == vec.size()) { // start and end of class
                str = str + "\n" + contents + "";
                int start = -1;
                int end = -1;
                start = contents.indexOf("import");
                end = contents.lastIndexOf("import");
                String lastImport = contents.substring(end);
                lastImport = lastImport.substring(0, lastImport.indexOf(";") + 1).trim();
                if (start != -1 && end != -1) {
                    imports = contents.subSequence(start, end) + "";
                    imports = imports.trim();
                    imports += lastImport;
                    System.out.println(imports);
                }
                //extract tht imports;
                imports = DirCreator.replaceImports(imports, "");
                hm.put("imports", imports);
            } else { // individual contents including methods and declarations
                str = str + "\n" + contents + ""; // for the impl class
                // code for AM Interface
                int startBraces = contents.indexOf("{");
                int semicolon = contents.indexOf(";");

                if (startBraces == -1 && semicolon != -1)
                    startBraces = semicolon + 1;
                else if (semicolon == -1 && startBraces != -1)
                    semicolon = startBraces + 1;
                String constructor = name.replace(".java", "") + "("; // can be parametereized as well

                if (semicolon > startBraces) {
                    startBraces = contents.indexOf("{");
                    String methodHeader = contents.subSequence(0, startBraces) + ";";
                    if (!methodHeader.contains("get") && !methodHeader.contains("set") &&
                        !methodHeader.contains("protected") && !(methodHeader.contains("private")) &&
                        !methodHeader.contains(constructor)) {
                        System.out.println(name + "()");
                        System.out.println(methodHeader);
                        methodHeader = methodHeader.replace("public", "");
                        interfaces += methodHeader.trim() + "\n";
                    }
                }
            }
        }

        hm.put("interfaceFileContents", interfaces);

        // replace imports of schema server an other sub directories under app
        String impVal = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        impVal =
            impVal.replace(dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model" +
                           FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                           FileReaderWritter.getSeparator(), "");
        impVal = impVal.substring(0, impVal.lastIndexOf(FileReaderWritter.getSeparator()));
        impVal = impVal.substring(0, impVal.lastIndexOf(FileReaderWritter.getSeparator())); // upto previous level
        impVal = impVal.replace(FileReaderWritter.getSeparator(), ".");
        str = str.replace("import " + impVal, "import model." + impVal);


        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);

        System.out.println("End Conv: handleAMImpl");

        return hm;

    }*/

    protected static HashMap handleAMImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleAMImpl " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "converting " + path);
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);

        HashMap hm = new HashMap();
        String str = "";
        str = FileReaderWritter.getCharContents(path);

        // handle multiline comments
        int start = -1;
        int end = -1;
        start = str.indexOf("/*");
        end = str.indexOf("*/");
        if (start != -1 && end != -1) {
            str = str.replace(str.subSequence(start, end + 2), "");
        }

        // change references
        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr);
        str = changed + str.substring(str.indexOf("{"));

        // handle logging
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);

        DirCreator.replacements(destination);

        // client interface data
        //        JavaCodeExtractor obj = new JavaCodeExtractor();
        //        Vector vec = obj.getMethodHeaders(path);
        //        String interfaceFileContents = "";
        //        for (int i = 0; i < vec.size(); i++) {
        //            interfaceFileContents = vec.get(i) + "\n";
        //        }
        //        hm.put("interfaceFileContents", interfaceFileContents);
        //
        //        int start = -1;
        //        int end = -1;
        //        start = str.indexOf("import");
        //        end = str.lastIndexOf("import");
        //        String lastImport = str.substring(end);
        //        lastImport = lastImport.substring(0, lastImport.indexOf(";") + 1).trim();
        //        String imports = "";
        //        if (start != -1 && end != -1) {
        //            imports = str.subSequence(start, end) + "";
        //            imports = imports.trim();
        //            imports += lastImport;
        //        }
        //        imports = DirCreator.replaceImports(imports, "");
        //        hm.put("imports", imports);

        System.out.println("End Conv: handleAMImpl");
        return hm;
    }


    /**
     * handle def file
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleAMDefImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleAMDefImpl " + path + " " + app + " " + dest);
        ErrorAndLog.handleLog(app, "converting " + path);
        String str = "";

        str = FileReaderWritter.getCharContents(path);

        // handle multiline comments
        int start = -1;
        int end = -1;
        start = str.indexOf("/*");
        end = str.indexOf("*/");
        if (start != -1 && end != -1) {
            str = str.replace(str.subSequence(start, end + 2), "");
        }

        // change references
        String temp = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(temp);
        str = changed + str.substring(str.indexOf("{"));

        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);
        
        DirCreator.replacements(destination);
        System.out.println("End Conv: handleAMDefImpl");

    }
}
