package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.io.GridCoverage2DReader;

public class GT_RastReclassify {

    
    public void execute(String rasterFile) throws IllegalArgumentException, IOException, NoSuchAuthorityCodeException, FactoryException {
        AbstractGridFormat format = GridFormatFinder.findFormat( new File(rasterFile) );

        System.out.println(rasterFile);
        
        GridCoverage2DReader reader = format.getReader(new File(rasterFile));
        GridCoverage2D cov = reader.read(null);
        
        CoordinateReferenceSystem swiss = CRS.decode("EPSG:2056", true);
        GridCoverage2D stamped = RasterReclassify.ensureCrs(cov, swiss);
        
        
        double[] breaks = {0, 55, 60, 65, 70, 500};
        int[] classValues = {0, 55, 60, 65, 70};
        double noData = -100;
        GridCoverage2D out1 = RasterReclassify.reclassifyByBreaks(stamped, 0, breaks, classValues, noData);
     
        

        GeoTiffWriter writer = null;
        try {
            writer = new GeoTiffWriter(new File("/Users/stefan/tmp/reclass.tif"));
            writer.write(out1, null);
        } finally {
            if (writer != null) writer.dispose();  // important: releases resources
        }
    }
}
