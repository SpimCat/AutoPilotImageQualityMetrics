package de.mpicbg.rhaase.scijava;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.IOException;
import java.util.DuplicateFormatFlagsException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */

@Plugin(type = Command.class, menuPath = "XWing>Quality measurement>Image Focus Measurements of a region slice by slice (Adapted Autopilot code, Royer et Al. 2016)")
public class MeasureFocusRegionPlugin<T extends RealType<T>> implements Command
{
  @Parameter
  private ImagePlus image;

  @Parameter
  private CommandService commandService;

  @Override
  public void run() {
    ImagePlus croppedImage = new Duplicator().run(image, 1,
                                                  image.getNChannels(), 1, image.getNSlices(), 1, image.getNFrames());
    croppedImage.show();

    commandService.run(MeasureFocusImagePlugin.class, true, new Object[]{"currentData", croppedImage});
    //croppedImage.hide();
  }

  public static void main(String... args) throws IOException
  {
    ImageJ ij = new ImageJ();
    ij.ui().showUI();

    ///System.out.println("ex: " + new File(" C:/structure/data/2017-12-07-15-54-10-00-Robert_EDF/000006-106-126-focus_example.tif").exists());
    //IJ.openImage("C:/structure/data/2017-12-07-15-54-10-00-Robert_EDF/000006-106-126-focus_example.tif");


    Object input = ij.io().open("C:\\structure\\data\\2017-12-07-15-54-10-00-Robert_EDF\\000006-106-126-focus_example.tif");
    //Object input = ij.io().open("src/main/resources/edf_sample.tif");
    ij.ui().show(input);

    ImagePlus image = IJ.getImage();
    image.setRoi(new Roi(20, 20, 50, 50));

    ij.command().run(MeasureFocusRegionPlugin.class, true, new Object[]{"image", image} );


  }


}
