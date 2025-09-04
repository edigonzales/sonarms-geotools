package ch.so.agi.gretl.steps;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.raster.RangeLookupProcess;
import org.jaitools.numeric.Range;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.util.ProgressListener;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class RasterReclassify {

    private RasterReclassify() {}

    /**
     * Reclassify using consecutive breakpoints.
     * Example breaks: {0,55,60,65,70,500} -> [0,55), [55,60), [60,65), [65,70), [70,500]
     * Class values are 1..N by default.
     */
    public static GridCoverage2D reclassifyByBreaks(
            GridCoverage2D source,
            int band,
            double[] breaks,
            double noData
    ) {
        int bins = validateBreaks(breaks);
        int[] classes = new int[bins];
        for (int i = 0; i < bins; i++) classes[i] = i + 1; // 1..N
        return reclassifyByBreaks(source, band, breaks, classes, noData);
    }

    /**
     * Reclassify using consecutive breakpoints with explicit class values.
     * Length of classValues must be breaks.length - 1.
     */
    public static GridCoverage2D reclassifyByBreaks(
            GridCoverage2D source,
            int band,
            double[] breaks,
            int[] classValues,
            double noData
    ) {
        Objects.requireNonNull(source, "source");
        int bins = validateBreaks(breaks);

        if (classValues == null || classValues.length != bins) {
            throw new IllegalArgumentException("classValues length must be breaks.length - 1");
        }

        // Build raw List<org.jaitools.numeric.Range> expected by RangeLookupProcess
        @SuppressWarnings("rawtypes")
        List<Range> ranges = new ArrayList<>(bins);
        for (int i = 0; i < bins; i++) {
            boolean minInc = true;               // left-closed
            boolean maxInc = (i == bins - 1);    // last bin right-closed, others right-open
            ranges.add(Range.create(breaks[i], minInc, breaks[i + 1], maxInc));
        }

        RangeLookupProcess proc = new RangeLookupProcess();
        return proc.execute(
                source,
                Integer.valueOf(band),
                ranges,
                classValues,
                Double.valueOf(noData),
                (ProgressListener) null
        );
    }

    /** Overload that accepts a List of breaks. */
    public static GridCoverage2D reclassifyByBreaks(
            GridCoverage2D source,
            int band,
            List<Double> breaks,
            double noData
    ) {
        double[] arr = breaks.stream().mapToDouble(Double::doubleValue).toArray();
        return reclassifyByBreaks(source, band, arr, noData);
    }

    // --- helpers ---
    private static int validateBreaks(double[] breaks) {
        Objects.requireNonNull(breaks, "breaks");
        if (breaks.length < 2) {
            throw new IllegalArgumentException("Provide at least two break values");
        }
        // Ensure strictly increasing
        for (int i = 1; i < breaks.length; i++) {
            if (!(breaks[i] > breaks[i - 1])) {
                throw new IllegalArgumentException("Breaks must be strictly increasing: " + Arrays.toString(breaks));
            }
        }
        return breaks.length - 1; // number of bins
    }
    
    public static GridCoverage2D ensureCrs(GridCoverage2D src, CoordinateReferenceSystem defaultCrs) {
        CoordinateReferenceSystem crs = src.getCoordinateReferenceSystem2D();
        boolean missing =
                crs == null ||
                (crs instanceof EngineeringCRS); // “pixel” CRS with no real world meaning

        if (!missing) {
            return src; // already has a real CRS
        }

        // Use the existing envelope extents but assign your CRS
        Envelope2D env = src.getEnvelope2D(); // minX, minY, width, height
        ReferencedEnvelope refEnv;
        if (env != null && !Double.isNaN(env.getWidth()) && !Double.isNaN(env.getHeight())) {
            refEnv = new ReferencedEnvelope(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY(), defaultCrs);
        } else {
            // Fallback if the source truly has no envelope info:
            RenderedImage img = src.getRenderedImage();
            // Treat pixel coordinates as the CRS (1 unit per pixel, origin at (0,0)).
            refEnv = new ReferencedEnvelope(0, img.getWidth(), 0, img.getHeight(), defaultCrs);
        }

        GridCoverageFactory gcf = new GridCoverageFactory();
        return gcf.create(src.getName().toString(), src.getRenderedImage(), refEnv);
    }
}
