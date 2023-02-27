package com.seda.payer.rtbatch.schema.sip;

import java.util.Properties;

public abstract class SipElementBuilder<E extends SipSchemaElement> {

	public abstract E buildSipElement(Properties data);

}
