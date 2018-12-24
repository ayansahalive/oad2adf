package conv;

import java.io.File;


public class ErrorAndLog {
    public ErrorAndLog() {
        super();
    }

    /**
     * print log
     * @param app
     * @param contents
     */
    protected static void handleErrors(String app, String contents) {
        try {
            String dest = System.getenv("ADF_DESTINATION") + FileReaderWritter.getSeparator() + app + "Errors.txt";
            File f = new File(dest);
            if (!f.exists()) {
                FileReaderWritter.writeFile(contents , dest, app);
            } else
                FileReaderWritter.appendFile(contents, dest, app);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * print error
     * @param app
     * @param contents
     */
    protected static void handleLog(String app, String contents) {
        try {
            String dest = System.getenv("ADF_DESTINATION") + FileReaderWritter.getSeparator() + app + "Log.txt";
            File f = new File(dest);
            if (!f.exists()) {
                FileReaderWritter.writeFile("Starting OAF to ADF Conversion of " + app + "\n", dest, app);
                FileReaderWritter.appendFile(contents, dest, app);
            } else
                FileReaderWritter.appendFile(contents, dest, app);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
