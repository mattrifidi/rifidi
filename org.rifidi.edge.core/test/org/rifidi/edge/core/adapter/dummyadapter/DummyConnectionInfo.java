package org.rifidi.edge.core.adapter.dummyadapter;

import org.rifidi.edge.core.readerPlugin.AbstractReaderInfo;

public class DummyConnectionInfo extends AbstractReaderInfo {
	
	private EDummyError errorToSet = EDummyError.NONE;

	@Override
	public Class<? extends AbstractReaderInfo> getReaderAdapterType() {
		return DummyConnectionInfo.class;
	}

	/**
	 * @return the errorToSet
	 */
	public EDummyError getErrorToSet() {
		return errorToSet;
	}

	/**
	 * @param errorToSet the errorToSet to set
	 */
	public void setErrorToSet(EDummyError errorToSet) {
		this.errorToSet = errorToSet;
	}

	
}
