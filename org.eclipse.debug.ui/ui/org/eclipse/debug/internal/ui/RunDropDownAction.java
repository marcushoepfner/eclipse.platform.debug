package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

public class RunDropDownAction extends LaunchDropDownAction {

	public RunDropDownAction() {
		super(new RunAction());
	}

	/**
	 * @see LaunchDropDownAction
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}

	/**
	 * @see LaunchDropDownAction
	 */
	public ILaunch[] getHistory() {
		return DebugUIPlugin.getDefault().getRunHistory();
	}
}

