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
 * GDALInfo.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.gdal;

import adams.core.QuickInfoHelper;
import adams.core.base.BaseRegExp;
import adams.core.base.DockerDirectoryMapping;
import adams.core.command.output.LineSplit;
import adams.core.command.output.OutputFormatter;
import adams.core.io.PlaceholderFile;
import adams.docker.SimpleDockerHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists information about a raster dataset (gdalinfo).
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class GDALInfo
  extends AbstractGDALCommand {

  private static final long serialVersionUID = -4318693242709080322L;

  /** whether to output JSON. */
  protected boolean m_Json;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Lists information about a raster dataset (" + getExecutable() + ").\n"
      + "Automatically adds the directory that the dataset resides in to the docker directory mappings as " + getWorkspaceDir() + ".\n"
      + "For more information see:\n"
      + "https://gdal.org/programs/gdalinfo.html";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "json", "json",
      false);
  }

  /**
   * Returns the default output formatter.
   *
   * @return		the default
   */
  @Override
  protected OutputFormatter getDefaultOutputFormatter() {
    LineSplit	result;

    result = new LineSplit();
    result.setRegExp(new BaseRegExp("ERROR .*"));
    result.setInvert(true);

    return result;
  }

  /**
   * Sets whether to execute in blocking or async fashion.
   *
   * @param value	true for blocking
   */
  @Override
  public void setBlocking(boolean value) {
    super.setBlocking(value);
    if (!m_Blocking)
      setJson(false);
  }

  /**
   * Sets whether to generate JSON or plain text output.
   *
   * @param value	true if JSON
   */
  public void setJson(boolean value) {
    m_Json = value;
    if (m_Json)
      setBlocking(true);
    reset();
  }

  /**
   * Returns whether to generate JSON or plain text output.
   *
   * @return		true if json
   */
  public boolean getJson() {
    return m_Json;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String jsonTipText() {
    return "If enabled, the output format is JSON rather than plain text; NB: only works in blocking mode (requires cleaning up output).";
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
    result += QuickInfoHelper.toString(this, "json", (m_Json ? "JSON" : "Plain text"), ", format: ");

    return result;
  }

  /**
   * Returns the name of the GDAL executable.
   *
   * @return the name
   */
  @Override
  public String getExecutable() {
    return "gdalinfo";
  }

  /**
   * Returns the automatic workspace directory in the container.
   *
   * @return		the workspace dir
   */
  protected String getWorkspaceDir() {
    return "/workspace/gdalinfo";
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
    File				input;
    DockerDirectoryMapping		mapping;

    result = new ArrayList<>(super.addCustomDirMappings(mappings, args));
    input  = new PlaceholderFile(args[0]);
    if (input.isFile())
      input = input.getParentFile();
    mapping = new DockerDirectoryMapping(input.getAbsolutePath(), getWorkspaceDir());
    if (!SimpleDockerHelper.addMapping(result, mapping))
      getLogger().warning("Unable to add mapping (for input): " + mapping);

    return result;
  }

  /**
   * Generates the command to execute.
   *
   * @return the command
   */
  protected List<String> buildCommand() {
    List<String>	result;

    result = super.buildCommand();
    if (m_Json)
      result.add("-json");

    return result;
  }
}
