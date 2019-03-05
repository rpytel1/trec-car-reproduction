package tutorial.index;

import java.io.IOException;


public class CreateIndex {


    public static void main(String[] args) {
        try {
            ImprovedIndexer improvedIndexer = new ImprovedIndexer();
            improvedIndexer.generateIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
