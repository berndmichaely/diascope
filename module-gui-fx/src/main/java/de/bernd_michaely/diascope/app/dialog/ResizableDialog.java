/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.bernd_michaely.diascope.app.dialog;

import de.bernd_michaely.diascope.app.control.ScaleBox;
import de.bernd_michaely.diascope.app.control.ScaleBox.SpaceGainingMode;
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.control.ScaleBox.SpaceGainingMode.NONE;
import static de.bernd_michaely.diascope.app.util.scene.HeaderBarUtil.createHeaderBar;
import static java.util.Objects.requireNonNull;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * Base class for resizable dialogs.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ResizableDialog implements AutoCloseable
{
	private static final String RESOURCE_PROPERTIES_NAME = "dialogs";
	private static final double GAP = Font.getDefault().getSize();
	private static final Insets insetsFirst = new Insets(GAP, 0, GAP, 0);
	private static final Insets insetsFollowing = new Insets(GAP, 0, GAP, GAP);
	private final DialogType dialogType;
	private final boolean upsize;
	private final SpaceGainingMode spaceGainingMode;
	private @Nullable BorderPane paneOuter;
	private @Nullable Stage stage;
	private @MonotonicNonNull String initialTitle;
	private @MonotonicNonNull Region root;
	private final HBox paneButtons;
	private @MonotonicNonNull ScaleBox scaleBox;
	private boolean committed;
	private @MonotonicNonNull Button buttonClose, buttonOK, buttonCancel;
	private @Nullable Runnable onOkAction;

	public enum DialogType
	{
		CLOSEABLE_DIALOG, OK_CANCEL_DIALOG
//		, YES_NO_CANCEL_DIALOG
	}

	/**
	 * Create a new instance of given spec.
	 *
	 * @param dialogType the dialog type
	 */
	public ResizableDialog(DialogType dialogType)
	{
		this(dialogType, null, false);
	}

	/**
	 * Create a new instance of given spec.
	 *
	 * @param dialogType       the dialog type
	 * @param spaceGainingMode the space gaining mode (default: null == none)
	 * @param upsize           whether to upsize the dialog content
	 */
	public ResizableDialog(DialogType dialogType,
		@Nullable SpaceGainingMode spaceGainingMode, boolean upsize)
	{
		this.dialogType = requireNonNull(dialogType, "DialogType must not be null");
		this.spaceGainingMode = spaceGainingMode != null ? spaceGainingMode : NONE;
		this.upsize = upsize;
		this.paneButtons = new HBox();
		switch (this.dialogType)
		{
			case CLOSEABLE_DIALOG ->
			{
				buttonClose = new Button();
				buttonClose.setText("Close");
				buttonClose.setDefaultButton(true);
				buttonClose.setCancelButton(true);
				buttonClose.setOnAction(_ -> _close(stage));
				setButtons(buttonClose);
			}
			case OK_CANCEL_DIALOG ->
			{
				buttonOK = new Button();
				buttonOK.setText("OK");
				buttonOK.setOnAction(_ ->
				{
					committed = true;
					if (onOkAction != null)
					{
						onOkAction.run();
					}
					_close(stage);
				});
				buttonCancel = new Button();
				buttonCancel.setText("Cancel");
				buttonOK.setDefaultButton(true);
				buttonCancel.setCancelButton(true);
				buttonCancel.setOnAction(_ -> _close(stage));
				setButtons(buttonOK, buttonCancel);
			}
		}
	}

	public void setTitle(String titleDialog)
	{
		initialTitle = titleDialog;
	}

	public void updateI18n(String titleDialog, Locale locale)
	{
		if (locale != null)
		{
			final Package p = getClass().getPackage();
			final String packageName = p != null ? p.getName() + '.' : "";
			final ResourceBundle bundle = ResourceBundle.getBundle(packageName + RESOURCE_PROPERTIES_NAME, locale);
			if (stage != null)
			{
				stage.setTitle((titleDialog != null) ? titleDialog : "Dialog");
				resetButtonWidths();
			}
			else
			{
				initialTitle = titleDialog;
			}
			if (buttonClose != null)
			{
				buttonClose.setText('_' + bundle.getString("dialog.button.close"));
			}
			if (buttonOK != null)
			{
				buttonOK.setText('_' + bundle.getString("dialog.button.ok"));
			}
			if (buttonCancel != null)
			{
				buttonCancel.setText('_' + bundle.getString("dialog.button.cancel"));
			}
			if (stage != null)
			{
				equalizeButtonWidths();
			}
		}
	}

	/**
	 * Show the content node in a dialog window with {@link Modality#NONE}.
	 *
	 * @param owner           the owner of the dialog
	 * @param content         the content node to show
	 * @param createHeaderBar true to use a {@link HeaderBar}
	 */
	public void show(Window owner, Region content)
	{
		show(owner, content, null);
	}

	public void show(Window owner, Region content, @Nullable Region headerBarCtrl)
	{
		show(owner, content, headerBarCtrl, null);
	}

	/**
	 * Show the content node in a dialog window.
	 *
	 * @param owner           the owner of the dialog
	 * @param content         the content node to show
	 * @param createHeaderBar true to use a {@link HeaderBar}
	 * @param modality        the given modality ({@code null} defaults to
	 *                        {@link Modality#NONE})
	 */
	void show(Window owner, Region content,
		@Nullable Region headerBarCtrl, @Nullable Modality modality)
	{
		if (this.stage == null)
		{
			final Stage s = new Stage(StageStyle.EXTENDED);
			this.stage = s;
			s.initOwner(owner);
			s.initModality(modality != null ? modality : Modality.NONE);
			final BorderPane po = new BorderPane();
			this.paneOuter = po;
			final Region r = switch (spaceGainingMode)
			{
				case NONE ->
				{
					po.setCenter(content);
					po.setBottom(this.paneButtons);
					yield po;
				}
				case SCALING ->
				{
					po.setCenter(content);
					po.setBottom(this.paneButtons);
					this.scaleBox = ScaleBox.create(po, false, upsize, spaceGainingMode);
					yield this.scaleBox.getDisplay();
				}
				case SCROLLING ->
				{
					this.scaleBox = ScaleBox.create(content, false, upsize, spaceGainingMode);
					po.setCenter(scaleBox.getDisplay());
					po.setBottom(this.paneButtons);
					yield po;
				}
			};
			paneButtons.setAlignment(Pos.CENTER);
			s.setOnShown(e -> postLayout());
			s.setOnCloseRequest(e -> onWindowCloseRequest());
			if (initialTitle == null)
			{
				initialTitle = "Dialog";
			}
			s.setTitle(initialTitle);
			final var rootPane = new BorderPane(r);
			rootPane.setTop((headerBarCtrl != null) ?
				createHeaderBar(s, headerBarCtrl) :
				createHeaderBar(s, new Label(initialTitle)));
			this.root = rootPane;
			final var scene = new Scene(this.root);
			SceneStylesheetUtil.setStylesheet(scene);
			s.setScene(scene);
		}
		if (this.stage != null)
		{
			final Stage s = this.stage;
			s.show();
			if (s.isIconified())
			{
				s.setIconified(false);
			}
			s.toFront();
		}
	}

	private static void _close(@Nullable Stage stage)
	{
		if (stage != null)
		{
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}

	@Override
	public void close()
	{
		_close(stage);
	}

	private void onWindowCloseRequest()
	{
		stage = null;
		paneOuter = null;
	}

	private void setButtons(@UnderInitialization ResizableDialog this,
    Button... buttons)
	{
		if (this.paneButtons != null && buttons != null)
		{
			final int n = buttons.length;
			this.paneButtons.getChildren().setAll(buttons);
			if (n > 0)
			{
				HBox.setMargin(buttons[0], insetsFirst);
				if (n > 1)
				{
					IntStream.range(1, n).forEach(i ->
						HBox.setMargin(buttons[i], insetsFollowing));
				}
			}
		}
	}

	public @Nullable
	Runnable getOnOkAction()
	{
		return onOkAction;
	}

	public void setOnOkAction(@Nullable Runnable onOkAction)
	{
		this.onOkAction = onOkAction;
	}

	/**
	 * Returns true, if the dialog was closed by pressing the OK button.
	 *
	 * @return true, if the dialog was closed by pressing the OK button
	 */
	public boolean isCommitted()
	{
		return committed;
	}

	private Stream<Button> streamButtons()
	{
		return paneButtons.getChildren().stream()
			.filter(node -> node instanceof Button)
			.map(node -> (Button) node);
	}

	private void equalizeButtonWidths()
	{
		final double withButtons = streamButtons()
			.map(Button::getPrefWidth)
			.max(Double::compare).orElse(USE_COMPUTED_SIZE);
		setButtonWidths(withButtons);
	}

	private void resetButtonWidths()
	{
		setButtonWidths(USE_COMPUTED_SIZE);
	}

	private void setButtonWidths(double withButtons)
	{
		streamButtons().forEach(button ->
		{
			button.setMinWidth(withButtons);
			button.setPrefWidth(withButtons);
			button.setMaxWidth(withButtons);
		});
	}

	private void postLayout()
	{
		if (this.root != null)
		{
			this.root.autosize();
			final ScaleBox scb = this.scaleBox;
			if (scb != null)
			{
				final double width = this.root.getWidth();
				final double height = this.root.getHeight();
				if (this.paneOuter != null)
				{
					final BorderPane po = this.paneOuter;
					po.setMinSize(width, height);
					po.setPrefSize(width, height);
					scb.initialize();
				}
			}
		}
		equalizeButtonWidths();
		if (this.stage != null)
		{
			this.stage.sizeToScene();
		}
	}

	public @Nullable
	Stage getDialog()
	{
		return this.stage;
	}
}
