package de.mpicbg.rhaase.scijava;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.javacl.ClearCLBackendJavaCL;
import net.haesleinhuepf.clearcl.utilities.ClearCLImageImgConverter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.IOException;

@Plugin(type = Command.class, menuPath = "Plugins>DoG (ClearCL)")
public class TranslateImagePlugin<T extends RealType<T>> implements Command
{
  private ClearCLContext mContext;

  @Parameter private Img currentData;

  @Parameter private UIService uiService;

  @Parameter private int translateX;
  @Parameter private int translateY;


  @Override public void run()
  {

  }


}
