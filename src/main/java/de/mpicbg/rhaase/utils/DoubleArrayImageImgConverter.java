package de.mpicbg.rhaase.utils;

import autopilot.image.DoubleArrayImage;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.Random;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
public class DoubleArrayImageImgConverter
{
  private DoubleArrayImage doubleArrayImage = null;
  private IterableInterval<FloatType> img = null;

  public DoubleArrayImageImgConverter(IterableInterval<FloatType> img ) {
    this.img = img;
  }

  public DoubleArrayImageImgConverter(DoubleArrayImage doubleArrayImage, FloatType typedPixel){
    this.doubleArrayImage = doubleArrayImage;
  }

  private void convertToDoubleArrayImage()
  {
    if (doubleArrayImage != null) {
      return;
    }
    int width = (int)img.dimension(0);
    int height = (int)img.dimension(1);
    double[] pixelValues = new double[width * height];

    Cursor<FloatType> cursor = img.cursor();
    int index = 0;

    while (cursor.hasNext()) {
      pixelValues[index] = cursor.next().getRealDouble();
      index++;
    }

    doubleArrayImage = new DoubleArrayImage(width, height, pixelValues);
  }

  private void convertToImg() {
    if (img != null) {
      return;
    }
    Img<FloatType> img = ArrayImgs.floats(new long[]{doubleArrayImage.getWidth(), doubleArrayImage.getHeight()});
    Cursor<FloatType> cursor = Views.iterable(img).localizingCursor();
    double[] pixelValues = doubleArrayImage.getArray();

    int index = 0;

    while (cursor.hasNext()) {
      cursor.next().setReal(pixelValues[index]);
      index++;
    }
    this.img = img;
  }

  public IterableInterval<FloatType> getImg()
  {
    convertToImg();
    return img;
  }

  public DoubleArrayImage getDoubleArrayImage()
  {
    convertToDoubleArrayImage();
    return doubleArrayImage;
  }
}
