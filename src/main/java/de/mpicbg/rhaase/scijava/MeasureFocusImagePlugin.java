package de.mpicbg.rhaase.scijava;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.util.ArrayList;
import java.util.HashMap;

@Plugin(type = Command.class, menuPath = "XWing>Internal>Image Focus Measurements slice by slice")
public class MeasureFocusImagePlugin<T extends RealType<T>> implements Command
{
  private static ArrayList<FocusMeasures.FocusMeasure>
      formerChoice = null;


  @Parameter private Img<T> currentData;

  @Parameter private UIService uiService;

  private static boolean showPlots = false;

  HashMap<FocusMeasures.FocusMeasure, double[]> resultMatrix = null;


  public MeasureFocusImagePlugin() {
    if (formerChoice == null) {
      formerChoice = new ArrayList<FocusMeasures.FocusMeasure>();
      for (FocusMeasures.FocusMeasure focusMeasure : FocusMeasures.getFocusMeasuresArray())
      {
        formerChoice.add(focusMeasure);
      }
    }
  }

  private boolean showDialog() {
    GenericDialog genericDialog = new GenericDialog("Focus measurements");
    genericDialog.addCheckbox("Plot results", showPlots);
    genericDialog.addMessage(" ");
    for (FocusMeasures.FocusMeasure focusMeasure : FocusMeasures.getFocusMeasuresArray()) {
      genericDialog.addCheckbox(focusMeasure.getLongName(), formerChoice.contains(focusMeasure));
    }

    genericDialog.showDialog();
    if (genericDialog.wasCanceled()) {
      return false;
    }
    showPlots = genericDialog.getNextBoolean();

    formerChoice.clear();

    for (FocusMeasures.FocusMeasure focusMeasure : FocusMeasures.getFocusMeasuresArray()) {
      if (genericDialog.getNextBoolean()){
        formerChoice.add(focusMeasure);
      }
    }

    return true;
  }


  @Override public void run()
  {
    if (!showDialog()) {
      return;
    }

    int numDimensions = currentData.numDimensions();

    if (showPlots) {
      if (numDimensions == 3)
      {
        resultMatrix = new HashMap<FocusMeasures.FocusMeasure, double[]>();
      } else {
        resultMatrix = null;
        IJ.log("Plotting is not possible for 2D images. Choose an image stack.");
      }
    }


    System.out.println("running");

    ResultsTable resultsTable = ResultsTable.getResultsTable();
    if (numDimensions == 2) {
      resultsTable.incrementCounter();
      process2D(currentData, 0);
    } else if (numDimensions == 3) {
      int numberOfSlices = (int) currentData.dimension(2);

      if (resultMatrix != null)
      {
        for (FocusMeasures.FocusMeasure focusMeasure : formerChoice)
        {
          resultMatrix.put(focusMeasure, new double[numberOfSlices]);
        }
      }

      for (int z = 0; z < numberOfSlices; z++)
      {
        System.out.println("Slice " + z);
        RandomAccessibleInterval<T>
            slice = Views.hyperSlice(currentData, 2, z);

        resultsTable.incrementCounter();

        process2D(slice, z);
      }

    }

    if (resultMatrix != null)
    {
      plotResultMatrix();
    }
    resultsTable.show("Results");
  }


  private void process2D(RandomAccessibleInterval<T> img, int slice) {
    ResultsTable resultsTable = ResultsTable.getResultsTable();
    resultsTable.addValue("slice", slice);

    int width = (int)img.dimension(0);
    int height = (int)img.dimension(1);
    double[] pixelValues = new double[width * height];

    Cursor<T> cursor = Views.iterable(img).cursor();
    int index = 0;

    while (cursor.hasNext()) {
      pixelValues[index] = cursor.next().getRealDouble();
      index++;
    }

    DoubleArrayImage image = new DoubleArrayImage(width, height, pixelValues);


    for (FocusMeasures.FocusMeasure focusMeasure : formerChoice) {
      System.out.println("Determining " + focusMeasure.getLongName());
      double focusMeasureValue = FocusMeasures.computeFocusMeasure(focusMeasure, image);
      resultsTable.addValue(focusMeasure.getLongName(), focusMeasureValue);

      if (resultMatrix != null)
      {
        resultMatrix.get(focusMeasure)[slice] = focusMeasureValue;
      }
    }
  }

  private void plotResultMatrix() {

    double[] xValues = null;
    for (FocusMeasures.FocusMeasure focusMeasure : formerChoice) {
      double[] yValues = resultMatrix.get(focusMeasure);
      if (xValues == null) {
        xValues = new double[yValues.length];
        for (int i = 0; i < xValues.length; i++) {
          xValues[i] = i;
        }
      }

      Plot plot = new Plot(focusMeasure.getLongName(), "slice", focusMeasure.getLongName(), xValues, yValues);
      plot.show();
    }
  }

}
