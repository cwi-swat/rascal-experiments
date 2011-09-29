@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Paul Klint - Paul.Klint@cwi.nl - CWI}
module experiments::ModelTransformations::Tree2List

import IO;

/*
 * Example taken from "ATL Basic Examples and Patterns" at
 * http://www.eclipse.org/m2m/atl/basicExamples_Patterns/
 *
 * See http://www.eclipse.org/m2m/atl/basicExamples_Patterns/article.php?file=Tree2List/index.html
 */
 
 // Source Model: TreeNode
 
 data LeafSize = small() | medium() | large();
 
 data TreeElement = treeNode(str name, list[TreeElement] children) 
                  | leaf(str name, LeafSize size)
                  ;
 
 // Target Model: ElementList
 
 data Element = root(str name) | common(str name);
 alias ElementList = list[Element];
 
 
 public ElementList treenode2elementlist(TreeElement t){
   if(treeNode(name, children) := t){
     Element root = root(t.name);
     list[Element] smallElements = [];
     list[Element] mediumElements = [];
     list[Element] largeElements = [];
     visit(t.children){
        case leaf(str name, small()):  smallElements  += common(name);
        case leaf(str name, medium()): mediumElements += common(name);
        case leaf(str name, large()):  largeElements  += common(name);
     }
        
     return [root, largeElements, mediumElements, smallElements];
   }
   // "Root element is not a treeNode";
 }
 
// Tests
// See http://www.eclipse.org/m2m/atl/basicExamples_Patterns/article.php?file=Tree2List/index.html
// for a picture of this example
   
private TreeElement input = 
           treeNode("0",
                    [ leaf("1", small()),
                      treeNode("2",
                               [ leaf("6", medium()),
                                 leaf("7", large()),
                                 leaf("8", small()),
                                 leaf("9", medium())
                               ]),
                      treeNode("3", [ leaf("10", small()) ]),
                      leaf("4", small()),
                      leaf("5", medium())
                    ]);
 
private ElementList output = [root("0"), common("7"), common("6"), common("9"), common("5"), common("1"), 
            common("8"), common("10"), common("4")];
            
public test bool t1() = treenode2elementlist(input) == output;
