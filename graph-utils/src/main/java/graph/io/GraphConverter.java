package graph.io;

import java.io.File;

import data.DataUtil;
import data.ScriptUtil;
import graph.dm.Edge;
import graph.dm.Graph;
import graph.ext.dm.fs.FileNode;
import graph.ext.dm.fs.FileSystemNode;
import graph.ext.mod.fs.FileSystemReader;

public class GraphConverter {
	public void convertGV2GML_PNG_SVG(String path, boolean execute) {
		FileSystemReader fsr = new FileSystemReader();
		Graph<FileSystemNode, Edge<FileSystemNode>> fs = fsr.read(path, ".*\\.gv");
		StringBuilder files = new StringBuilder();
		for (FileSystemNode file : fs.filterNodes(f -> f instanceof FileNode))
			files.append(file.getId() + "\n");
		String correctedPath = path.replace(DataUtil.PATH_SEPARATOR, File.separator) + File.separator;
		String dotfile = correctedPath + "dotconvert.txt";
		DataUtil.getInstance().writeToFile(dotfile, files.toString());

		String script = "FOR  /F %%F IN (" + dotfile + ") DO dot -Tsvg -O %%F\n";
		script += "REM FOR  /F %%F IN (" + dotfile + ") DO dot -Tpng -O %%F\n";
		script += "FOR  /F %%F IN (" + dotfile + ") DO gv2gml %%F > %%F.gml\n";

		if (execute)
			ScriptUtil.getInstance().executeBatchScript(script, null, -1);
		else
			DataUtil.getInstance().writeToFile(correctedPath + "gvcvt.cmd", script);
	}
}
