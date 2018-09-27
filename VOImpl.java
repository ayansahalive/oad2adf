package conv;

import java.util.*;


public class VOImpl {
    public VOImpl() {
        super();
    }

    /**
     * convert vo impl
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleVOImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleVOImpl " + path + " " + app + " " + dest);
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
        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr);
        str = changed + str.substring(str.indexOf("{"));

        if (null != str && !"".equals(str) && !"null".equals(str)) {
            // datasourceOverride option selected
            if (str.contains("getEstimatedRowCount")) {

                String optional_code =
                    "    /**" + "     * getQueryHitCount - overridden for custom java data source support." +
                    "     */" + "    @Override" + "    public long getQueryHitCount(ViewRowSetImpl viewRowSet) {" +
                    "        long value = super.getQueryHitCount(viewRowSet);" + "        return value;" + "    }" +
                    "" + "    /**" + "     * getCappedQueryHitCount - overridden for custom java data source support." +
                    "     */" + "    @Override" +
                    "    public long getCappedQueryHitCount(ViewRowSetImpl viewRowSet, Row[] masterRows, long oldCap, long cap) {" +
                    "        long value = super.getCappedQueryHitCount(viewRowSet, masterRows, oldCap, cap);" +
                    "        return value;" + "    }";

                int lastBrace = str.lastIndexOf("}");
                String temp = "";
                temp = str.substring(0, lastBrace) + optional_code + str.substring(lastBrace);
                str = temp;
            }

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
        }

        System.out.println("End Conv: handleVOImpl ");
    }

    /**
     * convert vo Row
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleVORowImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleVORowImpl " + path + " " + app + " " + dest);
        ErrorAndLog.handleLog(app, "converting " + path);
        //        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        JavaCodeExtractor obj = new JavaCodeExtractor();
        List<String> vec = obj.start(path);
        String str = "";

        Vector attrVec = new Vector();


        // remove accessor
        for (int i = 0; i < vec.size(); i++) {
            String contents = vec.get(i) + "";
            if (i == 0) {
                // change references
                String changed = DirCreator.replaceImports(contents);
                contents = changed + contents.substring(contents.indexOf("{") + 1);
            } else if (contents.contains("setAttrInvokeAccessor") || contents.contains("getAttrInvokeAccessor")) {
                continue;
            } else if (contents.contains("public static final int")) {
                String attr =
                    contents.subSequence(contents.indexOf("int") + 3, contents.indexOf("=")).toString().trim();
                attr = FileReaderWritter.toInitCap(attr);
                String temp =
                    (contents.substring(0, contents.indexOf("=")) + " = AttributesEnum." + attr + ".index();").trim();
                vec.set(i, temp);
                attrVec.addElement(attr);
            }
            str = str + contents + "\n";
        }

        String accessor = generateEnumeration(attrVec);
        str = str.substring(0, str.lastIndexOf("}")) + accessor + "}";

        //        str = str.replace(" Number", "oracle.jbo.domain.Number");

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

        System.out.println("End Conv: handleVORowImpl ");
    }

    /**
     * convert VO def
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleVODef(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleVODef " + path + " " + app + " " + dest);
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

        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr);
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

        System.out.println("End Conv: handleVODef ");
    }

    /**
     * handle vo client class
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleVOClient(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleVODef " + path + " " + app + " " + dest);
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

        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr);
        str = changed + str.substring(str.indexOf("{"));

        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        FileReaderWritter.writeFile(str, destination);
        
        DirCreator.replacements(destination);

        System.out.println("End Conv: handleVODef ");
    }

    /**
     * generate enumeration for row indices
     * @param attrVec
     * @return
     */
    private static String generateEnumeration(Vector attrVec) {
        System.out.println("Start Conv: generateEnumeration ");
        String strHead = "public enum AttributesEnum {";

        String strBody = "";
        int size = attrVec.size();
        for (int i = 0; i < size; i++) {
            String contents = attrVec.get(i) + "";
            if (i == size - 1)
                strBody = strBody + "\n" + contents + ";";
            else if (i < size)
                strBody = strBody + "\n" + contents + ",";
        }

        String strTail =
            " private static AttributesEnum[] vals = null; private static final int firstIndex = 0; public int index() {" +
            " return AttributesEnum.firstIndex() + ordinal(); } public static final int firstIndex() { return firstIndex; } public static int count() {" +
            " return AttributesEnum.firstIndex() + AttributesEnum.staticValues().length; } public static final AttributesEnum[] staticValues() {" +
            " if (vals == null) { vals = AttributesEnum.values(); } return vals; }}";

        System.out.println("End Conv: generateEnumeration ");
        return strHead + " " + strBody + " " + strTail;
    }

    protected static void handleVORowInterface(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleVORowInterface " + path + " " + app + " " + dest);
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
        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr);
        str = changed + str.substring(str.indexOf("{"));
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);
        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";
        FileReaderWritter.writeFile(str, destination);
        DirCreator.replacements(destination);
        System.out.println("End Conv: handleVORowInterface ");
    }

}
