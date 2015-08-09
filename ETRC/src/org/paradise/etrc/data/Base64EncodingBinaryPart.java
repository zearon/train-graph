package org.paradise.etrc.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sun.misc.BASE64Decoder;

public abstract class Base64EncodingBinaryPart extends
		TrainGraphPart {
	
	ByteArrayOutputStream out;
	BASE64Decoder decoder;
	
	public Base64EncodingBinaryPart() {
		super();
	}

	@Override
	public final boolean isBase64Encoded() { 
		return true;
	}

	@Override
	public final void encodeToBase64(OutputStream out) throws IOException {
		new sun.misc.BASE64Encoder().encode(encode(), out);
	}

	@Override
	protected final void decodeFromBase64Start() {
		out = new ByteArrayOutputStream();
		decoder = new BASE64Decoder();
	}

	@Override
	protected final void decodeFromBase64NewLine(String base64Line) {
		if ("".equals(base64Line))
			return;
		
		if (base64Line.contains("}"))
			return;
		
		try {
			out.write( decoder.decodeBuffer(base64Line) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected final void decodeFromBase64End() {
		decode(out.toByteArray());
		System.gc();
	}
	
	protected abstract byte[] encode();
	
	protected abstract void decode(byte[] byteBuffer);

}