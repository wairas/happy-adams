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
 * Row.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.object.tools;

import adams.data.report.AnnotationHelper;
import adams.data.report.Report;
import adams.flow.transformer.locateobjects.LocatedObjects;
import adams.gui.core.BaseCheckBox;
import adams.gui.core.BaseTextField;
import adams.gui.core.ImageManager;
import adams.gui.core.MouseUtils;
import adams.gui.core.NumberTextField;
import adams.gui.core.ParameterPanel;
import adams.gui.visualization.image.ReportObjectOverlay;
import adams.gui.visualization.object.annotator.NullAnnotator;
import adams.gui.visualization.object.mouseclick.AddMetaData;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Lets the user select a row (with specified padding) to use as an annotation.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class Row
  extends AbstractToolWithParameterPanel {

  private static final long serialVersionUID = -2018674577410589154L;

  /** the object prefix to use. */
  protected BaseTextField m_TextPrefix;

  /** the amount of padding around the column. */
  protected NumberTextField m_TextPadding;

  /** whether to add meta-data. */
  protected BaseCheckBox m_CheckBoxMetaData;

  /** the object prefix. */
  protected String m_Prefix;

  /** the padding. */
  protected int m_Padding;

  /** whether to add meta-data. */
  protected boolean m_MetaData;

  /** for adding the meta-data. */
  protected AddMetaData m_AddMetaData;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Lets the user select a row (with specified padding) to use as an annotation.";
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    m_Prefix      = ReportObjectOverlay.PREFIX_DEFAULT;
    m_Padding     = 3;
    m_MetaData    = false;
    m_AddMetaData = new AddMetaData();
  }

  /**
   * The name of the tool.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return "Row";
  }

  /**
   * The icon of the tool.
   *
   * @return the icon
   */
  @Override
  public Icon getIcon() {
    return ImageManager.getIcon("row.png");
  }

  /**
   * Creates the mouse cursor to use.
   *
   * @return the cursor
   */
  @Override
  protected Cursor createCursor() {
    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }

  /**
   * Creates the mouse listener to use.
   *
   * @return the listener, null if not applicable
   */
  @Override
  protected ToolMouseAdapter createMouseListener() {
    return new ToolMouseAdapter(this) {
      @Override
      public void mouseClicked(MouseEvent e) {
	if (MouseUtils.isLeftClick(e) && MouseUtils.hasNoModifierKey(e)) {
	  Report report = getCanvas().getOwner().getReport();
	  int y = getCanvas().mouseToPixelLocation(e.getPoint()).y;
	  int index = AnnotationHelper.findLastIndex(getCanvas().getOwner().getReport(), m_Prefix) + 1;
          String current = m_Prefix + index;
          Rectangle rect = new Rectangle(0, y, getCanvas().getImage().getWidth(), 1 + m_Padding*2);
          getCanvas().getOwner().addUndoPoint("Adding row: " + rect);
	  report.setNumericValue(current + LocatedObjects.KEY_X, rect.x);
	  report.setNumericValue(current + LocatedObjects.KEY_Y, rect.y);
	  report.setNumericValue(current + LocatedObjects.KEY_WIDTH, rect.width);
	  report.setNumericValue(current + LocatedObjects.KEY_HEIGHT, rect.height);
	  getCanvas().getOwner().setReport(report);
	  getCanvas().getOwner().annotationsChanged(this);
	  e.consume();
          if (m_MetaData)
            m_AddMetaData.process(getCanvas().getOwner(), e);
	}
	super.mouseClicked(e);
      }
    };
  }

  /**
   * Creates the mouse motion listener to use.
   *
   * @return the listener, null if not applicable
   */
  @Override
  protected ToolMouseMotionAdapter createMouseMotionListener() {
    return null;
  }

  /**
   * Checks the parameters before applying them.
   *
   * @return		null if checks passed, otherwise error message (gets displayed in GUI)
   */
  @Override
  protected String checkBeforeApply() {
    String	result;

    result = super.checkBeforeApply();

    if (result == null) {
      if (m_TextPrefix.getText().trim().isEmpty())
        result = "Prefix cannot be empty, maybe use 'Object.'?";
    }

    return result;
  }

  /**
   * Applies the settings.
   */
  @Override
  protected void doApply() {
    m_Prefix   = m_TextPrefix.getText();
    m_Padding  = m_TextPadding.getValue().intValue();
    m_MetaData = m_CheckBoxMetaData.isSelected();
  }

  /**
   * Fills the parameter panel with the options.
   *
   * @param paramPanel for adding the options to
   */
  @Override
  protected void addOptions(ParameterPanel paramPanel) {
    m_TextPrefix = new BaseTextField(m_Prefix, 10);
    m_TextPrefix.addAnyChangeListener((ChangeEvent e) -> setApplyButtonState(m_ButtonApply, true));
    paramPanel.addParameter("Object prefix", m_TextPrefix);

    m_TextPadding = new NumberTextField(NumberTextField.Type.INTEGER, "" + m_Padding);
    m_TextPadding.setColumns(5);
    m_TextPadding.setToolTipText("The padding in pixels around the row");
    m_TextPadding.setCheckModel(new NumberTextField.BoundedNumberCheckModel(NumberTextField.Type.INTEGER, 1, null));
    m_TextPadding.addAnyChangeListener((ChangeEvent e) -> setApplyButtonState(m_ButtonApply, true));
    paramPanel.addParameter("Padding", m_TextPadding);

    m_CheckBoxMetaData = new BaseCheckBox();
    m_CheckBoxMetaData.addActionListener((ActionEvent e) -> setApplyButtonState(m_ButtonApply, true));
    paramPanel.addParameter("Add meta-data?", m_CheckBoxMetaData);
  }

  /**
   * Gets called to activate the tool.
   */
  @Override
  public void activate() {
    super.activate();
    getCanvas().getOwner().setAnnotator(new NullAnnotator());
  }
}
