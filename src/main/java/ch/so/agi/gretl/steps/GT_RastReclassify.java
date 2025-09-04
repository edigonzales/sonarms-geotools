package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.GridCoverage2DReader;

public class GT_RastReclassify {

    
    public void execute(String rasterFile) throws IllegalArgumentException, IOException {
        File f = new File("ArcGrid.asc");
        AbstractGridFormat format = GridFormatFinder.findFormat( f );

        GridCoverage2DReader reader = format.getReader(f);
        GridCoverage2D gc = reader.read(null);
     
     
     
    }
}
