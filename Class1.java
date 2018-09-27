package conv;


public class Class1 {
    public Class1() {
        super();
    }

    public static void main(String[] args) {
        Class1 class1 = new Class1();
        String path = "C:\\forw\\server.xml";
        System.out.println(path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1));
    }
}
