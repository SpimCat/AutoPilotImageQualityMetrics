package de.mpicbg.rhaase.scijava;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import autopilot.utils.ArrayMatrix;
import de.mpicbg.rhaase.utils.DoubleArrayImageImgConverter;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */

@Plugin(type = Command.class, menuPath = "SpimCat>Quality measurement>Image Focus Measurements tiles (Adapted Autopilot code, Royer et Al. 2016)")
public class MeasureFocusTiledPlugin<T extends RealType<T>> extends AbstractFocusMeasuresPlugin implements
                                                            Command
{

  @Parameter private Img<T> currentData;

  @Parameter private int numberOfTilesX = 16;

  @Parameter private int numberOfTilesY = 32;

  @Parameter private OpService ops;

  @Parameter private UIService ui;

  private HashMap<FocusMeasures.FocusMeasure, Img<FloatType>>
      resultMaps;

  @Override public void run()
  {
    if (!showDialog()) {
      return;
    }

    int numDimensions = currentData.numDimensions();


    Img<FloatType> floatData = ops.convert().float32(currentData);

    resultMaps = new HashMap<>();

    if (numDimensions == 2) {
      for (FocusMeasures.FocusMeasure focusMeasure : formerChoice)
      {
        resultMaps.put(focusMeasure, ArrayImgs.floats(new long[]{numberOfTilesX, numberOfTilesY}));
      }

      process2D(floatData, 0);
    } else if (numDimensions == 3) {

      int numberOfSlices = (int) currentData.dimension(2);
      for (FocusMeasures.FocusMeasure focusMeasure : formerChoice)
      {
        resultMaps.put(focusMeasure, ArrayImgs.floats(new long[]{numberOfTilesX, numberOfTilesY, numberOfSlices}));
      }

      for (int z = 0; z < numberOfSlices; z++)
      {
        System.out.println("Slice " + z);
        RandomAccessibleInterval<FloatType>
            slice = Views.hyperSlice(floatData, 2, z);


        process2D(slice, z);
      }

    }
    for (FocusMeasures.FocusMeasure focusMeasure : formerChoice)
    {
      Img<FloatType> img = resultMaps.get(focusMeasure);
      ui.show(focusMeasure.getLongName(), img);
    }
  }


  private void process2D(RandomAccessibleInterval<FloatType> img, int slice) {

    //int blockWidth = (int)img.dimension(0) / numberOfTilesX;
    //int blockHeight = (int)img.dimension(0) / numberOfTilesY;

    for (FocusMeasures.FocusMeasure focusMeasure : formerChoice) {

      Img<FloatType> resultImg = resultMaps.get(focusMeasure);

      mapFeatureToImg(img, slice, focusMeasure, resultImg);
      /*
      for (int blockX = 0; blockX + blockWidth < img.dimension(0); blockX += blockWidth ) {
        for (int blockY = 0; blockY + blockHeight < img.dimension(1); blockY += blockHeight ) {
          IterableInterval<FloatType> ii = Views.interval(img, Intervals.createMinSize(blockX, blockY, blockWidth, blockHeight));

          DoubleArrayImage image = new DoubleArrayImageImgConverter(ii).getDoubleArrayImage();

          double focusMeasureValue = FocusMeasures.computeFocusMeasure(focusMeasure, image);

          if (slice < 0) { // 2D
            position = new long[]{blockX / blockWidth, blockY / blockHeight};
          } else { // 3D?
            position = new long[]{blockX / blockWidth, blockY / blockHeight, slice};
          }
          resultRA.setPosition(position);
          resultRA.get().setReal(focusMeasureValue);

        }
      }*/

    }
  }

  private void mapFeatureToImg(RandomAccessibleInterval<FloatType> img,
                               int slice,
                               FocusMeasures.FocusMeasure focusMeasure,
                               Img<FloatType> resultImg)
  {
    RandomAccess<FloatType> resultRA = resultImg.randomAccess();
    long[] position;

    System.out.println("Determining " + focusMeasure.getLongName());

    DoubleArrayImage
        image = new DoubleArrayImageImgConverter(Views.iterable(img)).getDoubleArrayImage();

    final ArrayMatrix<DoubleArrayImage>
        lTiles = image.extractTiles(numberOfTilesX, numberOfTilesY);

    for (int x = 0; x < numberOfTilesX; x++)
    {
      for (int y = 0; y < numberOfTilesY; y++)
      {
        final DoubleArrayImage lTile = lTiles.get(x, y);
        double
            focusMeasureValue =
            FocusMeasures.computeFocusMeasure(focusMeasure, lTile);

        if (slice < 0)
        { // 2D
          position = new long[] { x, y };
        }
        else
        { // 3D?
          position = new long[] { x, y, slice };
        }
        resultRA.setPosition(position);
        resultRA.get().setReal(focusMeasureValue);
      }
    }
  }

  public static void main(String... args) throws IOException
  {
    ImageJ ij = new ImageJ();
    ij.ui().showUI();

    ImagePlus
        input = IJ.openImage("C:/structure/temp/xy_over_time.tif");

    ij.ui().show(input);

    Object result = ij.command().run(MeasureFocusTiledPlugin.class, true, new Object[]{"currentData", input, "numberOfTilesX", 32, "numberOfTilesY", 64} );
    System.out.println(result);
  }
}
