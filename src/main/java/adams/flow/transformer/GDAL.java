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
 * GDAL.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;
import adams.flow.standalone.GDALConfiguration;
import adams.flow.standalone.SimpleDockerConnection;
import adams.gdal.GDALCommand;
import adams.gdal.GDALInfo;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class GDAL
  extends AbstractTransformer {

  private static final long serialVersionUID = 463922754200204096L;

  /** the GDAL command to execute. */
  protected GDALCommand m_Command;

  /** the docker connection. */
  protected transient SimpleDockerConnection m_Connection;

  /** the GDAL configuration. */
  protected transient GDALConfiguration m_Configuration;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Executes the specified GDAL program, supplying it with the incoming file(s)/dir(s).";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "command", "command",
      new GDALInfo());
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "command", m_Command);
  }

  /**
   * Sets the command to run.
   *
   * @param value	the command
   */
  public void setCommand(GDALCommand value) {
    m_Command = value;
    reset();
  }

  /**
   * Returns the command to run.
   *
   * @return		the command
   */
  public GDALCommand getCommand() {
    return m_Command;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String commandTipText() {
    return "The GDAL command to run.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{String.class, String[].class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{m_Command.generates()};
  }

  /**
   * Initializes the item for flow execution.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String setUp() {
    String	result;

    result = super.setUp();

    if (result == null) {
      m_Connection = (SimpleDockerConnection) ActorUtils.findClosestType(this, SimpleDockerConnection.class, true);
      if (m_Connection == null)
	result = "No " + Utils.classToString(SimpleDockerConnection.class) + " actor found!";
      else if (m_Connection.getAcualBinary() == null)
	result = "No docker binary available from: " + m_Connection.getFullName();
    }

    if (result == null) {
      m_Configuration = (GDALConfiguration) ActorUtils.findClosestType(this, GDALConfiguration.class, true);
      if (m_Configuration == null)
	result = "No " + Utils.classToString(GDALConfiguration.class) + " actor found!";
    }

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    String[]	args;

    result = null;
    args   = new String[0];

    if (m_InputToken.hasPayload(String.class))
      args = new String[]{m_InputToken.getPayload(String.class)};
    else if (m_InputToken.hasPayload(String[].class))
      args = m_InputToken.getPayload(String[].class);
    else
      result = m_InputToken.unhandledData();

    // check number of parameters
    if (result == null) {
      if (m_Command.minArguments() > -1) {
        if (args.length < m_Command.minArguments())
          result = "Not enough parameters: supplied " + args.length + " but expected at least " + m_Command.minArguments();
      }
    }
    if (result == null) {
      if (m_Command.maxArguments() > -1) {
        if (args.length > m_Command.maxArguments())
          result = "Too many parameters: supplied " + args.length + " but expected at most " + m_Command.maxArguments();
      }
    }

    if (result == null) {
      m_Command.setFlowContext(this);
      m_Command.setConnection(m_Connection);
      m_Command.setConfiguration(m_Configuration);
      result = m_Command.execute(args);
    }

    return result;
  }

  /**
   * Checks whether there is pending output to be collected after
   * executing the flow item.
   *
   * @return		true if there is pending output
   */
  @Override
  public boolean hasPendingOutput() {
    return (m_Command != null) && m_Command.hasOutput();
  }

  /**
   * Returns the generated token.
   *
   * @return		the generated token
   */
  @Override
  public Token output() {
    Token	result;
    Object	output;

    result = null;

    if (m_Command != null) {
      output = m_Command.output();
      if (output != null)
        result = new Token(output);
    }

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_Command != null)
      m_Command.stopExecution();
    super.stopExecution();
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    if (m_Command != null) {
      m_Command.cleanUp();
      m_Command = null;
    }
    super.wrapUp();
  }
}
