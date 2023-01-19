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
import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.Variables;
import adams.core.base.BaseObject;
import adams.core.base.BaseString;
import adams.core.base.BaseText;
import adams.core.base.DockerDirectoryMapping;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.OptionUtils;
import adams.docker.SimpleDockerHelper;
import adams.docker.simpledocker.GenericWithArgs;
import adams.docker.simpledocker.stderrprocessing.AbstractStdErrProcessing;
import adams.docker.simpledocker.stderrprocessing.Log;
import adams.flow.core.Actor;
import adams.flow.standalone.GDALConfiguration;
import adams.flow.standalone.SimpleDockerConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract ancestor for GDAL commands.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractGDALCommand<O>
  extends AbstractOptionHandler
  implements GDALCommand<O> {

  private static final long serialVersionUID = 6921470826046130145L;

  /** whether to use blocking or async mode. */
  protected boolean m_Blocking;

  /** the handler for processing output on stderr. */
  protected AbstractStdErrProcessing m_StdErrProcessing;

  /** the options for the command. */
  protected BaseString[] m_Options;

  /** the options as single string. */
  protected BaseText m_OptionsString;

  /** the docker connection. */
  protected transient SimpleDockerConnection m_Connection;

  /** the GDAL configuration. */
  protected transient GDALConfiguration m_Configuration;

  /** the command was executed. */
  protected boolean m_Executed;

  /** whether the execution was stopped. */
  protected boolean m_Stopped;

  /** the flow context. */
  protected Actor m_FlowContext;

  /** the underlying docker command to execute. */
  protected transient GenericWithArgs m_DockerCommand;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "stderr-processing", "stdErrProcessing",
      getDefaultStdErrProcessing());

    m_OptionManager.add(
      "blocking", "blocking",
      true);

    m_OptionManager.add(
      "option", "options",
      new BaseString[0]);

    m_OptionManager.add(
      "options-string", "optionsString",
      new BaseText());
  }

  /**
   * Returns the default handler for processing output on stderr.
   *
   * @return		the handler
   */
  protected AbstractStdErrProcessing getDefaultStdErrProcessing() {
    return new Log();
  }

  /**
   * Sets the handler for processing the output received on stderr.
   *
   * @param value	the handler
   */
  public void setStdErrProcessing(AbstractStdErrProcessing value) {
    m_StdErrProcessing = value;
    reset();
  }

  /**
   * Returns the handler for processing the output received on stderr.
   *
   * @return		the handler
   */
  public AbstractStdErrProcessing getStdErrProcessing() {
    return m_StdErrProcessing;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String stdErrProcessingTipText() {
    return "The handler for processing output received from the underlying docker command on stderr.";
  }

  /**
   * Sets whether to execute in blocking or async fashion.
   *
   * @param value	true for blocking
   */
  @Override
  public void setBlocking(boolean value) {
    m_Blocking = value;
    reset();
  }

  /**
   * Returns whether to execute in blocking or async fashion.
   *
   * @return		true for blocking
   */
  @Override
  public boolean getBlocking() {
    return m_Blocking;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String blockingTipText() {
    return "If enabled, the command is executed in blocking fashion rather than asynchronous.";
  }

  /**
   * Returns whether blocking or async mode is used.
   *
   * @return		true if blocking
   */
  @Override
  public boolean isUsingBlocking() {
    return m_Blocking;
  }

  /**
   * Sets the options for the command.
   *
   * @param value	the options
   */
  @Override
  public void setOptions(BaseString[] value) {
    m_Options = value;
    reset();
  }

  /**
   * Returns the options for the command.
   *
   * @return		the options
   */
  @Override
  public BaseString[] getOptions() {
    return m_Options;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String optionsTipText() {
    return "The options for the command; variables get expanded automatically.";
  }

  /**
   * Sets the options for the command.
   *
   * @param value	the options
   */
  @Override
  public void setOptionsString(BaseText value) {
    m_OptionsString = value;
    reset();
  }

  /**
   * Returns the options for the command as single string.
   *
   * @return		the options
   */
  @Override
  public BaseText getOptionsString() {
    return m_OptionsString;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String optionsStringTipText() {
    return "The options for the command as a single string; overrides the options array; variables get expanded automatically.";
  }

  /**
   * Returns the actual options to use. The options string takes precendence over the array.
   *
   * @return		the options
   */
  @Override
  public String[] getActualOptions() {
    String[]	result;
    int		i;
    Variables vars;

    vars = m_FlowContext.getVariables();
    try {
      if (!m_OptionsString.isEmpty()) {
	result = OptionUtils.splitOptions(vars.expand(m_OptionsString.getValue()));
      }
      else {
	result = BaseObject.toStringArray(m_Options);
	for (i = 0; i < result.length; i++)
	  result[i] = vars.expand(result[i]);
      }
      return result;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to parse options!", e);
      return new String[0];
    }
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "blocking", (m_Blocking ? "blocking" : "async"), "mode: ");
    result += QuickInfoHelper.toString(this, "stdErrProcessing", m_StdErrProcessing, ", stderr: ");
    if (!m_OptionsString.isEmpty() || getOptionManager().hasVariableForProperty("optionsString"))
      result += QuickInfoHelper.toString(this, "optionsString", (m_OptionsString.isEmpty() ? "-none-" : m_OptionsString), ", options string: ");
    else
      result += QuickInfoHelper.toString(this, "options", m_Options, ", options: ");

    return result;
  }

  /**
   * Sets the flow context.
   *
   * @param value the actor
   */
  public void setFlowContext(Actor value) {
    m_FlowContext = value;
  }

  /**
   * Returns the flow context, if any.
   *
   * @return the actor, null if none available
   */
  public Actor getFlowContext() {
    return m_FlowContext;
  }

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
  public void setConfiguration(GDALConfiguration value) {
    m_Configuration = value;
  }

  /**
   * Returns the GDAL configuration in use.
   *
   * @return		the configuration, null if none set
   */
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

    result = null;

    if (m_FlowContext == null)
      result = "No flow context set!";

    if (result == null) {
      if (m_Connection == null)
	result = "No docker connection available! Missing " + Utils.classToString(SimpleDockerConnection.class) + " standalone?";
    }

    if (result == null) {
      if (m_Configuration == null)
	result = "No GDAL configuration available! Missing " + Utils.classToString(GDALConfiguration.class) + " standalone?";
    }

    if (result == null)
      result = m_StdErrProcessing.setUp(this);

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

    result = new ArrayList<>();
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
    List<String>			cmd;
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

      cmd = new ArrayList<>();
      cmd.add("--rm");
      for (DockerDirectoryMapping mapping: mappings) {
	cmd.add("-v");
	cmd.add(mapping.getValue());
      }
      cmd.add("-t");
      cmd.add(m_Configuration.getImage());
      cmd.addAll(buildCommand());

      dockerCmd = new GenericWithArgs();
      dockerCmd.setLoggingLevel(getLoggingLevel());
      dockerCmd.setStdErrProcessing(ObjectCopyHelper.copyObject(m_StdErrProcessing));
      dockerCmd.setBlocking(m_Blocking);
      dockerCmd.setAdditionalArguments(containerArgs);
      dockerCmd.setCommand("run");
      dockerCmd.setOptions(cmd);
      dockerCmd.setFlowContext(m_FlowContext);
      dockerCmd.setConnection(m_Connection);
      m_DockerCommand = dockerCmd;
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
   * Returns whether the command was executed.
   *
   * @return		true if executed
   */
  public boolean isExecuted() {
    return m_Executed;
  }

  /**
   * Returns whether the command is currently running.
   *
   * @return		true if running
   */
  public boolean isRunning() {
    return (m_DockerCommand != null) && (m_DockerCommand.isRunning());
  }

  /**
   * Returns whether the command finished.
   *
   * @return		true if finished
   */
  public boolean isFinished() {
    return isExecuted() && !isRunning();
  }

  /**
   * Whether there is any pending output.
   *
   * @return		true if output pending
   */
  public boolean hasOutput() {
    return isRunning() || ((m_DockerCommand != null) && m_DockerCommand.hasOutput());
  }

  /**
   * Hook method for post-processing the output.
   * <br>
   * Default implementation just casts the data.
   *
   * @param output	the output to process
   * @return		the processed data
   */
  protected O postProcessOutput(Object output) {
    return (O) output;
  }

  /**
   * Returns the next output.
   *
   * @return		the output, null if none available
   */
  public O output() {
    O  		result;

    result = null;

    if (m_DockerCommand != null) {
      result = postProcessOutput(m_DockerCommand.output());
      if (m_DockerCommand.isFinished())
	m_DockerCommand = null;
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
   * Whether the execution has been stopped.
   *
   * @return		true if stopped
   */
  @Override
  public boolean isStopped() {
    return m_Stopped;
  }
}
