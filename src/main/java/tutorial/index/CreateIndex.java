package tutorial.index;

import tutorial.search.SearchTester;

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
