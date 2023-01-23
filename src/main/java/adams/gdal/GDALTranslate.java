/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * GDALTranslate.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.gdal;

import adams.core.QuickInfoHelper;
import adams.core.base.DockerDirectoryMapping;
import adams.core.io.FileWriter;
import adams.core.io.PlaceholderFile;
import adams.docker.SimpleDockerHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts raster data between different formats (gdal_translate).
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class GDALTranslate
  extends AbstractGDALCommand
  implements FileWriter {

  private static final long serialVersionUID = -4318693242709080322L;

  /** the output file. */
  protected PlaceholderFile m_OutputFile;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts raster data between different formats (" + getExecutable() + ").\n"
      + "Automatically adds the directories that the input/output dataset reside in to the docker directory mappings under " + getWorkspaceDir() + ".\n"
      + "For more information see:\n"
      + "https://gdal.org/programs/gdal_translate.html";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "output", "outputFile",
      getDefaultOutputFile());
  }

  /**
   * Returns the default output file.
   *
   * @return		the file
   */
  protected PlaceholderFile getDefaultOutputFile() {
    return new PlaceholderFile(".");
  }

  /**
   * Set output file.
   *
   * @param value	file
   */
  public void setOutputFile(PlaceholderFile value) {
    m_OutputFile = value;
    reset();
  }

  /**
   * Get output file.
   *
   * @return	file
   */
  public PlaceholderFile getOutputFile() {
    return m_OutputFile;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputFileTipText() {
    return "The name of the output file.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = super.getQuickInfo();
    result += QuickInfoHelper.toString(this, "outputFile", m_OutputFile, ", output: ");

    return result;
  }

  /**
   * Returns the name of the GDAL executable.
   *
   * @return the name
   */
  @Override
  public String getExecutable() {
    return "gdal_translate";
  }

  /**
   * Returns the automatic workspace directory in the container.
   *
   * @return		the workspace dir
   */
  protected String getWorkspaceDir() {
    return "/workspace/gdal_translate";
  }

  /**
   * Returns how many arguments have to be supplied at least.
   *
   * @return		the minimum (incl), unbounded if -1
   */
  public int minArguments() {
    return 1;
  }

  /**
   * Returns how many arguments can be supplied at most.
   *
   * @return		the maximum (incl), unbounded if -1
   */
  public int maxArguments() {
    return 1;
  }

  /**
   * Adds custom directory mappings to the already compiled list.
   *
   * @param mappings	the mappings to process
   * @param args 	the input arguments
   * @return		the updated mappings
   */
  protected List<DockerDirectoryMapping> addCustomDirMappings(List<DockerDirectoryMapping> mappings, String[] args) {
    List<DockerDirectoryMapping>	result;
    File 				localDir;
    String				contDir;
    DockerDirectoryMapping		mapping;

    result = new ArrayList<>(super.addCustomDirMappings(mappings, args));

    // input file
    localDir = new PlaceholderFile(args[0]).getParentFile();
    contDir  = SimpleDockerHelper.fixPath(getWorkspaceDir() + "/input");
    mapping = new DockerDirectoryMapping(localDir.getAbsolutePath(), contDir);
    if (!SimpleDockerHelper.addMapping(result, mapping))
      getLogger().warning("Unable to add mapping (for input): " + mapping);

    // output file
    localDir = m_OutputFile.getParentFile();
    contDir  = SimpleDockerHelper.fixPath(getWorkspaceDir() + "/output");
    mapping  = new DockerDirectoryMapping(localDir.getAbsolutePath(), contDir);
    if (!SimpleDockerHelper.addMapping(result, mapping))
      getLogger().warning("Unable to add mapping (for output): " + mapping);

    return result;
  }

  /**
   * Builds the container arguments from the input arguments and converts them to container paths.
   *
   * @param mappings	the mappings to use
   * @param args	the args to process
   * @return		the generated container args
   * @throws IOException        if converting of a path fails
   */
  protected String[] buildContainerArgs(List<DockerDirectoryMapping> mappings, String[] args) throws IOException {
    String[]	newArgs;

    // add the output directory
    newArgs = new String[args.length + 1];
    System.arraycopy(args, 0, newArgs, 0, args.length);
    newArgs[newArgs.length - 1] = m_OutputFile.getAbsolutePath();

    return SimpleDockerHelper.toContainerPaths(mappings, newArgs);
  }
}
