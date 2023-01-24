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
 * EnviInfo.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.core.command.spy;

import adams.core.base.DockerDirectoryMapping;
import adams.docker.SimpleDockerHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Outputs information on an ENVI file.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class EnviInfo
  extends AbstractSPyCommandFileWriter {

  private static final long serialVersionUID = 8740750360408153193L;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Outputs information on an ENVI file (" + getExecutable() + ").\n"
      + "Automatically adds the directories that the input/output files reside in to the docker directory mappings under " + getWorkspaceDir() + ".\n"
      + "For more information see:\n"
      + "https://github.com/wairas/spy-utilities";
  }

  /**
   * Returns the name of the SPy executable.
   *
   * @return the name
   */
  @Override
  public String getExecutable() {
    return "spy-envi_info";
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
    List<String>	newArgs;

    newArgs = new ArrayList<>();
    newArgs.add("-i");
    newArgs.add(args[0]);
    newArgs.add("-o");
    newArgs.add(m_OutputFile.getAbsolutePath());

    return SimpleDockerHelper.toContainerPaths(mappings, newArgs.toArray(new String[0]));
  }
}
