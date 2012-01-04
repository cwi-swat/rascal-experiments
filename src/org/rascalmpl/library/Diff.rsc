@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Paul Klint - Paul.Klint@cwi.nl - CWI}
module util::Diff

data Diff =  equal(str text)
           | \insert(str text)
           | delete(str text)  
           ; 
alias Diffs = list[Diff];

data Patch = patch(Diffs diffs, int start1, int start2, int length1, int length2);

alias Patches = list[Patch];

@javaClass{org.rascalmpl.library.util.Diff}
@reflect
public java Diffs diff(str text1, str text2);

@javaClass{org.rascalmpl.library.util.Diff}
public java str diffToHtml(Diffs diffs);

@javaClass{org.rascalmpl.library.util.Diff}
public java int levenshtein(Diffs diffs);

@javaClass{org.rascalmpl.library.util.Diff}
public java int levenshtein(str text1, str text2);

@javaClass{org.rascalmpl.library.util.Diff}
public java str patchToText(Patches patches);

@javaClass{org.rascalmpl.library.util.Diff}
@reflect
public java Patches patchFromText(str textLine);


@javaClass{org.rascalmpl.library.util.Diff}
@reflect
public java Patches makePatch(str text, Diffs diffs);

@javaClass{org.rascalmpl.library.util.Diff}
@reflect
public java str makePatch(str text1, str text2);


