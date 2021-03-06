package conv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;


public class FileReaderWritter {
    public FileReaderWritter() {
        super();
    }

    /**
     * Write contenets of a file to a directory
     * @param contents
     * @param path
     */
    protected static void writeFile(String contents, String path, String app) throws Exception {
        //        System.out.println("Start Conv: writeFile " + path + " " + app);
        //        ErrorAndLog.handleLog(app, "Start Conv: writeFile " + path + " " + app);
        if (null != contents && !"".equals(contents) && !"null".equals(contents)) {
            String dest = path.substring(0, path.lastIndexOf(getSeparator()));
            new File(dest).mkdirs();

            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] strToBytes = contents.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        }
        //        System.out.println("End Conv: writeFile");
    }

    /**
     * copy file from source to destination
     * @param src
     * @param dest
     */
    protected static void copyFile(String src, String dest, String app) throws Exception {
        System.out.println("Start Conv: copyFile " + src + " " + dest);
        ErrorAndLog.handleLog(app, "Start Conv: copyFile " + src + " " + dest);
        String Dest = dest.substring(0, dest.lastIndexOf(getSeparator()));
        new File(Dest).mkdirs();

        InputStream is = null;
        OutputStream os = null;
        new File(dest);

        is = new FileInputStream(src);
        os = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }

        is.close();
        os.close();
        System.out.println("End Conv: copyFile");
    }

    /**
     * return character contents of a file
     * @param path
     * @return
     */
    protected static String getCharContents(String path, String app) throws Exception {
        System.out.println("Start Conv: getCharContents " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: getCharContents " + path + " " + app);
        BufferedReader reader;
        String ret = "";

        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        while (line != null) {
            ret = ret + line + "\n";
            line = reader.readLine();
        }
        reader.close();
        System.out.println("End Conv: getCharContents ");
        return ret;
    }

    /**
     * finalizer and generate a modified XML file
     * @param doc
     * @param Dest
     */
    protected static void writeXMLFile(Document doc, String Dest, String app) throws Exception {
        System.out.println("Start Conv: writeXMLFile " + Dest + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: writeXMLFile " + Dest + " " + app);

        String dest = Dest.substring(0, Dest.lastIndexOf(getSeparator()));
        new File(dest).mkdirs();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DocumentType doctype = doc.getDoctype();
        if (null != doctype && null != doctype.getSystemId())
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
        Source source = new DOMSource(doc);
        Result result = new StreamResult(new File(Dest));
        transformer.transform(source, result);

        System.out.println("End Conv: writeXMLFile");
    }

    /**
     * append contents to a text file
     * @param content
     * @param filePath
     * @throws Exception
     */
    protected static void appendFile(String contents, String path, String app) throws Exception {
        //        System.out.println("Start Conv: appendFile " + path + " " + app);
        //        ErrorAndLog.handleLog(app, "Start Conv: appendFile " + path + " " + app);
        FileWriter fw = null;
        fw = new FileWriter(path, true);
        if (null == fw)
            writeFile(contents, path, app);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("\n" + contents);
        bw.newLine();
        bw.close();
        fw.close();
        //        System.out.println("End Conv: appendFile ");
    }

    /**
     * convert attributes to initcaps
     * @param param
     * @return
     */
    public static String toInitCap(String param, String app) {
        System.out.println("Start Conv: toInitCap " + param + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: toInitCap " + param + " " + app);
        if (param != null && param.length() > 0) {
            param = param.toLowerCase();
            char[] array = param.toCharArray();
            array[0] = Character.toUpperCase(array[0]);
            return new String(array);
        } else {
            System.out.println("End Conv: toInitCap ");
            return "";
        }
    }

    /**
     * get OS file separator
     * @return
     */
    protected static String getSeparator() {
        String separator = "/";
        String os = System.getProperty("os.name");
        if (os.contains("Windows") || os.contains("windows"))
            separator = "\\";
        return separator;
    }

    /**
     * Copy files that are not handled in code
     * @param path
     * @param app
     * @param dest
     * @param src
     * @throws Exception
     */
    protected static void unalteredFile(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: unalteredFile " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: unalteredFile " + path + " " + app + " " + dest + " " + src);
        String Dest = getModelDestinationPath(path, app, dest, src);
        if (path.contains(".java")) {
            String contents = FileReaderWritter.getCharContents(path, app);
            String temp = contents.substring(0, contents.indexOf("{"));
            String changed = DirCreator.replaceImports(temp, app);
            contents = changed + contents.substring(contents.indexOf("{"));
            FileReaderWritter.writeFile(contents, path, app);
        } else
            copyFile(path, Dest, app);

        System.out.println("End Conv: unalteredFile ");
    }

    /**
     * get the destination path of the files for Model
     * @param path
     * @param app
     * @param dest
     * @param src
     * @return
     * @throws Exception
     */
    protected static String getModelDestinationPath(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: getModelDestinationPath " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: getModelDestinationPath " + path + " " + app + " " + dest + " " + src);
        String newPath = path;
        newPath = newPath.replace(src + getSeparator(), "");
        String Dest =
            dest + getSeparator() + app + getSeparator() + "Model" + getSeparator() + "src" + getSeparator() + "model" +
            getSeparator() + newPath;

        String x = Dest.substring(0, Dest.lastIndexOf(getSeparator()));
        new File(x).mkdirs();
        System.out.println("End Conv: getModelDestinationPath");
        return Dest;
    }

    protected static String getViewDestinationPath(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: getViewDestinationPath " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: getViewDestinationPath " + path + " " + app + " " + dest + " " + src);
        String newPath = path;
        newPath = newPath.replace(src + getSeparator(), "");
        String Dest =
            dest + getSeparator() + app + getSeparator() + "ViewController" + getSeparator() + "public_html" +
            getSeparator() + "view" + getSeparator() + newPath;

        String x = Dest.substring(0, Dest.lastIndexOf(getSeparator()));
        new File(x).mkdirs();
        System.out.println("End Conv: getViewDestinationPath");
        return Dest;
    }

    protected static String getBeanDestinationPath(String path, String app, String dest, String src) throws Exception {
        System.out.println("Start Conv: getBeanDestinationPath " + path + " " + app + " " + dest + " " + src);
        ErrorAndLog.handleLog(app, "Start Conv: getBeanDestinationPath " + path + " " + app + " " + dest + " " + src);
        String newPath = path;
        newPath = newPath.replace(src + getSeparator(), "");
        String Dest =
            dest + getSeparator() + app + getSeparator() + "ViewController" + getSeparator() + "src" + getSeparator() +
            "view" + getSeparator() + newPath;

        String x = Dest.substring(0, Dest.lastIndexOf(getSeparator()));
        new File(x).mkdirs();
        System.out.println("End Conv: getBeanDestinationPath");
        return Dest;
    }

    protected static String getViewPageDefDestinationPath(String path, String app, String dest) throws Exception {
        System.out.println("Start Conv: getViewDestinationPath " + path + " " + app + " " + dest);
        ErrorAndLog.handleLog(app, "Start Conv: getViewDestinationPath " + path + " " + app + " " + dest);
        String newPath = path;
        String Dest =
            dest + getSeparator() + app + getSeparator() + "ViewController" + getSeparator() + "adfmsrc" +
            getSeparator() + "view" + getSeparator() + newPath;

        new File(Dest).mkdirs();
        System.out.println("End Conv: getViewDestinationPath");
        return Dest;
    }
}
