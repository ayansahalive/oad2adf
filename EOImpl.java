package conv;

import java.util.*;


public class EOImpl {
    public EOImpl() {
        super();
    }

    /**
     * convert EOImpl
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleEOImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEOImpl " + path + " " + app + " " + dest);
        ErrorAndLog.handleLog(app, "converting " + path);

        JavaCodeExtractor obj = new JavaCodeExtractor();
        List<String> vec = obj.start(path);
        String str = "";

        Vector attrVec = new Vector();

        for (int i = 0; i < vec.size(); i++) {
            String contents = vec.get(i) + "";
            if (i == 0) {
                // change references
                String changed = DirCreator.replaceImports(contents);
                vec.set(i, changed);
                contents = vec.get(i) + "";
            }
            if (contents.contains("setAttrInvokeAccessor") || contents.contains("getAttrInvokeAccessor")) {
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
            str = str + "\n" + vec.get(i) + "";
        }

        String accessor = generateEnumeration(attrVec);
        str = str.substring(0, str.lastIndexOf("}")) + accessor + "}";
        
        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods+"}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);
        
        DirCreator.replacements(destination);

        System.out.println("End Conv: handleEOImpl ");
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

    /**
     * convert EOdefinition
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleEODef(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEODef " + path + " " + app + " " + dest);
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
        str = str + newMethods+"}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);
        
        DirCreator.replacements(destination);

        System.out.println("End Conv: handleEODef ");
    }

    /**
     * convert EO COl
     * @param path
     * @param app
     * @param dest
     * @throws Exception
     */
    protected static void handleEOCol(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEOCol " + path + " " + app + " " + dest);
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
        str = str + newMethods+"}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination);
        
        DirCreator.replacements(destination);

        System.out.println("End Conv: handleEOCol ");
    }

}
