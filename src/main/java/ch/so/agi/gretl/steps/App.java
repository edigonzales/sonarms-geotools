package ch.so.agi.gretl.steps;

import java.io.IOException;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, NoSuchAuthorityCodeException, FactoryException {
        System.out.println(new App().getGreeting());
        
        
        GT_RastReclassify foo = new GT_RastReclassify();
        foo.execute("src/test/data/Beispiel_Rasterfile.asc");
        
        
    }
}
