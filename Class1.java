package conv;


public class Class1 {
    public Class1() {
        super();
    }
    
    public static String printOne(String name) {
        String returnVal = name + " This is my name.";
        System.out.println(returnVal);
        return returnVal;
    }
    
    public static void main(String[] args) { 
            // The comment below is magic.. 
        String s = "printOne(\"Sriknath\")";
            // \u000d printOne("Srikanth"); 
            System.out.println(s +" some new value");
            
        } 
//    public static void main(String[] args) {
//        Class1 class1 = new Class1();
//        String path = "C:\\forw\\server.xml";
//        System.out.println(path.substring(path.lastIndexOf(FileReaderWritter.getSeparator()) + 1));
//    }
}
