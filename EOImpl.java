package conv;

import java.util.List;
import java.util.Vector;


public class EOImpl {
    public EOImpl() {
        super();
    }

    /**
     * EO Class handle
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleEOImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEOImpl " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleEOImpl " + path + " " + app + " " + dest + " " + src);

        JavaCodeExtractor obj = new JavaCodeExtractor();
        List<String> vec = obj.start(path, app);
        String str = "";

        Vector attrVec = new Vector();

        for (int i = 0; i < vec.size(); i++) {
            String contents = vec.get(i) + "";
            if (i == 0) {
                // change references
                String changed = DirCreator.replaceImports(contents, app);
                vec.set(i, changed);
                contents = vec.get(i) + "";
            }
            if (contents.contains("setAttrInvokeAccessor") || contents.contains("getAttrInvokeAccessor")) {
                continue;
            } else if (contents.contains("public static final int")) {
                String attr =
                    contents.subSequence(contents.indexOf("int") + 3, contents.indexOf("=")).toString().trim();
                attr = FileReaderWritter.toInitCap(attr, app);
                String temp =
                    (contents.substring(0, contents.indexOf("=")) + " = AttributesEnum." + attr + ".index();").trim();
                vec.set(i, temp);
                attrVec.addElement(attr);
            }
            str = str + "\n" + vec.get(i) + "";
        }

        String accessor = generateEnumeration(attrVec, app);
        str = str.substring(0, str.lastIndexOf("}")) + accessor + "}";

        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className, app);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination, app);

        DirCreator.replacements(destination, app);

        System.out.println("End Conv: handleEOImpl ");
    }

    /**
     * Enumerations
     * @param attrVec
     * @param app
     * @return
     */
    private static String generateEnumeration(Vector attrVec, String app) {
        System.out.println("Start Conv: generateEnumeration ");
        ErrorAndLog.handleLog(app, "Start Conv: generateEnumeration ");
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
     * EO Def Calss handle
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleEODef(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEODef " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleEODef " + path + " " + app + " " + dest + " " + src);
        String str = "";
        str = FileReaderWritter.getCharContents(path, app);
        // handle multiline comments
        int start = -1;
        int end = -1;
        start = str.indexOf("/*");
        end = str.indexOf("*/");
        if (start != -1 && end != -1) {
            str = str.replace(str.subSequence(start, end + 2), "");
        }

        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr, app);
        str = changed + str.substring(str.indexOf("{"));

        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className, app);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination, app);

        DirCreator.replacements(destination, app);

        System.out.println("End Conv: handleEODef ");
    }

    /**
     * ADF EO mods
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleEOCol(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleEOCol " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleEOCol " + path + " " + app + " " + dest + " " + src);
        String str = "";

        str = FileReaderWritter.getCharContents(path, app);
        // handle multiline comments
        int start = -1;
        int end = -1;
        start = str.indexOf("/*");
        end = str.indexOf("*/");
        if (start != -1 && end != -1) {
            str = str.replace(str.subSequence(start, end + 2), "");
        }

        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr, app);
        str = changed + str.substring(str.indexOf("{"));

        // handle logging
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className, app);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination, app);

        DirCreator.replacements(destination, app);

        System.out.println("End Conv: handleEOCol ");
    }

}
