package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static processing.core.PApplet.*;

/**
 * Created by mukel on 6/12/15.
 */
public class BoardDetector {
    PApplet parent;    

    public BoardDetector(PApplet parent) {
        this.parent = parent;
    }

    public static RawImage preprocessImage(RawImage image) {    	
        RawImage filtered = ImageProcessing.colorFilterInPlace(image,
                0.34f * 255, 0.55f * 255,
                120, 256,
                30, 250
        );
        RawImage blurred = ImageProcessing.gaussianBlur(ImageProcessing.gaussianBlur(filtered));        
        RawImage t = ImageProcessing.thresholdFilterInPlace(filtered, 0,  255,  0,  255,  60, 255);
        return ImageProcessing.sobel(t);
    }

    public static PVector intersection(PVector line1, PVector line2) {
        float d = cos(line2.y)*sin(line1.y) - cos(line1.y)*sin(line2.y);
        if (abs(d) < 1e-4)
            return new PVector(-1, -1);
        float x = (line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d;
        float y = (-line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d;
        return new PVector(x, y);
    }

    public List<PVector[]> getCorners(PImage source) {

		int factor = max(1, max(source.width, source.height) / 100);
		source.resize(source.width / factor, source.height / factor);
    	RawImage image = new RawImage(source);
		
        image = preprocessImage(image);
       // parent.image(image.toPImage(parent), 0, 0);
        List<PVector> lines = ImageProcessing.hough(image, 6);
        //System.out.println("Lines found = " + lines.size());
        if (lines.size() < 4)
            return null;

        QuadGraph qg = new QuadGraph();
        qg.build(lines, parent.width, parent.height);
        List<int[]> quads = qg.findCycles();
        
        int imageArea = image.width * image.height;

        float largerArea = -1;
        // Filter malformed quads
        List<PVector[]> bestQuads = new ArrayList<>();
        for (int[] quad : quads) {
            PVector line1 = lines.get(quad[0]);
            PVector line2 = lines.get(quad[1]);
            PVector line3 = lines.get(quad[2]);
            PVector line4 = lines.get(quad[3]);

            PVector c1 = intersection(line1, line2);
            PVector c2 = intersection(line2, line3);
            PVector c3 = intersection(line3, line4);
            PVector c4 = intersection(line4, line1);

            if (QuadGraph.isConvex(c1, c2, c3, c4) &&
                    QuadGraph.nonFlatQuad(c1, c2, c3, c4) ) {
                    //QuadGraph.validArea(c1, c2, c3, c4, parent.width * parent.height, width * height / 100)) {
                float area = getQuadArea(c1, c2, c3, c4);
                if (imageArea / 20 < area && area  < imageArea) {
                	bestQuads.add(new PVector[]{c1, c2, c3, c4});
                }
            }
        }
        
        
/*
		for (PVector p: bestQuad) {				
			parent.fill(255, 128, 0);
			parent.ellipse(p.x, p.y, 10, 10);
		}
*/
        for (int i = 0; i < bestQuads.size(); ++i) {
        	PVector[] quad = bestQuads.get(i);
        	for (int j = 0; j < quad.length; ++j) {
        		quad[j].x *= factor;
        		quad[j].y *= factor;
        		//quad[j].z *= factor;
        	}
        	sortCorners(Arrays.asList(quad));
        }

        return bestQuads;
    }

    private float getQuadArea(PVector c1, PVector c2, PVector c3, PVector c4) {
        float cp = PVector.sub(c2, c1).cross(PVector.sub(c3, c1)).z + PVector.sub(c3, c1).cross(PVector.sub(c4, c1)).z;
        return 0.5f * abs(cp);
    }
    
    public static List<PVector> sortCorners(List<PVector> quad){
    	// Sort corners so that they are ordered clockwise
    	PVector a = quad.get(0);
    	PVector b = quad.get(2);
    	PVector center = new PVector((a.x+b.x)/2,(a.y+b.y)/2);
    	Collections.sort(quad,new CWComparator(center));
    	// TODO:
    	// Re-order the corners so that the first one is the closest to the
    	// origin (0,0) of the image.
    	//
    	// You can use Collections.rotate to shift the corners inside the quad.
    	
    	PVector origin = new PVector(0, 0);
    	int index = 0;
    	float minDist = Float.MAX_VALUE;
    	for (int i = 0; i < quad.size(); ++i) {
    		float d = quad.get(i).dist(origin);
    		if (d < minDist) {
    			minDist = d;
    			index = i;
    		}
    	}
    	
    	Collections.rotate(quad, index);
    	
    	return quad;
    }
    
    
    public static float getDistance(PVector[] old, PVector[] cows) {
    	float closestDist = Float.MAX_VALUE;
    	for (int a = 0; a < 4; ++a)
    		for (int b = 0; b < 4; ++b) if (b != a)
    			for (int c = 0; c < 4; ++c) if (c != a && c != b) {
    				int d = 0 + 1 + 2 + 3 - a - b - c;
    				float dist = sq(cows[0].dist(old[a])) +
    						sq(cows[1].dist(old[b])) +
    								sq(cows[2].dist(old[c])) +
    										sq(cows[3].dist(old[d])); 
    				
    				if (dist < closestDist)
    					closestDist = dist;
    			}    				
    	return closestDist;
    }    
}
