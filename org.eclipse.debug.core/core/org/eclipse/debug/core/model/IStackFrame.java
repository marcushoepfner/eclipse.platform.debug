package org.eclipse.debug.core.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;

/**
 * A stack frame represents a stack frame in a suspended thread. A
 * stack frame has element type <code>STACK_FRAME</code>, and a parent
 * of type <code>THREAD</code>. The children of a stack frame are of
 * type <code>VARIABLE</code>, and represent the visible variables in
 * the stack frame. Minimally, a stack frame supports
 * the following capabilities:
 * <ul>
 * <li>suspend/resume (convenience to resume this stack frame's thread)
 * <li>stepping
 * </ul>
 * <p>
 * An implementation may choose to re-use or discard
 * stack frames on iterative thread suspensions. Clients
 * cannot assume that stack frames are identical or equal across
 * iterative thread suspensions and must check for equality on iterative
 * suspensions if they wish to re-use the objects.
 * </p>
 * <p>
 * An implementation that preserves equality
 * across iterative suspensions may display more desirable behavior in
 * some clients. For example, if stack frames are preserved
 * while stepping, a UI client would be able to update the UI incrementally,
 * rather than collapse and redraw the entire list. 
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ISuspendResume
 * @see IStep
 */
public interface IStackFrame extends IDebugElement, IStep, ISuspendResume {
	/**
	 * Returns the line number of the instruction pointer in 
	 * this stack frame that corresponds to a line in an associated source
	 * element, or <code>-1</code> if line number information
	 * is unavailable.
	 *
	 * @return line number of instruction pointer in this stack frame
	 * @exception DebugException if unable to retrieve this stack frame's line number
	 *   from the target
	 */
	int getLineNumber() throws DebugException;
}
