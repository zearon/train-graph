package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public abstract class Base64EncodingBinaryPart<T, ET extends TrainGraphPart> extends
		TrainGraphPart<T, ET> {
	
	ByteArrayOutputStream out;
	BASE64Decoder decoder;
	
	Base64EncodingBinaryPart() {
		super();
	}

	@Override
	protected boolean isBinaryEncoded() { 
		return true;
	}

	@Override
	protected String encodeToBase64() {
		String base64Codes = new sun.misc.BASE64Encoder().encode(encode());
		return base64Codes;
	}

	@Override
	protected void decodeFromBase64Start() {
		out = new ByteArrayOutputStream();
		decoder = new BASE64Decoder();
	}

	@Override
	protected void decodeFromBase64NewLine(String base64Line) {
		try {
			out.write( decoder.decodeBuffer(base64Line) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decodeFromBase64End() {
		decode(out.toByteArray());
		System.gc();
	}
	
	protected abstract byte[] encode();
	
	protected abstract void decode(byte[] byteBuffer);

}