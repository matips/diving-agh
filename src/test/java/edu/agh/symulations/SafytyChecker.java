package edu.agh.symulations;

import net.deepocean.dodeco.calculator.*;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Mateusz Pszczolka (SG0220005) on 6/8/2015.
 */
public class SafytyChecker extends net.deepocean.dodeco.calculator.ZHL16Decompression {
    {
        diveHeight = Length.ZERO;
    }

    private volatile boolean wasUnsafe = false;

    public List<String> checkDiverSafety(Diver diver, Length depth) {
        final Vector<TissueCompartment> compartments = diver.getCompartments();
        return IntStream.range(0, compartments.size())
                .mapToObj(compartmentIndex -> {
                    final TissueCompartment compartment = compartments.get(compartmentIndex);
                    double fHeliumTension = compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
                    double fNitrogenTension = compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);

                    double fLimit = this.calculateTissueTensionLimit(compartmentIndex, new DepthPressure(depth, diveHeight), fNitrogenTension, fHeliumTension);

                    return Optional.of(fNitrogenTension + fHeliumTension)
                            .filter(tensionSum -> tensionSum > fLimit)
                            .map(tensionSum ->
                                            String.format("Tissue %d is unsafe! (tension %.3f / %.3f)",
                                                    compartmentIndex,
                                                    tensionSum,
                                                    fLimit
                                            )
                            );
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(ignore -> wasUnsafe = true)
                .collect(Collectors.toList());
    }

    public boolean wasUnsafe() {
        return wasUnsafe;
    }
}
