package conv;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class BC4JGen {
    public BC4JGen() {
        super();
    }

    /**
     * handle bc4j entry for AM
     * @param dest
     * @param repo
     * @throws Exception
     */
    protected static void handleBC4J(String dest, String repo, String app) throws Exception { // dest is the AMXML name
        System.out.println("Start Conv: handleBC4J " + dest + " " + repo + " " + app);

        String destTemp = dest.substring(0, dest.lastIndexOf(FileReaderWritter.getSeparator()));
        String amName = dest.substring(dest.lastIndexOf(FileReaderWritter.getSeparator()) + 1);
        amName = amName.replace(".xml", "");
        System.out.println(amName);

        File f =
            new File(destTemp + FileReaderWritter.getSeparator() + "common" + FileReaderWritter.getSeparator() +
                     "bc4j.xcfg");
        if (f.exists() && !f.isDirectory()) {
            appendBC4J(amName, destTemp, app);
        } else {
            createBC4J(destTemp, repo);
            appendBC4J(amName, destTemp, app);
        }
    }

    /**
     * create a blank one
     * @param dest
     * @param repo
     * @throws Exception
     */
    private static void createBC4J(String dest, String repo) throws Exception {
        System.out.println("Start Conv: createBC4J " + dest + " " + repo);

        String src =
            repo + FileReaderWritter.getSeparator() + "testApp" + FileReaderWritter.getSeparator() + "Model" +
            FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator() + "model" +
            FileReaderWritter.getSeparator() + "common" + FileReaderWritter.getSeparator() + "bc4j.xcfg";

        FileReaderWritter.copyFile(src,
                                   dest + FileReaderWritter.getSeparator() + "common" +
                                   FileReaderWritter.getSeparator() + "bc4j.xcfg");
        System.out.println("End Conv: createBC4J ");
    }

    /**
     * add entries
     * @param amName
     * @param dest
     * @throws Exception
     */
    private static void appendBC4J(String amName, String dest, String app) throws Exception {
        System.out.println("Start Conv: appendBC4J " + amName + " " + dest + " " + app);

        File bc4j =
            new File(dest + FileReaderWritter.getSeparator() + "common" + FileReaderWritter.getSeparator() +
                     "bc4j.xcfg");
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        DocumentBuilder d = f.newDocumentBuilder();
        Document doc = d.parse(bc4j);
        Node rootNode = doc.getDocumentElement();

        String Adfdest = System.getenv("ADF_DESTINATION");
        String destVal = dest;
        destVal =
            destVal.replace(Adfdest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() +
                            "Model" + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator(), "");
        destVal = destVal.replace(FileReaderWritter.getSeparator(), ".");

        Element AppModuleConfigBag = doc.createElement("AppModuleConfigBag");
        AppModuleConfigBag.setAttribute("ApplicationName", destVal + "." + amName);

        Element AppModuleConfig1 = doc.createElement("AppModuleConfig");
        AppModuleConfig1.setAttribute("name", amName + "Local");
        AppModuleConfig1.setAttribute("ApplicationName", destVal + "." + amName);
        AppModuleConfig1.setAttribute("DeployPlatform", "LOCAL");
        AppModuleConfig1.setAttribute("java.naming.factory.initial", "oracle.jbo.common.JboInitialContextFactory");

        Element Database1 = doc.createElement("Database");
        Database1.setAttribute("jbo.locking.mode", "optimistic");
        Database1.setAttribute("jbo.TypeMapEntries", "OracleApps");

        Element Security1 = doc.createElement("Security");
        Security1.setAttribute("AppModuleJndiName", destVal + "." + amName);


        AppModuleConfig1.appendChild(Database1);
        AppModuleConfig1.appendChild(Security1);
        AppModuleConfigBag.appendChild(AppModuleConfig1);


        /////////////

        Element AppModuleConfig2 = doc.createElement("AppModuleConfig");
        AppModuleConfig2.setAttribute("name", amName + "Shared");
        AppModuleConfig2.setAttribute("ApplicationName", destVal + "." + amName);
        AppModuleConfig2.setAttribute("DeployPlatform", "LOCAL");
        AppModuleConfig2.setAttribute("java.naming.factory.initial", "oracle.jbo.common.JboInitialContextFactory");

        Element AM_Pooling = doc.createElement("AM-Pooling");
        AM_Pooling.setAttribute("jbo.ampool.isuseexclusive", "false");
        AM_Pooling.setAttribute("jbo.ampool.maxpoolsize", "1");

        Element Database2 = doc.createElement("Database");
        Database2.setAttribute("jbo.locking.mode", "optimistic");
        Database2.setAttribute("jbo.TypeMapEntries", "OracleApps");

        Element Security2 = doc.createElement("Security");
        Security2.setAttribute("AppModuleJndiName", destVal + "." + amName);


        AppModuleConfig2.appendChild(AM_Pooling);
        AppModuleConfig2.appendChild(Database2);
        AppModuleConfig2.appendChild(Security2);
        AppModuleConfigBag.appendChild(AppModuleConfig2);

        rootNode.appendChild(AppModuleConfigBag);

        FileReaderWritter.writeXMLFile(doc,
                                       dest + FileReaderWritter.getSeparator() + "common" +
                                       FileReaderWritter.getSeparator() + "bc4j.xcfg");

        System.out.println("End Conv: appendBC4J ");
    }
}
