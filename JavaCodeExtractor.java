package conv;

import java.util.ArrayList;
import java.util.List;

public class JavaCodeExtractor {
    public JavaCodeExtractor() {
        super();
    }

    private List<String> vec = new ArrayList<>();

    public void setVec(List<String> vec) {
        this.vec = vec;
    }

    public List<String> getVec() {
        return vec;
    }


    /**
     * Copy conetents of a java file into elements of a vector by declaration and method signature
     * @param path
     * @return
     * @throws Exception
     */
    protected List<String> start(String path, String app) throws Exception {
        System.out.println("Start Conv: start " + path + " " + app);
        ErrorAndLog.handleLog(app, "Start Conv: start " + path + " " + app);
        String str = FileReaderWritter.getCharContents(path, app);

        int classStart = str.indexOf("{");
        int num = classStart + 1;
        String opener = str.substring(0, num).trim();

        // handle multiline comments
        int start = -1;
        int end = -1;
        start = opener.indexOf("/*");
        end = opener.indexOf("*/");
        if (start != -1 && end != -1) {
            opener = opener.replace(opener.subSequence(start, end + 2), "");
        }
        opener = opener.trim();
        getVec().add(opener);
        str = str.substring(classStart + 1);
        this.segregator(str, app);

        getVec().add("}");
        System.out.println("End Conv: start ");
        return getVec();
    }

    /**
     * recursive method for segregation
     * @param str
     */
    private void segregator(String str, String app) {
        int size = str.length();
        int head = 0;

        int startBraces = str.indexOf("{");
        int semicolon = str.indexOf(";");

        if (startBraces == -1 && semicolon != -1)
            startBraces = semicolon + 1;
        else if (semicolon == -1 && startBraces != -1)
            semicolon = startBraces + 1;

        if (semicolon < startBraces) {
            int num = semicolon + 1;
            String declaration = str.substring(0, num).trim();
            declaration = DirCreator.replaceImports(declaration, app);
            getVec().add(declaration);
            head = semicolon + 1;
            str = str.substring(head);
        } else if (semicolon > startBraces) {
            int counter = 1;
            int len = str.length();
            String closed = "";
            for (int i = 0; i < len; i++) {
                if (str.substring(i, i + 1).equals("{")) {
                    closed = "";
                    ++counter;
                } else if (str.substring(i, i + 1).equals("}")) {
                    closed = "yes";
                    --counter;
                }

                if (counter == 1 && closed.equals("yes")) {
                    int num = i - head + 1;
                    String method = str.substring(head, num).trim();
                    method = DirCreator.replaceImports(method, app);
                    getVec().add(method);
                    head = i + 1;
                    str = str.substring(head);
                    break;
                }
            }
        } else {
            return;
        }

        if (head < size && str != null)
            segregator(str, app);
    }

}
