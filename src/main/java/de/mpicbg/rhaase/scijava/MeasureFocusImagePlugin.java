package de.mpicbg.rhaase.scijava;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import de.mpicbg.rhaase.utils.DoubleArrayImageImgConverter;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.util.ArrayList;
import java.util.HashMap;

@Plugin(type = Command.class, menuPath = "Plugins>SpimCat>Quality measurement>Image Focus Measurements slice by slice (Adapted Autopilot code, Royer et Al. 2016)")
public class MeasureFocusImagePlugin<T extends RealType<T>> extends AbstractFocusMeasuresPlugin implements Command
{
  @Parameter private Img<T> currentData;

  @Parameter private OpService ops;

  @Parameter private UIService uiService;


  HashMap<FocusMeasures.FocusMeasure, double[]> resultMatrix = null;


  @Override public void run()
  {
    if (!showDialog()) {
      return;
    }

    Img<FloatType> floatData = ops.convert().float32(currentData);

    int numDimensions = floatData.numDimensions();

    if (showPlots) {
      if (numDimensions == 3)
      {
        resultMatrix = new HashMap<FocusMeasures.FocusMeasure, double[]>();
      } else {
        resultMatrix = new HashMap<FocusMeasures.FocusMeasure, double[]>();
        IJ.log("Plotting is not possible for 2D images. Choose an image stack.");
      }
    }


    System.out.println("running");

    ResultsTable resultsTable = ResultsTable.getResultsTable();
    if (numDimensions == 2) {
      resultsTable.incrementCounter();
      process2D(floatData, 0);
    } else if (numDimensions == 3) {
      int numberOfSlices = (int) floatData.dimension(2);

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
        RandomAccessibleInterval<FloatType>
            slice = Views.hyperSlice(floatData, 2, z);

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


  private void process2D(RandomAccessibleInterval<FloatType> img, int slice) {
    ResultsTable resultsTable = ResultsTable.getResultsTable();
    resultsTable.addValue("slice", slice);

    DoubleArrayImage image = new DoubleArrayImageImgConverter(Views.iterable(img)).getDoubleArrayImage();


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
