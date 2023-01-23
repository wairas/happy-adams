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
 * AbstractGDALCommand.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.gdal;

import adams.core.ObjectCopyHelper;
import adams.core.Utils;
import adams.core.base.DockerDirectoryMapping;
import adams.core.command.AbstractAsyncCapableExternalCommandWithOptions;
import adams.core.management.User;
import adams.docker.SimpleDockerHelper;
import adams.docker.simpledocker.GenericWithArgs;
import adams.flow.standalone.GDALConfiguration;
import adams.flow.standalone.SimpleDockerConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract ancestor for GDAL commands.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractGDALCommand
  extends AbstractAsyncCapableExternalCommandWithOptions
  implements GDALCommand {

  private static final long serialVersionUID = 6921470826046130145L;

  /** the docker connection. */
  protected transient SimpleDockerConnection m_Connection;

  /** the GDAL configuration. */
  protected transient GDALConfiguration m_Configuration;

  /** the underlying docker command to execute. */
  protected transient GenericWithArgs m_DockerCommand;

  /**
   * Sets the docker connection to use.
   *
   * @param value	the connection
   */
  @Override
  public void setConnection(SimpleDockerConnection value) {
    m_Connection = value;
  }

  /**
   * Returns the docker connection in use.
   *
   * @return		the connection, null if none set
   */
  @Override
  public SimpleDockerConnection getConnection() {
    return m_Connection;
  }

  /**
   * Sets the GDAL configuration to use.
   *
   * @param value	the configuration
   */
  @Override
  public void setConfiguration(GDALConfiguration value) {
    m_Configuration = value;
  }

  /**
   * Returns the GDAL configuration in use.
   *
   * @return		the configuration, null if none set
   */
  @Override
  public GDALConfiguration getConfiguration() {
    return m_Configuration;
  }

  /**
   * Hook method for performing checks before executing the command.
   *
   * @return		null if successful, otherwise error message
   */
  protected String check() {
    String	result;

    result = super.check();

    if (result == null) {
      if (m_Connection == null)
	result = "No docker connection available! Missing " + Utils.classToString(SimpleDockerConnection.class) + " standalone?";
    }

    if (result == null) {
      if (m_Configuration == null)
	result = "No GDAL configuration available! Missing " + Utils.classToString(GDALConfiguration.class) + " standalone?";
    }

    return result;
  }

  /**
   * Compiles the directory mappings.
   * <br>
   * Default implementation just returns the ones defined by the {@link SimpleDockerConnection}.
   *
   * @return		the directory mappings
   */
  protected List<DockerDirectoryMapping> buildDirMappings() {
    return Arrays.asList(m_Connection.getExpandedDirMappings());
  }

  /**
   * Generates the command to execute.
   *
   * @return the command
   */
  protected List<String> buildCommand() {
    List<String>	result;

    result = super.buildCommand();
    result.add(getExecutable());
    result.addAll(Arrays.asList(getActualOptions()));
    return result;
  }

  /**
   * Adds custom directory mappings to the already compiled list.
   * <br>
   * The default implementation just returns the input.
   *
   * @param mappings	the mappings to process
   * @param args 	the input arguments
   * @return		the updated mappings
   */
  protected List<DockerDirectoryMapping> addCustomDirMappings(List<DockerDirectoryMapping> mappings, String[] args) {
    return mappings;
  }

  /**
   * Builds the container arguments from the input arguments and converts them to container paths.
   *
   * @param mappings	the mappings to use
   * @param args	the args to process
   * @return		the generated container args
   * @throws IOException	if converting of a path fails
   */
  protected String[] buildContainerArgs(List<DockerDirectoryMapping> mappings, String[] args) throws IOException {
    return SimpleDockerHelper.toContainerPaths(mappings, args);
  }

  /**
   * Executes the command.
   *
   * @param args 	the arguments to append
   * @return		null if successful, otherwise error message
   */
  @Override
  public String execute(String[] args) {
    String				result;
    List<String> 			runOptions;
    List<DockerDirectoryMapping>	mappings;
    String[]				containerArgs;
    GenericWithArgs			dockerCmd;

    m_Executed = false;

    result = check();

    if (result == null) {
      mappings = addCustomDirMappings(buildDirMappings(), args);
      try {
	containerArgs = buildContainerArgs(mappings, args);
      }
      catch (Exception e) {
	return e.getMessage();
      }

      // options for "docker run"
      runOptions = new ArrayList<>();
      runOptions.add("--rm");
      runOptions.add("-u");
      runOptions.add(User.getUserID() + ":" + User.getGroupID());
      for (DockerDirectoryMapping mapping: mappings) {
	runOptions.add("-v");
	runOptions.add(mapping.getValue());
      }
      runOptions.add("-t");
      runOptions.add(m_Configuration.getImage());
      runOptions.addAll(buildCommand());

      // assemble docker command
      dockerCmd = new GenericWithArgs();
      dockerCmd.setLoggingLevel(getLoggingLevel());
      dockerCmd.setStdErrProcessor(ObjectCopyHelper.copyObject(m_StdErrProcessor));
      dockerCmd.setStdOutProcessor(ObjectCopyHelper.copyObject(m_StdOutProcessor));
      dockerCmd.setOutputFormatter(ObjectCopyHelper.copyObject(m_OutputFormatter));
      dockerCmd.setBlocking(m_Blocking);
      dockerCmd.setAdditionalArguments(containerArgs);
      dockerCmd.setCommand("run");
      dockerCmd.setOptions(runOptions);
      dockerCmd.setFlowContext(m_FlowContext);
      dockerCmd.setConnection(m_Connection);
      m_DockerCommand = dockerCmd;

      // execute docker command
      result = dockerCmd.execute();
      if (result != null) {
	if (dockerCmd.hasLastCommand())
	  result = "Following docker command:\n"
	    + Utils.flatten(dockerCmd.getLastCommand(), " ") + "\n"
	    + "Failed with message:\n" + result;
	else
	  result = "Docker command failed with message:\n" + result;
      }
    }

    m_Executed = true;

    return result;
  }

  /**
   * Returns whether the command is currently running.
   *
   * @return		true if running
   */
  @Override
  public boolean isRunning() {
    return (m_DockerCommand != null) && (m_DockerCommand.isRunning());
  }

  /**
   * Whether there is any pending output.
   *
   * @return		true if output pending
   */
  @Override
  public boolean hasOutput() {
    return isRunning() || ((m_DockerCommand != null) && m_DockerCommand.hasOutput());
  }

  /**
   * Returns the next output.
   *
   * @return		the output, null if none available
   */
  @Override
  public Object output() {
    Object 	result;

    result = null;

    if (m_DockerCommand != null) {
      result = m_DockerCommand.output();
      if (m_DockerCommand.isFinished()) {
        m_DockerCommand.cleanUp();
        m_DockerCommand = null;
      }
    }

    return result;
  }

  /**
   * Stops the execution.
   */
  @Override
  public void stopExecution() {
    if (m_DockerCommand != null)
      m_DockerCommand.stopExecution();
    m_Stopped = true;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    if (m_DockerCommand != null) {
      m_DockerCommand.cleanUp();
      m_DockerCommand = null;
    }
    super.cleanUp();
  }
}
