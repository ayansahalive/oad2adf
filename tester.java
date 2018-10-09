package conv;


public class tester {
    public tester() {
        super();
    }

    public static void main(String[] args) {
        tester tester = new tester();
        String path = "xxnuc.oracle.apps.inv.hello.server.CustomExtAppModuleDefImpl";

        path = path.substring(path.lastIndexOf("."));
        String retPath = "model" + path;
        System.out.println(retPath);
    }
}
