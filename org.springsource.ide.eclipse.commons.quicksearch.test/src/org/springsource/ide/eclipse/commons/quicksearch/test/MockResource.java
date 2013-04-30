package org.springsource.ide.eclipse.commons.quicksearch.test;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class MockResource implements IResource {

	IPath fullPath;
	
	public MockResource(String pathStr) {
		this.fullPath = new Path(pathStr);
	}
	
	public String toString() {
		return fullPath.toString();
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean contains(ISchedulingRule rule) {
		throw new Error("Not implemented");
	}

	public boolean isConflicting(ISchedulingRule rule) {
		throw new Error("Not implemented");
	}

	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
		throw new Error("Not implemented");
	}

	public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
		throw new Error("Not implemented");
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void accept(IResourceVisitor visitor, int depth,
			boolean includePhantoms) throws CoreException {
		throw new Error("Not implemented");
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void copy(IPath destination, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void copy(IProjectDescription description, boolean force,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void copy(IProjectDescription description, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public IMarker createMarker(String type) throws CoreException {
		// TODO Auto-generated method stub
		throw new Error("Not implemented");
	}

	public IResourceProxy createProxy() {
		// TODO Auto-generated method stub
		throw new Error("Not implemented");
	}

	public void delete(boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void delete(int updateFlags, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public boolean exists() {
		return true;
	}

	public IMarker findMarker(long id) throws CoreException {
		throw new Error("Not implemented");
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public int findMaxProblemSeverity(String type, boolean includeSubtypes,
			int depth) throws CoreException {
		throw new Error("Not implemented");
	}

	public String getFileExtension() {
		return fullPath.getFileExtension();
	}

	public IPath getFullPath() {
		return fullPath;
	}

	public long getLocalTimeStamp() {
		throw new Error("Not implemented");
	}

	public IPath getLocation() {
		throw new Error("Not implemented");
	}

	public URI getLocationURI() {
		throw new Error("Not implemented");
	}

	public IMarker getMarker(long id) {
		throw new Error("Not implemented");
	}

	public long getModificationStamp() {
		throw new Error("Not implemented");
	}

	public String getName() {
		String name = fullPath.lastSegment();
		if (name!=null) {
			return name;
		}
		return "";
	}

	public IPathVariableManager getPathVariableManager() {
		throw new Error("Not implemented");
	}

	public IContainer getParent() {
		throw new Error("Not implemented");
	}

	public Map<QualifiedName, String> getPersistentProperties()
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		throw new Error("Not implemented");
	}

	public IProject getProject() {
		throw new Error("Not implemented");
	}

	public IPath getProjectRelativePath() {
		throw new Error("Not implemented");
	}

	public IPath getRawLocation() {
		throw new Error("Not implemented");
	}

	public URI getRawLocationURI() {
		throw new Error("Not implemented");
	}

	public ResourceAttributes getResourceAttributes() {
		throw new Error("Not implemented");
	}

	public Map<QualifiedName, Object> getSessionProperties()
			throws CoreException {
		throw new Error("Not implemented");
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		throw new Error("Not implemented");
	}

	public int getType() {
		throw new Error("Not implemented");
	}

	public IWorkspace getWorkspace() {
		throw new Error("Not implemented");
	}

	public boolean isAccessible() {
		return true;
	}

	public boolean isDerived() {
		return false;
	}

	public boolean isDerived(int options) {
		return false;
	}

	public boolean isHidden() {
		return false;
	}

	public boolean isHidden(int options) {
		return false;
	}

	public boolean isLinked() {
		return false;
	}

	public boolean isVirtual() {
		return false;
	}

	public boolean isLinked(int options) {
		return false;
	}

	public boolean isLocal(int depth) {
		return false;
	}

	public boolean isPhantom() {
		return false;
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isSynchronized(int depth) {
		throw new Error("Not implemented");
	}

	public boolean isTeamPrivateMember() {
		throw new Error("Not implemented");
	}

	public boolean isTeamPrivateMember(int options) {
		throw new Error("Not implemented");
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void move(IPath destination, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void move(IProjectDescription description, boolean force,
			boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void move(IProjectDescription description, int updateFlags,
			IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

	public void refreshLocal(int depth, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void revertModificationStamp(long value) throws CoreException {
		throw new Error("Not implemented");
	}

	public void setDerived(boolean isDerived) throws CoreException {
		throw new Error("Not implemented");
	}

	public void setDerived(boolean isDerived, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void setHidden(boolean isHidden) throws CoreException {
		throw new Error("Not implemented");
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public long setLocalTimeStamp(long value) throws CoreException {
		throw new Error("Not implemented");
	}

	public void setPersistentProperty(QualifiedName key, String value)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void setReadOnly(boolean readOnly) {
		throw new Error("Not implemented");
	}

	public void setResourceAttributes(ResourceAttributes attributes)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void setSessionProperty(QualifiedName key, Object value)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void setTeamPrivateMember(boolean isTeamPrivate)
			throws CoreException {
		throw new Error("Not implemented");
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new Error("Not implemented");
	}

}
