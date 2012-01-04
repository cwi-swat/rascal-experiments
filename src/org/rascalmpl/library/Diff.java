package org.rascalmpl.library;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.rascalmpl.interpreter.IEvaluatorContext;

import name.fraser.neil.plaintext.*;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

public class Diff {
	private final IValueFactory values;
	private Type patchADT;
	private Type diffsType;
	private Type patchType;
	private Type diffADT;
	private static final TypeFactory types = TypeFactory.getInstance();

	public Diff(IValueFactory vf) {
		this.values = vf;
	}
	
	private void makeTypes(IEvaluatorContext ctx){
		if(diffADT == null){
			TypeStore tstore = ctx.getCurrentEnvt().getStore();
			diffADT = types.abstractDataType(tstore, "Diff");
			patchADT = types.abstractDataType(tstore, "Patch");
			diffsType = tstore.getAlias("Diffs");
			patchType = types.constructorFromTuple(tstore, patchADT, "patch", 
				types.tupleType(
						diffsType, 			 "diffs",
						types.integerType(), "start1",
						types.integerType(), "start2",
						types.integerType(), "length1",
						types.integerType(), "length2"));
		}
	}
	
	private LinkedList<diff_match_patch.Diff> makeDiffs(IList diffs){
		LinkedList<diff_match_patch.Diff> res = new LinkedList<diff_match_patch.Diff>();
		for(IValue d : diffs){
			IConstructor cons = (IConstructor) d;
			String cname = cons.getName();
			String text = ((IString)cons.get(0)).getValue();
			diff_match_patch.Operation op = diff_match_patch.Operation.EQUAL;
			
			if(cname.equals("insert"))
				op = diff_match_patch.Operation.INSERT;
			else if(cname.equals("delete"))
				op = diff_match_patch.Operation.DELETE;
			res.add(new diff_match_patch.Diff(op, text));
		}
		return res;
	}
	
	private IList makeDiffs(LinkedList<diff_match_patch.Diff> diffs, IEvaluatorContext ctx){
		TypeStore tstore = ctx.getCurrentEnvt().getStore();
		
		IListWriter writer = values.listWriter(diffADT);
		for (diff_match_patch.Diff diff: diffs) {
			String op = "";
			switch(diff.operation){
				case INSERT: op = "insert"; break;
				case DELETE: op = "delete"; break;
				case EQUAL: op = "equal"; break;
			}
			System.err.println(diff);
			Type diffType = types.constructorFromTuple(tstore, diffADT, op, types.tupleType(types.stringType(), "text"));
			writer.append(values.constructor(diffType, values.string(diff.text)));			
		}
		return writer.done();
	}
	
	private List<diff_match_patch.Patch> makePatch(IList patches){
		LinkedList<diff_match_patch.Patch> res = new LinkedList<diff_match_patch.Patch>();
		for(IValue p : patches){
			IConstructor cons = (IConstructor) p;
			Patch patch = new Patch();
		    patch.diffs = makeDiffs((IList) cons.get(0));
		    patch.start1 = ((IInteger)cons.get(1)).intValue();
		    patch.start2 = ((IInteger)cons.get(2)).intValue();
		    patch.length1 = ((IInteger)cons.get(3)).intValue();
		    patch.length2 = ((IInteger)cons.get(4)).intValue();
		    res.add(patch);
		}
		return res;
	}

	private IList makePatch(List<diff_match_patch.Patch> patches, IEvaluatorContext ctx){
		IListWriter writer = values.listWriter(patchADT);
		for (diff_match_patch.Patch p: patches) {
			writer.append(values.constructor(patchType,
					makeDiffs(p.diffs, ctx),
					values.integer(p.start1),
					values.integer(p.start2),
					values.integer(p.length1),
					values.integer(p.length2)));
		}
		return writer.done();
	}
	
	public IValue diff(IString text1, IString text2, IEvaluatorContext ctx){
		makeTypes(ctx);
		diff_match_patch dmp = new diff_match_patch();
		return makeDiffs(dmp.diff_main(text1.getValue(), text2.getValue()), ctx);
	}
	
	public IString diffToHtml(IList diffs){
		diff_match_patch dmp = new diff_match_patch();
		String html = dmp.diff_prettyHtml(makeDiffs(diffs));
		return values.string(html);
	}
	
	public IValue levenshtein(IList diffs){
		diff_match_patch dmp = new diff_match_patch();
		return values.integer(dmp.diff_levenshtein(makeDiffs(diffs)));
	}
	
	public IValue levenshtein(IString text1, IString text2){
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(text1.getValue(), text2.getValue());
		return values.integer(dmp.diff_levenshtein(diffs));
	}
	
	public IValue makePatch(IString text, IList diffs, IEvaluatorContext ctx){
		makeTypes(ctx);
		
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<Patch> patches = dmp.patch_make(text.getValue(), makeDiffs(diffs));
		IListWriter writer = values.listWriter(patchADT);
	
		for(Patch p : patches){
			writer.append(values.constructor(patchType,
					makeDiffs(p.diffs, ctx),
					values.integer(p.start1),
					values.integer(p.start2),
					values.integer(p.length1),
					values.integer(p.length2)));
		}
		return writer.done();
	}
	
	public IValue makePatch(IString text1, IString text2, IEvaluatorContext ctx){
		makeTypes(ctx);
		
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(text1.getValue(), text2.getValue());
		LinkedList<Patch> patches = dmp.patch_make(text1.getValue(), diffs);
		
		StringBuilder text = new StringBuilder();
		
		for(Patch patch : patches){
		    text.append(patch);
		}
		return values.string(text.toString());
	}
	
	
	
	public IString patchToText(IList patches){
		StringBuilder text = new StringBuilder();
		
		for(Patch patch : makePatch(patches)){
		    text.append(patch);
		}
		return values.string(text.toString());
	}
	
	public IList patchFromText(IString text, IEvaluatorContext ctx){
		makeTypes(ctx);
		diff_match_patch dmp = new diff_match_patch();
		return makePatch(dmp.patch_fromText(text.getValue()), ctx);
	}
			
}
