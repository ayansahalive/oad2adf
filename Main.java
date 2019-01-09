package conv;

import java.io.File;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private Map filePaths = new HashMap();

    /**
     * Sample code for testing
     * @param args
     */
    public static void main(String[] args) {
        String app = "inspect"; // change here
        String ret = startConverter(app);
        System.out.println(ret);
        ErrorAndLog.handleLog(app, ret);
    }

    /**
     * Call this from FE application
     */
    public static String startConverter(String app) {
        System.out.println("Start Conv: startConverter " + app);
        ErrorAndLog.handleLog(app, "Start Conv: startConverter " + app);
        Main obj = new Main(app);
        String src = obj.getSrc();
        String dest = obj.getDest();
        String repo = obj.getRepo();
        String returnVal = "Error encountered while converting. Please check the " + app + "Errors.txt";

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
                ErrorAndLog.handleErrors(app, "No Errors.");
                ErrorAndLog.handleErrors(app, "Please open the .jws from Jdeveloper 12.1.3 and compile the projects.");
            } catch (Exception e) {
                try {
                    String destination =
                        System.getenv("ADF_DESTINATION") + FileReaderWritter.getSeparator() + app + "Errors.txt";
                    File f = new File(destination);
                    PrintStream ps = new PrintStream(f);
                    e.printStackTrace();
                    e.printStackTrace(ps);
                    removeFolder(app, "ADF");
                    removeFolder(app, "OAF");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ErrorAndLog.handleErrors(app, "Error while printing exceptions.");
                }
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
        ErrorAndLog.handleLog(app, "Start Conv: removeExtracted " + app + " " + type);
        // handle OS here
        Runtime run = Runtime.getRuntime();
        Process zipDel = null;
        String dir = System.getProperty("user.dir");
        String cmd =
            dir + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "conv" +
            FileReaderWritter.getSeparator() + "removeExtracted.bat " + app + " " + type;
        zipDel = run.exec(cmd);
        zipDel.waitFor();
        zipDel.destroy();
        System.out.println("End Conv: removeExtracted ");
    }

    /**
     * Read source dir
     * @param path
     * @param level
     */
    private void readDir(String path, int level) throws Exception {
        //        System.out.println("Start Conv: readDir " + path + " " + level + " " + this.getSrc());
        //        ErrorAndLog.handleLog(app, "Start Conv: readDir " + path + " " + level + " " + this.getSrc());
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
        //        System.out.println("End Conv: readDir");
    }

    /**
     * check which xml which needs to be handled
     * @param path
     */
    private void xmlEditor(String path) throws Exception {
        System.out.println("Start Conv: xmlEditor " + path);
        ErrorAndLog.handleLog(app, "Start Conv: xmlEditor " + path);
        DirCreator.copyOAFDTD(this.getRepo(), path, app);
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inputFile);
        Element root = doc.getDocumentElement();
        String type = root.getTagName();
        if (type.equals("AppModule"))
            AMXml.handleAMXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.equals("ViewObject")) {
            VOXml.handleVOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
            String newPath = path;
            newPath = newPath.replace(src + FileReaderWritter.getSeparator(), "");
            String Dest =
                dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() + "Model" +
                FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
                FileReaderWritter.getSeparator() + newPath;

            filePaths.put(root.getAttribute("Name"), Dest);
        } else if (type.equals("Entity"))
            EOXml.handleEOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.equals("Association"))
            AOXml.handleAOXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.contains("ViewLink"))
            VLXml.handleVLXml(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc());
        else if (type.contains("page"))
            JSFGen.handlePage(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc(), filePaths);
        else if (type.contains("oa:"))
            JSFGen.handleRegion(path, this.getApp(), this.getDest(), this.getRepo(), this.getSrc(), filePaths);     
        else if (path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1).contains("server.xml"))
            ; // do nothing
        else
            FileReaderWritter.unalteredFile(path, this.getApp(), this.getDest(), this.getSrc());
        System.out.println("End Conv: xmlEditor");
    }

}
