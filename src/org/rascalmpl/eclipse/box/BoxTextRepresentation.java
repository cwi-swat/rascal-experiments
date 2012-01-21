/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class BoxTextRepresentation extends TextPresentation {
	
	private final IDocument d;
	
	BoxTextRepresentation(IDocument d) {
		this.d = d;
		// System.err.println("Doclen:"+d.getLength());
		setDefaultStyleRange(DF(new Position(0, d.getLength())));
			try {
				for (Position p: d.getPositions(IDocument.DEFAULT_CATEGORY)) {
					// System.err.println("S:"+c+" "+p);
					TypedPosition q = (TypedPosition) p;
					replaceStyleRange(p, Box.TAG.valueOf(q.getType()));
				}
			} catch (BadPositionCategoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	final IDocument getDocument() {
		return this.d;
	}
	
	private void replaceStyleRange(Position p, Box.TAG tag) {
		StyleRange r = new StyleRange(p.getOffset(), p.getLength(), tag.color, bgColor, tag.style);
		this.replaceStyleRange(r);
	}
	
	
	private StyleRange DF(Position p) {
		return new StyleRange(p.getOffset(), p.getLength(), textColor, bgColor, SWT.NORMAL);
	}

	static Color textColor = Box.blackColor;
	static Color bgColor = Box.whiteColor;
}
