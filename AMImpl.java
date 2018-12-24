package conv;

import java.util.HashMap;

public class AMImpl {
    public AMImpl() {
        super();
    }

    /**
     * AM Impl class handle
     * @param path
     * @param app
     * @param dest
     * @param src
     * @return
     * @throws Exception
     */
    protected static HashMap handleAMImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleAMImpl " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: handleAMImpl " + path + " " + app + " " + dest + " " + src);
        String name = path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1);

        HashMap hm = new HashMap();
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

        // change references
        String tempStr = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(tempStr, app);
        str = changed + str.substring(str.indexOf("{"));

        // handle logging
        String className = name.replace(".java", "");
        String newMethods = DirCreator.addMethods(className, app);
        str = str.subSequence(0, str.lastIndexOf("}")).toString();
        str = str + newMethods + "}";

        // write a new file
        String destination = FileReaderWritter.getModelDestinationPath(path, app, dest, src);

        FileReaderWritter.writeFile(str, destination, app);

        DirCreator.replacements(destination, app);

        System.out.println("End Conv: handleAMImpl");
        return hm;
    }


    /**
     * AM Def class handle
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void handleAMDefImpl(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: handleAMDefImpl " + path + " " + app + " " + dest);
        ErrorAndLog.handleLog(app, "Start Conv: handleAMDefImpl " + path + " " + app + " " + dest);
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

        // change references
        String temp = str.substring(0, str.indexOf("{"));
        String changed = DirCreator.replaceImports(temp, app);
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
        System.out.println("End Conv: handleAMDefImpl");

    }
}
