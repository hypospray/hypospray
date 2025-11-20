package eu.jbeernink.hypospray.codegeneration.testing;

import java.io.IOException;

@SuppressWarnings("unused")
class ProxiedClass {

	public void run() {

	}

	@SuppressWarnings("RedundantThrows")
	public void throwingMethod() throws IOException {

	}

	public String getResult() {
		return "foo";
	}

	private void foo() {

	}

	public boolean booleanMethod() {
		return false;
	}

	public byte byteMethod() {
		return -1;
	}

	public short shortMethod() {
		return -1;
	}

	public char charMethod() {
		return 'a';
	}

	public int intMethod() {
		return -1;
	}

	public long longMethod() {
		return -1L;
	}

	public float floatMethod() {
		return -1F;
	}

	public double doubleMethod() {
		return -1.0;
	}

	public String referenceMethod() {
		return "default";
	}

	public void booleanParameterMethod(boolean b) {
	}

	public void byteParameterMethod(byte b) {
	}

	public void shortParameterMethod(short s) {
	}

	public void charParameterMethod(char c) {
	}

	public void intParameterMethod(int i) {
	}

	public void longParameterMethod(long l) {
	}

	public void floatParameterMethod(float f) {
	}

	public void doubleParameterMethod(double d) {
	}

	@Override
	public String toString() {
		return super.toString();
	}
}