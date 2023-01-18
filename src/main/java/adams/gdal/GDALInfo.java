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

import adams.core.base.DockerDirectoryMapping;
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
  extends AbstractGDALCommand<String> {

  private static final long serialVersionUID = -4318693242709080322L;

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
   * Adds custom directory mappings to the already compiled list.
   *
   * @param mappings	the mappings to process
   * @param args 	the input arguments
   * @return		the updated mappings
   */
  protected List<DockerDirectoryMapping> addCustomDirMappings(List<DockerDirectoryMapping> mappings, String[] args) {
    List<DockerDirectoryMapping>	result;
    PlaceholderFile			input;
    DockerDirectoryMapping		mapping;

    input   = new PlaceholderFile(args[0]);
    result  = new ArrayList<>(super.addCustomDirMappings(mappings, args));
    for (String arg: args) {
      mapping = new DockerDirectoryMapping(new File(arg).getParentFile().getAbsolutePath(), getWorkspaceDir());
      if (SimpleDockerHelper.canAddMapping(mappings, mapping) == null)
	result.add(new DockerDirectoryMapping(input.getParentFile().getAbsolutePath(), getWorkspaceDir()));
    }

    return result;
  }

  /**
   * Returns the accepted classes.
   *
   * @return the classes
   */
  @Override
  public Class[] accepts() {
    return new Class[]{String.class};
  }

  /**
   * Returns the type of output being generated.
   *
   * @return the class of the output
   */
  @Override
  public Class generates() {
    return String.class;
  }
}
