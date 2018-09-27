package conv;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class Main {
    public Main(String app) {
        super();
        String src = System.getenv("OAF_SOURCE");
        String dest = System.getenv("ADF_DESTINATION");
        String repo = System.getenv("ADF_REPOSITORY");
        this.setApp(app);
        this.setDest(dest);
        this.setRepo(repo);
        this.setSrc(src);
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getDest() {
        return dest;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getRepo() {
        return repo;
    }
    private String app;
    private String src;
    private String dest;
    private String repo;

    /**
     * Sample code for testing
     * @param args
     */
    public static void main(String[] args) {
        String app = "eaf"; // change here
        String ret = startConverter(app);
        System.out.println(ret);
    }

    /**
     * Call this from FE application
     */
    public static String startConverter(String app) {
        System.out.println("Start Conv: startConverter " + app);

        Main obj = new Main(app);
        String src = obj.getSrc();
        String dest = obj.getDest();
        String repo = obj.getRepo();
        String returnVal = "";

        if (null == src || "".equals(src) || "null".equals(src) || null == dest || "".equals(dest) ||
            "null".equals(dest) || null == repo || "".equals(repo) || "null".equals(repo)) {
            String msg = "Please set environemnt variables OAF_SOURCE , ADF_DESTINATION, ADF_REPOSITORY.";
            ErrorAndLog.handleErrors(app, msg);
            returnVal = msg;
        } else {
            try {
                String ret =
                    DirCreator.unzip(src + FileReaderWritter.getSeparator() + obj.getApp() + ".zip",
                                     src + FileReaderWritter.getSeparator() + obj.getApp());
                if ("success".equalsIgnoreCase(ret)) {
                    obj.setSrc(obj.getSrc() + FileReaderWritter.getSeparator() + app);
                    removeFolder(app, "ADF");

                    DirCreator.createDir(obj.getApp(), dest, repo);

                    obj.readDir(obj.getSrc(), 0);

                    removeFolder(app, "OAF");

                    System.out.println("End Conv: startConverter ");
                }
                returnVal = "Success.";
                ErrorAndLog.handleLog(app, "Completed Conversion. ");
            } catch (Exception e) {
                ErrorAndLog.handleErrors(app, e.toString());
                ErrorAndLog.handleLog(app, "Conversion Error. Please check " + app + "Errors.txt for details.");
            }
        }
        return returnVal;
    }

    /**
     * remove the unzipped dir
     * @param app
     */
    private static void removeFolder(String app, String type) throws Exception {
        System.out.println("Start Conv: removeExtracted " + app + " " + type);
        // handle OS here
        Runtime run = Runtime.getRuntime();
        Process zipDel = null;
        String dir = System.getProperty("user.dir");
        String cmd =
            dir + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "conv" +
            FileReaderWritter.getSeparator() + "removeExtracted.bat " + app + " " + type;
        System.out.println(cmd);
        zipDel = run.exec(cmd);
        zipDel.waitFor();
        System.out.println(zipDel.exitValue());
        zipDel.destroy();
        System.out.println("End Conv: removeExtracted ");
    }

    /**
     * Read source dir
     * @param path
     * @param level
     */
    private void readDir(String path, int level) throws Exception {
        System.out.println("Start Conv: readDir " + path + " " + level + " " + this.getSrc());
        File dir = new File(path);
        File[] firstLevelFiles = dir.listFiles();
        if (firstLevelFiles != null && firstLevelFiles.length > 0) {
            for (File aFile : firstLevelFiles) {
                if (aFile.isDirectory()) {
                    // dir
                    this.readDir(aFile.getAbsolutePath(), level + 1);
                } else {
                    // file
                    if (aFile.toString().contains(".xml")) {
                        this.xmlEditor(aFile.getAbsolutePath());
                    }
                }
            }
        }
        System.out.println("End Conv: readDir");
    }

    /**
     * check which xml which needs to be handled
     * @param path
     */
    private void xmlEditor(String path) throws Exception {
        System.out.println("Start Conv: xmlEditor " + path);
        DirCreator.copyOAFDTD(this.getRepo(), path);
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inputFile);
        Element root = doc.getDocumentElement();
        String type = root.getTagName();
        if (type.equals("AppModule"))
            AMXml.handleAMXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.equals("ViewObject"))
            VOXml.handleVOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.equals("Entity"))
            EOXml.handleEOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.equals("Association"))
            AOXml.handleAOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.contains("ViewLink"))
            VLXml.handleVLXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.contains("page"))
            JSFGen.handlePage(path, this.getApp(), this.getDest(), this.getRepo());
        else if (path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1).contains("server.xml") ||
                 path.contains("bc4j.xcfg"))
            ; // do nothing
        else
            FileReaderWritter.unalteredFile(path, this.getApp(), this.getDest(), this.getSrc());
        System.out.println("End Conv: xmlEditor");
    }

}
