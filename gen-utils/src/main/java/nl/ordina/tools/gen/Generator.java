package nl.ordina.tools.gen;

import java.util.ArrayList;
import java.util.List;

import bucket.BuckereeDoodah;
import bucket.BucketManager;
import data.DataUtil;
import data.Sourcedir;
import data.Targetdir;

public class Generator implements BuckereeDoodah, Targetdir, Sourcedir {
	private String targetdir;
	private String sourcedir;
	private BucketManager bucketManager;
	private StringBuilder output;
	private String description;
	private String artefactname;

	public Generator(BucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

	public StringBuilder getOutput() {
		if (null == output)
			output = new StringBuilder();
		return output;
	}

	@Override
	public String getTargetdir() {
		return targetdir;
	}

	@Override
	public void setTargetdir(String targetdir) {
		this.targetdir = targetdir;
	}

	@Override
	public void setSourcedir(String sourcedir) {
		this.sourcedir = sourcedir;
	}

	@Override
	public String getSourcedir() {
		return this.sourcedir;
	}

	@Override
	public BucketManager getBuckereeDoodah() {
		return bucketManager;
	}

	public boolean isOk() {
		return false;
	}

	public void setOk(boolean ok) {
		throw new UnsupportedOperationException("setOk() not allowed for [" + getClass().getName() + "]");
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getArtefactname() {
		return artefactname;
	}

	public void setArtefactname(String artefactname) {
		this.artefactname = artefactname;
	}

	public String getTargetArtefactname() {
		return getTargetArtefactname(getArtefactname());
	}

	public List<String> getTargetArtefactnames() {
		List<String> result = new ArrayList<>();

		result.add(getTargetArtefactname());
		return result;
	}

	public String getTargetArtefactname(String artefactname) {
		return getTargetdir() + DataUtil.PATH_SEPARATOR + artefactname + getExtension();
	}

	public String getExtension() {
		return ".txt";
	}
}
