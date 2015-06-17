package edu.agh.symulations;

import net.deepocean.dodeco.calculator.GasMixture;
import net.deepocean.dodeco.calculator.IllegalActionException;

/**
 * Created by Mateusz Pszczolka (SG0220005) on 5/10/2015.
 */
public interface GasMixtures {
    GasMixture TRIMIX = createGasMixture(0.15, 0.45);   // trimix
    GasMixture NITROX = createGasMixture(0.36, 0.00);   // nitrox: 36% oxygen, 64% Nitrogen
    GasMixture NITROX_PURE_OXYGEN = createGasMixture(1.00, 0.00);   // nitrox: pure oxygen
    GasMixture AIR = createGasMixture(0.21, 0.00);   // plain air

    static GasMixture createGasMixture(double fO2Fraction, double fHe2Fraction) {

        try {
            return new GasMixture(fO2Fraction, fHe2Fraction);
        } catch (IllegalActionException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
