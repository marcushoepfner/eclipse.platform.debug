package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Provides the contents for a debug mode of the Launches viewer.
 */
public class DebugContentProvider extends BasicContentProvider implements IDebugEventListener, ILaunchListener, ITreeContentProvider {
	
	/**
	 * Constructs a new content provider.
	 */
	public DebugContentProvider() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
		plugin.getLaunchManager().addLaunchListener(this);
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		if (parent instanceof IDebugElement) {
			IDebugElement de= (IDebugElement) parent;
			if (de.getElementType() < IDebugElement.STACK_FRAME) {
				try {
					return de.getChildren();
				} catch (DebugException e) {
					DebugUIUtils.logError(e);
				}
			}
		} else
			if (parent instanceof ILaunch) {
				return ((ILaunch)parent).getChildren();
			} else
				if (parent instanceof ILaunchManager) {
					return ((ILaunchManager) parent).getLaunches();
				}
		return new Object[0];
	}

	/**
	 * Returns the <code>ILaunch</code>es for the <code>ILaunchManager</code>
	 */
	public Object[] getElements(Object element) {
		if (element instanceof ILaunchManager) {
			return ((ILaunchManager) element).getLaunches();
		}
		return new Object[0];
	}

	/**
	 * @see org.eclipse.jface.viewer.ITreeContentProvider
	 */
	public Object getParent(Object item) {
		Object parent= null;
		if (item instanceof IDebugElement) {
			IDebugElement de= (IDebugElement) item;
			parent= de.getParent();
			if (parent == null) {
				parent= de.getLaunch();
			}
		} else
			if (item instanceof IProcess) {
				parent= ((IProcess) item).getLaunch();
			} else
				if (item instanceof ILaunch) {
					parent= DebugPlugin.getDefault().getLaunchManager();
				}
		return parent;
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	public void doHandleDebugEvent(DebugEvent event) {
		Object element= event.getSource();
		if (element instanceof IDebugElement && ((IDebugElement) element).getElementType() == IDebugElement.VARIABLE) {
			// the debug view does not show variables
			return;
		}
		switch (event.getKind()) {
			case DebugEvent.CREATE :
				insert(element);
				break;
			case DebugEvent.TERMINATE :
				clearSourceSelection();
				Object parent= getParent(element);
				refresh(parent);
				updateButtons();
				break;
			case DebugEvent.RESUME :
				if (element instanceof ISuspendResume) {
					if (((ISuspendResume)element).isSuspended()) {
						return;
					}
				}
				clearSourceSelection();
				if (event.getDetail() != DebugEvent.STEP_START) {
					refresh(element);
					if (element instanceof IThread) {
						//select and reveal will update buttons
						//via selection changed callback
						selectAndReveal(element);
						break;
					}
				}
				labelChanged(element);
				updateButtons();
				break;
			case DebugEvent.SUSPEND :
				refresh(element);
				if (!DebugUIPlugin.getDefault().userPreferenceToSwitchPerspective(true)) {
					Object tos= null;
					if (element instanceof IThread) {
						// select the top stack frame
						try {
							tos= ((IThread) element).getTopStackFrame();	
						} catch (DebugException de) {
						}
					}
					if (tos != null) {
						selectAndReveal(tos);
					}
				}
				updateButtons();
				break;
			case DebugEvent.CHANGE :
				refresh(element);
				updateButtons();
				break;
		}
	}

	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		final Object parent= getParent(element);
		// a parent can be null for a debug target or process that has not yet been associated
		// with a launch
		if (parent != null) {
			((LaunchesViewer) fViewer).add(parent, element);
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread
	 */
	private void remove(Object element) {
		((LaunchesViewer) fViewer).remove(element);
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		((LaunchesViewer) fViewer).update(element, new String[] {IBasicPropertyConstants.P_TEXT});
	}

	/**
	 * Helper method to update the buttons of the viewer - must be called in UI thread
	 */
	protected void updateButtons() {
		((LaunchesViewer)fViewer).updateButtons();
	}

	/**
	 * Helper method to update the selection of the viewer - must be called in UI thread
	 */
	protected void updateMarkerForSelection() {
		((LaunchesViewer)fViewer).updateMarkerForSelection();
	}

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		((LaunchesViewer) fViewer).setSelection(new StructuredSelection(element), true);
	}

	/**
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object item) {
		if (item instanceof IStackFrame) {
			return false;
			// No children shown for IStackFrames in this view (see the variables view instead).
		} else
			if (item instanceof IDebugElement) {
				try {
					return ((IDebugElement) item).hasChildren();
				} catch (DebugException de) {
				}
			} else
				if (item instanceof ILaunch) {
					return true;
				} else
					if (item instanceof ILaunchManager) {
						return ((ILaunchManager) item).getLaunches().length > 0;
					}
		return false;
	}

	/**
	 * @see ILaunchListener
	 */
	public void launchDeregistered(final ILaunch launch) {
		Runnable r= new Runnable() {
			public void run() {
				remove(launch);
				updateButtons();
			}
		};

		asyncExec(r);
	}

	/**
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch launch) {
		Runnable r= new Runnable() {
			public void run() {
				insert(launch);
				if (!DebugUIPlugin.getDefault().userPreferenceToSwitchPerspective(true)) {
					Object dt = launch.getDebugTarget();
					if (dt != null) {
						selectAndReveal(dt);
					}
				}
			}
		};

		asyncExec(r);
	}

	/**
	 * Unregisters this content provider from the debug model so that
	 * this object can be garbage-collected.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
		plugin.getLaunchManager().removeLaunchListener(this);
	}

	/**
	 * Clear the selection in the editor - must be called in UI thread
	 */
	private void clearSourceSelection() {
		if (fViewer != null) {
			((LaunchesViewer)fViewer).clearSourceSelection();
		}
	}
}

