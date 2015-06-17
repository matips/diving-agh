package edu.agh.symulations;

import net.deepocean.dodeco.calculator.CalculationException;
import net.deepocean.dodeco.calculator.IllegalActionException;
import net.deepocean.dodeco.tools.MyXML;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Mateusz Pszczolka (SG0220005) on 5/10/2015.
 */
public class BubblesForm extends JFrame {
    private JPanel divingControl;
    private JPanel visualizationPanel;
    private JFormattedTextField depth;
    private JLabel time;
    private JTree tissiues;
    private ChartPanel timeChart;
    private ChartPanel tissueTensionChart;
    private JPanel warns;
    private JLabel warnsText;
    private JLabel summary;
    private XYSeries deepFromTimeSeries;
    private XYSeriesCollection tissiuDataset;
    private double depthValue;

    public BubblesForm() {
        super("Hello World");
        setContentPane(divingControl);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        try {
            final Simulation simulation = new Simulation(this);
            tissiues.setModel(simulation.getTreeModel());
            tissiues.addTreeSelectionListener(
                    (TreeSelectionEvent e) -> {
                        tissiuDataset.removeAllSeries();
                        Arrays.stream(e.getNewLeadSelectionPath().getPath())
                                .map(Object::toString)
                                .filter(name -> name.startsWith("Compartment"))
                                .map(simulation::getSeries)
                                .flatMap(Collection::stream)
                                .forEach(tissiuDataset::addSeries);
                    });
        } catch (CalculationException | MyXML.MyXMLException | IllegalActionException e) {
            e.printStackTrace();
        }
        tissiues.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    }

    public double getDepth() {
        try {
            final Object value = depth.getValue();
            if (value instanceof Double)
                depthValue = (double) value;
            else
                depthValue = (long) value;
            return depthValue;
        } catch (NumberFormatException | NullPointerException e) {
            return depthValue;
        }
    }

    public void setTime(double time) {
        this.time.setText(String.format("%1$.1f s", time));
    }

    public void setWarnsText(String text) {
        text = "<html><span color=\"red\">" +
                text.replace("\n", "<br>") +
                "</span></html>";
        this.warnsText.setText(text);
    }

    public void addDeepPointToChart(double time, double deep) {
        deepFromTimeSeries.add(time, deep);
    }

    private void createUIComponents() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        depth = new JFormattedTextField(numberFormat);


        XYSeriesCollection ds = new XYSeriesCollection();
        ;
        JFreeChart deepChart = ChartFactory.createXYLineChart("Deep",
                "time [s]", "deep [m]", ds, PlotOrientation.VERTICAL, true, true,
                false);
        timeChart = new ChartPanel(deepChart);
        deepFromTimeSeries = new XYSeries("deep");
        ds.addSeries(deepFromTimeSeries);


        tissiuDataset = new XYSeriesCollection();
        JFreeChart tensionChart = ChartFactory.createXYLineChart("Partial tension",
                "time [s]", "tension [atm]", tissiuDataset, PlotOrientation.VERTICAL, true, true,
                false);

        tissueTensionChart = new ChartPanel(tensionChart);
    }

    public void setSummaryText(String text) {
        summary.setText(text);
    }
}
