package tutorial.index;

import java.io.IOException;


public class CreateIndex {


    public static void main(String[] args) {
        try {
            ImprovedIndexer improvedIndexer = new ImprovedIndexer();
            if (args[0].startsWith("-indexDir=")) {
                args[0] = args[0].substring(10);
            }
            if (args[1].startsWith("-corpusDir=")) {
                args[1] = args[1].substring(11);
            }
            System.out.println(args[0]);
            System.out.println(args[1]);
           improvedIndexer.generateIndex(args[0],args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
