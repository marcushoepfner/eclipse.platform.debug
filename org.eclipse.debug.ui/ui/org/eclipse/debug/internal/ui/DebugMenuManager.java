package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

public class DebugMenuManager extends MenuManager implements IMenuListener {

	public DebugMenuManager(String text, String id) {
		super(text, id);
		setDirty(true);
		addMenuListener(this);
	}

	public void menuAboutToShow(IMenuManager manager) {
		setDirty(true);
	}

}

