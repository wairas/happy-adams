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
 * Translate.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.core.command.gdal;

import adams.core.base.DockerDirectoryMapping;
import adams.docker.SimpleDockerHelper;

import java.io.IOException;
import java.util.List;

/**
 * Converts raster data between different formats (gdal_translate).
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class Translate
  extends AbstractGDALCommandFileWriter {

  private static final long serialVersionUID = -4318693242709080322L;

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
   * Returns the name of the GDAL executable.
   *
   * @return the name
   */
  @Override
  public String getExecutable() {
    return "gdal_translate";
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
