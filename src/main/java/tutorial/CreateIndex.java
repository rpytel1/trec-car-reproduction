package tutorial;

import java.io.IOException;


public class CreateIndex {


    public static void main(String[] args) {
        SearchTester tester;
        try {
            ImprovedIndexer improvedIndexer = new ImprovedIndexer();
            improvedIndexer.generateIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
