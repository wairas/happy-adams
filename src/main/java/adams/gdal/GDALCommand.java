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
 * GDALCommand.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.gdal;

import adams.core.QuickInfoSupporter;
import adams.core.StoppableWithFeedback;
import adams.core.base.BaseString;
import adams.core.base.BaseText;
import adams.core.logging.LoggingSupporter;
import adams.core.option.OptionHandler;
import adams.docker.simpledocker.stderrprocessing.AbstractStdErrProcessing;
import adams.flow.core.FlowContextHandler;
import adams.flow.standalone.GDALConfiguration;
import adams.flow.standalone.SimpleDockerConnection;

/**
 * Interface for GDAL commands.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @param <O> the output data
 */
public interface GDALCommand<O>
  extends OptionHandler, QuickInfoSupporter, FlowContextHandler, StoppableWithFeedback, LoggingSupporter {

  /**
   * Sets the docker connection to use.
   *
   * @param value	the connection
   */
  public void setConnection(SimpleDockerConnection value);

  /**
   * Returns the docker connection in use.
   *
   * @return		the connection, null if none set
   */
  public SimpleDockerConnection getConnection();

  /**
   * Sets the GDAL configuration to use.
   *
   * @param value	the configuration
   */
  public void setConfiguration(GDALConfiguration value);

  /**
   * Returns the GDAL configuration in use.
   *
   * @return		the configuration, null if none set
   */
  public GDALConfiguration getConfiguration();

  /**
   * Sets the options for the command.
   *
   * @param value	the options
   */
  public void setOptions(BaseString[] value);

  /**
   * Returns the options for the command.
   *
   * @return		the options
   */
  public BaseString[] getOptions();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String optionsTipText();

  /**
   * Sets the options for the command.
   *
   * @param value	the options
   */
  public void setOptionsString(BaseText value);

  /**
   * Returns the options for the command as single string.
   *
   * @return		the options
   */
  public BaseText getOptionsString();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String optionsStringTipText();

  /**
   * Returns the actual options to use. The options string takes precendence over the array.
   *
   * @return		the options
   */
  public String[] getActualOptions();

  /**
   * Sets the handler for processing the output received on stderr.
   *
   * @param value	the handler
   */
  public void setStdErrProcessing(AbstractStdErrProcessing value);

  /**
   * Returns the handler for processing the output received on stderr.
   *
   * @return		the handler
   */
  public AbstractStdErrProcessing getStdErrProcessing();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String stdErrProcessingTipText();

  /**
   * Sets whether to execute in blocking or async fashion.
   *
   * @param value	true for blocking
   */
  public void setBlocking(boolean value);

  /**
   * Returns whether to execute in blocking or async fashion.
   *
   * @return		true for blocking
   */
  public boolean getBlocking();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String blockingTipText();

  /**
   * Returns the name of the GDAL executable.
   *
   * @return		the name
   */
  public String getExecutable();

  /**
   * Returns the type of output being generated.
   *
   * @return		the class of the output
   */
  public Class generates();

  /**
   * Whether the command is used in a blocking or async fashion.
   *
   * @return		true if blocking, false if async
   */
  public boolean isUsingBlocking();

  /**
   * Returns how many arguments have to be supplied at least.
   *
   * @return		the minimum (incl), unbounded if -1
   */
  public int minArguments();

  /**
   * Returns how many arguments can be supplied at most.
   *
   * @return		the maximum (incl), unbounded if -1
   */
  public int maxArguments();

  /**
   * Executes the command.
   *
   * @param args 	the arguments to append
   * @return		null if successful, otherwise error message
   */
  public String execute(String[] args);

  /**
   * Returns whether the command was executed.
   *
   * @return		true if executed
   */
  public boolean isExecuted();

  /**
   * Returns whether the command is currently running.
   *
   * @return		true if running
   */
  public boolean isRunning();

  /**
   * Returns whether the command finished.
   *
   * @return		true if finished
   */
  public boolean isFinished();

  /**
   * Whether there is any pending output.
   *
   * @return		true if output pending
   */
  public boolean hasOutput();

  /**
   * Returns the next output.
   *
   * @return		the output, null if none available
   */
  public O output();
}
