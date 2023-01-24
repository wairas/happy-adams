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
 * SPyCommand.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.core.command.spy;

import adams.core.command.AsyncCapableExternalCommand;
import adams.core.command.ExternalCommandWithOptions;
import adams.flow.standalone.SPyConfiguration;
import adams.flow.standalone.SimpleDockerConnection;

/**
 * Interface for SPy commands.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public interface SPyCommand
  extends AsyncCapableExternalCommand, ExternalCommandWithOptions {

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
   * Sets the SPy configuration to use.
   *
   * @param value	the configuration
   */
  public void setConfiguration(SPyConfiguration value);

  /**
   * Returns the SPy configuration in use.
   *
   * @return		the configuration, null if none set
   */
  public SPyConfiguration getConfiguration();

  /**
   * Returns the name of the SPy executable.
   *
   * @return		the name
   */
  public String getExecutable();

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
}
