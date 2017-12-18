package de.mpicbg.rhaase.scijava;

import autopilot.measures.FocusMeasures;
import ij.gui.GenericDialog;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
public class AbstractFocusMeasuresPlugin
{
  protected static ArrayList<FocusMeasures.FocusMeasure>
      formerChoice = null;

  protected static boolean showPlots = true;

  protected boolean showDialog() {
    GenericDialog
        genericDialog = new GenericDialog("Focus measurements");
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


  public AbstractFocusMeasuresPlugin(){
    if (formerChoice == null) {
      formerChoice = new ArrayList<FocusMeasures.FocusMeasure>();
    /*for (FocusMeasures.FocusMeasure focusMeasure : FocusMeasures.getFocusMeasuresArray())
    {
      formerChoice.add(focusMeasure);
    }*/
      formerChoice.add(FocusMeasures.FocusMeasure.SpectralNormDCTEntropyShannon);
      //formerChoice.add(FocusMeasures.FocusMeasure.StatisticVariance);
    }
  }
}
