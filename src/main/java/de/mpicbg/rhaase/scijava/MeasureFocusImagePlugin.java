package de.mpicbg.rhaase.scijava;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import ij.measure.ResultsTable;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;


@Plugin(type = Command.class, menuPath = "XWing>Internal>Image Focus Measurements slice by slice")
public class MeasureFocusImagePlugin<T extends RealType<T>> implements Command
{

  @Parameter private Img<T> currentData;

  @Parameter private UIService uiService;



  @Override public void run()
  {
    System.out.println("running");
    int numDimensions = currentData.numDimensions();

    ResultsTable resultsTable = ResultsTable.getResultsTable();
    if (numDimensions == 2) {
      resultsTable.incrementCounter();
      resultsTable.addValue("slice", 0);
      process2D(currentData);
    } else if (numDimensions == 3) {
      int numberOfSlices = (int) currentData.dimension(2);
      for (int z = 0; z < numberOfSlices; z++)
      {
        System.out.println("Slice " + z);
        RandomAccessibleInterval<T>
            slice = Views.hyperSlice(currentData, 2, z);

        resultsTable.incrementCounter();

        resultsTable.addValue("slice", z);
        process2D(slice);
      }
    }
    resultsTable.show("Results");
  }


  private void process2D(RandomAccessibleInterval<T> img) {
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

    ResultsTable resultsTable = ResultsTable.getResultsTable();

    for (FocusMeasures.FocusMeasure focusMeasure : FocusMeasures.getFocusMeasuresArray()) {
      System.out.println("Determining " + focusMeasure.getLongName());
      double focusMeasureValue = FocusMeasures.computeFocusMeasure(focusMeasure, image);
      resultsTable.addValue(focusMeasure.getLongName(), focusMeasureValue);
    }

  }
}
