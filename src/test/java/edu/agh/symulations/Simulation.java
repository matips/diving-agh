package edu.agh.symulations;

import net.deepocean.dodeco.calculator.*;
import net.deepocean.dodeco.tools.MyXML;
import org.jfree.data.xy.XYSeries;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Mateusz Pszczolka (SG0220005) on 5/11/2015.
 */
public class Simulation {
    private final DivingProfile divingProfile = new DivingProfile();
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Diver", true);
    private Diver diver;
    private DefaultTreeModel model = new DefaultTreeModel(root);
    private double oneStepTime = 1.0;
    private Map<Integer, DefaultMutableTreeNode[]> tissuesNodes = new HashMap<>();
    private Map<String, Map<String, XYSeries>> tensionsHistory = new HashMap<>();
    private double deep;
    private SafytyChecker safytyChecker = new SafytyChecker();

    public Simulation(BubblesForm bubblesForm) throws MyXML.MyXMLException, IllegalActionException, CalculationException {
        final double startTime = System.currentTimeMillis();
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(() -> {
                    try {
                        final double divingTime = (System.currentTimeMillis() - startTime) / 1000; // s
                        deep = bubblesForm.getDepth();
                        divingProfile.addPoint(deep, divingTime, GasMixtures.AIR);
                        diver = divingProfile.count();
                        updateModel(divingTime);
                        bubblesForm.setTime(divingTime);
                        bubblesForm.addDeepPointToChart(divingTime, deep);
                        bubblesForm.setWarnsText(verifySafety());
                        bubblesForm.setSummaryText(safytyChecker.wasUnsafe() ? "diving is dangerous!" : "");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                },
                0, (long) 1, TimeUnit.SECONDS);
    }


    public TreeModel getTreeModel() {
        updateModel(0);
        return model;
    }

    public Collection<XYSeries> getSeries(String label) {
        return tensionsHistory
                .getOrDefault(label, Collections.emptyMap())
                .values();
    }

    public String verifySafety() {
        return safytyChecker
                .checkDiverSafety(diver, new Length(deep, Length.UNITS_METER))
                .stream()
                .collect(Collectors.joining("\n"));
    }

    private void updateModel(double divingTime) {
        if (diver != null) {
            final Vector<TissueCompartment> compartments = diver.getCompartments();
            for (int i = 0; i < compartments.size(); i++) {
                final TissueCompartment compartment = compartments.get(i);
                final double he2TissueTension = compartment.getHe2TissueTension().getValue(Pressure.UNITS_ATM);
                final double n2TissueTension = compartment.getN2TissueTension().getValue(Pressure.UNITS_ATM);
                final String he2Desc = String.format("He2 tissue tension: %.5f atm", he2TissueTension);
                final String n2desc = String.format("N2  tissue tension: %.5f atm", n2TissueTension);

                tensionsHistory
                        .computeIfAbsent("Compartment " + i, s -> new HashMap<>())
                        .computeIfAbsent("He2 tissue tension", XYSeries::new)
                        .add(divingTime, he2TissueTension);

                tensionsHistory
                        .get("Compartment " + i)
                        .computeIfAbsent("N2  tissue tension", XYSeries::new)
                        .add(divingTime, n2TissueTension);

                if (tissuesNodes.containsKey(i)) {
                    tissuesNodes.get(i)[0].setUserObject(he2Desc);
                    tissuesNodes.get(i)[1].setUserObject(n2desc);
                    for (DefaultMutableTreeNode defaultMutableTreeNode : tissuesNodes.get(i)) {
                        model.reload(defaultMutableTreeNode);
                    }
                } else {
                    final DefaultMutableTreeNode compartmentNode = new DefaultMutableTreeNode("Compartment " + i);
                    final DefaultMutableTreeNode[] nodes = {
                            new DefaultMutableTreeNode(he2Desc, false),
                            new DefaultMutableTreeNode(n2desc, false),
                    };
                    tissuesNodes.put(i, nodes);
                    for (DefaultMutableTreeNode line : nodes) {
                        compartmentNode.add(line);
                    }
                    model.insertNodeInto(compartmentNode, root, 0);
                    model.reload();
                }
            }
        }
    }
}
