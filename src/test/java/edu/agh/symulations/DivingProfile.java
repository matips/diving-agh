package edu.agh.symulations;

import net.deepocean.dodeco.calculator.*;
import net.deepocean.dodeco.tools.MyXML;

import java.util.LinkedList;

/**
 * Created by Mateusz Pszczolka (SG0220005) on 5/10/2015.
 */
public class DivingProfile {

    public static final Length DIVE_HEIGHT = new Length(0.0, Length.UNITS_METER);

    private class Point {
        double time;
        final double deep;
        final GasMixture gasMixture;

        public Point(double deep, double time, GasMixture gasMixture) {
            this.deep = deep;
            this.time = time;
            this.gasMixture = gasMixture;
        }

        public Length getDeep() {
            return new Length(deep, Length.UNITS_METER);
        }
    }

    private final LinkedList<Point> points = new LinkedList<>();

    public Point getDeep() {
        return points.getLast();
    }
    public synchronized void addPoint(double deep, double time, GasMixture gasMixture) throws IllegalArgumentException {
        if (points.size() > 0 && points.getLast().time > time) {
            throw new IllegalArgumentException("You cannot go back in time!");
        } else if (points.size() > 0 && points.getLast().deep == deep && points.getLast().gasMixture.equals(gasMixture)) {
            points.getLast().time = time;
        } else {
            points.add(new Point(deep, time, gasMixture));
        }
    }

    public Diver count() throws MyXML.MyXMLException, IllegalActionException, CalculationException {
        Processor processor = new Processor();

        processor.setDecoAlgorithm(Processor.ALGORITHM_BUHLMANNBWITHGRADIENT);
        try {
            Saturation exposure = new Saturation(new Length(0.0, Length.UNITS_METER), GasMixtures.AIR);
            processor.addExposure(exposure);
        } catch (IllegalActionException e) {
            System.err.println(e.getMessage());
        }

        final Dive dive = new Dive("One true dive", DIVE_HEIGHT);


        double prevTime = 0;
        for (Point point : points) {
            dive.addConstantDepthSegment(new Length(point.deep, Length.UNITS_METER), point.time - prevTime, point.gasMixture);
            prevTime = point.time;
        }
        processor.addExposure(dive);

        processor.process();
        return processor.getDiver();
    }



}
