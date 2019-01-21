package conv;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DirCreator {
    public DirCreator() {
        super();
    }

    private static final int BUFFER_SIZE = 4096;

    /**
     * Create Dir
     * @param app
     * @param dest
     * @param repo
     * @throws Exception
     */
    protected static void createDir(String app, String dest, String repo) throws Exception {
        System.out.println("Start Conv: createDir " + app + " " + dest + " " + repo);
        ErrorAndLog.handleLog(app, "Start Conv: createDir " + app + " " + dest + " " + repo);
        String path = dest + FileReaderWritter.getSeparator() + app;
        File newFolder = new File(path);
        newFolder.mkdirs();

        // Model
        String pathModel = path + FileReaderWritter.getSeparator() + "Model";
        newFolder = new File(pathModel);
        newFolder.mkdirs();
        String pathModelsrc =
            pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model";
        newFolder = new File(pathModelsrc);
        newFolder.mkdirs();

        // src
        String pathSrc =
            path + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "META-INF";
        newFolder = new File(pathSrc);
        newFolder.mkdirs();


        // ViewController
        String pathVC = path + FileReaderWritter.getSeparator() + "ViewController";
        newFolder = new File(pathVC);
        newFolder.mkdirs();
        String pathVCsrc =
            pathVC + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "view" +
            FileReaderWritter.getSeparator() + "backing";
        newFolder = new File(pathVCsrc);
        newFolder.mkdirs();
        pathVCsrc = pathVC + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "META-INF";
        newFolder = new File(pathVCsrc);
        newFolder.mkdirs();
        String pathVCpub =
            pathVC + FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() + "WEB-INF";
        newFolder = new File(pathVCpub);
        newFolder.mkdirs();
        String pathVCmodel =
            pathVC + FileReaderWritter.getSeparator() + "model" + FileReaderWritter.getSeparator() + "WEB-INF";
        newFolder = new File(pathVCmodel);
        newFolder.mkdirs();
        String pathVCadf =
            pathVC + FileReaderWritter.getSeparator() + "adfmsrc" + FileReaderWritter.getSeparator() + "view" +
            FileReaderWritter.getSeparator() + "pageDefs";
        newFolder = new File(pathVCadf);
        newFolder.mkdirs();
        pathVCadf =
            pathVC + FileReaderWritter.getSeparator() + "adfmsrc" + FileReaderWritter.getSeparator() + "META-INF";
        newFolder = new File(pathVCadf);
        newFolder.mkdirs();

        // copy src
        copySrcFiles(repo, app, pathSrc);

        //copy VC
        copyVCFiles(pathVC, repo, app);

        // copy project
        copyProjectFiles(dest, app, repo);

        System.out.println("End Conv: createDir");
    }

    /**
     * Copy ADF Jdev files
     * @param dest
     * @param app
     * @param repo
     * @throws Exception
     */
    private static void copyProjectFiles(String dest, String app, String repo) throws Exception {
        System.out.println("Start Conv: copyProjectFiles " + dest + " " + app + " " + repo);
        ErrorAndLog.handleLog(app, "Start Conv: copyProjectFiles " + dest + " " + app + " " + repo);
        String pathModel = dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model";
        String pathVC =
            dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "ViewController";
        String pathJWS = dest + FileReaderWritter.getSeparator() + app;

        String Mjpr =
            FileReaderWritter.getCharContents(repo + FileReaderWritter.getSeparator() + "testApp" +
                                              FileReaderWritter.getSeparator() + "Model" +
                                              FileReaderWritter.getSeparator() + "Model.jpr", app);
        Mjpr = Mjpr.replace("testApp", app);
        FileReaderWritter.writeFile(Mjpr, pathModel + FileReaderWritter.getSeparator() + "Model.jpr", app);

        String VCjpr =
            FileReaderWritter.getCharContents(repo + FileReaderWritter.getSeparator() + "testApp" +
                                              FileReaderWritter.getSeparator() + "ViewController" +
                                              FileReaderWritter.getSeparator() + "ViewController.jpr", app);
        VCjpr = VCjpr.replace("testApp", app);
        FileReaderWritter.writeFile(VCjpr, pathVC + FileReaderWritter.getSeparator() + "ViewController.jpr", app);

        String jws =
            FileReaderWritter.getCharContents(repo + FileReaderWritter.getSeparator() + "testApp" +
                                              FileReaderWritter.getSeparator() + "testApp.jws", app);
        jws = jws.replace("testApp", app);
        FileReaderWritter.writeFile(jws, pathJWS + FileReaderWritter.getSeparator() + app + ".jws", app);

        String jpx =
            FileReaderWritter.getCharContents(repo + FileReaderWritter.getSeparator() + "testApp" +
                                              FileReaderWritter.getSeparator() + "Model" +
                                              FileReaderWritter.getSeparator() + "src" +
                                              FileReaderWritter.getSeparator() + "model" +
                                              FileReaderWritter.getSeparator() + "Model.jpx", app);
        FileReaderWritter.writeFile(jpx,
                                    pathModel + FileReaderWritter.getSeparator() + "src" +
                                    FileReaderWritter.getSeparator() + "model" + FileReaderWritter.getSeparator() +
                                    "Model.jpx", app);
        copyADFDTD(repo,
                   pathModel + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                   FileReaderWritter.getSeparator(), app);

        System.out.println("End Conv: copyProjectFiles");
    }

    /**
     * Temp DTD copy
     * @param repo
     * @param dest
     * @param app
     * @throws Exception
     */
    protected static void copyOAFDTD(String repo, String dest, String app) throws Exception {
        System.out.println("Start Conv: copyOAFDTD " + repo + " " + dest + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: copyOAFDTD " + repo + " " + dest + " " + app);
        dest = dest.substring(0, dest.lastIndexOf(FileReaderWritter.getSeparator()));
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "jbo_03_01_oaf.dtd",
                                   dest + FileReaderWritter.getSeparator() + "jbo_03_01.dtd", app);
        System.out.println("End Conv: copyOAFDTD");
    }

    /**
     * Temp DTD Copy
     * @param repo
     * @param dest
     * @param app
     * @throws Exception
     */
    protected static void copyADFDTD(String repo, String dest, String app) throws Exception {
        System.out.println("Start Conv: copyADFDTD " + repo + " " + dest + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: copyADFDTD " + repo + " " + dest + " " + app);
        dest = dest.substring(0, dest.lastIndexOf(FileReaderWritter.getSeparator()));
        new File(dest).mkdirs();
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "jbo_03_01_adf.dtd",
                                   dest + FileReaderWritter.getSeparator() + "jbo_03_01.dtd", app);
        System.out.println("End Conv: copyADFDTD");
    }

    /**
     * Source files copy
     * @param repo
     * @param app
     * @param pathSrc
     * @throws Exception
     */
    private static void copySrcFiles(String repo, String app, String pathSrc) throws Exception {
        System.out.println("Start Conv: copySrcFiles " + repo + " " + app + " " + pathSrc);
        ErrorAndLog.handleLog(app, "Start Conv: copySrcFiles " + repo + " " + app + " " + pathSrc);
        String jps =
            repo + FileReaderWritter.getSeparator() + "testApp" + FileReaderWritter.getSeparator() + "src" +
            FileReaderWritter.getSeparator() + "META-INF" + FileReaderWritter.getSeparator() + "jps-config.xml";
        String webapp =
            repo + FileReaderWritter.getSeparator() + "testApp" + FileReaderWritter.getSeparator() + "src" +
            FileReaderWritter.getSeparator() + "META-INF" + FileReaderWritter.getSeparator() +
            "weblogic-application.xml";

        jps = FileReaderWritter.getCharContents(jps, app);
        jps = jps.replace("testApp", app);
        webapp = FileReaderWritter.getCharContents(webapp, app);

        FileReaderWritter.writeFile(jps, pathSrc + FileReaderWritter.getSeparator() + "jps-config.xml", app);
        FileReaderWritter.writeFile(webapp, pathSrc + FileReaderWritter.getSeparator() + "weblogic-application.xml",
                                    app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() +
                                   "META-INF" + FileReaderWritter.getSeparator() + "cwallet.sso",
                                   pathSrc + FileReaderWritter.getSeparator() + "cwallet.sso", app);

        System.out.println("End Conv: copySrcFiles");
    }

    /**
     * View COntroller Project file copy
     * @param pathVC
     * @param repo
     * @param app
     * @throws Exception
     */
    private static void copyVCFiles(String pathVC, String repo, String app) throws Exception {
        System.out.println("Start Conv: copyVCFiles " + pathVC + " " + repo + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: copyVCFiles " + pathVC + " " + repo + " " + app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                                   "WEB-INF" + FileReaderWritter.getSeparator() + "faces-config.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "public_html" +
                                   FileReaderWritter.getSeparator() + "WEB-INF" + FileReaderWritter.getSeparator() +
                                   "faces-config.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                                   "WEB-INF" + FileReaderWritter.getSeparator() + "trinidad-config.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "public_html" +
                                   FileReaderWritter.getSeparator() + "WEB-INF" + FileReaderWritter.getSeparator() +
                                   "trinidad-config.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                                   "WEB-INF" + FileReaderWritter.getSeparator() + "web.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "public_html" +
                                   FileReaderWritter.getSeparator() + "WEB-INF" + FileReaderWritter.getSeparator() +
                                   "web.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() +
                                   "META-INF" + FileReaderWritter.getSeparator() + "adf-settings.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "META-INF" + FileReaderWritter.getSeparator() +
                                   "adf-settings.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "adfmsrc" + FileReaderWritter.getSeparator() +
                                   "META-INF" + FileReaderWritter.getSeparator() + "adfm.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "adfmsrc" +
                                   FileReaderWritter.getSeparator() + "META-INF" + FileReaderWritter.getSeparator() +
                                   "adfm.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                                   "WEB-INF" + FileReaderWritter.getSeparator() + "adfc-config.xml",
                                   pathVC + FileReaderWritter.getSeparator() + "public_html" +
                                   FileReaderWritter.getSeparator() + "WEB-INF" + FileReaderWritter.getSeparator() +
                                   "adfc-config.xml", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "testApp" +
                                   FileReaderWritter.getSeparator() + "ViewController" +
                                   FileReaderWritter.getSeparator() + "adfmsrc" + FileReaderWritter.getSeparator() +
                                   "view" + FileReaderWritter.getSeparator() + "DataBindings.cpx",
                                   pathVC + FileReaderWritter.getSeparator() + "adfmsrc" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "DataBindings.cpx", app);
        // util files
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "StringUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "StringUtils.java", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "PopupUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "PopupUtils.java", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "OperationUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "OperationUtils.java", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "JSFUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "JSFUtils.java", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "AmisUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "AmisUtils.java", app);
        FileReaderWritter.copyFile(repo + FileReaderWritter.getSeparator() + "util" + FileReaderWritter.getSeparator() +
                                   "ADFUtils.java",
                                   pathVC + FileReaderWritter.getSeparator() + "src" +
                                   FileReaderWritter.getSeparator() + "view" + FileReaderWritter.getSeparator() +
                                   "util" + FileReaderWritter.getSeparator() + "ADFUtils.java", app);

        //
        JSFGen.createJSF(app,
                         pathVC + FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                         app + "PG.jsf");
        TFGen.createBTF(app,
                        pathVC + FileReaderWritter.getSeparator() + "public_html" + FileReaderWritter.getSeparator() +
                        "WEB-INF" + FileReaderWritter.getSeparator() + "BTF" + app + ".xml");

        System.out.println("End Conv: copyVCFiles");
    }

    /**
     * Unzip app
     * @param zipFilePath
     * @param destDirectory
     * @return
     * @throws Exception
     */
    protected static String unzip(String zipFilePath, String destDirectory) throws Exception {
        System.out.println("Start Conv: unzip " + zipFilePath + " " + destDirectory);
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        System.out.println("End Conv: unzip ");
        return "success";

    }

    /**
     * Unzip and extract
     * @param zipIn
     * @param filePath
     * @throws Exception
     */
    protected static void extractFile(ZipInputStream zipIn, String filePath) throws Exception {
        System.out.println("Start Conv: extractFile " + filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        System.out.println("End Conv: extractFile ");
    }

    /**
     * return changed class path to ADF
     * @param path
     * @return
     */
    protected static String changedModelClassPath(String path, String app) {
        System.out.println("Start Conv: changedClassPath " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: changedClassPath " + path + " " + app);
        String retPath = "model." + path;
        System.out.println("End Conv: changedClassPath");
        return retPath;
    }

    /**
     * changed class paths for VL and AO
     * @param path
     * @return
     */
    protected static String changedVLAOClassPath(String path, String app) {
        System.out.println("Start Conv: changedVLAOClassPath " + path + " +app");
        ErrorAndLog.handleLog(app, "Start Conv: changedVLAOClassPath " + path + " " + app);
        String ret = "";
        String col = path.substring(0, path.lastIndexOf("."));
        String vo = col.substring(0, col.lastIndexOf("."));
        path = path.replace(vo, "");
        ret = "model" + path;
        System.out.println("End Conv: changedVLAOClassPath");
        return ret;
    }

    /**
     * replacements of imports for content read files
     * replacements of imports and body for vector read files
     * @param str
     * @return
     */
    protected static String replaceImports(String str,
                                           String app) {
        //        System.out.println("Start Conv: replaceImports " + app);
        //        ErrorAndLog.handleLog(app, "Start Conv: replaceImports " + app);
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;",
                        "import oracle.jbo.server.ApplicationModuleImpl;");
        str = str.replace("OAApplicationModuleImpl", "ApplicationModuleImpl");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAViewObjectImpl;",
                        "import oracle.jbo.server.ViewObjectImpl; import oracle.jbo.server.ViewRowSetImpl; import oracle.jbo.Row;");
        str = str.replace("OAViewObjectImpl", "ViewObjectImpl");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAViewRowImpl;",
                        "import oracle.jbo.server.ViewRowImpl;");
        str =
            str.replace("import oracle.apps.fnd.framework.server.ViewRowImpl;",
                        "import oracle.jbo.server.ViewRowImpl;");
        str = str.replace("OAViewRowImpl", "ViewRowImpl");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAEntityImpl;",
                        "import oracle.jbo.server.EntityImpl;");
        str = str.replace("OAEntityImpl", "EntityImpl");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAEntityDefImpl;",
                        "import oracle.jbo.server.EntityDefImpl;");
        str = str.replace("OAEntityDefImpl", "EntityDefImpl");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OAEntityCache;",
                        "import oracle.jbo.server.EntityCache;");
        str = str.replace("OAEntityCache", "EntityCache");

        str = str.replace("import oracle.apps.fnd.framework.OAViewObject;", "import oracle.jbo.ViewObject;");
        str = str.replace("import oracle.apps.fnd.framework.OAFwkConstants;", "");
        str = str.replace("import oracle.apps.fnd.common.MessageToken;", "");
        str = str.replace("com.sun.java.util.collections", "java.util");
        str =
            str.replace("import oracle.apps.fnd.framework.server.OADBTransaction;",
                        "import oracle.jbo.server.DBTransaction;");
        str = str.replace("import oracle.apps.fnd.framework.OARow;", "import oracle.jbo.Row;");
        str = str.replace("import oracle.apps.fnd.framework.OAException;", "import oracle.jbo.JboException;");
        try {
            String pkg = str.substring(str.indexOf("package"), str.indexOf(";")).trim();
            pkg = pkg.replace("package ", "");
            str = str.replace(pkg, changedModelClassPath(pkg, app));

            // replace pattern of imported classes of the same project
            String all = pkg.substring(0, pkg.indexOf(".oracle.apps."));
            str = str.replace("import " + all, "import model." + all);
        } catch (StringIndexOutOfBoundsException e) {
            // System.out.println("********* No immports to replace **********");
            ;
        }

        //        System.out.println("End Conv: replaceImports ");
        return str;
    }

    /**
     * methods to handle logging
     * @param className
     * @return
     */
    protected static String addMethods(String className, String app) {
        System.out.println("Start Conv: addMethods " + className + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: addMethods " + className + " " + app);
        String str =
            "    private boolean isLoggingEnabled(" + className + ".OAFwkConstants oaFwkConstants) {\n" +
            "        return false;\n" + "    }\n" + "\n" + "    private void writeDiagnostics(" + className +
            " this1, String string,\n" + "                                  " + className +
            ".OAFwkConstants oaFwkConstants) {\n" + "    }\n" + "\n" + "    enum OAFwkConstants {\n" +
            "        STATEMENT\n" + "    }" + "    private boolean isLoggingEnabled(int i) {\n" +
            "        return false;\n" + "    }" + "    private void writeDiagnostics(" + className +
            " this1, String string, int i) {\n" + "    }" + "    private static class MessageToken {\n" +
            "        private MessageToken(String string, String string1) {\n" + "        }\n" + "    }" +
            "    private boolean isPreparedForExecution() {\n" + "        return true;\n" + "    }";
        return str;
    }

    /**
     * replacements of body for content read files
     * @param path
     * @throws Exception
     */
    protected static void replacements(String path, String app) throws Exception {
        System.out.println("Start Conv: replacements " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: replacements " + path + " " + app);
        String str = FileReaderWritter.getCharContents(path, app);
        /// replacements =====================================================
        str = str.replace("OAException", "JboException");
        str = str.replace("OADBTransaction", "DBTransaction");
        str = str.replace("getOADBTransaction", "getDBTransaction");
        str = str.replace("OAViewObject", "ViewObject");
        str = str.replace("OARow", "Row");
        str = str.replace("OAViewRowImpl", "ViewRowImpl");

        //// =================
        BufferedReader reader;
        String ret = "";
        reader = new BufferedReader(new StringReader(str));
        String line = reader.readLine();
        while (line != null) {
            ret = ret + line + "\n";
            line = reader.readLine();
            if (line != null && line.contains("OracleConnection")) {
                line = "//" + line;
                if (line.lastIndexOf(");") == -1) {
                    ret = ret + line + "\n";
                    line = reader.readLine();
                    line = "//" + line;
                }
            } else if (line != null && line.contains(".prepareStatement")) {
                line =
                    line.replace(line.substring(line.indexOf("=") + 1, line.indexOf(".prepareStatement") + 8),
                                 "getDBTransaction().createPrepared");
                line = line.replace(line.substring(line.lastIndexOf(");"), line.lastIndexOf(");") + 2), ",0);");
            }
        }
        str = ret;
        FileReaderWritter.writeFile(str, path, app);
    }

    /**
     * replace webbean references in Controller
     * @param path
     * @throws Exception
     */
    protected static void WebBeanReplacements(String path, String app) throws Exception {
        System.out.println("Start Conv: WebBeanReplacements " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: WebBeanReplacements " + path + " " + app);

        String str = FileReaderWritter.getCharContents(path, app);

        // replace import not written yet

        //        str.replace("OAAddTableRowBean","");
        //        str.replace("OAAdvancedSearchBean","");
        str = str.replace("OAAdvancedTableBean", "RichTable");
        //        str.replace("OAApplicationSwitcherBean","");
        //        str.replace("OAAttachmentImageBean","");
        //        str.replace("OAAttachmentTableBean","");
        //        str.replace("OABodyBean","");
        //        str.replace("OABorderLayoutBean","");
        //        str.replace("OABreadCrumbsBean","");
        //        str.replace("OABrowseMenuBean","");
        //        str.replace("OABulletedListBean","");
        str = str.replace("OAButtonBean", "RichButton");
        //        str.replace("OAButtonSpacerBean","");
        //        str.replace("OAButtonSpacerRowBean","");
        //        str.replace("OACardBean","");
        //        str.replace("OACellFormatBean","");
        str = str.replace("OACheckBoxBean", "RichSelectBooleanCheckbox");
        str = str.replace("OAChoiceBean", "RichSelectManyChoice");
        //        str.replace("OAColumnBean","");
        //        str.replace("OAColumnGroupBean","");
        //        str.replace("OAContentContainerBean","");
        //        str.replace("OAContentFooterBean","");
        //        str.replace("OADataScopeBean","");
        //        str.replace("OADateFieldBean", "");
        //        str.replace("OADefaultDoubleColumnBean","");
        //        str.replace("OADefaultFormStackLayoutBean","");
        //        str.replace("OADefaultHideShowBean","");
        //        str.replace("OADefaultListBean","");
        //        str.replace("OADefaultShuttleBean", "");
        //        str.replace("OADefaultSingleColumnBean","");
        //        str.replace("OADefaultStackLayoutBean","");
        //        str.replace("OADefaultTableLayoutBean","");
        //        str.replace("OADefaultTreeBean","");
        //        str.replace("OADescriptiveFlexBean","");
        //        str.replace("OADocumentBean","");
        //        str.replace("OADownloadBean","");
        //        str.replace("OAExportBean","");
        //        str.replace("OAFieldTableLayoutBean","");
        //        str.replace("OAFileUploadBean","");
        //        str.replace("OAFlexBean","");
        //        str.replace("OAFlexibleCellLayoutBean","");
        //        str.replace("OAFlexibleContentBean","");
        //        str.replace("OAFlexibleContentListBean","");
        //        str.replace("OAFlexibleLayoutBean","");
        //        str.replace("OAFlexibleRowLayoutBean","");
        //        str.replace("OAFlowLayoutBean","");
        //        str.replace("OAFooterBean","");
        str = str.replace("OAFormattedTextBean", "RichOutputFormatted");
        //        str.replace("OAFormBean","");
        //        str.replace("OAFormParameterBean","");
        //        str.replace("OAFormValueBean","");
        //        str.replace("OAFrameBean","");
        //        str.replace("OAFrameBorderLayoutBean","");
        //        str.replace("OAGanttBean","");
        //        str.replace("OAGlobalButtonBarBean","");
        //        str.replace("OAGlobalButtonBean","");
        //        str.replace("OAGlobalHeaderBean","");
        //        str.replace("OAGraphTableBean","");
        //        str.replace("OAHeadBean","");
        //        str.replace("OAHeaderBean","");
        //        str.replace("OAHGridBean","");
        //        str.replace("OAHGridHierarchyBean","");
        //        str.replace("OAHideShowBean","");
        //        str.replace("OAHideShowHeaderBean","");
        //        str.replace("OAHideShowSubTabLayoutBean","");
        //        str.replace("OAHTMLWebBean","");
        str = str.replace("OAIconBean", "RichIcon");
        str = str.replace("OAImageBean", "RichImage");
        //        str.replace("OAImportScriptBean","");
        //        str.replace("OAIncludeBean","");
        //        str.replace("OAInfotileBean","");
        //        str.replace("OAInlineDatePickerBean","");
        //        str.replace("OAInlineMessageBean","");
        //        str.replace("OAInternalFileUploadBean","");
        //        str.replace("OAKeyFlexBean","");
        //        str.replace("OAKFFLovBean","");
        //        str.replace("OALabelBean","");
        //        str.replace("OALabeledFieldLayoutBean","");
        str = str.replace("OALinkBean", "RichLink");
        //        str.replace("OAListBean","");
        //        str.replace("OAListOfValuesBean","");
        //        str.replace("OALovActionButtonBean","");
        //        str.replace("OALovBean","");
        //        str.replace("OALovSelectColumnBean","");
        //        str.replace("OALovTextInputBean","");
        //        str.replace("OAMenuBean","");
        //        str.replace("OAMenuItemBean","");
        //        str.replace("OAMessageAttachmentLinkBean","");
        //        str.replace("OAMessageBoxBean","");
        str = str.replace("OAMessageCheckBoxBean", "RichSelectBooleanCheckbox");
        str = str.replace("OAMessageChoiceBean", "RichSelectOneChoice");
        str = str.replace("OAMessageColorFieldBean", "RichChooseColor");
        //        str.replace("OAMessageComponentLayoutBean","");
        str = str.replace("OAMessageDateFieldBean", "RichInputDate");
        //        str.replace("OAMessageDownloadBean","");
        //        str.replace("OAMessageFileUploadBean","");
        //        str.replace("OAMessageGaugeBean","");
        //        str.replace("OAMessageInlineAttachmentBean","");
        //        str.replace("OAMessageLayoutBean","");
        //        str.replace("OAMessageListBean","");
        str = str.replace("OAMessageLovChoiceBean", "RichInputComboboxListOfValues");
        str = str.replace("OAMessageLovInputBean", "RichInputListOfValues");
        //        str.replace("OAMessageLovTextInputBean", "");
        //        str.replace("OAMessagePromptBean","");
        str = str.replace("OAMessageRadioButtonBean", "RichSelectBooleanRadio");
        str = str.replace("OAMessageRadioGroupBean", "RichSelectOneRadio");
        //        str.replace("OAMessageRatingBarBean","");
        str = str.replace("OAMessageRichTextEditorBean", "RichTextEditor");
        str = str.replace("OAMessageSpinBoxBean", "RichInputNumberSpinbox");
        str = str.replace("OAMessageStyledTextBean", "RichOutputText");
        str = str.replace("OAMessageTextInputBean", "RichInputText");
        //        str.replace("OAMultipleSelectionBean", "");
        //        str.replace("OANavigationBarBean","");
        //        str.replace("OAOptionBean","");
        //        str.replace("OAPageButtonBarBean","");
        //        str.replace("OAPageHeaderLayoutBean","");
        //        str.replace("OAPageLayoutBean","");
        //        str.replace("OAPanelSplitterBean","");
        str = str.replace("OAPopupBean", "RichPopup");
        //        str.replace("OAPortletStyleSheetBean","");
        //        str.replace("OAProcessingBean","");
        //        str.replace("OAQueryBean","");
        //        str.replace("OAQuickLinksBean", "");
        //        str.replace("OARadioButtonBean", "");
        //        str.replace("OARadioGroupBean", "");
        str = str.replace("OARawTextBean", "RichOutputFormatted");
        //        str.replace("OARepeaterBean","");
        //        str.replace("OAResetButtonBean", "");
        //        str.replace("OARichContainerBean","");
        //        str.replace("OARichTextEditorBean", "");
        //        str.replace("OARowLayoutBean","");
        //        str.replace("OAScriptBean","");
        //        str.replace("OASelectionButtonBean","");
        str = str.replace("OASeparatorBean", "RichSeparator");
        //        str.replace("OAServletIncludeBean","");
        str = str.replace("OAShuttleBean", "RichSelectManyShuttle");
        //        str.replace("OASideBarBean","");
        //        str.replace("OASideNavBean","");
        //        str.replace("OASingleSelectionBean", "");
        //        str.replace("OASortableHeaderBean","");
        str = str.replace("OASpacerBean", "RichSpacer");
        //        str.replace("OASpacerCellBean","");
        //        str.replace("OASpacerRowBean","");
        //        str.replace("OASpringboardBean","");
        //        str.replace("OASpringboardItemBean","");
        //        str.replace("OAStackLayoutBean","");
        //        str.replace("OAStaticStyledTextBean", "");
        //        str.replace("OAStyledItemBean","");
        //        str.replace("OAStyledListBean","");
        //        str.replace("OAStyledTextBean", "");
        //        str.replace("OAStyleSheetBean","");
        str = str.replace("OASubmitButtonBean", "HtmlCommandButton");
        //        str.replace("OASubTabBarBean","");
        //        str.replace("OASubTabLayoutBean","");
        //        str.replace("OASwitcherBean","");
        //        str.replace("OATabBarBean","");
        //        str.replace("OATableBean","");
        //        str.replace("OATableFooterBean","");
        //        str.replace("OATableLayoutBean","");
        //        str.replace("OATextInputBean","");
        //        str.replace("OATileBean","");
        //        str.replace("OATileHeaderBean","");
        //        str.replace("OATipBean","");
        //        str.replace("OATotalRowBean","");
        //        str.replace("OATrainBean","");
        //        str.replace("OATrainStepBean","");
        //        str.replace("OATreeBean","");
        //        str.replace("OATreeChildBean","");
        //        str.replace("OATreeDefinitionBean","");
        //        str.replace("OATreeLevelBean","");
        //        str.replace("OATreeRecursiveBean","");
        //        str.replace("OATryBean","");
        //        str.replace("OAUrlIncludeBean","");
        //        str.replace("OAWebBean","");
        str = str.replace("OAException", "JboException");
        FileReaderWritter.writeFile(str, path, app);
    }
}
